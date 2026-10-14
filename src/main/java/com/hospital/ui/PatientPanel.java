package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import com.hospital.ui.dialogs.PatientDialog;
import com.hospital.util.DateUtil;
import com.hospital.util.FileUtil;

/**
 * Patient Management Panel.
 * Provides complete CRUD operations, overloaded searches, and medical history views.
 */
public class PatientPanel extends JPanel {

    private final PatientService patientService;
    private final MedicalRecordService medicalRecordService;
    private final MainDashboardFrame parentFrame;

    private JTextField txtSearch;
    private DefaultTableModel tableModel;
    private JTable patientTable;
    private List<Patient> currentPatients;

    public PatientPanel(MainDashboardFrame parentFrame, PatientService patientService, MedicalRecordService medicalRecordService) {
        this.parentFrame = parentFrame;
        this.patientService = patientService;
        this.medicalRecordService = medicalRecordService;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        loadPatients();
    }

    private void initComponents() {
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Top Toolbar: Search & Action Buttons
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Theme.SURFACE);

        // Search Bar (Left)
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setBackground(Theme.SURFACE);

        JLabel lblSearch = new JLabel("Search Patient:");
        lblSearch.setFont(Theme.FONT_SUBHEADER);
        lblSearch.setForeground(Theme.TEXT_MAIN);

        txtSearch = UIHelper.createTextField();
        txtSearch.setPreferredSize(new Dimension(220, 32));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = UIHelper.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = UIHelper.createOutlineButton("Clear");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadPatients();
        });

        searchBox.add(lblSearch);
        searchBox.add(txtSearch);
        searchBox.add(btnSearch);
        searchBox.add(btnReset);
        toolbar.add(searchBox, BorderLayout.WEST);

        // CRUD Action Buttons (Right)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Theme.SURFACE);

        JButton btnRegister = UIHelper.createPrimaryButton("+ Register Patient");
        btnRegister.addActionListener(e -> openRegisterDialog());

        JButton btnEdit = UIHelper.createOutlineButton("Edit Selected");
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnHistory = UIHelper.createSecondaryButton("Medical History");
        btnHistory.addActionListener(e -> viewMedicalHistory());

        JButton btnExportHistory = null;
        if (parentFrame.isAdministrator()) {
            btnExportHistory = UIHelper.createOutlineButton("Export History TXT");
            btnExportHistory.addActionListener(e -> exportMedicalHistory());
        }

        JButton btnDelete = UIHelper.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteSelectedPatient());

        actions.add(btnRegister);
        actions.add(btnEdit);
        actions.add(btnHistory);
        if (btnExportHistory != null) actions.add(btnExportHistory);
        actions.add(btnDelete);
        toolbar.add(actions, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {
            "ID", "Full Name", "Age", "Gender", "Contact Phone", "Blood Group", "Residential Address", "Emergency Contact", "Registration Date"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        patientTable = UIHelper.createTable(tableModel);
        patientTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        patientTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        patientTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        patientTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        patientTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        patientTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        patientTable.getColumnModel().getColumn(6).setPreferredWidth(220);
        patientTable.getColumnModel().getColumn(7).setPreferredWidth(130);
        patientTable.getColumnModel().getColumn(8).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadPatients() {
        try {
            currentPatients = patientService.getAllPatients();
            populateTable(currentPatients);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load patient records: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        try {
            currentPatients = patientService.searchPatients(query);
            populateTable(currentPatients);
            if (currentPatients.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No patient records matched: \"" + query + "\"", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Patient> patients) {
        tableModel.setRowCount(0);
        for (Patient p : patients) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getFullName(),
                p.getAge(),
                p.getGender(),
                p.getPhone(),
                p.getBloodGroup(),
                p.getAddress(),
                p.getEmergencyContact() != null ? p.getEmergencyContact() : "-",
                DateUtil.formatDate(p.getRegistrationDate())
            });
        }
    }

    private void openRegisterDialog() {
        PatientDialog dialog = new PatientDialog(parentFrame, patientService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadPatients();
            parentFrame.refreshDashboardMetrics();
        }
    }

    private void openEditDialog() {
        Patient selected = getSelectedPatient();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PatientDialog dialog = new PatientDialog(parentFrame, patientService, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadPatients();
        }
    }

    private void deleteSelectedPatient() {
        Patient selected = getSelectedPatient();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete patient:\n" +
            selected.getFullName() + " (ID: #" + selected.getId() + ")?\n\n" +
            "WARNING: Associated appointments and medical records will be removed.",
            "Confirm Patient Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = patientService.deletePatient(selected.getId());
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Patient record removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPatients();
                    parentFrame.refreshDashboardMetrics();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Unable to delete patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewMedicalHistory() {
        Patient selected = getSelectedPatient();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient to inspect medical history.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<MedicalRecord> records = medicalRecordService.getRecordsByPatientId(selected.getId());
            StringBuilder sb = new StringBuilder();
            sb.append(selected.getDetails()).append("\n\n");
            sb.append("======================================================================\n");
            sb.append("                    CLINICAL MEDICAL HISTORY LOG                      \n");
            sb.append("======================================================================\n\n");

            if (records.isEmpty()) {
                sb.append("No previous clinical examination or diagnostic records found for this patient.\n");
            } else {
                for (int i = 0; i < records.size(); i++) {
                    MedicalRecord r = records.get(i);
                    sb.append(String.format("CONSULTATION #%d [%s]\n", (i + 1), r.getFormattedDate()));
                    sb.append(String.format("Consulting Physician: %s (%s)\n", r.getDoctorName(), r.getDoctorSpecialization() != null ? r.getDoctorSpecialization() : "General"));
                    sb.append("Clinical Diagnosis  :\n  ").append(r.getDiagnosis().replace("\n", "\n  ")).append("\n");
                    sb.append("Prescriptions (Rx)  :\n  ").append(r.getPrescription().replace("\n", "\n  ")).append("\n");
                    if (r.getTreatmentNotes() != null && !r.getTreatmentNotes().trim().isEmpty()) {
                        sb.append("Physician Notes     :\n  ").append(r.getTreatmentNotes().replace("\n", "\n  ")).append("\n");
                    }
                    sb.append("----------------------------------------------------------------------\n\n");
                }
            }

            javax.swing.JTextArea txtArea = new javax.swing.JTextArea(sb.toString());
            txtArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
            txtArea.setEditable(false);
            txtArea.setCaretPosition(0);

            JScrollPane scroll = new JScrollPane(txtArea);
            scroll.setPreferredSize(new Dimension(640, 480));
            if (parentFrame.isAdministrator()) {
                Object[] options = {"Export This Patient's History", "Close"};
                int choice = JOptionPane.showOptionDialog(this, scroll,
                    "Patient Clinical History - " + selected.getFullName(),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[1]);
                if (choice == 0) exportMedicalHistory(selected);
            } else {
                JOptionPane.showMessageDialog(this, scroll, "Patient Clinical History - " + selected.getFullName(), JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to retrieve medical history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportMedicalHistory() {
        if (!parentFrame.isAdministrator()) {
            JOptionPane.showMessageDialog(this, "Only administrators can export medical history.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Patient selected = getSelectedPatient();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient to export medical history.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            exportMedicalHistory(selected);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to export medical history: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportMedicalHistory(Patient selected) {
        try {
            List<MedicalRecord> records = medicalRecordService.getRecordsByPatientId(selected.getId());
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export Medical History for " + selected.getFullName());
            chooser.setSelectedFile(new File("medical_history_patient_" + selected.getId() + ".txt"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".txt")) {
                target = new File(target.getParentFile(), target.getName() + ".txt");
            }

            StringBuilder history = new StringBuilder();
            history.append(selected.getDetails()).append("\n\n");
            if (records.isEmpty()) {
                history.append("No previous clinical examination or diagnostic records found for this patient.\n");
            } else {
                for (int i = 0; i < records.size(); i++) {
                    MedicalRecord record = records.get(i);
                    history.append(String.format("CONSULTATION #%d [%s]\n", i + 1, record.getFormattedDate()));
                    history.append(String.format("Consulting Physician: %s (%s)\n",
                        record.getDoctorName(), record.getDoctorSpecialization() != null ? record.getDoctorSpecialization() : "General"));
                    history.append("Clinical Diagnosis:\n").append(record.getDiagnosis()).append("\n\n");
                    history.append("Prescriptions (Rx):\n").append(record.getPrescription()).append("\n\n");
                    String notes = record.getTreatmentNotes();
                    history.append("Physician Notes:\n")
                        .append(notes != null && !notes.trim().isEmpty() ? notes : "None").append("\n");
                    history.append("----------------------------------------------------------------------\n\n");
                }
            }

            FileUtil.exportToTextFile(target, "Medical History - " + selected.getFullName(), List.of(history.toString()));
            JOptionPane.showMessageDialog(this, "Medical history for " + selected.getFullName() + " exported to:\n" + target.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to export medical history: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Patient getSelectedPatient() {
        int row = patientTable.getSelectedRow();
        if (row < 0 || currentPatients == null || row >= currentPatients.size()) {
            return null;
        }
        return currentPatients.get(row);
    }
}
