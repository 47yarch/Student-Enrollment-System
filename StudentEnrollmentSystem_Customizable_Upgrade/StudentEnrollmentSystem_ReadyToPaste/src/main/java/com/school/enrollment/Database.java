
package com.school.enrollment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class Database {

    private static final String URL =
            "jdbc:derby:student-enrollment-db;create=true";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Initializes missing tables and columns without deleting existing data.
     * Existing identity IDs are not reset or renumbered.
     */
    public static void initialize() throws Exception {

        try (Connection c = getConnection();
             Statement s = c.createStatement()) {

            // ---------------------------------------------------------
            // USER ACCOUNTS
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE APP_USERS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "USERNAME VARCHAR(60) NOT NULL UNIQUE,"
                    + "FULL_NAME VARCHAR(120) NOT NULL,"
                    + "ROLE_NAME VARCHAR(20) NOT NULL,"
                    + "SALT VARCHAR(100) NOT NULL,"
                    + "PASS_HASH VARCHAR(200) NOT NULL,"
                    + "ACTIVE SMALLINT NOT NULL DEFAULT 1"
                    + ")");

            // ---------------------------------------------------------
            // PROGRAMS
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE PROGRAMS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "PROGRAM_CODE VARCHAR(20) NOT NULL UNIQUE,"
                    + "PROGRAM_NAME VARCHAR(150) NOT NULL"
                    + ")");

            // ---------------------------------------------------------
            // SUBJECTS
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE SUBJECTS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "SUBJECT_CODE VARCHAR(30) NOT NULL UNIQUE,"
                    + "SUBJECT_TITLE VARCHAR(150) NOT NULL,"
                    + "UNITS INT NOT NULL DEFAULT 3"
                    + ")");

            // ---------------------------------------------------------
            // STUDENTS
            // PROGRAM is retained for compatibility with older records.
            // PROGRAM_ID supports the program foreign-key relationship.
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE STUDENTS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "STUDENT_NO VARCHAR(40) NOT NULL UNIQUE,"
                    + "FIRST_NAME VARCHAR(80) NOT NULL,"
                    + "LAST_NAME VARCHAR(80) NOT NULL,"
                    + "EMAIL VARCHAR(120),"
                    + "PROGRAM VARCHAR(100),"
                    + "YEAR_LEVEL VARCHAR(20),"
                    + "STATUS VARCHAR(20) NOT NULL DEFAULT 'Active'"
                    + ")");

            // ---------------------------------------------------------
            // INSTRUCTORS
            // USER_ID links an instructor to an APP_USERS login.
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE INSTRUCTORS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "EMPLOYEE_NO VARCHAR(40) NOT NULL UNIQUE,"
                    + "FULL_NAME VARCHAR(120) NOT NULL,"
                    + "EMAIL VARCHAR(120),"
                    + "DEPARTMENT VARCHAR(100)"
                    + ")");

            // ---------------------------------------------------------
            // PROGRAM SUBJECTS
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE PROGRAM_SUBJECTS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "PROGRAM_ID INT NOT NULL,"
                    + "SUBJECT_ID INT NOT NULL,"
                    + "CONSTRAINT FK_PS_PROG FOREIGN KEY (PROGRAM_ID)"
                    + " REFERENCES PROGRAMS(ID),"
                    + "CONSTRAINT FK_PS_SUB FOREIGN KEY (SUBJECT_ID)"
                    + " REFERENCES SUBJECTS(ID),"
                    + "CONSTRAINT UQ_PS UNIQUE (PROGRAM_ID, SUBJECT_ID)"
                    + ")");

            // ---------------------------------------------------------
            // CLASS SECTIONS
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE CLASS_SECTIONS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "PROGRAM_ID INT NOT NULL,"
                    + "YEAR_LEVEL VARCHAR(20) NOT NULL,"
                    + "SECTION_NAME VARCHAR(40) NOT NULL,"
                    + "SCHOOL_YEAR VARCHAR(20) NOT NULL,"
                    + "SEMESTER VARCHAR(30) NOT NULL,"
                    + "CONSTRAINT FK_SEC_PROG FOREIGN KEY (PROGRAM_ID)"
                    + " REFERENCES PROGRAMS(ID)"
                    + ")");

            // ---------------------------------------------------------
            // TEACHING ASSIGNMENTS
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE TEACHING_ASSIGNMENTS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "INSTRUCTOR_ID INT NOT NULL,"
                    + "SECTION_ID INT NOT NULL,"
                    + "SUBJECT_ID INT NOT NULL,"
                    + "CONSTRAINT FK_TA_INS FOREIGN KEY (INSTRUCTOR_ID)"
                    + " REFERENCES INSTRUCTORS(ID),"
                    + "CONSTRAINT FK_TA_SEC FOREIGN KEY (SECTION_ID)"
                    + " REFERENCES CLASS_SECTIONS(ID),"
                    + "CONSTRAINT FK_TA_SUB FOREIGN KEY (SUBJECT_ID)"
                    + " REFERENCES SUBJECTS(ID),"
                    + "CONSTRAINT UQ_TA UNIQUE"
                    + " (INSTRUCTOR_ID, SECTION_ID, SUBJECT_ID)"
                    + ")");

            // ---------------------------------------------------------
            // ENROLLMENTS
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE ENROLLMENTS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "STUDENT_ID INT NOT NULL,"
                    + "SUBJECT_ID INT NOT NULL,"
                    + "SCHOOL_YEAR VARCHAR(20) NOT NULL,"
                    + "SEMESTER VARCHAR(30) NOT NULL,"
                    + "ENROLLMENT_DATE DATE NOT NULL,"
                    + "CONSTRAINT FK_ENR_STU FOREIGN KEY (STUDENT_ID)"
                    + " REFERENCES STUDENTS(ID),"
                    + "CONSTRAINT FK_ENR_SUB FOREIGN KEY (SUBJECT_ID)"
                    + " REFERENCES SUBJECTS(ID),"
                    + "CONSTRAINT UQ_ENR UNIQUE"
                    + " (STUDENT_ID, SUBJECT_ID, SCHOOL_YEAR, SEMESTER)"
                    + ")");

            // ---------------------------------------------------------
            // ADDITIONAL ADMIN MANAGEMENT TABLES
            // Create missing tables only; never drop or reset existing data.
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE DEPARTMENTS ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "DEPARTMENT_NAME VARCHAR(120) NOT NULL,"
                    + "DEAN_NAME VARCHAR(120),"
                    + "EMAIL VARCHAR(120),"
                    + "ACTIVE SMALLINT NOT NULL DEFAULT 1"
                    + ")");

            create(s,
                    "CREATE TABLE CLASS_SCHEDULES ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "SECTION_ID INT NOT NULL,"
                    + "SUBJECT_ID INT NOT NULL,"
                    + "DAY_NAME VARCHAR(20) NOT NULL,"
                    + "START_TIME VARCHAR(20) NOT NULL,"
                    + "END_TIME VARCHAR(20) NOT NULL,"
                    + "ROOM VARCHAR(80)"
                    + ")");

            create(s,
                    "CREATE TABLE SUBJECT_PREREQUISITES ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "SUBJECT_ID INT NOT NULL,"
                    + "PREREQUISITE_SUBJECT_ID INT NOT NULL,"
                    + "CONSTRAINT UQ_SUB_PREREQ UNIQUE (SUBJECT_ID, PREREQUISITE_SUBJECT_ID)"
                    + ")");

            create(s,
                    "CREATE TABLE GRADE_AUDIT_LOG ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "ACTION_NAME VARCHAR(40),"
                    + "TABLE_NAME VARCHAR(60),"
                    + "RECORD_ID INT,"
                    + "DETAILS VARCHAR(1000),"
                    + "ACTOR VARCHAR(120),"
                    + "ACTION_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ")");

            // ---------------------------------------------------------
            // GRADES
            // ---------------------------------------------------------
            create(s,
                    "CREATE TABLE GRADES ("
                    + "ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                    + "ENROLLMENT_ID INT NOT NULL UNIQUE,"
                    + "FINAL_GRADE DECIMAL(5,2),"
                    + "REMARKS VARCHAR(300),"
                    + "CONSTRAINT FK_GRADE_ENR FOREIGN KEY (ENROLLMENT_ID)"
                    + " REFERENCES ENROLLMENTS(ID)"
                    + ")");
        }

        // -------------------------------------------------------------
        // ADDITIONAL COLUMNS
        // Safe for existing databases: add only if the column is missing.
        // -------------------------------------------------------------
        try (Connection c = getConnection()) {

            // Instructor account relationship.
            addColumnIfMissing(
                    c, "INSTRUCTORS", "USER_ID", "INT");

            // Student program relationship.
            addColumnIfMissing(
                    c, "STUDENTS", "PROGRAM_ID", "INT");

            // Class section relationship for enrollment.
            addColumnIfMissing(
                    c, "ENROLLMENTS", "SECTION_ID", "INT");

            // ---------------------------------------------------------
            // LECTURE GRADING COMPONENTS
            // Written Works 30%, Unit Test 40%, Term Test 30%.
            // ---------------------------------------------------------
            addColumnIfMissing(
                    c, "GRADES", "LECTURE_WRITTEN_WORKS", "DECIMAL(5,2)");

            addColumnIfMissing(
                    c, "GRADES", "LECTURE_UNIT_TEST", "DECIMAL(5,2)");

            addColumnIfMissing(
                    c, "GRADES", "LECTURE_TERM_TEST", "DECIMAL(5,2)");

            // ---------------------------------------------------------
            // LABORATORY GRADING COMPONENTS
            // Written Works 20%, Unit Test 25%, Term Test 20%,
            // Lab/Field Exercises 35%.
            // ---------------------------------------------------------
            addColumnIfMissing(
                    c, "GRADES", "LAB_WRITTEN_WORKS", "DECIMAL(5,2)");

            addColumnIfMissing(
                    c, "GRADES", "LAB_UNIT_TEST", "DECIMAL(5,2)");

            addColumnIfMissing(
                    c, "GRADES", "LAB_TERM_TEST", "DECIMAL(5,2)");

            addColumnIfMissing(
                    c, "GRADES", "PERFORMANCE_TASKS", "DECIMAL(5,2)");

            // Indicates whether the subject has a laboratory component.
            addColumnIfMissing(
                    c, "GRADES", "HAS_LABORATORY", "SMALLINT DEFAULT 0");

            // College equivalent: 1.00 through 5.00.
            addColumnIfMissing(
                    c, "GRADES", "EQUIVALENT_GRADE", "VARCHAR(4)");
        }

        // Create the default administrator only if it does not exist.
        seedAdmin();
    }

    /**
     * Creates a table only when it does not already exist.
     * Derby SQLState X0Y32 means the table already exists.
     */
    private static void create(
            Statement statement,
            String sql) throws SQLException {

        try {
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            if (!"X0Y32".equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }

    /**
     * Adds a column only when it is missing.
     * Existing values in the table are retained.
     */
    private static void addColumnIfMissing(
            Connection connection,
            String table,
            String column,
            String type) throws SQLException {

        boolean exists = false;

        try (ResultSet result = connection.getMetaData()
                .getColumns(null, null, table, column)) {
            exists = result.next();
        }

        if (!exists) {
            String sql = "ALTER TABLE " + table
                    + " ADD COLUMN " + column + " " + type;

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        }
    }

    /**
     * Inserts the default administrator only if username "admin"
     * is not already present.
     */
    private static void seedAdmin() throws Exception {

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT ID FROM APP_USERS WHERE USERNAME = ?")) {

            p.setString(1, "admin");

            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return;
                }
            }
        }

        String salt = Auth.newSalt();
        String hash = Auth.hash(
                "ChangeMe123!".toCharArray(), salt);

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "INSERT INTO APP_USERS "
                     + "(USERNAME, FULL_NAME, ROLE_NAME, SALT, PASS_HASH, ACTIVE) "
                     + "VALUES (?, ?, ?, ?, ?, ?)")) {

            p.setString(1, "admin");
            p.setString(2, "System Administrator");
            p.setString(3, "Administrator");
            p.setString(4, salt);
            p.setString(5, hash);
            p.setInt(6, 1);

            p.executeUpdate();
        }
    }

    /**
     * Executes a SELECT statement and returns all rows as strings.
     * Supports parameterized SQL, for example:
     * Database.query("SELECT ID FROM APP_USERS WHERE USERNAME=?", username)
     */
    public static List<String[]> query(
            String sql,
            Object... params) throws SQLException {

        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                p.setObject(i + 1, params[i]);
            }

            try (ResultSet r = p.executeQuery()) {

                ResultSetMetaData metadata = r.getMetaData();
                int columnCount = metadata.getColumnCount();

                List<String[]> rows = new ArrayList<>();

                while (r.next()) {
                    String[] row = new String[columnCount];

                    for (int column = 1;
                         column <= columnCount;
                         column++) {

                        row[column - 1] = r.getString(column);
                    }

                    rows.add(row);
                }

                return rows;
            }
        }
    }
}
