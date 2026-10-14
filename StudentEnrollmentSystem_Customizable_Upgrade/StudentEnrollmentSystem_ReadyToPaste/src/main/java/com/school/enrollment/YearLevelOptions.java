
package com.school.enrollment;

import javax.swing.JComboBox;

public final class YearLevelOptions {

    private YearLevelOptions() {}

    public static JComboBox<String> create() {
        return new JComboBox<>(new String[] {
            "1st Year",
            "2nd Year",
            "3rd Year",
            "4th Year",
            "5th Year",
            "6th Year"
        });
    }
}
