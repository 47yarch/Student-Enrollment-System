STUDENT ENROLLMENT, GRADING AND RECORDS SYSTEM
Java 21 + Swing + Apache Derby + Maven

OPEN IN NETBEANS
1. Extract this ZIP to a normal writable folder.
2. NetBeans: File > Open Project, then select the extracted folder containing pom.xml.
3. Wait for Maven dependencies to download.
4. Run the project (Main class: com.school.enrollment.Main).

FIRST LOGIN
Username: admin
Password: ChangeMe123!
Change the starter password if your build exposes account/password management before using real student data.

CURRENT MODULES
Dashboard; Programs; Departments; Subjects; Program Subjects; Students; Instructors and instructor accounts; Class Sections; Class Schedules; Subject Prerequisites; Instructor Assignments; Enrollment; Grades; instructor-restricted My Classes / Grades; Grade Audit; Reports; Appearance & Settings.

RECORD MANAGEMENT / CRUD
Programs, Subjects, Program Subjects, Students, Instructors, Class Sections, Instructor Assignments, and Enrollment screens include Add Record, Edit Selected, Delete Selected, Search, and Refresh controls. Grades includes Save Grade, Edit Selected Grade, Delete Selected Grade, Refresh Grades, and Print Grade Sheet. Departments, Class Schedules, and Subject Prerequisites include Add, Edit Selected, Delete Selected, Search, Refresh, and Export PDF. Grade Audit is read-only by design. Reports are read-only. Database constraints can block deletion of records referenced by existing data; the app reports the failure instead of deleting dependent records.

APPEARANCE CUSTOMIZATION
Open Appearance & Settings in the navigation. Choose a font size from 12 to 24 and Light/Dark theme, then Apply and Save Preferences. Preferences are stored per operating-system user and do not modify the school database. Default font size is 16 for improved readability.

DATABASE SAFETY
The app uses the relative folder student-enrollment-db and performs additive schema migrations. This ZIP intentionally does NOT include a database folder or any student records. When running an existing installation, keep using your existing project folder/database and make a backup copy of student-enrollment-db before testing. Do not delete that folder if you need your existing records and IDs. The ZIP itself does not reset, import, or replace your existing database.

CRUD NOTE
The generic Add/Edit dialogs expose database columns directly. Enter linked IDs where fields end in _ID, and follow the required-field/data-type prompts. Back up the database before performing destructive changes.

GRADING
The existing GradingCalculator and grade component columns are retained. Confirm that the configured grading weights match your institution's official grading policy before official use.

USER-FRIENDLY NAVIGATION
The administrator sidebar is grouped into collapsible sections: Academic Setup, People, Classes & Enrollment, Grades & Reports, and System. Click a group heading to expand or collapse it, reducing visual clutter while keeping every module available. Add/Edit dialogs show examples under fields and in tooltips; use the examples as formats, not as records to copy blindly. For normal student and instructor registration, use the Students and Instructors forms so TUPT and EMP numbers are generated automatically.
