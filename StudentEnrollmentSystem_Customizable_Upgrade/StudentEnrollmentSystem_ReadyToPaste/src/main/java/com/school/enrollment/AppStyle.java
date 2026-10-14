package com.school.enrollment;

import java.awt.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.table.JTableHeader;

/** User-customizable appearance. Preferences are stored per OS user, not in the database. */
public final class AppStyle {
    private static final Preferences PREFS = Preferences.userNodeForPackage(AppStyle.class);
    private AppStyle() {}
    public static int fontSize() { return PREFS.getInt("fontSize", 16); }
    public static String theme() { return PREFS.get("theme", "Light"); }
    public static void setFontSize(int size) { PREFS.putInt("fontSize", Math.max(12, Math.min(24, size))); }
    public static void setTheme(String value) { PREFS.put("theme", value); }
    public static void apply() {
        int size = fontSize();
        Font base = new Font("Dialog", Font.PLAIN, size);
        for (String key : new String[]{"Label.font","Button.font","ToggleButton.font","TextField.font","PasswordField.font","TextArea.font","ComboBox.font","List.font","Table.font","TableHeader.font","TabbedPane.font","Menu.font","MenuItem.font","CheckBox.font","RadioButton.font","OptionPane.messageFont","OptionPane.buttonFont"}) UIManager.put(key, base);
        UIManager.put("Table.rowHeight", size + 14);
        UIManager.put("Button.margin", new Insets(8, 14, 8, 14));
        if ("Dark".equalsIgnoreCase(theme())) {
            Color bg=new Color(36,40,48), panel=new Color(47,53,63), fg=new Color(238,241,245), field=new Color(57,64,76), accent=new Color(66,133,244);
            UIManager.put("Panel.background",bg); UIManager.put("OptionPane.background",bg); UIManager.put("Label.foreground",fg);
            UIManager.put("Button.background",panel); UIManager.put("Button.foreground",fg); UIManager.put("TextField.background",field); UIManager.put("TextField.foreground",fg);
            UIManager.put("PasswordField.background",field); UIManager.put("PasswordField.foreground",fg); UIManager.put("ComboBox.background",field); UIManager.put("ComboBox.foreground",fg);
            UIManager.put("Table.background",panel); UIManager.put("Table.foreground",fg); UIManager.put("Table.selectionBackground",accent); UIManager.put("Table.selectionForeground",Color.WHITE);
            UIManager.put("TableHeader.background",field); UIManager.put("TableHeader.foreground",fg); UIManager.put("ScrollPane.background",bg);
        } else {
            UIManager.put("Panel.background",new Color(246,248,251)); UIManager.put("Label.foreground",new Color(35,43,55));
            UIManager.put("Button.background",new Color(232,238,246)); UIManager.put("Button.foreground",new Color(25,40,60));
            UIManager.put("Table.rowHeight", size + 14); UIManager.put("Table.selectionBackground",new Color(190,218,255));
        }
    }
    public static void refreshTree(Component c) { SwingUtilities.updateComponentTreeUI((Window)c); }
}
