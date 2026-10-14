
package com.school.enrollment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class DashboardFrame extends JFrame {

    private final String fullName;
    private final String role;
    private final int userId;

    private final JPanel content = new JPanel(new BorderLayout(10, 10));
    private final JLabel status = new JLabel("Ready");

    public DashboardFrame(String fullName, String role) {
        this(fullName, role, -1);
    }

    public DashboardFrame(String fullName, String role, int userId) {
        this.fullName = fullName;
        this.role = role;
        this.userId = userId;

        setTitle("Student Enrollment, Grading and Records");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1360, 850);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel head = new JPanel(new BorderLayout());
        head.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("STUDENT INFORMATION SYSTEM");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        head.add(title, BorderLayout.WEST);
        head.add(new JLabel(fullName + " | " + role + "   "), BorderLayout.EAST);
        add(head, BorderLayout.NORTH);

        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        side.setBackground(new Color(34, 47, 62));

        if (isInstructor()) {
            addMenuButton(side, "Dashboard");
            addMenuButton(side, "My Classes / Grades");
            addMenuButton(side, "Appearance & Settings");
            addMenuButton(side, "Logout");
        } else {
            addMenuGroup(side, "HOME", new String[]{"Dashboard"}, true);
            addMenuGroup(side, "ACADEMIC SETUP", new String[]{
                "Programs", "Departments", "Subjects", "Program Subjects",
                "Subject Prerequisites", "Class Schedules"
            }, false);
            addMenuGroup(side, "PEOPLE", new String[]{"Students", "Instructors"}, false);
            addMenuGroup(side, "CLASSES & ENROLLMENT", new String[]{
                "Class Sections", "Instructor Assignments", "Enrollment"
            }, false);
            addMenuGroup(side, "GRADES & REPORTS", new String[]{"Grades", "Grade Audit", "Reports"}, false);
            addMenuGroup(side, "SYSTEM", new String[]{"Appearance & Settings", "Logout"}, false);
        }

        // Keep the navigation fixed to the left, but make its contents vertically
        // scrollable so every Administrator menu item remains reachable on smaller screens.
        JScrollPane sideScroll = new JScrollPane(side,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.setPreferredSize(new Dimension(265, 100));
        sideScroll.setMinimumSize(new Dimension(230, 100));
        sideScroll.setBorder(BorderFactory.createEmptyBorder());
        sideScroll.getViewport().setBackground(new Color(34, 47, 62));
        sideScroll.getVerticalScrollBar().setUnitIncrement(18);
        add(sideScroll, BorderLayout.WEST);

        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(content, BorderLayout.CENTER);

        status.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        add(status, BorderLayout.SOUTH);

        home();
    }

    private void addMenuButton(JPanel side, String item) {
        JButton button = new JButton(item);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(230, 38));
        button.setPreferredSize(new Dimension(230, 38));
        button.setFocusPainted(false);
        button.addActionListener(e -> navigate(item));
        side.add(button);
        side.add(Box.createVerticalStrut(4));
    }

    private void addMenuGroup(JPanel side, String heading, String[] items, boolean expanded) {
        JButton toggle = new JButton((expanded ? "▾  " : "▸  ") + heading);
        toggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        toggle.setHorizontalAlignment(SwingConstants.LEFT);
        toggle.setMaximumSize(new Dimension(230, 34));
        toggle.setPreferredSize(new Dimension(230, 34));
        toggle.setFont(toggle.getFont().deriveFont(Font.BOLD, 11f));
        JPanel children = new JPanel();
        children.setOpaque(false);
        children.setLayout(new BoxLayout(children, BoxLayout.Y_AXIS));
        for (String item : items) addMenuButton(children, item);
        children.setVisible(expanded);
        toggle.addActionListener(e -> {
            boolean show = !children.isVisible();
            children.setVisible(show);
            toggle.setText((show ? "▾  " : "▸  ") + heading);
            side.revalidate(); side.repaint();
        });
        side.add(toggle);
        side.add(children);
        side.add(Box.createVerticalStrut(5));
    }

    private boolean isInstructor() {
        return role != null && role.equalsIgnoreCase("Instructor");
    }

    private boolean admin() {
        return role != null
                && (role.equalsIgnoreCase("Administrator")
                || role.equalsIgnoreCase("Admin"));
    }

    private void navigate(String screen) {
        try {
            switch (screen) {
                case "Dashboard" -> home();
                case "Programs" -> {
                    requireAdmin();
                    programs();
                }
                case "Departments" -> { requireAdmin(); advanced("Departments"); }
                case "Class Schedules" -> { requireAdmin(); advanced("Class Schedules"); }
                case "Subject Prerequisites" -> { requireAdmin(); advanced("Subject Prerequisites"); }
                case "Grade Audit" -> { requireAdmin(); advanced("Grade Audit"); }
                case "Subjects" -> {
                    requireAdmin();
                    subjects();
                }
                case "Program Subjects" -> {
                    requireAdmin();
                    programSubjects();
                }
                case "Students" -> {
                    requireAdmin();
                    students();
                }
                case "Instructors" -> {
                    requireAdmin();
                    instructors();
                }
                case "Class Sections" -> {
                    requireAdmin();
                    sections();
                }
                case "Instructor Assignments" -> {
                    requireAdmin();
                    assignments();
                }
                case "Enrollment" -> {
                    requireAdmin();
                    enrollment();
                }
                case "Grades" -> {
                    requireAdmin();
                    grades(false);
                }
                case "My Classes / Grades" -> grades(true);
                case "Reports" -> {
                    requireAdmin();
                    reports();
                }
                case "Appearance & Settings" -> appearanceSettings();
                case "Logout" -> {
                    dispose();
                    new LoginFrame().setVisible(true);
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void advanced(String name) {
        content.removeAll();
        content.add(new AdvancedManagementPanel(name, fullName), BorderLayout.CENTER);
        content.revalidate(); content.repaint();
    }

    private void appearanceSettings() {
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 12));
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JSpinner fontSize = new JSpinner(new SpinnerNumberModel(AppStyle.fontSize(), 12, 24, 1));
        JComboBox<String> theme = new JComboBox<>(new String[]{"Light", "Dark"});
        theme.setSelectedItem(AppStyle.theme());
        JCheckBox largerTables = new JCheckBox("Use larger table rows", true);
        form.add(new JLabel("Application font size")); form.add(fontSize);
        form.add(new JLabel("Color theme")); form.add(theme);
        form.add(new JLabel("Table spacing")); form.add(largerTables);
        JButton apply = new JButton("Apply and Save Preferences");
        JButton reset = new JButton("Restore Default Appearance");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT)); buttons.add(apply); buttons.add(reset);
        apply.addActionListener(e -> {
            AppStyle.setFontSize((Integer) fontSize.getValue());
            AppStyle.setTheme(String.valueOf(theme.getSelectedItem()));
            AppStyle.apply();
            SwingUtilities.updateComponentTreeUI(this);
            setSize(Math.max(getWidth(), 1100), Math.max(getHeight(), 700));
            message("Appearance saved for your computer user. Some existing controls refresh immediately; reopen a screen if needed.");
        });
        reset.addActionListener(e -> {
            fontSize.setValue(16); theme.setSelectedItem("Light");
            AppStyle.setFontSize(16); AppStyle.setTheme("Light"); AppStyle.apply(); SwingUtilities.updateComponentTreeUI(this);
        });
        content.removeAll();
        JLabel heading = new JLabel("Personalize the application");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        content.add(heading, BorderLayout.NORTH); content.add(form, BorderLayout.CENTER); content.add(buttons, BorderLayout.SOUTH);
        status.setText("Preferences are stored separately from student records."); refresh();
    }

    private void requireAdmin() {
        if (!admin()) {
            throw new SecurityException(
                    "This screen is available to administrators only.");
        }
    }

    // =========================================================
    // DASHBOARDS
    // =========================================================

    private void home() {
        content.removeAll();

        if (isInstructor()) {
            instructorHome();
        } else {
            adminHome();
        }

        refresh();
    }

    private void instructorHome() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));

        JLabel heading = new JLabel(
                "Instructor Dashboard — " + fullName);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 18f));

        panel.add(heading, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(0, 1, 8, 8));
        info.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        int assignments = 0;
        int gradeCount = 0;
        int studentCount = 0;

        try {
            java.util.List<String[]> rows = Database.query(
                    "SELECT COUNT(*) FROM TEACHING_ASSIGNMENTS A "
                    + "JOIN INSTRUCTORS I ON I.ID=A.INSTRUCTOR_ID "
                    + "WHERE I.USER_ID=?", userId);

            if (!rows.isEmpty()) {
                assignments = Integer.parseInt(rows.get(0)[0]);
            }

            rows = Database.query(
                    "SELECT COUNT(*) FROM GRADES G "
                    + "JOIN ENROLLMENTS E ON E.ID=G.ENROLLMENT_ID "
                    + "JOIN TEACHING_ASSIGNMENTS A "
                    + "ON A.SECTION_ID=E.SECTION_ID "
                    + "AND A.SUBJECT_ID=E.SUBJECT_ID "
                    + "JOIN INSTRUCTORS I ON I.ID=A.INSTRUCTOR_ID "
                    + "WHERE I.USER_ID=?", userId);

            if (!rows.isEmpty()) {
                gradeCount = Integer.parseInt(rows.get(0)[0]);
            }

            rows = Database.query(
                    "SELECT COUNT(DISTINCT E.STUDENT_ID) "
                    + "FROM ENROLLMENTS E "
                    + "JOIN TEACHING_ASSIGNMENTS A "
                    + "ON A.SECTION_ID=E.SECTION_ID "
                    + "AND A.SUBJECT_ID=E.SUBJECT_ID "
                    + "JOIN INSTRUCTORS I ON I.ID=A.INSTRUCTOR_ID "
                    + "WHERE I.USER_ID=?", userId);

            if (!rows.isEmpty()) {
                studentCount = Integer.parseInt(rows.get(0)[0]);
            }

        } catch (Exception ex) {
            error(ex);
        }

        info.add(new JLabel("Welcome, " + fullName + "."));
        info.add(new JLabel("Your teaching assignments: " + assignments));
        info.add(new JLabel("Students in your assigned classes: " + studentCount));
        info.add(new JLabel("Grades recorded: " + gradeCount));
        info.add(new JLabel(
                "You can view assigned students and enter or update their grades."));

        JButton openGrades = new JButton("Open My Classes / Grades");
        openGrades.addActionListener(e -> {
            try {
                grades(true);
            } catch (Exception ex) {
                error(ex);
            }
        });

        panel.add(info, BorderLayout.CENTER);
        panel.add(openGrades, BorderLayout.SOUTH);

        content.add(panel, BorderLayout.CENTER);
        status.setText("Instructor access: assigned classes and grades only.");
    }

    private void adminHome() {
        JPanel tiles = new JPanel(new GridLayout(2, 2, 12, 12));

        String[][] tables = {
            {"Programs", "PROGRAMS"},
            {"Students", "STUDENTS"},
            {"Subjects", "SUBJECTS"},
            {"Class Sections", "CLASS_SECTIONS"}
        };

        for (String[] item : tables) {
            int count = 0;

            try {
                java.util.List<String[]> rows = Database.query(
                        "SELECT COUNT(*) FROM " + item[1]);

                if (!rows.isEmpty()) {
                    count = Integer.parseInt(rows.get(0)[0]);
                }
            } catch (Exception ignored) {
                // A count failure should not stop the dashboard.
            }

            JPanel tile = new JPanel(new GridLayout(2, 1));
            tile.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(18, 18, 18, 18)));

            tile.add(new JLabel(item[0]));

            JLabel number = new JLabel(String.valueOf(count));
            number.setFont(number.getFont().deriveFont(Font.BOLD, 28f));
            tile.add(number);

            tiles.add(tile);
        }

        content.add(
                new JLabel("Administrator Dashboard Overview"),
                BorderLayout.NORTH);
        content.add(tiles, BorderLayout.CENTER);

        status.setText(
                "Signed in as " + role
                + ". Existing database records are retained.");
    }

    private void refresh() {
        content.revalidate();
        content.repaint();
    }

    private void error(Exception ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage() == null ? ex.toString() : ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    // =========================================================
    // COMMON TABLE AND FORM HELPERS
    // =========================================================

    private void tableScreen(
            String title,
            String sql,
            String[] headers) {

        content.removeAll();

        JTable table = new JTable();
        table.setAutoCreateRowSorter(true);

        try {
            setTable(table, headers, Database.query(sql));
        } catch (Exception ex) {
            error(ex);
        }

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel(title), BorderLayout.WEST);

        JButton reload = new JButton("Refresh");
        reload.addActionListener(e -> {
            try {
                setTable(table, headers, Database.query(sql));
            } catch (Exception ex) {
                error(ex);
            }
        });

        top.add(reload, BorderLayout.EAST);

        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    private void setTable(
            JTable table,
            String[] headers,
            java.util.List<String[]> rows) {

        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (String[] row : rows) {
            model.addRow(row);
        }

        table.setModel(model);
    }

    @FunctionalInterface
    private interface Loader {
        void run() throws Exception;
    }

    private void formScreen(
            String title,
            JComponent form,
            JTable table,
            Loader loader) {

        content.removeAll();

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);

        // Every record-management screen now exposes the same explicit CRUD
        // toolbar. These operations use the real database table, not just the
        // visible form controls, and keep identity IDs untouched.
        String crudTable = crudTableFor(title);
        if (crudTable != null) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addRecord = new JButton("Add Record");
            JButton editRecord = new JButton("Edit Selected");
            JButton deleteRecord = new JButton("Delete Selected");
            JButton refreshRecords = new JButton("Refresh");
            JButton searchRecords = new JButton("Search");
            actions.add(addRecord);
            actions.add(editRecord);
            actions.add(deleteRecord);
            actions.add(searchRecords);
            actions.add(refreshRecords);
            panel.add(actions, BorderLayout.SOUTH);
            addRecord.addActionListener(e -> crudEdit(crudTable, table, loader, false));
            editRecord.addActionListener(e -> crudEdit(crudTable, table, loader, true));
            deleteRecord.addActionListener(e -> crudDelete(crudTable, table, loader));
            refreshRecords.addActionListener(e -> runLoader(loader));
            searchRecords.addActionListener(e -> searchTable(table));
        }

        content.add(panel, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        try {
            loader.run();
        } catch (Exception ex) {
            error(ex);
        }

        refresh();
    }

    private String crudTableFor(String title) {
        return switch (title) {
            case "Create programs" -> "PROGRAMS";
            case "Create subjects and units" -> "SUBJECTS";
            case "Define which subjects belong to each program" -> "PROGRAM_SUBJECTS";
            case "Register students" -> "STUDENTS";
            case "Create instructor and login account" -> "INSTRUCTORS";
            case "Create class sections" -> "CLASS_SECTIONS";
            case "Assign an instructor to a subject in a section" -> "TEACHING_ASSIGNMENTS";
            case "Enroll a student using dropdowns" -> "ENROLLMENTS";
            default -> null;
        };
    }

    private void runLoader(Loader loader) {
        try { loader.run(); refresh(); } catch (Exception ex) { error(ex); }
    }

    private void searchTable(JTable table) {
        String q = JOptionPane.showInputDialog(this, "Search visible table for:", "Search Records", JOptionPane.QUESTION_MESSAGE);
        if (q == null) return;
        String needle = q.trim().toLowerCase(Locale.ROOT);
        if (!(table.getModel() instanceof DefaultTableModel model)) return;
        if (table.getRowSorter() == null) table.setAutoCreateRowSorter(true);
        if (table.getRowSorter() instanceof javax.swing.table.TableRowSorter<?> raw) {
            @SuppressWarnings("unchecked") javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
                    (javax.swing.table.TableRowSorter<DefaultTableModel>) raw;
            sorter.setRowFilter(needle.isEmpty() ? null : new javax.swing.RowFilter<>() {
                @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    for (int i=0;i<entry.getValueCount();i++)
                        if (Objects.toString(entry.getValue(i), "").toLowerCase(Locale.ROOT).contains(needle)) return true;
                    return false;
                }
            });
        }
    }

    private java.util.List<String> crudColumns(String tableName) throws SQLException {
        java.util.List<String> columns = new ArrayList<>();
        try (Connection c = Database.getConnection(); ResultSet rs = c.getMetaData().getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                String auto = rs.getString("IS_AUTOINCREMENT");
                if (!"ID".equalsIgnoreCase(name) && !"YES".equalsIgnoreCase(auto)) columns.add(name);
            }
        }
        return columns;
    }

    private void crudEdit(String tableName, JTable table, Loader loader, boolean editing) {
        int modelRow = -1;
        Object recordId = null;
        if (editing) {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) { message("Select a record in the table first."); return; }
            modelRow = table.convertRowIndexToModel(viewRow);
            if (table.getModel().getColumnCount() == 0) return;
            recordId = table.getModel().getValueAt(modelRow, 0);
            if (recordId == null) { message("Could not identify the selected record. Refresh and try again."); return; }
        }
        try (Connection c = Database.getConnection()) {
            java.util.List<String> columns = crudColumns(tableName);
            LinkedHashMap<String,JTextField> inputs = new LinkedHashMap<>();
            JPanel form = new JPanel(new GridLayout(0,2,8,8));
            for (String col : columns) {
                String existing = "";
                if (editing) {
                    try (PreparedStatement ps = c.prepareStatement("SELECT " + col + " FROM " + tableName + " WHERE ID=?")) {
                        ps.setObject(1, recordId);
                        try (ResultSet rs = ps.executeQuery()) { if (rs.next()) existing = Objects.toString(rs.getObject(1), ""); }
                    }
                }
                JTextField input = new JTextField(existing, 24);
                String example = crudExample(tableName, col);
                input.setToolTipText(example.isBlank() ? "Enter " + col.replace('_', ' ').toLowerCase(Locale.ROOT) : "Example: " + example);
                inputs.put(col,input);
                form.add(new JLabel("<html>" + col.replace('_',' ') + (example.isBlank() ? "" : "<br><small>Example: " + example + "</small>") + "</html>"));
                form.add(input);
            }
            JScrollPane formScroll = new JScrollPane(form);
            formScroll.setPreferredSize(new Dimension(620, Math.min(520, Math.max(240, columns.size() * 58))));
            JLabel guidance = new JLabel("<html><b>Fill in the fields below.</b> Examples are shown under each field. Leave optional fields blank.</html>");
            JPanel dialogContent = new JPanel(new BorderLayout(8, 8));
            dialogContent.add(guidance, BorderLayout.NORTH);
            dialogContent.add(formScroll, BorderLayout.CENTER);
            int answer = JOptionPane.showConfirmDialog(this, dialogContent, (editing ? "Edit " : "Add ") + tableName.replace('_',' '), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return;
            if (editing) {
                String set = String.join(",", columns.stream().map(col -> col + "=?").toList());
                try (PreparedStatement ps = c.prepareStatement("UPDATE " + tableName + " SET " + set + " WHERE ID=?")) {
                    int i=1; for (var entry : inputs.entrySet()) ps.setObject(i++, crudValue(entry.getValue().getText(), entry.getKey()));
                    ps.setObject(i, recordId); ps.executeUpdate();
                }
            } else {
                String names = String.join(",", columns);
                String marks = String.join(",", Collections.nCopies(columns.size(), "?"));
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + tableName + " (" + names + ") VALUES (" + marks + ")")) {
                    int i=1; for (var entry : inputs.entrySet()) ps.setObject(i++, crudValue(entry.getValue().getText(), entry.getKey()));
                    ps.executeUpdate();
                }
            }
            runLoader(loader);
        } catch (Exception ex) {
            error(new SQLException("Save failed. Check required fields, data types, unique values, and linked record IDs. " + ex.getMessage(), ex));
        }
    }

    private String crudExample(String table, String col) {
        String key = col.toUpperCase(Locale.ROOT);
        if (key.equals("STUDENT_NO")) return "TUPT-26-0001 (generated in Student form)";
        if (key.equals("EMPLOYEE_NO")) return "EMP-26-0001 (generated in Instructor form)";
        if (key.equals("PROGRAM_CODE")) return "BSIT";
        if (key.equals("PROGRAM_NAME")) return "Bachelor of Science in Information Technology";
        if (key.equals("SUBJECT_CODE")) return "IT101";
        if (key.equals("SUBJECT_TITLE")) return "Introduction to Computing";
        if (key.equals("FIRST_NAME")) return "Juan";
        if (key.equals("LAST_NAME")) return "Dela Cruz";
        if (key.equals("FULL_NAME")) return "Juan Dela Cruz";
        if (key.equals("EMAIL")) return "name@school.edu";
        if (key.equals("YEAR_LEVEL")) return "1";
        if (key.equals("SECTION_NAME")) return "1A";
        if (key.equals("SCHOOL_YEAR")) return "2026-2027";
        if (key.equals("SEMESTER")) return "1st Semester";
        if (key.equals("CAPACITY")) return "40";
        if (key.equals("UNITS")) return "3";
        if (key.equals("ACTIVE")) return "1 for active, 0 for inactive";
        if (key.equals("ENROLLMENT_DATE")) return "2026-10-13 (YYYY-MM-DD)";
        if (key.endsWith("_ID")) return "Enter the existing numeric ID from the related table";
        if (key.contains("STATUS")) return "Active";
        if (key.contains("GRADE") || key.endsWith("_WW") || key.endsWith("_TEST")) return "90";
        if (key.contains("DAY")) return "Monday";
        if (key.contains("TIME")) return "08:00";
        if (key.equals("ROOM")) return "Room 201";
        if (key.contains("DEAN")) return "Juan Dela Cruz";
        if (key.contains("DEPARTMENT")) return "Information Technology";
        if (key.contains("NAME")) return "Enter the full name";
        return "Enter the appropriate value";
    }

    private Object crudValue(String raw, String column) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return null;
        if (column.endsWith("_ID") || column.equals("UNITS") || column.equals("CAPACITY") || column.equals("ACTIVE") || column.equals("HAS_LABORATORY")) return Integer.valueOf(value);
        if ((column.contains("GRADE") && !column.equals("EQUIVALENT_GRADE")) || column.endsWith("_WW") || column.endsWith("_TEST") || column.equals("PERFORMANCE_TASKS")) return new BigDecimal(value);
        if (column.equals("ENROLLMENT_DATE")) return java.sql.Date.valueOf(value);
        return value;
    }

    private void crudDelete(String tableName, JTable table, Loader loader) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { message("Select a record in the table first."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object id = table.getModel().getValueAt(modelRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete selected " + tableName.replace('_',' ') + " record? Records referenced by other data may be protected.", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM " + tableName + " WHERE ID=?")) {
            ps.setObject(1,id); ps.executeUpdate(); runLoader(loader);
        } catch (Exception ex) { error(new SQLException("Delete failed. This record may be linked to existing enrollments, grades, sections, or other records. " + ex.getMessage(), ex)); }
    }

    private JTextField field(int columns) {
        return new JTextField(columns);
    }

    private String nextGeneratedNumber(String column, String table, String prefix) {
        String year = String.format("%02d", LocalDate.now().getYear() % 100);
        String stem = prefix + "-" + year + "-";
        int max = 0;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + column + " FROM " + table + " WHERE " + column + " LIKE ?")) {
            ps.setString(1, stem + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String value = rs.getString(1);
                    try { max = Math.max(max, Integer.parseInt(value.substring(stem.length()))); }
                    catch (Exception ignored) { }
                }
            }
        } catch (Exception ex) { error(ex); }
        return stem + String.format("%04d", max + 1);
    }

    private String[] schoolYearOptions() {
        int current = LocalDate.now().getYear();
        int start = LocalDate.now().getMonthValue() >= 6 ? current : current - 1;
        String[] values = new String[6];
        for (int i = 0; i < values.length; i++) values[i] = (start + i) + "-" + (start + i + 1);
        return values;
    }

    private String yearCode(String year) {
        if (year == null) return "1";
        return switch (year) {
            case "1st Year" -> "1"; case "2nd Year" -> "2"; case "3rd Year" -> "3";
            case "4th Year" -> "4"; case "5th Year" -> "5"; case "6th Year" -> "6";
            default -> year.replaceAll("[^0-9]", "");
        };
    }

    private JPanel gridForm() {
        return new JPanel(new GridLayout(0, 2, 8, 8));
    }

    private void add(JPanel panel, String label, JComponent component) {
        panel.add(new JLabel(label));
        panel.add(component);
    }

    private JComboBox<Item> combo(String sql) throws SQLException {
        JComboBox<Item> combo = new JComboBox<>();

        for (String[] row : Database.query(sql)) {
            combo.addItem(new Item(
                    Integer.parseInt(row[0]), row[1]));
        }

        return combo;
    }

    private static class Item {
        final int id;
        final String label;

        Item(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // =========================================================
    // PROGRAMS
    // =========================================================

    private void programs() {
        JTextField code = field(14);
        JTextField name = field(24);

        JPanel form = gridForm();
        add(form, "Program code (e.g. BSIT)", code);
        add(form, "Program name", name);

        JButton save = new JButton("Add Program");
        form.add(new JLabel(""));
        form.add(save);

        JTable table = new JTable();

        save.addActionListener(e -> {
            try {
                if (code.getText().isBlank() || name.getText().isBlank()) {
                    throw new IllegalArgumentException(
                            "Enter both the program code and program name.");
                }

                try (Connection c = Database.getConnection();
                     PreparedStatement p = c.prepareStatement(
                             "INSERT INTO PROGRAMS(PROGRAM_CODE,PROGRAM_NAME) "
                             + "VALUES(?,?)")) {

                    p.setString(1, code.getText().trim().toUpperCase());
                    p.setString(2, name.getText().trim());
                    p.executeUpdate();
                }

                code.setText("");
                name.setText("");

                setTable(table,
                        new String[]{"ID", "Code", "Program"},
                        Database.query(
                                "SELECT ID,PROGRAM_CODE,PROGRAM_NAME "
                                + "FROM PROGRAMS ORDER BY PROGRAM_CODE"));

                message("Program added.");

            } catch (Exception ex) {
                error(ex);
            }
        });

        formScreen(
                "Create programs",
                form,
                table,
                () -> setTable(table,
                        new String[]{"ID", "Code", "Program"},
                        Database.query(
                                "SELECT ID,PROGRAM_CODE,PROGRAM_NAME "
                                + "FROM PROGRAMS ORDER BY PROGRAM_CODE")));
    }

    // =========================================================
    // SUBJECTS
    // =========================================================

    private void subjects() {
        JTextField code = field(12);
        JTextField name = field(20);
        JTextField units = field(4);
        units.setText("3");

        JPanel form = gridForm();
        add(form, "Subject code", code);
        add(form, "Subject title", name);
        add(form, "Units", units);

        JButton save = new JButton("Add Subject");
        form.add(new JLabel(""));
        form.add(save);

        JTable table = new JTable();

        save.addActionListener(e -> {
            try {
                if (code.getText().isBlank() || name.getText().isBlank()) {
                    throw new IllegalArgumentException(
                            "Enter the subject code and title.");
                }

                int unitCount = Integer.parseInt(units.getText().trim());

                if (unitCount <= 0) {
                    throw new IllegalArgumentException(
                            "Units must be greater than zero.");
                }

                try (Connection c = Database.getConnection();
                     PreparedStatement p = c.prepareStatement(
                             "INSERT INTO SUBJECTS"
                             + "(SUBJECT_CODE,SUBJECT_TITLE,UNITS) VALUES(?,?,?)")) {

                    p.setString(1, code.getText().trim().toUpperCase());
                    p.setString(2, name.getText().trim());
                    p.setInt(3, unitCount);
                    p.executeUpdate();
                }

                setTable(table,
                        new String[]{"ID", "Code", "Subject", "Units"},
                        Database.query(
                                "SELECT ID,SUBJECT_CODE,SUBJECT_TITLE,UNITS "
                                + "FROM SUBJECTS ORDER BY SUBJECT_CODE"));

                message("Subject added.");

            } catch (Exception ex) {
                error(ex);
            }
        });

        formScreen(
                "Create subjects and units",
                form,
                table,
                () -> setTable(table,
                        new String[]{"ID", "Code", "Subject", "Units"},
                        Database.query(
                                "SELECT ID,SUBJECT_CODE,SUBJECT_TITLE,UNITS "
                                + "FROM SUBJECTS ORDER BY SUBJECT_CODE")));
    }

    // =========================================================
    // PROGRAM SUBJECTS
    // =========================================================

    private void programSubjects() throws Exception {
        JComboBox<Item> program = combo(
                "SELECT ID,PROGRAM_CODE || ' - ' || PROGRAM_NAME "
                + "FROM PROGRAMS ORDER BY PROGRAM_CODE");

        JComboBox<Item> subject = combo(
                "SELECT ID,SUBJECT_CODE || ' - ' || SUBJECT_TITLE "
                + "FROM SUBJECTS ORDER BY SUBJECT_CODE");

        JPanel form = gridForm();
        add(form, "Program", program);
        add(form, "Subject", subject);

        JButton save = new JButton("Link Subject to Program");
        form.add(new JLabel(""));
        form.add(save);

        JTable table = new JTable();

        save.addActionListener(e -> {
            try {
                Item p = (Item) program.getSelectedItem();
                Item s = (Item) subject.getSelectedItem();

                if (p == null || s == null) {
                    throw new IllegalArgumentException(
                            "Create a program and subject first.");
                }

                try (Connection c = Database.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                             "INSERT INTO PROGRAM_SUBJECTS(PROGRAM_ID,SUBJECT_ID) "
                             + "VALUES(?,?)")) {

                    ps.setInt(1, p.id);
                    ps.setInt(2, s.id);
                    ps.executeUpdate();
                }

                setTable(table,
                        new String[]{"ID", "Program", "Subject"},
                        programSubjectRows());

                message("Subject linked to program.");

            } catch (Exception ex) {
                error(ex);
            }
        });

        formScreen(
                "Define which subjects belong to each program",
                form,
                table,
                () -> setTable(table,
                        new String[]{"ID", "Program", "Subject"},
                        programSubjectRows()));
    }

    private java.util.List<String[]> programSubjectRows()
            throws SQLException {

        return Database.query(
                "SELECT PS.ID,"
                + "P.PROGRAM_CODE || ' - ' || P.PROGRAM_NAME,"
                + "S.SUBJECT_CODE || ' - ' || S.SUBJECT_TITLE "
                + "FROM PROGRAM_SUBJECTS PS "
                + "JOIN PROGRAMS P ON P.ID=PS.PROGRAM_ID "
                + "JOIN SUBJECTS S ON S.ID=PS.SUBJECT_ID "
                + "ORDER BY P.PROGRAM_CODE,S.SUBJECT_CODE");
    }

    // =========================================================
    // STUDENTS
    // =========================================================

    private void students() throws Exception {
        JTextField studentNo = field(12);
        studentNo.setEditable(false);
        studentNo.setText(nextGeneratedNumber("STUDENT_NO", "STUDENTS", "TUPT"));
        JTextField firstName = field(12);
        JTextField lastName = field(12);
        JTextField email = field(18);

        JComboBox<String> year = new JComboBox<>(new String[]{
            "1st Year", "2nd Year", "3rd Year",
            "4th Year", "5th Year", "6th Year"
        });

        JComboBox<Item> program = combo(
                "SELECT ID,PROGRAM_CODE || ' - ' || PROGRAM_NAME "
                + "FROM PROGRAMS ORDER BY PROGRAM_CODE");

        JPanel form = gridForm();
        add(form, "Student number", studentNo);
        add(form, "First name", firstName);
        add(form, "Last name", lastName);
        add(form, "Email", email);
        add(form, "Program", program);
        add(form, "Year level", year);

        JButton save = new JButton("Add Student");
        form.add(new JLabel(""));
        form.add(save);

        JTable table = new JTable();

        save.addActionListener(e -> {
            try {
                Item selectedProgram = (Item) program.getSelectedItem();

                if (firstName.getText().isBlank()
                        || lastName.getText().isBlank()
                        || selectedProgram == null) {
                    throw new IllegalArgumentException(
                            "Complete the required student fields.");
                }

                try (Connection c = Database.getConnection();
                     PreparedStatement p = c.prepareStatement(
                             "INSERT INTO STUDENTS"
                             + "(STUDENT_NO,FIRST_NAME,LAST_NAME,EMAIL,"
                             + "PROGRAM,YEAR_LEVEL,PROGRAM_ID,STATUS) "
                             + "VALUES(?,?,?,?,?,?,?,'Active')")) {

                    studentNo.setText(nextGeneratedNumber("STUDENT_NO", "STUDENTS", "TUPT"));
                    p.setString(1, studentNo.getText().trim());
                    p.setString(2, firstName.getText().trim());
                    p.setString(3, lastName.getText().trim());
                    p.setString(4, email.getText().trim());
                    p.setString(5, selectedProgram.label);
                    p.setString(6, (String) year.getSelectedItem());
                    p.setInt(7, selectedProgram.id);
                    p.executeUpdate();
                }

                setTable(table,
                        new String[]{"ID", "Student No", "First Name",
                            "Last Name", "Email", "Program", "Year", "Status"},
                        studentRows());

                message("Student added.");

            } catch (Exception ex) {
                error(ex);
            }
        });

        formScreen(
                "Register students",
                form,
                table,
                () -> setTable(table,
                        new String[]{"ID", "Student No", "First Name",
                            "Last Name", "Email", "Program", "Year", "Status"},
                        studentRows()));
    }

    private java.util.List<String[]> studentRows() throws SQLException {
        return Database.query(
                "SELECT ST.ID,ST.STUDENT_NO,ST.FIRST_NAME,ST.LAST_NAME,"
                + "ST.EMAIL,COALESCE(P.PROGRAM_CODE,ST.PROGRAM),"
                + "ST.YEAR_LEVEL,ST.STATUS "
                + "FROM STUDENTS ST LEFT JOIN PROGRAMS P ON P.ID=ST.PROGRAM_ID "
                + "ORDER BY ST.LAST_NAME,ST.FIRST_NAME");
    }

    // =========================================================
    // INSTRUCTORS AND LOGIN ACCOUNTS
    // =========================================================

    private void instructors() throws Exception {
        JTextField employeeNo = field(12);
        employeeNo.setEditable(false);
        employeeNo.setText(nextGeneratedNumber("EMPLOYEE_NO", "INSTRUCTORS", "EMP"));
        JTextField name = field(16);
        JTextField email = field(18);
        JTextField department = field(12);
        JTextField username = field(12);
        JPasswordField password = new JPasswordField(12);

        JPanel form = gridForm();
        add(form, "Employee number", employeeNo);
        add(form, "Full name", name);
        add(form, "Email", email);
        add(form, "Department", department);
        add(form, "Login username", username);
        add(form, "Temporary password (10+ characters)", password);

        JButton save = new JButton("Add Instructor + Login");
        form.add(new JLabel(""));
        form.add(save);

        JTable table = new JTable();

        save.addActionListener(e -> {
            try {
                char[] pw = password.getPassword();

                if (name.getText().isBlank()
                        || username.getText().isBlank()) {
                    throw new IllegalArgumentException(
                            "Enter instructor name and username.");
                }

                if (pw.length < 10) {
                    throw new IllegalArgumentException(
                            "Temporary password must be at least 10 characters.");
                }

                String salt = Auth.newSalt();
                String hash = Auth.hash(pw, salt);

                try (Connection c = Database.getConnection()) {
                    c.setAutoCommit(false);

                    try {
                        int userId;

                        try (PreparedStatement p = c.prepareStatement(
                                "INSERT INTO APP_USERS"
                                + "(USERNAME,FULL_NAME,ROLE_NAME,SALT,PASS_HASH,ACTIVE) "
                                + "VALUES(?,?,'Instructor',?,?,1)",
                                Statement.RETURN_GENERATED_KEYS)) {

                            p.setString(1, username.getText().trim());
                            p.setString(2, name.getText().trim());
                            p.setString(3, salt);
                            p.setString(4, hash);
                            p.executeUpdate();

                            try (ResultSet rs = p.getGeneratedKeys()) {
                                if (!rs.next()) {
                                    throw new SQLException(
                                            "Could not retrieve the new account ID.");
                                }
                                userId = rs.getInt(1);
                            }
                        }

                        employeeNo.setText(nextGeneratedNumber("EMPLOYEE_NO", "INSTRUCTORS", "EMP"));
                        try (PreparedStatement p = c.prepareStatement(
                                "INSERT INTO INSTRUCTORS"
                                + "(EMPLOYEE_NO,FULL_NAME,EMAIL,DEPARTMENT,USER_ID) "
                                + "VALUES(?,?,?,?,?)")) {

                            p.setString(1, employeeNo.getText().trim());
                            p.setString(2, name.getText().trim());
                            p.setString(3, email.getText().trim());
                            p.setString(4, department.getText().trim());
                            p.setInt(5, userId);
                            p.executeUpdate();
                        }

                        c.commit();

                    } catch (Exception ex) {
                        c.rollback();
                        throw ex;
                    } finally {
                        c.setAutoCommit(true);
                    }
                }

                password.setText("");

                setTable(table,
                        new String[]{"ID", "Employee No", "Name",
                            "Email", "Department", "Username"},
                        instructorRows());

                message("Instructor account created. Share the temporary "
                        + "login details securely.");

            } catch (Exception ex) {
                error(ex);
            }
        });

        formScreen(
                "Create instructor and login account",
                form,
                table,
                () -> setTable(table,
                        new String[]{"ID", "Employee No", "Name",
                            "Email", "Department", "Username"},
                        instructorRows()));
    }

    private java.util.List<String[]> instructorRows()
            throws SQLException {

        return Database.query(
                "SELECT I.ID,I.EMPLOYEE_NO,I.FULL_NAME,I.EMAIL,"
                + "I.DEPARTMENT,U.USERNAME "
                + "FROM INSTRUCTORS I LEFT JOIN APP_USERS U ON I.USER_ID=U.ID "
                + "ORDER BY I.FULL_NAME");
    }

    // =========================================================
    // CLASS SECTIONS
    // =========================================================

    private void sections() throws Exception {
        JComboBox<Item> program = combo(
                "SELECT ID,PROGRAM_CODE || ' - ' || PROGRAM_NAME "
                + "FROM PROGRAMS ORDER BY PROGRAM_CODE");

        JComboBox<String> year = new JComboBox<>(new String[]{
            "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year", "6th Year"
        });
        JComboBox<String> section = new JComboBox<>(new String[]{"A", "B"});
        JComboBox<String> schoolYear = new JComboBox<>(schoolYearOptions());
        JComboBox<String> semester = new JComboBox<>(new String[]{
            "1st Semester", "2nd Semester", "Summer"
        });

        JPanel form = gridForm();
        add(form, "Program", program);
        add(form, "Year level", year);
        add(form, "Section name", section);
        add(form, "School year", schoolYear);
        add(form, "Semester", semester);

        JButton save = new JButton("Create Section");
        form.add(new JLabel(""));
        form.add(save);

        JTable table = new JTable();

        save.addActionListener(e -> {
            try {
                Item p = (Item) program.getSelectedItem();

                if (p == null) {
                    throw new IllegalArgumentException(
                            "Complete all class-section fields.");
                }

                try (Connection c = Database.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                             "INSERT INTO CLASS_SECTIONS"
                             + "(PROGRAM_ID,YEAR_LEVEL,SECTION_NAME,SCHOOL_YEAR,SEMESTER) "
                             + "VALUES(?,?,?,?,?)")) {

                    ps.setInt(1, p.id);
                    ps.setString(2, (String) year.getSelectedItem());
                    ps.setString(3, yearCode((String) year.getSelectedItem()) + (String) section.getSelectedItem());
                    ps.setString(4, (String) schoolYear.getSelectedItem());
                    ps.setString(5, (String) semester.getSelectedItem());
                    ps.executeUpdate();
                }

                setTable(table,
                        new String[]{"ID", "Program", "Year",
                            "Section", "School Year", "Semester"},
                        sectionRows());

                message("Section created.");

            } catch (Exception ex) {
                error(ex);
            }
        });

        formScreen(
                "Create class sections",
                form,
                table,
                () -> setTable(table,
                        new String[]{"ID", "Program", "Year",
                            "Section", "School Year", "Semester"},
                        sectionRows()));
    }

    private java.util.List<String[]> sectionRows() throws SQLException {
        return Database.query(
                "SELECT C.ID,P.PROGRAM_CODE,C.YEAR_LEVEL,C.SECTION_NAME,"
                + "C.SCHOOL_YEAR,C.SEMESTER "
                + "FROM CLASS_SECTIONS C "
                + "JOIN PROGRAMS P ON P.ID=C.PROGRAM_ID "
                + "ORDER BY C.SCHOOL_YEAR,C.SEMESTER,P.PROGRAM_CODE,"
                + "C.YEAR_LEVEL,C.SECTION_NAME");
    }

    // =========================================================
    // INSTRUCTOR ASSIGNMENTS
    // =========================================================

    private void assignments() throws Exception {
        JComboBox<Item> instructor = combo(
                "SELECT ID,FULL_NAME FROM INSTRUCTORS ORDER BY FULL_NAME");

        JComboBox<Item> section = combo(
                "SELECT C.ID,P.PROGRAM_CODE || ' Yr ' || C.YEAR_LEVEL "
                + "|| ' Sec ' || C.SECTION_NAME || ' (' || C.SCHOOL_YEAR "
                + "|| ' ' || C.SEMESTER || ')' "
                + "FROM CLASS_SECTIONS C JOIN PROGRAMS P ON P.ID=C.PROGRAM_ID "
                + "ORDER BY C.ID");

        JComboBox<Item> subject = combo(
                "SELECT ID,SUBJECT_CODE || ' - ' || SUBJECT_TITLE "
                + "FROM SUBJECTS ORDER BY SUBJECT_CODE");

        JPanel form = gridForm();
        add(form, "Instructor", instructor);
        add(form, "Class section", section);
        add(form, "Subject", subject);

        JButton save = new JButton("Assign Instructor");
        form.add(new JLabel(""));
        form.add(save);

        JTable table = new JTable();

        save.addActionListener(e -> {
            try {
                Item i = (Item) instructor.getSelectedItem();
                Item s = (Item) section.getSelectedItem();
                Item u = (Item) subject.getSelectedItem();

                if (i == null || s == null || u == null) {
                    throw new IllegalArgumentException(
                            "Create an instructor, section, and subject first.");
                }

                try (Connection c = Database.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                             "INSERT INTO TEACHING_ASSIGNMENTS"
                             + "(INSTRUCTOR_ID,SECTION_ID,SUBJECT_ID) VALUES(?,?,?)")) {

                    ps.setInt(1, i.id);
                    ps.setInt(2, s.id);
                    ps.setInt(3, u.id);
                    ps.executeUpdate();
                }

                setTable(table,
                        new String[]{"ID", "Instructor", "Section", "Subject"},
                        assignmentRows());

                message("Teaching assignment saved.");

            } catch (Exception ex) {
                error(ex);
            }
        });

        formScreen(
                "Assign an instructor to a subject in a section",
                form,
                table,
                () -> setTable(table,
                        new String[]{"ID", "Instructor", "Section", "Subject"},
                        assignmentRows()));
    }

    private java.util.List<String[]> assignmentRows()
            throws SQLException {

        return Database.query(
                "SELECT A.ID,I.FULL_NAME,"
                + "P.PROGRAM_CODE || ' Yr ' || C.YEAR_LEVEL || ' Sec ' "
                + "|| C.SECTION_NAME || ' (' || C.SCHOOL_YEAR || ' ' || C.SEMESTER || ')',"
                + "S.SUBJECT_CODE || ' - ' || S.SUBJECT_TITLE "
                + "FROM TEACHING_ASSIGNMENTS A "
                + "JOIN INSTRUCTORS I ON I.ID=A.INSTRUCTOR_ID "
                + "JOIN CLASS_SECTIONS C ON C.ID=A.SECTION_ID "
                + "JOIN PROGRAMS P ON P.ID=C.PROGRAM_ID "
                + "JOIN SUBJECTS S ON S.ID=A.SUBJECT_ID "
                + "ORDER BY I.FULL_NAME,P.PROGRAM_CODE,C.YEAR_LEVEL,"
                + "C.SECTION_NAME,S.SUBJECT_CODE");
    }

    // =========================================================
    // ENROLLMENT
    // =========================================================

    private void enrollment() throws Exception {
        JComboBox<Item> program = combo(
                "SELECT ID,PROGRAM_CODE || ' - ' || PROGRAM_NAME FROM PROGRAMS ORDER BY PROGRAM_CODE");
        JComboBox<String> year = new JComboBox<>(new String[]{
            "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year", "6th Year"
        });
        JComboBox<String> sectionName = new JComboBox<>(new String[]{"A", "B"});
        JComboBox<String> schoolYear = new JComboBox<>(schoolYearOptions());
        JComboBox<String> semester = new JComboBox<>(new String[]{"1st Semester", "2nd Semester", "Summer"});
        JComboBox<Item> student = new JComboBox<>();
        JComboBox<Item> section = new JComboBox<>();
        JComboBox<Item> subject = new JComboBox<>();

        Runnable reloadStudents = () -> {
            student.removeAllItems();
            Item pr = (Item) program.getSelectedItem();
            String yr = (String) year.getSelectedItem();
            if (pr == null || yr == null) return;
            try {
                for (String[] r : Database.query(
                        "SELECT ID,STUDENT_NO || ' - ' || FIRST_NAME || ' ' || LAST_NAME "
                        + "FROM STUDENTS WHERE STATUS='Active' AND PROGRAM_ID=? AND YEAR_LEVEL=? ORDER BY LAST_NAME,FIRST_NAME",
                        pr.id, yr)) student.addItem(new Item(Integer.parseInt(r[0]), r[1]));
            } catch (Exception ex) { error(ex); }
        };
        Runnable reloadSections = () -> {
            section.removeAllItems(); subject.removeAllItems();
            Item pr = (Item) program.getSelectedItem();
            if (pr == null) return;
            String yr = (String) year.getSelectedItem();
            String sec = yr == null ? "" : yearCode(yr) + (String) sectionName.getSelectedItem();
            String sy = (String) schoolYear.getSelectedItem();
            String sem = (String) semester.getSelectedItem();
            try {
                for (String[] r : Database.query(
                        "SELECT C.ID,P.PROGRAM_CODE || ' - ' || C.YEAR_LEVEL || ' ' || C.SECTION_NAME "
                        + "|| ' (' || C.SCHOOL_YEAR || ', ' || C.SEMESTER || ')' "
                        + "FROM CLASS_SECTIONS C JOIN PROGRAMS P ON P.ID=C.PROGRAM_ID "
                        + "WHERE C.PROGRAM_ID=? AND C.YEAR_LEVEL=? AND C.SECTION_NAME=? AND C.SCHOOL_YEAR=? AND C.SEMESTER=? ORDER BY C.ID",
                        pr.id, yr, sec, sy, sem)) section.addItem(new Item(Integer.parseInt(r[0]), r[1]));
                Item selected = (Item) section.getSelectedItem();
                if (selected != null) {
                    for (String[] r : Database.query(
                            "SELECT S.ID,S.SUBJECT_CODE || ' - ' || S.SUBJECT_TITLE FROM SUBJECTS S "
                            + "JOIN PROGRAM_SUBJECTS PS ON PS.SUBJECT_ID=S.ID "
                            + "JOIN CLASS_SECTIONS C ON C.PROGRAM_ID=PS.PROGRAM_ID "
                            + "WHERE C.ID=? ORDER BY S.SUBJECT_CODE", selected.id))
                        subject.addItem(new Item(Integer.parseInt(r[0]), r[1]));
                }
            } catch (Exception ex) { error(ex); }
        };
        program.addActionListener(e -> { reloadStudents.run(); reloadSections.run(); });
        year.addActionListener(e -> { reloadStudents.run(); reloadSections.run(); });
        sectionName.addActionListener(e -> reloadSections.run());
        schoolYear.addActionListener(e -> reloadSections.run());
        semester.addActionListener(e -> reloadSections.run());
        reloadStudents.run(); reloadSections.run();

        JPanel form = gridForm();
        add(form, "Program", program);
        add(form, "Year level", year);
        add(form, "Section", sectionName);
        add(form, "School year", schoolYear);
        add(form, "Semester", semester);
        add(form, "Student", student);
        add(form, "Matching class section", section);
        add(form, "Program subject", subject);
        JButton save = new JButton("Enroll Student");
        form.add(new JLabel("")); form.add(save);
        JTable table = new JTable();
        save.addActionListener(e -> {
            try {
                Item st = (Item) student.getSelectedItem();
                Item sc = (Item) section.getSelectedItem();
                Item su = (Item) subject.getSelectedItem();
                if (st == null || sc == null || su == null)
                    throw new IllegalArgumentException("No matching student, class section, or program subject. Check Class Sections and Program Subjects first.");
                String[] info = Database.query("SELECT SCHOOL_YEAR,SEMESTER FROM CLASS_SECTIONS WHERE ID=?", sc.id).get(0);
                try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO ENROLLMENTS(STUDENT_ID,SUBJECT_ID,SCHOOL_YEAR,SEMESTER,ENROLLMENT_DATE,SECTION_ID) VALUES(?,?,?,?,?,?)")) {
                    ps.setInt(1, st.id); ps.setInt(2, su.id); ps.setString(3, info[0]); ps.setString(4, info[1]);
                    ps.setDate(5, java.sql.Date.valueOf(LocalDate.now())); ps.setInt(6, sc.id); ps.executeUpdate();
                }
                setTable(table, new String[]{"ID", "Student No", "Student", "Program", "Section", "Subject", "School Year", "Semester"}, enrollmentRows());
                message("Student enrolled.");
            } catch (Exception ex) { error(ex); }
        });
        formScreen("Enroll a student using program/year/section/semester dropdowns", form, table,
                () -> setTable(table, new String[]{"ID", "Student No", "Student", "Program", "Section", "Subject", "School Year", "Semester"}, enrollmentRows()));
    }

    private java.util.List<String[]> enrollmentRows()
            throws SQLException {

        return Database.query(
                "SELECT E.ID,ST.STUDENT_NO,"
                + "ST.FIRST_NAME || ' ' || ST.LAST_NAME,"
                + "COALESCE(P.PROGRAM_CODE,ST.PROGRAM),C.SECTION_NAME,"
                + "S.SUBJECT_CODE,E.SCHOOL_YEAR,E.SEMESTER "
                + "FROM ENROLLMENTS E "
                + "JOIN STUDENTS ST ON ST.ID=E.STUDENT_ID "
                + "JOIN SUBJECTS S ON S.ID=E.SUBJECT_ID "
                + "LEFT JOIN CLASS_SECTIONS C ON C.ID=E.SECTION_ID "
                + "LEFT JOIN PROGRAMS P ON P.ID=C.PROGRAM_ID "
                + "ORDER BY E.ID");
    }

    // =========================================================
    // GRADING SYSTEM — ADMIN AND INSTRUCTOR
    // =========================================================

    private void grades(boolean instructor) throws Exception {
        if (instructor && !isInstructor()) {
            throw new SecurityException("Instructor access required.");
        }

        if (!instructor && !admin()) {
            throw new SecurityException("Administrator access required.");
        }

        content.removeAll();

        JComboBox<Item> enrollment = new JComboBox<>();

        String enrollmentSql =
                "SELECT E.ID,ST.STUDENT_NO || ' - ' || ST.FIRST_NAME || ' ' "
                + "|| ST.LAST_NAME || ' / ' || S.SUBJECT_CODE "
                + "|| ' / ' || COALESCE(P.PROGRAM_CODE || ' Sec ' || C.SECTION_NAME,"
                + "'Legacy enrollment') "
                + "FROM ENROLLMENTS E "
                + "JOIN STUDENTS ST ON ST.ID=E.STUDENT_ID "
                + "JOIN SUBJECTS S ON S.ID=E.SUBJECT_ID "
                + "LEFT JOIN CLASS_SECTIONS C ON C.ID=E.SECTION_ID "
                + "LEFT JOIN PROGRAMS P ON P.ID=C.PROGRAM_ID";

        if (instructor) {
            enrollmentSql +=
                    " JOIN TEACHING_ASSIGNMENTS A ON A.SECTION_ID=E.SECTION_ID "
                    + "AND A.SUBJECT_ID=E.SUBJECT_ID "
                    + "JOIN INSTRUCTORS I ON I.ID=A.INSTRUCTOR_ID "
                    + "WHERE I.USER_ID=?";
        }

        enrollmentSql += " ORDER BY E.ID";

        java.util.List<String[]> enrollmentRows = instructor
                ? Database.query(enrollmentSql, userId)
                : Database.query(enrollmentSql);

        for (String[] row : enrollmentRows) {
            enrollment.addItem(new Item(
                    Integer.parseInt(row[0]), row[1]));
        }

        JTextField lectureWW = field(6);
        JTextField lectureUnit = field(6);
        JTextField lectureTerm = field(6);

        JTextField labWW = field(6);
        JTextField labUnit = field(6);
        JTextField labTerm = field(6);
        JTextField performance = field(6);

        JTextField remarks = field(18);
        JCheckBox hasLab = new JCheckBox("Subject includes laboratory");

        JLabel lectureResult = new JLabel("Lecture weighted grade: —");
        JLabel labResult = new JLabel("Laboratory weighted grade: —");
        JLabel finalResult = new JLabel("Final percentage: —");
        JLabel equivalentResult = new JLabel("College equivalent: —");

        JPanel form = gridForm();

        add(form, "Enrollment / Student / Subject", enrollment);

        add(form, "Lecture Written Works (30%)", lectureWW);
        add(form, "Lecture Unit Test (40%)", lectureUnit);
        add(form, "Lecture Term Test (30%)", lectureTerm);

        add(form, "Laboratory option", hasLab);
        add(form, "Lab Written Works (20%)", labWW);
        add(form, "Lab Unit Test (25%)", labUnit);
        add(form, "Lab Term Test (20%)", labTerm);
        add(form, "Lab/Field Exercises (35%)", performance);

        add(form, "Remarks", remarks);
        add(form, "", lectureResult);
        add(form, "", labResult);
        add(form, "", finalResult);
        add(form, "", equivalentResult);

        Runnable toggleLab = () -> {
            boolean enabled = hasLab.isSelected();
            labWW.setEnabled(enabled);
            labUnit.setEnabled(enabled);
            labTerm.setEnabled(enabled);
            performance.setEnabled(enabled);
        };

        hasLab.addActionListener(e -> toggleLab.run());
        toggleLab.run();

        Runnable calculate = () -> {
            try {
                double lecture = GradingCalculator.calculateLecture(
                        readScore(lectureWW),
                        readScore(lectureUnit),
                        readScore(lectureTerm));

                double lab = 0.0;

                if (hasLab.isSelected()) {
                    lab = GradingCalculator.calculateLaboratory(
                            readScore(labWW),
                            readScore(labUnit),
                            readScore(labTerm),
                            readScore(performance));
                }

                double raw = GradingCalculator.finalRawGrade(
                        lecture, lab, hasLab.isSelected());

                lectureResult.setText(
                        "Lecture weighted grade: "
                        + String.format(java.util.Locale.ROOT, "%.2f%%", lecture));

                labResult.setText(hasLab.isSelected()
                        ? "Laboratory weighted grade: "
                        + String.format(java.util.Locale.ROOT, "%.2f%%", lab)
                        : "Laboratory weighted grade: Not applicable");

                finalResult.setText(
                        "Final percentage: "
                        + String.format(java.util.Locale.ROOT, "%.2f%%", raw));

                equivalentResult.setText(
                        "College equivalent: "
                        + GradingCalculator.equivalentGrade(raw));

            } catch (Exception ex) {
                error(ex);
            }
        };

        JButton calculateButton = new JButton("Calculate Grade");
        JButton saveButton = new JButton("Save Grade");
        JButton refreshButton = new JButton("Refresh Grades");
        JButton printButton = new JButton("Print Grade Sheet");
        JButton editSelectedGrade = new JButton("Edit Selected Grade");
        JButton deleteSelectedGrade = new JButton("Delete Selected Grade");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(calculateButton);
        actions.add(saveButton);
        actions.add(editSelectedGrade);
        actions.add(deleteSelectedGrade);
        actions.add(refreshButton);
        actions.add(printButton);

        JTable table = new JTable();
        table.setAutoCreateRowSorter(true);

        Runnable loadSelected = () -> {
            Item selected = (Item) enrollment.getSelectedItem();

            if (selected == null) {
                return;
            }

            try {
                java.util.List<String[]> rows = Database.query(
                        "SELECT LECTURE_WRITTEN_WORKS,LECTURE_UNIT_TEST,"
                        + "LECTURE_TERM_TEST,LAB_WRITTEN_WORKS,LAB_UNIT_TEST,"
                        + "LAB_TERM_TEST,PERFORMANCE_TASKS,HAS_LABORATORY,REMARKS "
                        + "FROM GRADES WHERE ENROLLMENT_ID=?",
                        selected.id);

                if (rows.isEmpty()) {
                    lectureWW.setText("0");
                    lectureUnit.setText("0");
                    lectureTerm.setText("0");

                    labWW.setText("0");
                    labUnit.setText("0");
                    labTerm.setText("0");
                    performance.setText("0");

                    hasLab.setSelected(false);
                    remarks.setText("");

                } else {
                    String[] row = rows.get(0);

                    lectureWW.setText(orZero(row[0]));
                    lectureUnit.setText(orZero(row[1]));
                    lectureTerm.setText(orZero(row[2]));

                    labWW.setText(orZero(row[3]));
                    labUnit.setText(orZero(row[4]));
                    labTerm.setText(orZero(row[5]));
                    performance.setText(orZero(row[6]));

                    hasLab.setSelected(
                            "1".equals(row[7])
                            || "true".equalsIgnoreCase(row[7]));

                    remarks.setText(row[8] == null ? "" : row[8]);
                }

                toggleLab.run();
                calculate.run();

            } catch (Exception ex) {
                error(ex);
            }
        };

        calculateButton.addActionListener(e -> calculate.run());
        enrollment.addActionListener(e -> loadSelected.run());

        editSelectedGrade.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) { message("Select a grade row first."); return; }
            int modelRow = table.convertRowIndexToModel(viewRow);
            Object enrollmentId = table.getModel().getValueAt(modelRow, 1);
            for (int i=0; i<enrollment.getItemCount(); i++) {
                Item item = enrollment.getItemAt(i);
                if (String.valueOf(item.id).equals(String.valueOf(enrollmentId))) {
                    enrollment.setSelectedIndex(i);
                    loadSelected.run();
                    return;
                }
            }
            message("That enrollment is not available to this account.");
        });
        deleteSelectedGrade.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) { message("Select a grade row first."); return; }
            int modelRow = table.convertRowIndexToModel(viewRow);
            Object gradeId = table.getModel().getValueAt(modelRow, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete the selected grade record?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM GRADES WHERE ID=?")) {
                ps.setObject(1, gradeId);
                ps.executeUpdate();
                loadGradesTable(table, instructor);
                loadSelected.run();
            } catch (Exception ex) { error(ex); }
        });

        refreshButton.addActionListener(e -> {
            try {
                loadGradesTable(table, instructor);
                loadSelected.run();
            } catch (Exception ex) {
                error(ex);
            }
        });

        printButton.addActionListener(e -> {
            try {
                table.print();
            } catch (Exception ex) {
                error(ex);
            }
        });

        saveButton.addActionListener(e -> {
            Item selected = (Item) enrollment.getSelectedItem();

            if (selected == null) {
                message(
                        "No eligible enrollments are available. "
                        + "For instructors, check teaching assignments.");
                return;
            }

            try {
                double lww = readScore(lectureWW);
                double lut = readScore(lectureUnit);
                double ltt = readScore(lectureTerm);

                double bww = readScore(labWW);
                double but = readScore(labUnit);
                double btt = readScore(labTerm);
                double pt = readScore(performance);

                boolean labEnabled = hasLab.isSelected();

                double lecture = GradingCalculator.calculateLecture(
                        lww, lut, ltt);

                double lab = labEnabled
                        ? GradingCalculator.calculateLaboratory(
                                bww, but, btt, pt)
                        : 0.0;

                double raw = GradingCalculator.finalRawGrade(
                        lecture, lab, labEnabled);

                String equivalent =
                        GradingCalculator.equivalentGrade(raw);

                try (Connection c = Database.getConnection()) {
                    c.setAutoCommit(false);

                    try {
                        // Verify instructor assignment again before saving.
                        if (instructor) {
                            try (PreparedStatement check = c.prepareStatement(
                                    "SELECT COUNT(*) FROM ENROLLMENTS E "
                                    + "JOIN TEACHING_ASSIGNMENTS A "
                                    + "ON A.SECTION_ID=E.SECTION_ID "
                                    + "AND A.SUBJECT_ID=E.SUBJECT_ID "
                                    + "JOIN INSTRUCTORS I ON I.ID=A.INSTRUCTOR_ID "
                                    + "WHERE E.ID=? AND I.USER_ID=?")) {

                                check.setInt(1, selected.id);
                                check.setInt(2, userId);

                                try (ResultSet rs = check.executeQuery()) {
                                    rs.next();

                                    if (rs.getInt(1) == 0) {
                                        throw new SecurityException(
                                                "You are not assigned to this enrollment.");
                                    }
                                }
                            }
                        }

                        boolean exists;

                        try (PreparedStatement check = c.prepareStatement(
                                "SELECT ID FROM GRADES WHERE ENROLLMENT_ID=?")) {

                            check.setInt(1, selected.id);

                            try (ResultSet rs = check.executeQuery()) {
                                exists = rs.next();
                            }
                        }

                        String sql = exists
                                ? "UPDATE GRADES SET "
                                + "LECTURE_WRITTEN_WORKS=?,LECTURE_UNIT_TEST=?,"
                                + "LECTURE_TERM_TEST=?,LAB_WRITTEN_WORKS=?,"
                                + "LAB_UNIT_TEST=?,LAB_TERM_TEST=?,PERFORMANCE_TASKS=?,"
                                + "HAS_LABORATORY=?,FINAL_GRADE=?,EQUIVALENT_GRADE=?,"
                                + "REMARKS=? WHERE ENROLLMENT_ID=?"
                                : "INSERT INTO GRADES("
                                + "LECTURE_WRITTEN_WORKS,LECTURE_UNIT_TEST,"
                                + "LECTURE_TERM_TEST,LAB_WRITTEN_WORKS,LAB_UNIT_TEST,"
                                + "LAB_TERM_TEST,PERFORMANCE_TASKS,HAS_LABORATORY,"
                                + "FINAL_GRADE,EQUIVALENT_GRADE,REMARKS,ENROLLMENT_ID) "
                                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

                        try (PreparedStatement ps = c.prepareStatement(sql)) {
                            ps.setBigDecimal(1, BigDecimal.valueOf(lww));
                            ps.setBigDecimal(2, BigDecimal.valueOf(lut));
                            ps.setBigDecimal(3, BigDecimal.valueOf(ltt));

                            ps.setBigDecimal(4, BigDecimal.valueOf(bww));
                            ps.setBigDecimal(5, BigDecimal.valueOf(but));
                            ps.setBigDecimal(6, BigDecimal.valueOf(btt));
                            ps.setBigDecimal(7, BigDecimal.valueOf(pt));

                            ps.setInt(8, labEnabled ? 1 : 0);
                            ps.setBigDecimal(9, BigDecimal.valueOf(raw));
                            ps.setString(10, equivalent);
                            ps.setString(11, remarks.getText().trim());
                            ps.setInt(12, selected.id);

                            ps.executeUpdate();
                        }

                        c.commit();

                    } catch (Exception ex) {
                        c.rollback();
                        throw ex;

                    } finally {
                        c.setAutoCommit(true);
                    }
                }

                message(
                        "Grade saved successfully.\n"
                        + "Final percentage: "
                        + String.format(java.util.Locale.ROOT, "%.2f%%", raw)
                        + "\nCollege equivalent: " + equivalent);

                calculate.run();
                loadGradesTable(table, instructor);

            } catch (Exception ex) {
                error(ex);
            }
        });

        JPanel top = new JPanel(new BorderLayout(8, 8));

        top.add(new JLabel(instructor
                ? "Instructor — grade only students in your assigned classes"
                : "Administrator — grade management"), BorderLayout.NORTH);

        top.add(new JScrollPane(form), BorderLayout.CENTER);
        top.add(actions, BorderLayout.SOUTH);

        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        loadGradesTable(table, instructor);
        loadSelected.run();
        refresh();
    }

    private double readScore(JTextField field) {
        String text = field.getText().trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException(
                    "Enter a score from 0 to 100 in each component.");
        }

        double value = Double.parseDouble(text);

        if (!Double.isFinite(value) || value < 0 || value > 100) {
            throw new IllegalArgumentException(
                    "Every component score must be between 0 and 100.");
        }

        return value;
    }

    private String orZero(String value) {
        return value == null || value.isBlank() ? "0" : value;
    }

    private void loadGradesTable(JTable table, boolean instructor)
            throws Exception {

        String sql =
                "SELECT G.ID,E.ID,ST.STUDENT_NO,"
                + "ST.FIRST_NAME || ' ' || ST.LAST_NAME,"
                + "S.SUBJECT_CODE,"
                + "COALESCE(P.PROGRAM_CODE || ' - ' || C.SECTION_NAME,'Legacy'),"
                + "G.LECTURE_WRITTEN_WORKS,G.LECTURE_UNIT_TEST,"
                + "G.LECTURE_TERM_TEST,G.LAB_WRITTEN_WORKS,"
                + "G.LAB_UNIT_TEST,G.LAB_TERM_TEST,G.PERFORMANCE_TASKS,"
                + "G.FINAL_GRADE,G.EQUIVALENT_GRADE,G.REMARKS "
                + "FROM GRADES G "
                + "JOIN ENROLLMENTS E ON E.ID=G.ENROLLMENT_ID "
                + "JOIN STUDENTS ST ON ST.ID=E.STUDENT_ID "
                + "JOIN SUBJECTS S ON S.ID=E.SUBJECT_ID "
                + "LEFT JOIN CLASS_SECTIONS C ON C.ID=E.SECTION_ID "
                + "LEFT JOIN PROGRAMS P ON P.ID=C.PROGRAM_ID";

        if (instructor) {
            sql +=
                    " JOIN TEACHING_ASSIGNMENTS A "
                    + "ON A.SECTION_ID=E.SECTION_ID "
                    + "AND A.SUBJECT_ID=E.SUBJECT_ID "
                    + "JOIN INSTRUCTORS I ON I.ID=A.INSTRUCTOR_ID "
                    + "WHERE I.USER_ID=" + userId;
        }

        sql += " ORDER BY G.ID";

        setTable(table, new String[]{
            "Grade ID",
            "Enrollment ID",
            "Student No",
            "Student",
            "Subject",
            "Section",
            "Lecture WW",
            "Lecture Unit Test",
            "Lecture Term Test",
            "Lab WW",
            "Lab Unit Test",
            "Lab Term Test",
            "Lab/Field Exercises",
            "Final %",
            "Equivalent",
            "Remarks"
        }, Database.query(sql));
    }

    // =========================================================
    // REPORTS
    // =========================================================

    private void reports() {
        tableScreen(
                "Enrollment and grades report",
                "SELECT ST.STUDENT_NO,"
                + "ST.FIRST_NAME || ' ' || ST.LAST_NAME,"
                + "S.SUBJECT_CODE,S.SUBJECT_TITLE,"
                + "E.SCHOOL_YEAR,E.SEMESTER,"
                + "G.FINAL_GRADE,G.EQUIVALENT_GRADE,G.REMARKS "
                + "FROM ENROLLMENTS E "
                + "JOIN STUDENTS ST ON ST.ID=E.STUDENT_ID "
                + "JOIN SUBJECTS S ON S.ID=E.SUBJECT_ID "
                + "LEFT JOIN GRADES G ON G.ENROLLMENT_ID=E.ID "
                + "ORDER BY ST.LAST_NAME,S.SUBJECT_CODE",
                new String[]{
                    "Student No", "Student", "Subject Code", "Subject",
                    "School Year", "Semester", "Final %", "Equivalent", "Remarks"
                });
    }
}
