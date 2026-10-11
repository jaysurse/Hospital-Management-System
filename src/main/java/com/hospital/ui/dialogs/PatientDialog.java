package com.hospital.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import com.hospital.config.HospitalConstants;
import com.hospital.exception.InvalidPatientException;
import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import com.hospital.ui.Theme;
import com.hospital.ui.UIHelper;
import com.hospital.util.DateUtil;

/**
 * Modal dialog for registering and editing patient records.
 */
public class PatientDialog extends JDialog {

    private final PatientService patientService;
    private Patient patient;
    private boolean saved = false;

    private JTextField txtName;
    private JTextField txtAge;
    private JComboBox<String> cbGender;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JComboBox<String> cbBloodGroup;
    private JTextField txtEmergencyContact;
    private JTextField txtRegDate;

    public PatientDialog(Frame owner, PatientService patientService, Patient existingPatient) {
        super(owner, existingPatient == null ? "Register New Patient" : "Edit Patient Profile", true);
        this.patientService = patientService;
        this.patient = existingPatient;

        initComponents();
        if (existingPatient != null) {
            populateFields(existingPatient);
        }

        setSize(520, 560);
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
        JLabel lblTitle = new JLabel(patient == null ? "Patient Intake & Registration" : "Update Patient Details");
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.PRIMARY);
        JLabel lblSub = new JLabel("Please enter accurate demographics and emergency contact information.");
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.TEXT_MUTED);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Form Fields (GridBagLayout)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Theme.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = UIHelper.createTextField();
        txtAge = UIHelper.createTextField();
        cbGender = UIHelper.createComboBox(HospitalConstants.GENDERS);
        txtPhone = UIHelper.createTextField();
        txtEmail = UIHelper.createTextField();
        txtAddress = UIHelper.createTextField();
        cbBloodGroup = UIHelper.createComboBox(HospitalConstants.BLOOD_GROUPS);
        txtEmergencyContact = UIHelper.createTextField();
        txtRegDate = UIHelper.createTextField();
        txtRegDate.setText(DateUtil.formatIsoDate(LocalDate.now()));

        int row = 0;
        addFormField(formPanel, gbc, row++, "Full Name *", txtName);
        addFormField(formPanel, gbc, row++, "Age (Years) *", txtAge);
        addFormField(formPanel, gbc, row++, "Gender *", cbGender);
        addFormField(formPanel, gbc, row++, "Contact Phone *", txtPhone);
        addFormField(formPanel, gbc, row++, "Email Address", txtEmail);
        addFormField(formPanel, gbc, row++, "Residential Address *", txtAddress);
        addFormField(formPanel, gbc, row++, "Blood Group *", cbBloodGroup);
        addFormField(formPanel, gbc, row++, "Emergency Contact", txtEmergencyContact);
        addFormField(formPanel, gbc, row++, "Registration Date (YYYY-MM-DD)", txtRegDate);

        contentPane.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Theme.SURFACE);

        JButton btnCancel = UIHelper.createOutlineButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UIHelper.createPrimaryButton(patient == null ? "Save Patient" : "Update Profile");
        btnSave.addActionListener(e -> savePatient());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, javax.swing.JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SUBHEADER);
        label.setForeground(Theme.TEXT_MAIN);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private void populateFields(Patient p) {
        txtName.setText(p.getFullName());
        txtAge.setText(String.valueOf(p.getAge()));
        cbGender.setSelectedItem(p.getGender());
        txtPhone.setText(p.getPhone());
        txtEmail.setText(p.getEmail() != null ? p.getEmail() : "");
        txtAddress.setText(p.getAddress());
        cbBloodGroup.setSelectedItem(p.getBloodGroup());
        txtEmergencyContact.setText(p.getEmergencyContact() != null ? p.getEmergencyContact() : "");
        txtRegDate.setText(DateUtil.formatIsoDate(p.getRegistrationDate()));
    }

    private void savePatient() {
        String name = txtName.getText().trim();
        String ageStr = txtAge.getText().trim();
        String gender = (String) cbGender.getSelectedItem();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        String bloodGroup = (String) cbBloodGroup.getSelectedItem();
        String emergency = txtEmergencyContact.getText().trim();
        String regDateStr = txtRegDate.getText().trim();

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric age.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtAge.requestFocus();
            return;
        }

        LocalDate regDate = DateUtil.parseDate(regDateStr);
        if (regDate == null) {
            regDate = LocalDate.now();
        }

        try {
            if (patient == null) {
                // Creating new patient
                patient = new Patient(name, age, gender, phone, address, bloodGroup);
                patient.setEmail(email.isEmpty() ? null : email);
                patient.setEmergencyContact(emergency.isEmpty() ? null : emergency);
                patient.setRegistrationDate(regDate);
                patientService.registerPatient(patient);
                JOptionPane.showMessageDialog(this, "Patient registered successfully! Patient ID: " + patient.getId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Updating existing patient
                patient.setFullName(name);
                patient.setAge(age);
                patient.setGender(gender);
                patient.setPhone(phone);
                patient.setEmail(email.isEmpty() ? null : email);
                patient.setAddress(address);
                patient.setBloodGroup(bloodGroup);
                patient.setEmergencyContact(emergency.isEmpty() ? null : emergency);
                patient.setRegistrationDate(regDate);
                patientService.updatePatient(patient);
                JOptionPane.showMessageDialog(this, "Patient profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();
        } catch (InvalidPatientException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Patient getPatient() {
        return patient;
    }
}
