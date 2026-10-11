package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import com.hospital.config.HospitalConstants;
import com.hospital.model.Doctor;
import com.hospital.service.DoctorService;
import com.hospital.ui.dialogs.DoctorDialog;
import com.hospital.util.DateUtil;

/**
 * Doctor Management Panel.
 * Manages medical specialists, consultation fees, and clinical availability.
 */
public class DoctorPanel extends JPanel {

    private final DoctorService doctorService;
    private final MainDashboardFrame parentFrame;

    private JTextField txtSearch;
    private JComboBox<String> cbDepartment;
    private DefaultTableModel tableModel;
    private JTable doctorTable;
    private List<Doctor> currentDoctors;

    public DoctorPanel(MainDashboardFrame parentFrame, DoctorService doctorService) {
        this.parentFrame = parentFrame;
        this.doctorService = doctorService;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        loadDoctors();
    }

    private void initComponents() {
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Top Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Theme.SURFACE);

        // Filter / Search (Left)
        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setBackground(Theme.SURFACE);

        JLabel lblSearch = new JLabel("Filter Specialization:");
        lblSearch.setFont(Theme.FONT_SUBHEADER);
        lblSearch.setForeground(Theme.TEXT_MAIN);

        List<String> depts = new ArrayList<>();
        depts.add("All Departments");
        for (String s : HospitalConstants.SPECIALIZATIONS) depts.add(s);
        cbDepartment = UIHelper.createComboBox(depts.toArray(new String[0]));
        cbDepartment.addActionListener(e -> performFilter());

        txtSearch = UIHelper.createTextField();
        txtSearch.setPreferredSize(new Dimension(180, 32));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = UIHelper.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = UIHelper.createOutlineButton("Clear");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbDepartment.setSelectedIndex(0);
            loadDoctors();
        });

        filterBox.add(lblSearch);
        filterBox.add(cbDepartment);
        filterBox.add(txtSearch);
        filterBox.add(btnSearch);
        filterBox.add(btnReset);
        toolbar.add(filterBox, BorderLayout.WEST);

        // Actions (Right)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Theme.SURFACE);

        JButton btnAdd = UIHelper.createPrimaryButton("+ Add Doctor");
        btnAdd.addActionListener(e -> openAddDialog());

        JButton btnEdit = UIHelper.createOutlineButton("Edit Credentials");
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnToggle = UIHelper.createSecondaryButton("Toggle Availability");
        btnToggle.addActionListener(e -> toggleDoctorAvailability());

        JButton btnDelete = UIHelper.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteSelectedDoctor());

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnToggle);
        actions.add(btnDelete);
        toolbar.add(actions, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {
            "ID", "Doctor Name", "Specialization", "Qualifications", "Phone", "Email", "Fee (₹)", "Status", "OPD Room", "Joining Date"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        doctorTable = UIHelper.createTable(tableModel);
        doctorTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        doctorTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        doctorTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        doctorTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        doctorTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        doctorTable.getColumnModel().getColumn(5).setPreferredWidth(160);
        doctorTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        doctorTable.getColumnModel().getColumn(7).setPreferredWidth(110);
        doctorTable.getColumnModel().getColumn(8).setPreferredWidth(90);
        doctorTable.getColumnModel().getColumn(9).setPreferredWidth(100);

        doctorTable.getColumnModel().getColumn(7).setCellRenderer(UIHelper.createStatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadDoctors() {
        try {
            currentDoctors = doctorService.getAllDoctors();
            populateTable(currentDoctors);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load doctors: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performFilter() {
        String spec = (String) cbDepartment.getSelectedItem();
        try {
            currentDoctors = doctorService.filterBySpecialization(spec);
            populateTable(currentDoctors);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Filter error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        try {
            currentDoctors = doctorService.searchDoctors(query);
            populateTable(currentDoctors);
            if (currentDoctors.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No doctor records found matching: \"" + query + "\"", "Search", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Doctor> doctors) {
        tableModel.setRowCount(0);
        for (Doctor d : doctors) {
            tableModel.addRow(new Object[]{
                d.getId(),
                d.getFullName(),
                d.getSpecialization(),
                d.getQualification(),
                d.getPhone(),
                d.getEmail() != null ? d.getEmail() : "-",
                String.format("%.2f", d.getConsultationFee()),
                d.isAvailable() ? "Completed" : "Cancelled", // reuses green / red status renderer
                d.getRoomNumber(),
                DateUtil.formatDate(d.getJoiningDate())
            });
        }
    }

    private void openAddDialog() {
        DoctorDialog dialog = new DoctorDialog(parentFrame, doctorService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadDoctors();
            parentFrame.refreshDashboardMetrics();
        }
    }

    private void openEditDialog() {
        Doctor selected = getSelectedDoctor();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DoctorDialog dialog = new DoctorDialog(parentFrame, doctorService, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadDoctors();
        }
    }

    private void toggleDoctorAvailability() {
        Doctor selected = getSelectedDoctor();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to toggle status.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            selected.setAvailable(!selected.isAvailable());
            doctorService.updateDoctor(selected);
            loadDoctors();
            JOptionPane.showMessageDialog(this, "Availability status toggled for " + selected.getFullName() + 
                "\nNew Status: " + (selected.isAvailable() ? "AVAILABLE" : "ON LEAVE / UNAVAILABLE"), 
                "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to update availability: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedDoctor() {
        Doctor selected = getSelectedDoctor();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to remove physician:\n" +
            selected.getFullName() + " (" + selected.getSpecialization() + ")?\n\n" +
            "NOTE: Active scheduled appointments will be deleted.",
            "Confirm Doctor Removal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = doctorService.deleteDoctor(selected.getId());
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Doctor record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadDoctors();
                    parentFrame.refreshDashboardMetrics();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Unable to delete doctor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Doctor getSelectedDoctor() {
        int row = doctorTable.getSelectedRow();
        if (row < 0 || currentDoctors == null || row >= currentDoctors.size()) {
            return null;
        }
        return currentDoctors.get(row);
    }
}
