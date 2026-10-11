package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import com.hospital.config.HospitalConstants;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillDAO;
import com.hospital.dao.DBConnection;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Bill;
import com.hospital.model.Doctor;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.model.PaymentStatus;
import com.hospital.service.AppointmentService;
import com.hospital.service.AuthService;
import com.hospital.service.BillingService;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import com.hospital.ui.dialogs.ReceiptDialog;
import com.hospital.util.DateUtil;

/**
 * Patient Dashboard Window.
 * 
 * Professional, Green-themed (#10B981) portal allowing patients to:
 * 1. View personal demographics & emergency profile
 * 2. Schedule consultations with conflict detection
 * 3. Review personal appointment history & cancel appointments
 * 4. Read clinical diagnoses, prescriptions, and physician instructions
 * 5. Review bills, pay online, and print invoices/receipts
 */
public class PatientDashboardFrame extends JFrame {

    private final AuthService authService;
    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;
    private final AppointmentDAO appointmentDAO;
    private final MedicalRecordDAO medicalRecordDAO;
    private final BillDAO billDAO;

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final BillingService billingService;

    private Patient currentPatient;

    // Header UI elements
    private JLabel lblPatientName;
    private JLabel lblPatientDetails;
    private JLabel lblLiveClock;

    // Overview Tab
    private JLabel lblStatAppts;
    private JLabel lblStatRecords;
    private JLabel lblStatBills;
    private JLabel lblStatPending;

    // Booking Tab Form Controls
    private JComboBox<String> cbDepartment;
    private JComboBox<DoctorItem> cbDoctor;
    private JTextField txtApptDate;
    private JComboBox<String> cbTimeSlot;
    private JTextField txtReason;
    private JLabel lblDoctorFeeInfo;

    // Appointments Tab
    private JTable tblAppointments;
    private DefaultTableModel apptTableModel;
    private List<Appointment> loadedAppointments = new ArrayList<>();

    // Medical Records Tab
    private JTable tblRecords;
    private DefaultTableModel recordTableModel;
    private List<MedicalRecord> loadedRecords = new ArrayList<>();
    private JTextArea txtRecordDetail;

    // Billing Tab
    private JTable tblBills;
    private DefaultTableModel billTableModel;
    private List<Bill> loadedBills = new ArrayList<>();

    public PatientDashboardFrame() {
        this.authService = AuthService.getInstance();
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.medicalRecordDAO = new MedicalRecordDAO();
        this.billDAO = new BillDAO();

        this.patientService = new PatientService(patientDAO);
        this.doctorService = new DoctorService(doctorDAO);
        this.appointmentService = new AppointmentService(appointmentDAO, doctorDAO, patientDAO, null);
        this.medicalRecordService = new MedicalRecordService(medicalRecordDAO, patientDAO, doctorDAO);
        this.billingService = new BillingService(billDAO, patientDAO, null);

        loadPatientSession();
        initComponents();
        refreshAllData();
    }

    private void loadPatientSession() {
        this.currentPatient = authService.getCurrentPatient();
        if (this.currentPatient == null) {
            try {
                List<Patient> patients = patientDAO.findAll();
                if (!patients.isEmpty()) {
                    this.currentPatient = patients.get(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initComponents() {
        String patientName = (currentPatient != null) ? currentPatient.getFullName() : "Patient";
        setTitle(HospitalConstants.HOSPITAL_NAME + " - Patient Portal (" + patientName + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);

        JPanel rootPane = new JPanel(new BorderLayout());
        rootPane.setBackground(Theme.BG);

        // 1. Top Green Header
        rootPane.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_SUBHEADER);
        tabbedPane.setBackground(Theme.BG);

        tabbedPane.addTab("  My Health Overview  ", createOverviewPanel());
        tabbedPane.addTab("  Book Consultation  ", createBookingPanel());
        tabbedPane.addTab("  My Appointments  ", createAppointmentsPanel());
        tabbedPane.addTab("  Prescriptions & Records  ", createRecordsPanel());
        tabbedPane.addTab("  Billing & Receipts  ", createBillingPanel());

        rootPane.add(tabbedPane, BorderLayout.CENTER);

        // 3. Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.SURFACE);
        statusBar.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, false),
            new EmptyBorder(6, 16, 6, 16)
        ));

        JLabel lblCopyright = new JLabel("Patient Portal • HIPAA & Privacy Protected • City General Hospital");
        lblCopyright.setFont(Theme.FONT_SMALL);
        lblCopyright.setForeground(Theme.TEXT_MUTED);

        JLabel lblDbStatus = new JLabel("Database Engine: " + DBConnection.getInstance().getActiveDatabaseInfo());
        lblDbStatus.setFont(Theme.FONT_SMALL);
        lblDbStatus.setForeground(Theme.TEXT_MUTED);

        statusBar.add(lblCopyright, BorderLayout.WEST);
        statusBar.add(lblDbStatus, BorderLayout.EAST);
        rootPane.add(statusBar, BorderLayout.SOUTH);

        setContentPane(rootPane);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(Theme.PATIENT_GREEN);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Left Branding
        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftBox.setBackground(Theme.PATIENT_GREEN);

        JLabel lblIcon = new JLabel(UIHelper.createMedicalCrossIcon(36, Color.WHITE));
        leftBox.add(lblIcon);

        JPanel detailsBox = new JPanel(new GridLayout(2, 1, 0, 2));
        detailsBox.setBackground(Theme.PATIENT_GREEN);

        String pName = (currentPatient != null) ? currentPatient.getFullName() : "Patient Portal";
        lblPatientName = new JLabel(pName);
        lblPatientName.setFont(new Font(Theme.FONT_TITLE.getName(), Font.BOLD, 19));
        lblPatientName.setForeground(Color.WHITE);

        int pId = (currentPatient != null) ? currentPatient.getId() : 0;
        String bg = (currentPatient != null && currentPatient.getBloodGroup() != null) ? currentPatient.getBloodGroup() : "N/A";
        lblPatientDetails = new JLabel("Patient ID: #" + pId + "  •  Blood Group: " + bg + "  •  Reg Date: " +
            (currentPatient != null ? DateUtil.formatDate(currentPatient.getRegistrationDate()) : ""));
        lblPatientDetails.setFont(Theme.FONT_SMALL);
        lblPatientDetails.setForeground(new Color(0xD1, 0xFA, 0xE5));

        detailsBox.add(lblPatientName);
        detailsBox.add(lblPatientDetails);
        leftBox.add(detailsBox);

        header.add(leftBox, BorderLayout.WEST);

        // Right Meta
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightBox.setBackground(Theme.PATIENT_GREEN);

        lblLiveClock = new JLabel();
        lblLiveClock.setFont(Theme.FONT_SMALL);
        lblLiveClock.setForeground(Color.WHITE);
        updateClock();
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateClock();
            }
        });
        timer.start();
        rightBox.add(lblLiveClock);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(Theme.FONT_SMALL_BOLD);
        btnLogout.setForeground(Theme.PATIENT_GREEN);
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(new CompoundBorder(
            new LineBorder(Color.WHITE, 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogout();
            }
        });
        rightBox.add(btnLogout);

        header.add(rightBox, BorderLayout.EAST);
        return header;
    }

    private void updateClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy • hh:mm:ss a");
        lblLiveClock.setText(LocalDateTime.now().format(dtf));
    }

    /**
     * Tab 1: Overview & Profile
     */
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Top Metrics
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setBackground(Theme.BG);

        lblStatAppts = new JLabel("0");
        lblStatRecords = new JLabel("0");
        lblStatBills = new JLabel("0");
        lblStatPending = new JLabel("₹0.00");

        statsRow.add(createMiniStatCard("TOTAL APPOINTMENTS", lblStatAppts, "Booked consultations", Theme.PATIENT_GREEN));
        statsRow.add(createMiniStatCard("MEDICAL RECORDS", lblStatRecords, "Diagnoses & Rx logged", Theme.PRIMARY));
        statsRow.add(createMiniStatCard("TOTAL INVOICES", lblStatBills, "Hospital bill records", Theme.SECONDARY));
        statsRow.add(createMiniStatCard("PENDING BALANCE", lblStatPending, "Unpaid consultation dues", Theme.WARNING));

        panel.add(statsRow, BorderLayout.NORTH);

        // Center Profile Card
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        JLabel lblHeading = new JLabel("PATIENT MEDICAL PROFILE & REGISTRATION RECORD");
        lblHeading.setFont(Theme.FONT_TITLE);
        lblHeading.setForeground(Theme.PATIENT_GREEN_DARK);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        card.add(lblHeading, gbc);

        gbc.gridwidth = 1;

        if (currentPatient != null) {
            addProfileField(card, gbc, row++, "Full Legal Name:", currentPatient.getFullName());
            addProfileField(card, gbc, row++, "Hospital Patient ID:", "#" + currentPatient.getId());
            addProfileField(card, gbc, row++, "Age & Gender:", currentPatient.getAge() + " years  /  " + currentPatient.getGender());
            addProfileField(card, gbc, row++, "Blood Group:", currentPatient.getBloodGroup());
            addProfileField(card, gbc, row++, "Primary Contact Phone:", currentPatient.getPhone());
            addProfileField(card, gbc, row++, "Email Address:", (currentPatient.getEmail() != null && !currentPatient.getEmail().isEmpty()) ? currentPatient.getEmail() : "N/A");
            addProfileField(card, gbc, row++, "Residential Address:", currentPatient.getAddress());
            addProfileField(card, gbc, row++, "Emergency Contact:", (currentPatient.getEmergencyContact() != null) ? currentPatient.getEmergencyContact() : "None registered");
            addProfileField(card, gbc, row++, "Registration Date:", DateUtil.formatDate(currentPatient.getRegistrationDate()));
        }

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void addProfileField(JPanel parent, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SUBHEADER);
        lbl.setForeground(Theme.TEXT_MUTED);
        parent.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel val = new JLabel(value);
        val.setFont(Theme.FONT_REGULAR);
        val.setForeground(Theme.TEXT_MAIN);
        parent.add(val, gbc);
    }

    /**
     * Tab 2: Book Consultation
     */
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 32, 16, 32));

        JPanel formCard = UIHelper.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 12, 10, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel lblTitle = new JLabel("SCHEDULE A DOCTOR CONSULTATION");
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.PATIENT_GREEN_DARK);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formCard.add(lblTitle, gbc);

        JLabel lblSub = new JLabel("Select an available medical specialist and pick an appointment slot:");
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.TEXT_MUTED);
        gbc.gridy = row++;
        formCard.add(lblSub, gbc);

        gbc.gridwidth = 1;

        // Department Selector
        String[] depts = {"All Departments", "Cardiology", "Pediatrics", "Orthopedics", "Neurology", "General Medicine", "Dermatology"};
        cbDepartment = UIHelper.createComboBox(depts);
        cbDepartment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadDoctorsByDepartment();
            }
        });
        addFormRow(formCard, gbc, row++, "Medical Department *", cbDepartment);

        // Doctor Selector
        cbDoctor = new JComboBox<>();
        cbDoctor.setFont(Theme.FONT_REGULAR);
        cbDoctor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDoctorFeeDisplay();
            }
        });
        addFormRow(formCard, gbc, row++, "Consulting Physician *", cbDoctor);

        // Doctor Fee Display
        lblDoctorFeeInfo = new JLabel("Consultation Fee: ₹0.00  •  Room: N/A");
        lblDoctorFeeInfo.setFont(Theme.FONT_SMALL_BOLD);
        lblDoctorFeeInfo.setForeground(Theme.PRIMARY);
        gbc.gridx = 1;
        gbc.gridy = row++;
        formCard.add(lblDoctorFeeInfo, gbc);

        // Appointment Date
        txtApptDate = UIHelper.createTextField();
        txtApptDate.setText(LocalDate.now().plusDays(1).toString()); // Default to tomorrow
        addFormRow(formCard, gbc, row++, "Appointment Date (YYYY-MM-DD) *", txtApptDate);

        // Time Slot
        String[] slots = {"09:00:00", "09:30:00", "10:15:00", "11:00:00", "11:45:00", "14:00:00", "14:45:00", "15:30:00", "16:15:00"};
        cbTimeSlot = UIHelper.createComboBox(slots);
        addFormRow(formCard, gbc, row++, "Preferred Time Slot *", cbTimeSlot);

        // Reason for Visit
        txtReason = UIHelper.createTextField();
        txtReason.setToolTipText("e.g. Follow-up consultation, knee pain, routine checkup");
        addFormRow(formCard, gbc, row++, "Reason for Consultation *", txtReason);

        // Submit Button
        JButton btnBook = UIHelper.createGreenButton("✓ Confirm & Book Appointment");
        btnBook.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performAppointmentBooking();
            }
        });

        gbc.gridx = 1;
        gbc.gridy = row++;
        formCard.add(btnBook, gbc);

        panel.add(formCard, BorderLayout.NORTH);
        return panel;
    }

    private void addFormRow(JPanel parent, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SUBHEADER);
        lbl.setForeground(Theme.TEXT_MAIN);
        parent.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        parent.add(comp, gbc);
    }

    private void loadDoctorsByDepartment() {
        cbDoctor.removeAllItems();
        String selectedDept = (String) cbDepartment.getSelectedItem();
        try {
            List<Doctor> doctors;
            if (selectedDept == null || selectedDept.equals("All Departments")) {
                doctors = doctorService.getAvailableDoctors();
            } else {
                doctors = doctorDAO.searchBySpecialization(selectedDept);
            }

            for (Doctor d : doctors) {
                if (d.isAvailable()) {
                    cbDoctor.addItem(new DoctorItem(d));
                }
            }
            updateDoctorFeeDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDoctorFeeDisplay() {
        DoctorItem item = (DoctorItem) cbDoctor.getSelectedItem();
        if (item != null && item.doctor != null) {
            lblDoctorFeeInfo.setText(String.format("Consultation Fee: ₹%.2f  •  OPD Room: %s  •  Qualifications: %s",
                item.doctor.getConsultationFee(), item.doctor.getRoomNumber(), item.doctor.getQualification()));
        } else {
            lblDoctorFeeInfo.setText("No physician selected.");
        }
    }

    private void performAppointmentBooking() {
        if (currentPatient == null) {
            JOptionPane.showMessageDialog(this, "No active patient session.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DoctorItem item = (DoctorItem) cbDoctor.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an available doctor.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateStr = txtApptDate.getText().trim();
        String timeStr = (String) cbTimeSlot.getSelectedItem();
        String reason = txtReason.getText().trim();

        if (dateStr.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both appointment date and reason for visit.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
            if (date.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Appointment date cannot be in the past.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter date in YYYY-MM-DD format (e.g. 2026-10-15).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalTime time = LocalTime.parse(timeStr);

        try {
            Appointment appt = appointmentService.bookAppointment(
                currentPatient.getId(),
                item.doctor.getId(),
                date,
                time,
                reason
            );

            JOptionPane.showMessageDialog(
                this,
                "Appointment confirmed successfully!\n\n" +
                "Appointment ID : #" + appt.getId() + "\n" +
                "Doctor         : " + item.doctor.getFullName() + " (" + item.doctor.getSpecialization() + ")\n" +
                "Room Number    : " + item.doctor.getRoomNumber() + "\n" +
                "Date & Time    : " + date + " at " + time + "\n" +
                "SMS confirmation dispatched to: " + currentPatient.getPhone(),
                "Appointment Booked",
                JOptionPane.INFORMATION_MESSAGE
            );

            txtReason.setText("");
            refreshAllData();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage(), "Conflict / Booking Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Tab 3: My Appointments
     */
    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("My Scheduled & Past Appointments");
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(Theme.TEXT_MAIN);
        card.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Date", "Time", "Attending Doctor", "Department / Specialization", "Reason for Visit", "Status", "Doctor Notes"};
        apptTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblAppointments = UIHelper.createTable(apptTableModel);
        tblAppointments.getColumnModel().getColumn(6).setCellRenderer(UIHelper.createStatusBadgeRenderer());
        card.add(new JScrollPane(tblAppointments), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnBar.setBackground(Theme.SURFACE);

        JButton btnCancel = UIHelper.createDangerButton("✕ Cancel Appointment");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelSelectedAppointment();
            }
        });

        JButton btnRefresh = UIHelper.createOutlineButton("🔄 Refresh");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAppointmentsData();
            }
        });

        btnBar.add(btnRefresh);
        btnBar.add(btnCancel);
        card.add(btnBar, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void cancelSelectedAppointment() {
        int row = tblAppointments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the list.", "Select Appointment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int apptId = (int) apptTableModel.getValueAt(row, 0);
        String status = (String) apptTableModel.getValueAt(row, 6);

        if (!"Scheduled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only scheduled appointments can be cancelled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel Appointment #" + apptId + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                appointmentService.cancelAppointment(apptId);
                JOptionPane.showMessageDialog(this, "Appointment #" + apptId + " has been cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                loadAppointmentsData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Tab 4: My Medical Records & Prescriptions
     */
    private JPanel createRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Official Clinical History & Prescriptions");
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(Theme.TEXT_MAIN);
        card.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Record ID", "Consultation Date", "Doctor Name", "Specialization", "Clinical Diagnosis"};
        recordTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblRecords = UIHelper.createTable(recordTableModel);

        txtRecordDetail = new JTextArea(8, 30);
        txtRecordDetail.setFont(Theme.FONT_REGULAR);
        txtRecordDetail.setEditable(false);
        txtRecordDetail.setLineWrap(true);
        txtRecordDetail.setWrapStyleWord(true);
        txtRecordDetail.setText("Select any clinical record above to inspect full diagnosis details and prescription dosage instructions.");

        tblRecords.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    showSelectedRecordDetail();
                }
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(tblRecords),
            new JScrollPane(txtRecordDetail)
        );
        split.setDividerLocation(260);
        card.add(split, BorderLayout.CENTER);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void showSelectedRecordDetail() {
        int row = tblRecords.getSelectedRow();
        if (row < 0 || row >= loadedRecords.size()) return;

        MedicalRecord mr = loadedRecords.get(row);
        Doctor d = null;
        try {
            d = doctorDAO.findById(mr.getDoctorId());
        } catch (Exception ignored) {}

        String text = String.format(
            "========================================================================================\n" +
            " CLINICAL PRESCRIPTION & DIAGNOSTIC SLIP [Record ID #%d]\n" +
            "========================================================================================\n" +
            " Attending Physician : %s (%s)\n" +
            " OPD Room            : %s\n" +
            " Consultation Date   : %s\n\n" +
            " CLINICAL DIAGNOSIS:\n%s\n\n" +
            " MEDICINES & PRESCRIPTION DOSAGE:\n%s\n\n" +
            " PHYSICIAN TREATMENT INSTRUCTIONS / DIETARY DIRECTIVES:\n%s\n" +
            "========================================================================================",
            mr.getId(),
            (d != null ? d.getFullName() : "Doctor #" + mr.getDoctorId()),
            (d != null ? d.getSpecialization() : "OPD"),
            (d != null ? d.getRoomNumber() : "N/A"),
            mr.getRecordedDate(),
            mr.getDiagnosis(),
            mr.getPrescription(),
            (mr.getTreatmentNotes() != null ? mr.getTreatmentNotes() : "Follow general medical advice.")
        );
        txtRecordDetail.setText(text);
        txtRecordDetail.setCaretPosition(0);
    }

    /**
     * Tab 5: Billing & Invoicing
     */
    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("My Hospital Bills & Financial Invoices");
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(Theme.TEXT_MAIN);
        card.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Bill ID", "Invoice Date", "Consultation Fee", "Extra Charges", "Tax", "Discount", "Total Amount", "Status", "Payment Method"};
        billTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblBills = UIHelper.createTable(billTableModel);
        tblBills.getColumnModel().getColumn(7).setCellRenderer(UIHelper.createStatusBadgeRenderer());
        card.add(new JScrollPane(tblBills), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnBar.setBackground(Theme.SURFACE);

        JButton btnPay = UIHelper.createGreenButton("💳 Pay Bill Online");
        btnPay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paySelectedBill();
            }
        });

        JButton btnReceipt = UIHelper.createOutlineButton("🖨️ View & Print Receipt");
        btnReceipt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printSelectedReceipt();
            }
        });

        JButton btnRefresh = UIHelper.createOutlineButton("🔄 Refresh Bills");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadBillsData();
            }
        });

        btnBar.add(btnRefresh);
        btnBar.add(btnReceipt);
        btnBar.add(btnPay);
        card.add(btnBar, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void paySelectedBill() {
        int row = tblBills.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the list to pay.", "Select Bill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int billId = (int) billTableModel.getValueAt(row, 0);
        String status = (String) billTableModel.getValueAt(row, 7);

        if ("PAID".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This invoice is already marked as PAID.", "Already Paid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String totalStr = String.valueOf(billTableModel.getValueAt(row, 6));

        String[] methods = {"CARD", "UPI", "CASH", "INSURANCE"};
        String selectedMethod = (String) JOptionPane.showInputDialog(
            this,
            "Select Payment Gateway / Method for " + totalStr + ":",
            "Complete Bill Payment",
            JOptionPane.QUESTION_MESSAGE,
            null,
            methods,
            methods[0]
        );

        if (selectedMethod != null) {
            try {
                billingService.processPayment(billId, selectedMethod);
                JOptionPane.showMessageDialog(this, "Payment of " + totalStr + " processed successfully via " + selectedMethod + "!", "Payment Success", JOptionPane.INFORMATION_MESSAGE);
                loadBillsData();

                // Open receipt immediately for convenience
                Bill updated = billDAO.findById(billId);
                if (updated != null) {
                    ReceiptDialog receipt = new ReceiptDialog(this, updated);
                    receipt.setVisible(true);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Payment failed: " + ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printSelectedReceipt() {
        int row = tblBills.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill from the table first.", "Select Bill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int billId = (int) billTableModel.getValueAt(row, 0);
        try {
            Bill bill = billDAO.findById(billId);
            if (bill != null) {
                ReceiptDialog dialog = new ReceiptDialog(this, bill);
                dialog.setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // Data Loading
    // =========================================================================

    public void refreshAllData() {
        loadDoctorsByDepartment();
        loadAppointmentsData();
        loadRecordsData();
        loadBillsData();
    }

    private void loadAppointmentsData() {
        apptTableModel.setRowCount(0);
        if (currentPatient == null) return;

        try {
            loadedAppointments = appointmentService.getAppointmentsByPatient(currentPatient.getId());
            lblStatAppts.setText(String.valueOf(loadedAppointments.size()));

            for (Appointment a : loadedAppointments) {
                Doctor d = doctorDAO.findById(a.getDoctorId());
                String docName = (d != null) ? d.getFullName() : "Doctor #" + a.getDoctorId();
                String spec = (d != null) ? d.getSpecialization() : "OPD";

                apptTableModel.addRow(new Object[]{
                    a.getId(),
                    a.getAppointmentDate(),
                    a.getAppointmentTime(),
                    docName,
                    spec,
                    a.getReason(),
                    a.getStatus().name(),
                    (a.getNotes() != null ? a.getNotes() : "")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecordsData() {
        recordTableModel.setRowCount(0);
        if (currentPatient == null) return;

        try {
            loadedRecords = medicalRecordService.getRecordsByPatient(currentPatient.getId());
            lblStatRecords.setText(String.valueOf(loadedRecords.size()));

            for (MedicalRecord mr : loadedRecords) {
                Doctor d = doctorDAO.findById(mr.getDoctorId());
                String docName = (d != null) ? d.getFullName() : "Doctor #" + mr.getDoctorId();
                String spec = (d != null) ? d.getSpecialization() : "OPD";
                String dateStr = mr.getRecordedDate() != null ? mr.getRecordedDate().toString() : "N/A";

                recordTableModel.addRow(new Object[]{
                    mr.getId(),
                    dateStr,
                    docName,
                    spec,
                    mr.getDiagnosis()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBillsData() {
        billTableModel.setRowCount(0);
        if (currentPatient == null) return;

        try {
            loadedBills = billingService.getBillsByPatient(currentPatient.getId());
            lblStatBills.setText(String.valueOf(loadedBills.size()));

            double pendingTotal = 0.0;

            for (Bill b : loadedBills) {
                if (b.getPaymentStatus() == PaymentStatus.PENDING) {
                    pendingTotal += b.getTotalAmount();
                }

                String dateStr = b.getBillDate() != null ? b.getBillDate().toString() : "N/A";
                billTableModel.addRow(new Object[]{
                    b.getId(),
                    dateStr,
                    String.format("₹%.2f", b.getConsultationFee()),
                    String.format("₹%.2f", b.getAdditionalCharges()),
                    String.format("₹%.2f", b.getTaxAmount()),
                    String.format("₹%.2f", b.getDiscount()),
                    String.format("₹%.2f", b.getTotalAmount()),
                    b.getPaymentStatus().name(),
                    (b.getPaymentMethod() != null ? b.getPaymentMethod() : "Pending")
                });
            }

            lblStatPending.setText(String.format("₹%.2f", pendingTotal));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createMiniStatCard(String title, JLabel valLabel, String sub, Color color) {
        JPanel c = new JPanel(new BorderLayout(8, 4)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setColor(color);
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        c.setBackground(Theme.SURFACE);
        c.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_SMALL_BOLD);
        t.setForeground(Theme.TEXT_MUTED);

        valLabel.setFont(new Font(Theme.FONT_TITLE.getName(), Font.BOLD, 22));
        valLabel.setForeground(Theme.TEXT_MAIN);

        JLabel s = new JLabel(sub);
        s.setFont(Theme.FONT_SMALL);
        s.setForeground(Theme.TEXT_MUTED);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 2));
        center.setBackground(Theme.SURFACE);
        center.add(valLabel);
        center.add(s);

        c.add(t, BorderLayout.NORTH);
        c.add(center, BorderLayout.CENTER);
        return c;
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out of the Patient Portal?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            new RoleSelectionFrame().setVisible(true);
        }
    }

    private static class DoctorItem {
        final Doctor doctor;

        DoctorItem(Doctor doctor) {
            this.doctor = doctor;
        }

        @Override
        public String toString() {
            return String.format("%s (%s - %s, ₹%.0f)", 
                doctor.getFullName(), doctor.getSpecialization(), doctor.getRoomNumber(), doctor.getConsultationFee());
        }
    }
}
