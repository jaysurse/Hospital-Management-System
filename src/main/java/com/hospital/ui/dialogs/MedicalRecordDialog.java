package com.hospital.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import com.hospital.model.Doctor;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import com.hospital.ui.Theme;
import com.hospital.ui.UIHelper;

/**
 * Modal dialog for recording clinical diagnoses, prescriptions, and physician observations.
 */
public class MedicalRecordDialog extends JDialog {

    private final MedicalRecordService recordService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private MedicalRecord record;
    private boolean saved = false;

    private JComboBox<PatientOption> cbPatient;
    private JComboBox<DoctorOption> cbDoctor;
    private JTextArea txtDiagnosis;
    private JTextArea txtPrescription;
    private JTextArea txtTreatmentNotes;

    public MedicalRecordDialog(Frame owner, MedicalRecordService recordService, 
                               PatientService patientService, DoctorService doctorService, 
                               MedicalRecord existing) {
        super(owner, existing == null ? "New Clinical Examination & Diagnosis" : "Update Medical Record", true);
        this.recordService = recordService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.record = existing;

        initComponents();
        loadDropdownData();
        if (existing != null) {
            populateFields(existing);
        }

        setSize(600, 640);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 16));
        contentPane.setBackground(Theme.SURFACE);
        contentPane.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.SURFACE);
        JLabel lblTitle = new JLabel(record == null ? "Clinical Examination Record" : "Modify Diagnostic Entry");
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.PRIMARY);
        JLabel lblSub = new JLabel("Enter detailed differential diagnosis, prescriptions, and follow-up directives.");
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.TEXT_MUTED);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Theme.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.BOTH;

        cbPatient = new JComboBox<>();
        cbPatient.setFont(Theme.FONT_REGULAR);

        cbDoctor = new JComboBox<>();
        cbDoctor.setFont(Theme.FONT_REGULAR);

        txtDiagnosis = new JTextArea(3, 20);
        txtDiagnosis.setFont(Theme.FONT_REGULAR);
        txtDiagnosis.setLineWrap(true);
        txtDiagnosis.setWrapStyleWord(true);
        JScrollPane scrollDiagnosis = new JScrollPane(txtDiagnosis);

        txtPrescription = new JTextArea(4, 20);
        txtPrescription.setFont(Theme.FONT_REGULAR);
        txtPrescription.setLineWrap(true);
        txtPrescription.setWrapStyleWord(true);
        JScrollPane scrollRx = new JScrollPane(txtPrescription);

        txtTreatmentNotes = new JTextArea(3, 20);
        txtTreatmentNotes.setFont(Theme.FONT_REGULAR);
        txtTreatmentNotes.setLineWrap(true);
        txtTreatmentNotes.setWrapStyleWord(true);
        JScrollPane scrollNotes = new JScrollPane(txtTreatmentNotes);

        int row = 0;
        addFormField(formPanel, gbc, row++, "Select Patient *", cbPatient, 0.1);
        addFormField(formPanel, gbc, row++, "Consulting Physician *", cbDoctor, 0.1);
        addFormField(formPanel, gbc, row++, "Clinical Diagnosis *", scrollDiagnosis, 0.25);
        addFormField(formPanel, gbc, row++, "Rx / Prescriptions *", scrollRx, 0.35);
        addFormField(formPanel, gbc, row++, "Treatment Directives & Notes", scrollNotes, 0.2);

        contentPane.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Theme.SURFACE);

        JButton btnCancel = UIHelper.createOutlineButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UIHelper.createPrimaryButton(record == null ? "Save Clinical Record" : "Update Record");
        btnSave.addActionListener(e -> saveRecord());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, javax.swing.JComponent comp, double weightY) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.30;
        gbc.weighty = weightY;
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SUBHEADER);
        label.setForeground(Theme.TEXT_MAIN);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.70;
        panel.add(comp, gbc);
    }

    private void loadDropdownData() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            for (Patient p : patients) {
                cbPatient.addItem(new PatientOption(p.getId(), p.getFullName(), p.getAge(), p.getGender()));
            }

            List<Doctor> doctors = doctorService.getAllDoctors();
            for (Doctor d : doctors) {
                cbDoctor.addItem(new DoctorOption(d.getId(), d.getFullName(), d.getSpecialization()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load options: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields(MedicalRecord rec) {
        for (int i = 0; i < cbPatient.getItemCount(); i++) {
            if (cbPatient.getItemAt(i).id == rec.getPatientId()) {
                cbPatient.setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < cbDoctor.getItemCount(); i++) {
            if (cbDoctor.getItemAt(i).id == rec.getDoctorId()) {
                cbDoctor.setSelectedIndex(i);
                break;
            }
        }
        txtDiagnosis.setText(rec.getDiagnosis());
        txtPrescription.setText(rec.getPrescription());
        txtTreatmentNotes.setText(rec.getTreatmentNotes() != null ? rec.getTreatmentNotes() : "");
    }

    private void saveRecord() {
        PatientOption patientOpt = (PatientOption) cbPatient.getSelectedItem();
        DoctorOption doctorOpt = (DoctorOption) cbDoctor.getSelectedItem();

        if (patientOpt == null) {
            JOptionPane.showMessageDialog(this, "Please select an assigned patient.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (doctorOpt == null) {
            JOptionPane.showMessageDialog(this, "Please select the consulting physician.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String diagnosis = txtDiagnosis.getText().trim();
        String prescription = txtPrescription.getText().trim();
        String notes = txtTreatmentNotes.getText().trim();

        if (diagnosis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Clinical diagnosis statement cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtDiagnosis.requestFocus();
            return;
        }
        if (prescription.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Prescription details cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtPrescription.requestFocus();
            return;
        }

        try {
            if (record == null) {
                record = new MedicalRecord(null, patientOpt.id, doctorOpt.id, diagnosis, prescription, notes);
                recordService.addRecord(record);
                JOptionPane.showMessageDialog(this, "Clinical medical record recorded successfully! Record ID: #" + record.getId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                record.setPatientId(patientOpt.id);
                record.setDoctorId(doctorOpt.id);
                record.setDiagnosis(diagnosis);
                record.setPrescription(prescription);
                record.setTreatmentNotes(notes);
                recordService.updateRecord(record);
                JOptionPane.showMessageDialog(this, "Clinical record updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving record: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private static class PatientOption {
        final int id;
        final String name;
        final int age;
        final String gender;

        PatientOption(int id, String name, int age, String gender) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.gender = gender;
        }

        @Override
        public String toString() {
            return String.format("#%d - %s (%d yrs, %s)", id, name, age, gender);
        }
    }

    private static class DoctorOption {
        final int id;
        final String name;
        final String spec;

        DoctorOption(int id, String name, String spec) {
            this.id = id;
            this.name = name;
            this.spec = spec;
        }

        @Override
        public String toString() {
            return String.format("%s (%s)", name, spec);
        }
    }
}
