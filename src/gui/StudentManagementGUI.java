package gui;

import client.ClientConnection;
import common.Request;
import common.Response;
import common.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class StudentManagementGUI extends JFrame {

    private JTextField tfId, tfName, tfEmail, tfCourse, tfCgpa;
    private JTextField tfSearch, tfViewId, tfFilterCgpa, tfFilterCourse;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel statusLabel;

    public StudentManagementGUI() {
        setTitle("Student Management System — MSIT IT Dept.");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // always open fullscreen
        setLayout(new BorderLayout(8, 8));

        add(buildFormPanel(), BorderLayout.NORTH);

        // JSplitPane ensures the bottom toolbar always gets its space
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildTablePanel(),
                buildBottomPanel());
        split.setResizeWeight(0.65);          // table gets 65%, bottom 35%
        split.setDividerSize(5);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        loadAllStudents();
        setVisible(true);
    }

    // ─── Form Panel (unchanged) ───────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Student Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        tfId     = addField(form, gbc, "ID (auto):", 0, true);
        tfName   = addField(form, gbc, "Name:",      1, false);
        tfEmail  = addField(form, gbc, "Email:",     2, false);
        tfCourse = addField(form, gbc, "Course:",    3, false);
        tfCgpa   = addField(form, gbc, "CGPA:",      4, false);

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        form.add(buildButtonPanel(), gbc);
        return form;
    }

    private JTextField addField(JPanel p, GridBagConstraints gbc,
                                String label, int row, boolean readOnly) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0.3;
        p.add(new JLabel(label), gbc);
        JTextField tf = new JTextField(18);
        tf.setEditable(!readOnly);
        if (readOnly) tf.setBackground(new Color(230, 230, 230));
        gbc.gridx = 1; gbc.weightx = 0.7;
        p.add(tf, gbc);
        return tf;
    }

    private JPanel buildButtonPanel() {
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        String[][] buttons = {
                {"Add",    "#4CAF50"}, {"Update",  "#2196F3"},
                {"Delete", "#f44336"}, {"Refresh", "#FF9800"},
                {"Clear",  "#9E9E9E"}
        };
        for (String[] b : buttons) {
            JButton btn = makeButton(b[0], b[1]);
            btn.addActionListener(e -> handleButton(b[0]));
            bp.add(btn);
        }
        return bp;
    }

    // ─── Table Panel ──────────────────────────────────────────────────────────

    private JScrollPane buildTablePanel() {
        String[] cols = {"ID", "Name", "Email", "Course", "CGPA"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Attach sorter so sort/filter can work together
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        return new JScrollPane(table);
    }

    // ─── Bottom panel: Search | Sort | Filter | View by ID ───────────────────

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        JPanel topRows = new JPanel(new GridLayout(2, 1, 4, 4));
        topRows.add(buildSearchSortPanel());
        topRows.add(buildFilterViewPanel());
        bottom.add(topRows, BorderLayout.CENTER);
        bottom.add(buildStatusBar(), BorderLayout.SOUTH);
        bottom.setPreferredSize(new Dimension(0, 160)); // guarantee space
        return bottom;
    }

    /** Row 1: Search field + sort buttons */
    private JPanel buildSearchSortPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setBorder(BorderFactory.createTitledBorder("Search & Sort"));

        // Search
        p.add(new JLabel("Search (name/email):"));
        tfSearch = new JTextField(16);
        p.add(tfSearch);

        JButton btnSearch = makeButton("Search", "#673AB7");
        btnSearch.addActionListener(e -> applySearch());
        p.add(btnSearch);

        JButton btnClearSearch = makeButton("Clear Search", "#9E9E9E");
        btnClearSearch.addActionListener(e -> {
            tfSearch.setText("");
            rowSorter.setRowFilter(null);
            status("Search cleared — showing all records.");
        });
        p.add(btnClearSearch);

        // Sort
        p.add(Box.createHorizontalStrut(20));
        p.add(new JLabel("Sort:"));

        JButton btnSortName = makeButton("By Name ↑", "#00897B");
        btnSortName.addActionListener(e -> {
            rowSorter.setComparator(1, Comparator.naturalOrder());
            rowSorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
            status("Sorted by Name (A → Z).");
        });
        p.add(btnSortName);

        JButton btnSortCgpa = makeButton("By CGPA ↓", "#00897B");
        btnSortCgpa.addActionListener(e -> {
            rowSorter.setComparator(4, Comparator.comparingDouble(
                    o -> Double.parseDouble(o.toString())));
            rowSorter.setSortKeys(List.of(new RowSorter.SortKey(4, SortOrder.DESCENDING)));
            status("Sorted by CGPA (highest first).");
        });
        p.add(btnSortCgpa);

        JButton btnResetSort = makeButton("Reset Sort", "#9E9E9E");
        btnResetSort.addActionListener(e -> {
            rowSorter.setSortKeys(null);
            status("Sort reset.");
        });
        p.add(btnResetSort);

        return p;
    }

    /** Row 2: Filter controls + View by ID */
    private JPanel buildFilterViewPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setBorder(BorderFactory.createTitledBorder("Filter & View"));

        // Filter by CGPA
        p.add(new JLabel("CGPA ≥"));
        tfFilterCgpa = new JTextField("8.0", 5);
        p.add(tfFilterCgpa);
        JButton btnFilterCgpa = makeButton("Filter CGPA", "#E65100");
        btnFilterCgpa.addActionListener(e -> filterByCgpa());
        p.add(btnFilterCgpa);

        // Filter by Course
        p.add(Box.createHorizontalStrut(10));
        p.add(new JLabel("Course ="));
        tfFilterCourse = new JTextField("B.Tech IT", 10);
        p.add(tfFilterCourse);
        JButton btnFilterCourse = makeButton("Filter Course", "#E65100");
        btnFilterCourse.addActionListener(e -> filterByCourse());
        p.add(btnFilterCourse);

        // Clear filters
        JButton btnClearFilter = makeButton("Clear Filters", "#9E9E9E");
        btnClearFilter.addActionListener(e -> {
            rowSorter.setRowFilter(null);
            status("Filters cleared — showing all records.");
        });
        p.add(btnClearFilter);

        // View by ID
        p.add(Box.createHorizontalStrut(20));
        p.add(new JLabel("View by ID:"));
        tfViewId = new JTextField(5);
        p.add(tfViewId);
        JButton btnViewId = makeButton("View", "#1565C0");
        btnViewId.addActionListener(e -> viewById());
        p.add(btnViewId);

        return p;
    }

    private JLabel buildStatusBar() {
        statusLabel = new JLabel("Ready. Click a row to select, then use Update or Delete.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return statusLabel;
    }

    // ─── Feature Implementations ──────────────────────────────────────────────

    /** 1. Search by name OR email (case-insensitive) */
    private void applySearch() {
        String query = tfSearch.getText().trim();
        if (query.isEmpty()) {
            rowSorter.setRowFilter(null);
            status("Search cleared.");
            return;
        }
        // Column 1 = Name, Column 2 = Email
        RowFilter<DefaultTableModel, Object> nameFilter =
                RowFilter.regexFilter("(?i)" + query, 1);
        RowFilter<DefaultTableModel, Object> emailFilter =
                RowFilter.regexFilter("(?i)" + query, 2);
        rowSorter.setRowFilter(RowFilter.orFilter(List.of(nameFilter, emailFilter)));
        status("Search results for: \"" + query + "\"");
    }

    /** 2+3. Filter by CGPA ≥ threshold */
    private void filterByCgpa() {
        try {
            double threshold = Double.parseDouble(tfFilterCgpa.getText().trim());
            rowSorter.setRowFilter(RowFilter.numberFilter(
                    RowFilter.ComparisonType.AFTER, threshold - 0.001, 4));
            status("Showing students with CGPA ≥ " + threshold);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid CGPA value (e.g. 8.0).",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** 2+3. Filter by exact course match (case-insensitive) */
    private void filterByCourse() {
        String course = tfFilterCourse.getText().trim();
        if (course.isEmpty()) {
            rowSorter.setRowFilter(null);
            status("Course filter cleared.");
            return;
        }
        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)^" + course + "$", 3));
        status("Showing students in course: " + course);
    }

    /** 4. View single student by ID — highlights in table and populates form */
    private void viewById() {
        String idText = tfViewId.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an ID.", "No ID", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int targetId;
        try {
            targetId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID must be a number.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Search through visible (filtered) rows first, then all rows
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            Object cellId = tableModel.getValueAt(modelRow, 0);
            if (cellId != null && Integer.parseInt(cellId.toString()) == targetId) {
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                populateFormFromTable();
                status("Found student with ID " + targetId + ".");
                return;
            }
        }
        // Not found in current view — check if filter is active
        if (rowSorter.getRowFilter() != null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Student ID " + targetId + " is not visible (filters may be active).\n" +
                            "Clear filters and search again?",
                    "Not Found in Current View", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                rowSorter.setRowFilter(null);
                viewById(); // recurse once after clearing
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No student found with ID " + targetId + ".",
                    "Not Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ─── CRUD Handlers ────────────────────────────────────────────────────────

    private void handleButton(String action) {
        switch (action) {
            case "Add"     -> addStudent();
            case "Update"  -> updateStudent();
            case "Delete"  -> deleteStudent();
            case "Refresh" -> loadAllStudents();
            case "Clear"   -> clearForm();
        }
    }

    private void addStudent() {
        if (!validateForm()) return;
        Student s = buildStudentFromForm(0);
        runAsync(
                () -> ClientConnection.send(new Request(Request.Action.ADD, s)),
                "Student added successfully!", "Failed to add student."
        );
    }

    private void updateStudent() {
        String idText = tfId.getText().trim();
        if (idText.isBlank() || idText.equals("0")) {
            JOptionPane.showMessageDialog(this,
                    "Please click a row in the table first, then click Update.",
                    "No Student Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!validateForm()) return;
        Student s = buildStudentFromForm(Integer.parseInt(idText));
        runAsync(
                () -> ClientConnection.send(new Request(Request.Action.UPDATE, s)),
                "Student updated successfully!", "Update failed."
        );
    }

    private void deleteStudent() {
        String idText = tfId.getText().trim();
        if (idText.isBlank() || idText.equals("0")) {
            JOptionPane.showMessageDialog(this,
                    "Please click a row in the table first, then click Delete.",
                    "No Student Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idText);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student with ID " + id + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        runAsync(
                () -> ClientConnection.send(new Request(Request.Action.DELETE, id)),
                "Student deleted successfully!", "Delete failed."
        );
    }

    private void loadAllStudents() {
        status("Loading...");
        new SwingWorker<Response, Void>() {
            @Override protected Response doInBackground() throws Exception {
                return ClientConnection.send(new Request(Request.Action.GET_ALL));
            }
            @Override protected void done() {
                try {
                    Response resp = get();
                    tableModel.setRowCount(0);
                    List<Student> list = resp.getStudents();
                    if (list != null) {
                        for (Student s : list)
                            tableModel.addRow(new Object[]{
                                    s.getId(), s.getName(), s.getEmail(),
                                    s.getCourse(), s.getCgpa()
                            });
                    }
                    status("Loaded " + (list != null ? list.size() : 0)
                            + " records. Click any row to select it.");
                } catch (Exception ex) {
                    status("Error loading: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @FunctionalInterface
    interface ThrowingSupplier { Response get() throws Exception; }

    private void runAsync(ThrowingSupplier task, String successMsg, String failMsg) {
        new SwingWorker<Response, Void>() {
            @Override protected Response doInBackground() throws Exception { return task.get(); }
            @Override protected void done() {
                try {
                    Response r = get();
                    status(r.isSuccess() ? successMsg : failMsg + " — " + r.getMessage());
                    if (r.isSuccess()) { loadAllStudents(); clearForm(); }
                } catch (Exception ex) {
                    status("Network error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= table.getRowCount()) return;
        int modelRow = table.convertRowIndexToModel(row); // ← respect sort/filter
        tfId.setText(tableModel.getValueAt(modelRow, 0).toString());
        tfName.setText(tableModel.getValueAt(modelRow, 1).toString());
        tfEmail.setText(tableModel.getValueAt(modelRow, 2).toString());
        tfCourse.setText(tableModel.getValueAt(modelRow, 3).toString());
        tfCgpa.setText(tableModel.getValueAt(modelRow, 4).toString());
        status("Selected ID " + tfId.getText() + " — edit fields then click Update or Delete.");
    }

    private Student buildStudentFromForm(int id) {
        return new Student(id,
                tfName.getText().trim(),
                tfEmail.getText().trim(),
                tfCourse.getText().trim(),
                Double.parseDouble(tfCgpa.getText().trim()));
    }

    private boolean validateForm() {
        if (tfName.getText().isBlank() || tfEmail.getText().isBlank()
                || tfCourse.getText().isBlank() || tfCgpa.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "All fields (Name, Email, Course, CGPA) are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try { Double.parseDouble(tfCgpa.getText().trim()); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "CGPA must be a valid number (e.g. 8.5).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearForm() {
        tfId.setText(""); tfName.setText(""); tfEmail.setText("");
        tfCourse.setText(""); tfCgpa.setText("");
        table.clearSelection();
        status("Form cleared.");
    }

    private void status(String msg) {
        statusLabel.setText("  ➤  " + msg);
    }

    /** Reusable styled button factory */
    private JButton makeButton(String text, String hexColor) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.decode(hexColor));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentManagementGUI::new);
    }
}
