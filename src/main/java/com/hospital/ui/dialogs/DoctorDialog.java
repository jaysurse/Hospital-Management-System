package com.hospital.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import com.hospital.config.HospitalConstants;
import com.hospital.model.Doctor;
import com.hospital.service.DoctorService;
import com.hospital.ui.Theme;
import com.hospital.ui.UIHelper;
import com.hospital.util.DateUtil;

/**
 * Modal dialog for onboarding and managing doctor credentials.
 */
public class DoctorDialog extends JDialog {

    private final DoctorService doctorService;
    private Doctor doctor;
    private boolean saved = false;

    private JTextField txtName;
    private JComboBox<String> cbSpecialization;
    private JTextField txtQualification;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtFee;
    private JTextField txtRoom;
    private JCheckBox chkAvailable;
    private JTextField txtJoinDate;

    public DoctorDialog(Frame owner, DoctorService doctorService, Doctor existingDoctor) {
        super(owner, existingDoctor == null ? "Add Consulting Doctor" : "Update Doctor Credentials", true);
        this.doctorService = doctorService;
        this.doctor = existingDoctor;

        initComponents();
        if (existingDoctor != null) {
            populateFields(existingDoctor);
        }

        setSize(520, 530);
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
        JLabel lblTitle = new JLabel(doctor == null ? "Physician Onboarding" : "Modify Doctor Information");
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.PRIMARY);
        JLabel lblSub = new JLabel("Configure medical qualifications, OPD room number, and consultation fee.");
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
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = UIHelper.createTextField();
        cbSpecialization = UIHelper.createComboBox(HospitalConstants.SPECIALIZATIONS);
        txtQualification = UIHelper.createTextField();
        txtQualification.setText("MBBS, MD");
        txtPhone = UIHelper.createTextField();
        txtEmail = UIHelper.createTextField();
        txtFee = UIHelper.createTextField();
        txtFee.setText("600.00");
        txtRoom = UIHelper.createTextField();
        txtRoom.setText("OPD-101");
        chkAvailable = new JCheckBox("Available for Consultations", true);
        chkAvailable.setFont(Theme.FONT_SUBHEADER);
        chkAvailable.setBackground(Theme.SURFACE);
        txtJoinDate = UIHelper.createTextField();
        txtJoinDate.setText(DateUtil.formatIsoDate(LocalDate.now()));

        int row = 0;
        addFormField(formPanel, gbc, row++, "Doctor Full Name *", txtName);
        addFormField(formPanel, gbc, row++, "Specialization / Dept *", cbSpecialization);
        addFormField(formPanel, gbc, row++, "Medical Qualifications *", txtQualification);
        addFormField(formPanel, gbc, row++, "Contact Phone *", txtPhone);
        addFormField(formPanel, gbc, row++, "Email Address", txtEmail);
        addFormField(formPanel, gbc, row++, "Consultation Fee (₹) *", txtFee);
        addFormField(formPanel, gbc, row++, "OPD Room Number *", txtRoom);
        addFormField(formPanel, gbc, row++, "Status", chkAvailable);
        addFormField(formPanel, gbc, row++, "Date of Joining (YYYY-MM-DD)", txtJoinDate);

        contentPane.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Theme.SURFACE);

        javax.swing.JButton btnCancel = UIHelper.createOutlineButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        javax.swing.JButton btnSave = UIHelper.createPrimaryButton(doctor == null ? "Add Doctor" : "Save Changes");
        btnSave.addActionListener(e -> saveDoctor());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, javax.swing.JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.38;
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SUBHEADER);
        label.setForeground(Theme.TEXT_MAIN);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.62;
        panel.add(comp, gbc);
    }

    private void populateFields(Doctor d) {
        txtName.setText(d.getFullName());
        cbSpecialization.setSelectedItem(d.getSpecialization());
        txtQualification.setText(d.getQualification());
        txtPhone.setText(d.getPhone());
        txtEmail.setText(d.getEmail() != null ? d.getEmail() : "");
        txtFee.setText(String.format("%.2f", d.getConsultationFee()));
        txtRoom.setText(d.getRoomNumber());
        chkAvailable.setSelected(d.isAvailable());
        txtJoinDate.setText(DateUtil.formatIsoDate(d.getJoiningDate()));
    }

    private void saveDoctor() {
        String name = txtName.getText().trim();
        String spec = (String) cbSpecialization.getSelectedItem();
        String qual = txtQualification.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String feeStr = txtFee.getText().trim();
        String room = txtRoom.getText().trim();
        boolean avail = chkAvailable.isSelected();
        String joinDateStr = txtJoinDate.getText().trim();

        double fee;
        try {
            fee = Double.parseDouble(feeStr);
            if (fee < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive consultation fee.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtFee.requestFocus();
            return;
        }

        LocalDate joinDate = DateUtil.parseDate(joinDateStr);
        if (joinDate == null) joinDate = LocalDate.now();

        try {
            if (doctor == null) {
                doctor = new Doctor(name, spec, phone, fee, room);
                doctor.setQualification(qual);
                doctor.setEmail(email.isEmpty() ? null : email);
                doctor.setAvailable(avail);
                doctor.setJoiningDate(joinDate);
                doctorService.addDoctor(doctor);
                JOptionPane.showMessageDialog(this, "Doctor record added successfully! Doctor ID: " + doctor.getId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                doctor.setFullName(name);
                doctor.setSpecialization(spec);
                doctor.setQualification(qual);
                doctor.setPhone(phone);
                doctor.setEmail(email.isEmpty() ? null : email);
                doctor.setConsultationFee(fee);
                doctor.setRoomNumber(room);
                doctor.setAvailable(avail);
                doctor.setJoiningDate(joinDate);
                doctorService.updateDoctor(doctor);
                JOptionPane.showMessageDialog(this, "Doctor credentials updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Doctor getDoctor() {
        return doctor;
    }
}
