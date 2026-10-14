package com.school.enrollment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Additive management screens for departments, schedules, prerequisites and grade audit.
 * Table names are whitelisted; all SQL values are bound parameters.
 */
public final class AdvancedManagementPanel extends JPanel {
    private final String screen, actor;
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel();
    private final JTextField search = new JTextField(22);
    private String tableName;
    private List<String> columns = new ArrayList<>();
    private List<String> editable = new ArrayList<>();

    public AdvancedManagementPanel(String screen, String actor) {
        this.screen=screen; this.actor=actor;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JLabel heading=new JLabel(screen); heading.setFont(new Font("SansSerif",Font.BOLD,24));
        JPanel top=new JPanel(new BorderLayout(8,8)); top.add(heading,BorderLayout.NORTH);
        JPanel tools=new JPanel(new FlowLayout(FlowLayout.LEFT));
        tools.add(new JLabel("Search:")); tools.add(search);
        JButton find=new JButton("Search"), refresh=new JButton("Refresh");
        tools.add(find); tools.add(refresh);
        top.add(tools,BorderLayout.CENTER); add(top,BorderLayout.NORTH);
        table.setModel(model); table.setFont(new Font("SansSerif",Font.PLAIN,14));
        table.setRowHeight(30); table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("Add"), edit=new JButton("Edit Selected"), del=new JButton("Delete Selected"), print=new JButton("Export PDF");
        actions.add(add); actions.add(edit); actions.add(del); actions.add(print); add(actions,BorderLayout.SOUTH);
        find.addActionListener(e->filter()); refresh.addActionListener(e->load());
        search.addActionListener(e->filter()); add.addActionListener(e->editRow(false));
        edit.addActionListener(e->editRow(true)); del.addActionListener(e->deleteRow());
        print.addActionListener(e->exportPdf());
        configure(); load();
    }

    private void configure() {
        switch(screen) {
            case "Departments" -> {tableName="DEPARTMENTS"; editable=List.of("DEPARTMENT_NAME","DEAN_NAME","EMAIL","ACTIVE");}
            case "Class Schedules" -> {tableName="CLASS_SCHEDULES"; editable=List.of("SECTION_ID","SUBJECT_ID","DAY_NAME","START_TIME","END_TIME","ROOM");}
            case "Subject Prerequisites" -> {tableName="SUBJECT_PREREQUISITES"; editable=List.of("SUBJECT_ID","PREREQUISITE_SUBJECT_ID");}
            case "Grade Audit" -> {tableName="GRADE_AUDIT_LOG"; editable=List.of();}
            default -> throw new IllegalArgumentException("Unsupported screen");
        }
    }

    private void load() {
        try(Connection c=Database.getConnection(); Statement s=c.createStatement();
            ResultSet r=s.executeQuery("SELECT * FROM "+tableName+" ORDER BY ID")) {
            ResultSetMetaData md=r.getMetaData(); columns=new ArrayList<>();
            model.setRowCount(0); model.setColumnCount(0);
            for(int i=1;i<=md.getColumnCount();i++){String col=md.getColumnName(i);columns.add(col);model.addColumn(col);}
            while(r.next()){Object[] row=new Object[columns.size()];for(int i=0;i<row.length;i++)row[i]=r.getObject(i+1);model.addRow(row);}
            filter();
        } catch(Exception ex){error("Could not load "+screen+": "+ex.getMessage());}
    }

    private void filter() {
        String q=search.getText().trim().toLowerCase(Locale.ROOT);
        if(table.getRowSorter() instanceof javax.swing.table.TableRowSorter<?> sorter) {
            @SuppressWarnings("unchecked") javax.swing.table.TableRowSorter<DefaultTableModel> typed=(javax.swing.table.TableRowSorter<DefaultTableModel>)sorter;
            typed.setRowFilter(q.isEmpty()?null:new javax.swing.RowFilter<>() {
                public boolean include(Entry<? extends DefaultTableModel,? extends Integer> e) {
                    for(int i=0;i<e.getValueCount();i++) if(String.valueOf(e.getValue(i)).toLowerCase(Locale.ROOT).contains(q))return true;
                    return false;
                }
            });
        }
    }

    private void editRow(boolean edit) {
        if(edit && table.getSelectedRow()<0){error("Select a row first.");return;}
        if(edit && editable.isEmpty()){error("This is a read-only audit log.");return;}
        int modelRow=edit?table.convertRowIndexToModel(table.getSelectedRow()):-1;
        LinkedHashMap<String,JComponent> fields=new LinkedHashMap<>();
        JPanel form=new JPanel(new GridLayout(0,2,8,8));
        for(String col:editable) {
            JTextField input=new JTextField(edit?Objects.toString(model.getValueAt(modelRow,columns.indexOf(col)),""):"",24);
            fields.put(col,input);form.add(new JLabel(col.replace('_',' ')));form.add(input);
        }
        if(fields.isEmpty())return;
        int result=JOptionPane.showConfirmDialog(this,new JScrollPane(form),edit?"Edit "+screen:"Add "+screen,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(result!=JOptionPane.OK_OPTION)return;
        try(Connection c=Database.getConnection()) {
            if(edit) {
                String set=String.join(",",editable.stream().map(x->x+"=?").toList());
                try(PreparedStatement ps=c.prepareStatement("UPDATE "+tableName+" SET "+set+" WHERE ID=?")) {
                    int i=1;for(var en:fields.entrySet())ps.setObject(i++,value(en.getValue(),en.getKey()));
                    ps.setObject(i,model.getValueAt(modelRow,columns.indexOf("ID")));ps.executeUpdate();
                }
            } else {
                String names=String.join(",",editable),marks=String.join(",",Collections.nCopies(editable.size(),"?"));
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO "+tableName+" ("+names+") VALUES ("+marks+")")) {
                    int i=1;for(var en:fields.entrySet())ps.setObject(i++,value(en.getValue(),en.getKey()));ps.executeUpdate();
                }
            }
            load();
        } catch(Exception ex){error("Save failed. Check required values and linked IDs. "+ex.getMessage());}
    }

    private Object value(JComponent component,String col) {
        String value=((JTextField)component).getText().trim();
        if(value.isEmpty())return null;
        if(col.equals("ACTIVE")||col.endsWith("_ID"))return Integer.valueOf(value);
        return value;
    }

    private void deleteRow() {
        int selected=table.getSelectedRow();if(selected<0){error("Select a row first.");return;}
        if(!editable.isEmpty() && tableName.equals("GRADE_AUDIT_LOG")){error("Audit records cannot be deleted.");return;}
        if(JOptionPane.showConfirmDialog(this,"Delete selected record? Related records may prevent deletion.","Confirm delete",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM "+tableName+" WHERE ID=?")) {
            ps.setObject(1,model.getValueAt(table.convertRowIndexToModel(selected),columns.indexOf("ID")));ps.executeUpdate();load();
        }catch(Exception ex){error("Delete blocked or failed. Linked records may depend on this record. "+ex.getMessage());}
    }

    private void exportPdf() {
        try {
            String school=setting("SCHOOL_NAME","Technological University of the Philippines - Taguig");
            StringBuilder body=new StringBuilder("STUDENT INFORMATION SYSTEM\n").append(screen).append("\nSchool: ").append(school)
              .append("\nGenerated by: ").append(actor).append("\nGenerated at: ")
              .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            for(int i=0;i<model.getColumnCount();i++)body.append(model.getColumnName(i)).append(i==model.getColumnCount()-1?'\n':'\t');
            for(int r=0;r<model.getRowCount();r++){for(int c=0;c<model.getColumnCount();c++)body.append(Objects.toString(model.getValueAt(r,c),"")).append(c==model.getColumnCount()-1?'\n':'\t');}
            Path path=Path.of("reports");Files.createDirectories(path);
            Path file=path.resolve(screen.replaceAll("[^A-Za-z0-9]+","_")+"_"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))+".pdf");
            SimplePdf.write(file,body.toString());
            JOptionPane.showMessageDialog(this,"PDF report saved to:\n"+file.toAbsolutePath());
        }catch(Exception ex){error("PDF export failed: "+ex.getMessage());}
    }
    private String setting(String key,String fallback) {
        try {List<String[]> rows=Database.query("SELECT SETTING_VALUE FROM SYSTEM_SETTINGS WHERE SETTING_KEY=?",key);if(!rows.isEmpty()&&rows.get(0)[0]!=null&&!rows.get(0)[0].isBlank())return rows.get(0)[0];}catch(Exception ignored){}
        return fallback;
    }
    private void error(String message){JOptionPane.showMessageDialog(this,message,"Student Information System",JOptionPane.WARNING_MESSAGE);}
}
