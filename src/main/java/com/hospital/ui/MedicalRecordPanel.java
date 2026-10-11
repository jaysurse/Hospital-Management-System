package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import com.hospital.model.MedicalRecord;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import com.hospital.ui.dialogs.MedicalRecordDialog;

/**
 * Clinical Medical Records Panel.
 * Provides management of diagnoses, pharmacotherapy prescriptions, and clinical history.
 */
public class MedicalRecordPanel extends JPanel {

    private final MedicalRecordService recordService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final MainDashboardFrame parentFrame;

    private JTextField txtSearch;
    private DefaultTableModel tableModel;
    private JTable recordTable;
    private List<MedicalRecord> currentRecords;

    public MedicalRecordPanel(MainDashboardFrame parentFrame,
                              MedicalRecordService recordService, 
                              PatientService patientService, 
                              DoctorService doctorService) {
        this.parentFrame = parentFrame;
        this.recordService = recordService;
        this.patientService = patientService;
        this.doctorService = doctorService;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        loadRecords();
    }

    private void initComponents() {
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Theme.SURFACE);

        // Search (Left)
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setBackground(Theme.SURFACE);

        JLabel lblSearch = new JLabel("Search Records:");
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
            loadRecords();
        });

        searchBox.add(lblSearch);
        searchBox.add(txtSearch);
        searchBox.add(btnSearch);
        searchBox.add(btnReset);
        toolbar.add(searchBox, BorderLayout.WEST);

        // Actions (Right)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Theme.SURFACE);

        JButton btnAdd = UIHelper.createPrimaryButton("+ New Clinical Entry");
        btnAdd.addActionListener(e -> openAddDialog());

        JButton btnView = UIHelper.createSecondaryButton("Inspect Details");
        btnView.addActionListener(e -> inspectRecord());

        JButton btnEdit = UIHelper.createOutlineButton("Edit Entry");
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnDelete = UIHelper.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteRecord());

        actions.add(btnAdd);
        actions.add(btnView);
        actions.add(btnEdit);
        actions.add(btnDelete);
        toolbar.add(actions, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {
            "Rec ID", "Patient Name", "Consulting Doctor", "Specialization", "Recorded Date", "Clinical Diagnosis", "Prescription Summary"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        recordTable = UIHelper.createTable(tableModel);
        recordTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        recordTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        recordTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        recordTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        recordTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        recordTable.getColumnModel().getColumn(5).setPreferredWidth(230);
        recordTable.getColumnModel().getColumn(6).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(recordTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadRecords() {
        try {
            currentRecords = recordService.getAllRecords();
            populateTable(currentRecords);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load medical records: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        try {
            currentRecords = recordService.searchRecords(query);
            populateTable(currentRecords);
            if (currentRecords.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No clinical records found matching: \"" + query + "\"", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<MedicalRecord> list) {
        tableModel.setRowCount(0);
        for (MedicalRecord r : list) {
            String shortRx = r.getPrescription().replace("\n", "; ");
            if (shortRx.length() > 60) shortRx = shortRx.substring(0, 57) + "...";

            tableModel.addRow(new Object[]{
                "#" + r.getId(),
                r.getPatientName(),
                r.getDoctorName(),
                r.getDoctorSpecialization() != null ? r.getDoctorSpecialization() : "General",
                r.getFormattedDate(),
                r.getDiagnosis(),
                shortRx
            });
        }
    }

    private void openAddDialog() {
        MedicalRecordDialog dialog = new MedicalRecordDialog(parentFrame, recordService, patientService, doctorService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRecords();
        }
    }

    private void openEditDialog() {
        MedicalRecord selected = getSelectedRecord();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MedicalRecordDialog dialog = new MedicalRecordDialog(parentFrame, recordService, patientService, doctorService, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRecords();
        }
    }

    private void inspectRecord() {
        MedicalRecord selected = getSelectedRecord();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a record to inspect.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append(String.format(" CLINICAL EXAMINATION ENTRY #%d\n", selected.getId()));
        sb.append("======================================================================\n\n");
        sb.append(String.format("PATIENT NAME      : %s (ID: #%d)\n", selected.getPatientName(), selected.getPatientId()));
        sb.append(String.format("CONSULTING DOCTOR : %s (%s)\n", selected.getDoctorName(), selected.getDoctorSpecialization()));
        sb.append(String.format("DATE RECORDED     : %s\n\n", selected.getFormattedDate()));
        sb.append("CLINICAL DIAGNOSIS:\n");
        sb.append("----------------------------------------------------------------------\n");
        sb.append(selected.getDiagnosis()).append("\n\n");
        sb.append("PRESCRIPTION & PHARMACOTHERAPY (Rx):\n");
        sb.append("----------------------------------------------------------------------\n");
        sb.append(selected.getPrescription()).append("\n\n");
        sb.append("PHYSICIAN DIRECTIVES & TREATMENT NOTES:\n");
        sb.append("----------------------------------------------------------------------\n");
        sb.append(selected.getTreatmentNotes() != null && !selected.getTreatmentNotes().trim().isEmpty() 
            ? selected.getTreatmentNotes() : "No special post-consultation directives.").append("\n");

        JTextArea txt = new JTextArea(sb.toString());
        txt.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        txt.setEditable(false);
        txt.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(txt);
        scroll.setPreferredSize(new Dimension(620, 460));
        JOptionPane.showMessageDialog(this, scroll, "Clinical Record - #" + selected.getId(), JOptionPane.PLAIN_MESSAGE);
    }

    private void deleteRecord() {
        MedicalRecord selected = getSelectedRecord();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete medical record #" + selected.getId() + " for " + selected.getPatientName() + "?",
            "Confirm Record Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                recordService.deleteRecord(selected.getId());
                loadRecords();
                JOptionPane.showMessageDialog(this, "Clinical record deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to delete record: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private MedicalRecord getSelectedRecord() {
        int row = recordTable.getSelectedRow();
        if (row < 0 || currentRecords == null || row >= currentRecords.size()) {
            return null;
        }
        return currentRecords.get(row);
    }
}
