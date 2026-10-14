package com.school.enrollment;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

final class SimplePdf {
    private SimplePdf(){}
    static void write(Path path,String text) throws IOException {
        StringBuilder stream=new StringBuilder("BT\n/F1 9 Tf\n40 800 Td\n");
        String[] lines=text.replace("\r","").split("\n");
        int count=0;
        for(String line:lines) {
            String safe=line.replace("\\","\\\\").replace("(","\\(").replace(")","\\)").replace("\t","    ");
            if(safe.length()>145)safe=safe.substring(0,145);
            stream.append("(").append(safe).append(") Tj\n0 -12 Td\n");
            if(++count>=58){stream.append("ET\n");break;}
        }
        stream.append("ET\n");
        byte[] content=stream.toString().getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        java.util.ArrayList<Integer> offsets=new java.util.ArrayList<>();
        write(out,"%PDF-1.4\n");
        offsets.add(out.size());write(out,"1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
        offsets.add(out.size());write(out,"2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");
        offsets.add(out.size());write(out,"3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n");
        offsets.add(out.size());write(out,"4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");
        offsets.add(out.size());write(out,"5 0 obj << /Length "+content.length+" >> stream\n");out.write(content);write(out,"endstream endobj\n");
        int xref=out.size();write(out,"xref\n0 6\n0000000000 65535 f \n");
        for(int offset:offsets)write(out,String.format(java.util.Locale.ROOT,"%010d 00000 n \n",offset));
        write(out,"trailer << /Size 6 /Root 1 0 R >>\nstartxref\n"+xref+"\n%%EOF");
        Files.write(path,out.toByteArray());
    }
    private static void write(OutputStream out,String value)throws IOException{out.write(value.getBytes(StandardCharsets.US_ASCII));}
}
