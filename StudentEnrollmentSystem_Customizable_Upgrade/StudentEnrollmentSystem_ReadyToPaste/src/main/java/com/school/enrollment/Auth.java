package com.school.enrollment;

import java.security.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public final class Auth {
    private Auth() {}
    public static String newSalt() {
        byte[] b=new byte[16]; new SecureRandom().nextBytes(b); return Base64.getEncoder().encodeToString(b);
    }
    public static String hash(char[] password, String salt) throws Exception {
        PBEKeySpec spec=new PBEKeySpec(password, Base64.getDecoder().decode(salt), 120000, 256);
        try {
            byte[] result=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(result);
        } finally { spec.clearPassword(); java.util.Arrays.fill(password, '\0'); }
    }
    public static boolean login(String username, char[] password) throws Exception {
        try (var c=Database.getConnection();
             var p=c.prepareStatement("SELECT SALT,PASS_HASH,ROLE_NAME,FULL_NAME FROM APP_USERS WHERE USERNAME=? AND ACTIVE=1")) {
            p.setString(1,username.trim());
            try (var rs=p.executeQuery()) {
                if(!rs.next()){ java.util.Arrays.fill(password,'\0'); return false; }
                String actual=hash(password,rs.getString(1));
                return MessageDigest.isEqual(actual.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    rs.getString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        }
    }
}
