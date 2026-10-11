package com.hospital.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import com.hospital.config.HospitalConstants;
import com.hospital.exception.AppointmentException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.ui.Theme;
import com.hospital.ui.UIHelper;
import com.hospital.util.DateUtil;

/**
 * Modal dialog for booking and updating clinical appointments.
 * Strictly prevents double-booking and validates doctor availability.
 */
public class AppointmentDialog extends JDialog {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private Appointment appointment;
    private boolean saved = false;

    private JComboBox<PatientItem> cbPatients;
    private JComboBox<DoctorItem> cbDoctors;
    private JTextField txtDate;
    private JComboBox<String> cbTimeSlot;
    private JTextField txtReason;
    private JComboBox<String> cbStatus;
    private JTextArea txtNotes;

    private static final String[] TIME_SLOTS = {
        "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
        "12:00", "12:30", "14:00", "14:30", "15:00", "15:30",
        "16:00", "16:30", "17:00", "17:30"
    };

    public AppointmentDialog(Frame owner, AppointmentService appointmentService, 
                             PatientService patientService, DoctorService doctorService, 
                             Appointment existing) {
        super(owner, existing == null ? "Schedule Clinical Appointment" : "Reschedule / Edit Appointment", true);
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointment = existing;

        initComponents();
        loadDropdownData();
        if (existing != null) {
            populateFields(existing);
        }

        setSize(560, 620);
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
        JLabel lblTitle = new JLabel(appointment == null ? "Appointment Booking" : "Modify Consultation Schedule");
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.PRIMARY);
        JLabel lblSub = new JLabel("Enforces automatic doctor conflict prevention and scheduling checks.");
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

        cbPatients = new JComboBox<>();
        cbPatients.setFont(Theme.FONT_REGULAR);

        cbDoctors = new JComboBox<>();
        cbDoctors.setFont(Theme.FONT_REGULAR);

        txtDate = UIHelper.createTextField();
        txtDate.setText(DateUtil.formatIsoDate(LocalDate.now()));

        cbTimeSlot = UIHelper.createComboBox(TIME_SLOTS);
        txtReason = UIHelper.createTextField();
        cbStatus = UIHelper.createComboBox(HospitalConstants.APPOINTMENT_STATUSES);

        txtNotes = new JTextArea(3, 20);
        txtNotes.setFont(Theme.FONT_REGULAR);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane scrollNotes = new JScrollPane(txtNotes);

        int row = 0;
        addFormField(formPanel, gbc, row++, "Select Patient *", cbPatients);
        addFormField(formPanel, gbc, row++, "Consulting Doctor *", cbDoctors);
        addFormField(formPanel, gbc, row++, "Date (YYYY-MM-DD) *", txtDate);
        addFormField(formPanel, gbc, row++, "Time Slot *", cbTimeSlot);
        addFormField(formPanel, gbc, row++, "Reason for Consultation *", txtReason);
        addFormField(formPanel, gbc, row++, "Appointment Status", cbStatus);
        addFormField(formPanel, gbc, row++, "Clinical / Admin Notes", scrollNotes);

        contentPane.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Theme.SURFACE);

        JButton btnCancel = UIHelper.createOutlineButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UIHelper.createPrimaryButton(appointment == null ? "Confirm Booking" : "Update Appointment");
        btnSave.addActionListener(e -> saveAppointment());

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

    private void loadDropdownData() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            for (Patient p : patients) {
                cbPatients.addItem(new PatientItem(p.getId(), p.getFullName(), p.getPhone()));
            }

            List<Doctor> doctors = doctorService.getAllDoctors();
            for (Doctor d : doctors) {
                cbDoctors.addItem(new DoctorItem(d.getId(), d.getFullName(), d.getSpecialization(), d.isAvailable()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load patient or doctor records: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields(Appointment apt) {
        // Match Patient
        for (int i = 0; i < cbPatients.getItemCount(); i++) {
            if (cbPatients.getItemAt(i).id == apt.getPatientId()) {
                cbPatients.setSelectedIndex(i);
                break;
            }
        }

        // Match Doctor
        for (int i = 0; i < cbDoctors.getItemCount(); i++) {
            if (cbDoctors.getItemAt(i).id == apt.getDoctorId()) {
                cbDoctors.setSelectedIndex(i);
                break;
            }
        }

        txtDate.setText(DateUtil.formatIsoDate(apt.getAppointmentDate()));
        if (apt.getAppointmentTime() != null) {
            String timeStr = DateUtil.formatIsoTime(apt.getAppointmentTime());
            cbTimeSlot.setSelectedItem(timeStr);
        }
        txtReason.setText(apt.getReason());
        cbStatus.setSelectedItem(apt.getStatus().name());
        txtNotes.setText(apt.getNotes() != null ? apt.getNotes() : "");
    }

    private void saveAppointment() {
        PatientItem patientItem = (PatientItem) cbPatients.getSelectedItem();
        DoctorItem doctorItem = (DoctorItem) cbDoctors.getSelectedItem();

        if (patientItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an existing patient.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (doctorItem == null) {
            JOptionPane.showMessageDialog(this, "Please select a consulting doctor.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateStr = txtDate.getText().trim();
        LocalDate date = DateUtil.parseDate(dateStr);
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtDate.requestFocus();
            return;
        }

        String timeStr = (String) cbTimeSlot.getSelectedItem();
        LocalTime time = DateUtil.parseTime(timeStr);
        if (time == null) {
            time = LocalTime.of(10, 0);
        }

        String reason = txtReason.getText().trim();
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide the reason for consultation / chief complaint.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtReason.requestFocus();
            return;
        }

        AppointmentStatus status = AppointmentStatus.fromString((String) cbStatus.getSelectedItem());
        String notes = txtNotes.getText().trim();

        try {
            if (appointment == null) {
                // New appointment
                appointment = new Appointment(patientItem.id, doctorItem.id, date, time, reason);
                appointment.setStatus(status);
                appointment.setNotes(notes.isEmpty() ? null : notes);
                appointmentService.scheduleAppointment(appointment);
                JOptionPane.showMessageDialog(this, "Appointment scheduled successfully! Reference ID: #" + appointment.getId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Updating appointment
                appointment.setPatientId(patientItem.id);
                appointment.setDoctorId(doctorItem.id);
                appointment.setAppointmentDate(date);
                appointment.setAppointmentTime(time);
                appointment.setReason(reason);
                appointment.setStatus(status);
                appointment.setNotes(notes.isEmpty() ? null : notes);
                appointmentService.updateAppointment(appointment);
                JOptionPane.showMessageDialog(this, "Appointment schedule updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();
        } catch (AppointmentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Scheduling Conflict Warning", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "System Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    // Helper item classes for dropdowns
    private static class PatientItem {
        final int id;
        final String name;
        final String phone;

        PatientItem(int id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }

        @Override
        public String toString() {
            return String.format("#%d - %s (%s)", id, name, phone);
        }
    }

    private static class DoctorItem {
        final int id;
        final String name;
        final String spec;
        final boolean available;

        DoctorItem(int id, String name, String spec, boolean available) {
            this.id = id;
            this.name = name;
            this.spec = spec;
            this.available = available;
        }

        @Override
        public String toString() {
            return String.format("%s - %s %s", name, spec, available ? "" : "[UNAVAILABLE]");
        }
    }
}
