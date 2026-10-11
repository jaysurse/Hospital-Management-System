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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import com.hospital.dao.DBConnection;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Doctor;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.model.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.AuthService;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import com.hospital.ui.dialogs.MedicalRecordDialog;

/**
 * Doctor Dashboard Window.
 * 
 * Clinical, Teal-themed workspace (#0D9488) allowing medical doctors to:
 * 1. View assigned appointments queue & today's schedule
 * 2. Add clinical diagnoses and prescriptions
 * 3. Update appointment status (Completed / Cancelled)
 * 4. View authorized patient medical history
 * 5. Manage doctor profile & toggle availability status
 */
public class DoctorDashboardFrame extends JFrame {

    private final AuthService authService;
    private final DoctorDAO doctorDAO;
    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;
    private final MedicalRecordDAO medicalRecordDAO;

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    private Doctor currentDoctor;

    // Header UI elements
    private JLabel lblDoctorName;
    private JLabel lblDoctorSpec;
    private JLabel lblRoomBadge;
    private JLabel lblAvailabilityBadge;
    private JLabel lblLiveTime;

    // Appointments Queue tab
    private JTable tblAppointments;
    private DefaultTableModel appointmentTableModel;
    private List<Appointment> loadedAppointments = new ArrayList<>();
    private JLabel lblStatTotal;
    private JLabel lblStatScheduled;
    private JLabel lblStatCompleted;
    private JTextField txtSearchAppt;

    // My Patients tab
    private JTable tblPatients;
    private DefaultTableModel patientTableModel;
    private List<Patient> loadedPatients = new ArrayList<>();

    // Medical Records tab
    private JTable tblRecords;
    private DefaultTableModel recordTableModel;
    private List<MedicalRecord> loadedRecords = new ArrayList<>();
    private JTextArea txtRecordDetail;

    // Profile tab
    private JLabel lblProfileName;
    private JLabel lblProfileSpec;
    private JLabel lblProfileQual;
    private JLabel lblProfileFee;
    private JLabel lblProfileRoom;
    private JLabel lblProfilePhone;
    private JLabel lblProfileEmail;
    private JLabel lblProfileStatus;
    private JButton btnToggleStatus;

    public DoctorDashboardFrame() {
        this.authService = AuthService.getInstance();
        this.doctorDAO = new DoctorDAO();
        this.patientDAO = new PatientDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.medicalRecordDAO = new MedicalRecordDAO();

        this.doctorService = new DoctorService(doctorDAO);
        this.patientService = new PatientService(patientDAO);
        this.appointmentService = new AppointmentService(appointmentDAO, doctorDAO, patientDAO, null);
        this.medicalRecordService = new MedicalRecordService(medicalRecordDAO, patientDAO, doctorDAO);

        loadDoctorSession();
        initComponents();
        refreshAllData();
    }

    private void loadDoctorSession() {
        this.currentDoctor = authService.getCurrentDoctor();
        if (this.currentDoctor == null) {
            User user = authService.getCurrentUser();
            if (user != null) {
                try {
                    List<Doctor> docs = doctorDAO.search(user.getFullName());
                    if (!docs.isEmpty()) {
                        this.currentDoctor = docs.get(0);
                    } else {
                        List<Doctor> allDocs = doctorDAO.findAll();
                        if (!allDocs.isEmpty()) {
                            this.currentDoctor = allDocs.get(0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initComponents() {
        String docName = (currentDoctor != null) ? currentDoctor.getFullName() : "Doctor";
        setTitle(HospitalConstants.HOSPITAL_NAME + " - Doctor Portal (" + docName + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);

        JPanel rootPane = new JPanel(new BorderLayout());
        rootPane.setBackground(Theme.BG);

        // 1. Top Teal Header
        rootPane.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Tabbed Workspace
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_SUBHEADER);
        tabbedPane.setBackground(Theme.BG);

        tabbedPane.addTab("  Appointments Queue  ", createAppointmentsPanel());
        tabbedPane.addTab("  My Patients  ", createPatientsPanel());
        tabbedPane.addTab("  Prescriptions & Records  ", createRecordsPanel());
        tabbedPane.addTab("  Doctor Profile & Schedule  ", createProfilePanel());

        rootPane.add(tabbedPane, BorderLayout.CENTER);

        // 3. Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.SURFACE);
        statusBar.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, false),
            new EmptyBorder(6, 16, 6, 16)
        ));

        JLabel lblCopyright = new JLabel("Doctor Portal • Real-Time MySQL Synchronization • Encrypted Records");
        lblCopyright.setFont(Theme.FONT_SMALL);
        lblCopyright.setForeground(Theme.TEXT_MUTED);

        JLabel lblActiveDb = new JLabel("Database Engine: " + DBConnection.getInstance().getActiveDatabaseInfo());
        lblActiveDb.setFont(Theme.FONT_SMALL);
        lblActiveDb.setForeground(Theme.TEXT_MUTED);

        statusBar.add(lblCopyright, BorderLayout.WEST);
        statusBar.add(lblActiveDb, BorderLayout.EAST);
        rootPane.add(statusBar, BorderLayout.SOUTH);

        setContentPane(rootPane);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(Theme.DOCTOR_TEAL);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Left Branding & Doctor Details
        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftBox.setBackground(Theme.DOCTOR_TEAL);

        JLabel lblIcon = new JLabel(UIHelper.createMedicalCrossIcon(36, Color.WHITE));
        leftBox.add(lblIcon);

        JPanel detailsBox = new JPanel(new GridLayout(2, 1, 0, 2));
        detailsBox.setBackground(Theme.DOCTOR_TEAL);

        String docName = (currentDoctor != null) ? currentDoctor.getFullName() : "Physician Portal";
        lblDoctorName = new JLabel(docName);
        lblDoctorName.setFont(new Font(Theme.FONT_TITLE.getName(), Font.BOLD, 19));
        lblDoctorName.setForeground(Color.WHITE);

        String spec = (currentDoctor != null) ? currentDoctor.getSpecialization() : "General Practice";
        String room = (currentDoctor != null) ? currentDoctor.getRoomNumber() : "OPD";
        lblDoctorSpec = new JLabel(spec + "  •  OPD Room: " + room);
        lblDoctorSpec.setFont(Theme.FONT_SMALL);
        lblDoctorSpec.setForeground(new Color(0xCC, 0xFB, 0xF1));

        detailsBox.add(lblDoctorName);
        detailsBox.add(lblDoctorSpec);
        leftBox.add(detailsBox);

        // Availability badge
        boolean isAvail = (currentDoctor == null || currentDoctor.isAvailable());
        lblAvailabilityBadge = new JLabel(isAvail ? "  Available for Consultation  " : "  On Leave / Unavailable  ");
        lblAvailabilityBadge.setFont(Theme.FONT_SMALL_BOLD);
        lblAvailabilityBadge.setForeground(isAvail ? new Color(0x06, 0x5F, 0x46) : new Color(0x99, 0x1B, 0x1B));
        lblAvailabilityBadge.setBackground(isAvail ? new Color(0xD1, 0xFA, 0xE5) : new Color(0xFE, 0xE2, 0xE2));
        lblAvailabilityBadge.setOpaque(true);
        lblAvailabilityBadge.setBorder(new LineBorder(isAvail ? new Color(0xA7, 0xF3, 0xD0) : new Color(0xFE, 0xCA, 0xCA), 1, true));
        leftBox.add(lblAvailabilityBadge);

        header.add(leftBox, BorderLayout.WEST);

        // Right Meta: Clock + Logout
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightBox.setBackground(Theme.DOCTOR_TEAL);

        lblLiveTime = new JLabel();
        lblLiveTime.setFont(Theme.FONT_SMALL);
        lblLiveTime.setForeground(Color.WHITE);
        updateClock();
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateClock();
            }
        });
        timer.start();
        rightBox.add(lblLiveTime);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(Theme.FONT_SMALL_BOLD);
        btnLogout.setForeground(Theme.DOCTOR_TEAL);
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
        lblLiveTime.setText(LocalDateTime.now().format(dtf));
    }

    /**
     * Tab 1: Appointments Queue
     */
    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Row: 3 Summary Stat Cards
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setBackground(Theme.BG);

        lblStatTotal = new JLabel("0");
        lblStatScheduled = new JLabel("0");
        lblStatCompleted = new JLabel("0");

        statsRow.add(createMiniStatCard("TOTAL ASSIGNED", lblStatTotal, "All patient appointments", Theme.DOCTOR_TEAL));
        statsRow.add(createMiniStatCard("SCHEDULED QUEUE", lblStatScheduled, "Pending consultations", Theme.WARNING));
        statsRow.add(createMiniStatCard("COMPLETED TODAY", lblStatCompleted, "Consultations finished", Theme.SUCCESS));

        panel.add(statsRow, BorderLayout.NORTH);

        // Center: Search bar + Appointments Table
        JPanel tableContainer = UIHelper.createCardPanel();
        tableContainer.setLayout(new BorderLayout(0, 10));

        JPanel filterBar = new JPanel(new BorderLayout());
        filterBar.setBackground(Theme.SURFACE);

        JLabel lblListTitle = new JLabel("Assigned Consultations Queue");
        lblListTitle.setFont(Theme.FONT_HEADER);
        lblListTitle.setForeground(Theme.TEXT_MAIN);
        filterBar.add(lblListTitle, BorderLayout.WEST);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBox.setBackground(Theme.SURFACE);
        searchBox.add(new JLabel("Search:"));
        txtSearchAppt = UIHelper.createTextField();
        txtSearchAppt.setPreferredSize(new Dimension(180, 30));
        txtSearchAppt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterAppointments();
            }
        });
        searchBox.add(txtSearchAppt);

        JButton btnSearch = UIHelper.createOutlineButton("Find");
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterAppointments();
            }
        });
        searchBox.add(btnSearch);

        JButton btnReset = UIHelper.createOutlineButton("Reset");
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearchAppt.setText("");
                loadAppointmentsData();
            }
        });
        searchBox.add(btnReset);
        filterBar.add(searchBox, BorderLayout.EAST);

        tableContainer.add(filterBar, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Patient ID", "Patient Name", "Date", "Time", "Reason for Visit", "Status", "Clinical Notes"};
        appointmentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblAppointments = UIHelper.createTable(appointmentTableModel);
        tblAppointments.getColumnModel().getColumn(6).setCellRenderer(UIHelper.createStatusBadgeRenderer());
        tableContainer.add(new JScrollPane(tblAppointments), BorderLayout.CENTER);

        // Action Toolbar
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        actionRow.setBackground(Theme.SURFACE);

        JButton btnAddRecord = UIHelper.createTealButton("🩺 Add Diagnosis & Rx");
        btnAddRecord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDiagnosisForSelectedAppointment();
            }
        });

        JButton btnComplete = UIHelper.createSuccessButton("✓ Mark as Completed");
        btnComplete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedAppointmentStatus(AppointmentStatus.COMPLETED);
            }
        });

        JButton btnCancel = UIHelper.createDangerButton("✕ Cancel Appointment");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedAppointmentStatus(AppointmentStatus.CANCELLED);
            }
        });

        JButton btnViewPatient = UIHelper.createOutlineButton("👤 View Patient Profile");
        btnViewPatient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewSelectedPatientDetails();
            }
        });

        JButton btnRefresh = UIHelper.createOutlineButton("🔄 Refresh Queue");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAppointmentsData();
            }
        });

        actionRow.add(btnRefresh);
        actionRow.add(btnViewPatient);
        actionRow.add(btnCancel);
        actionRow.add(btnComplete);
        actionRow.add(btnAddRecord);

        tableContainer.add(actionRow, BorderLayout.SOUTH);

        panel.add(tableContainer, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tab 2: My Patients
     */
    private JPanel createPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.SURFACE);
        JLabel lblTitle = new JLabel("Patients Treated / Scheduled Under My Care");
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(Theme.TEXT_MAIN);
        topBar.add(lblTitle, BorderLayout.WEST);

        card.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Patient ID", "Full Name", "Age", "Gender", "Phone", "Blood Group", "Address", "Emergency Contact"};
        patientTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblPatients = UIHelper.createTable(patientTableModel);
        card.add(new JScrollPane(tblPatients), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnBar.setBackground(Theme.SURFACE);

        JButton btnViewHistory = UIHelper.createTealButton("📜 View Medical History");
        btnViewHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewHistoryForSelectedPatient();
            }
        });

        JButton btnRefresh = UIHelper.createOutlineButton("🔄 Refresh Patients");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPatientsData();
            }
        });

        btnBar.add(btnRefresh);
        btnBar.add(btnViewHistory);
        card.add(btnBar, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tab 3: Prescriptions & Records History
     */
    private JPanel createRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Clinical Examination Records & Prescriptions Issued");
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(Theme.TEXT_MAIN);
        card.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Record ID", "Patient ID", "Patient Name", "Recorded Date", "Clinical Diagnosis", "Prescription Summary"};
        recordTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblRecords = UIHelper.createTable(recordTableModel);

        // Lower detail pane
        txtRecordDetail = new JTextArea(7, 30);
        txtRecordDetail.setFont(Theme.FONT_REGULAR);
        txtRecordDetail.setEditable(false);
        txtRecordDetail.setLineWrap(true);
        txtRecordDetail.setWrapStyleWord(true);
        txtRecordDetail.setText("Select any record above to inspect full diagnostic findings, prescription dosages, and treatment notes.");

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
        split.setDividerLocation(280);
        card.add(split, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnBar.setBackground(Theme.SURFACE);

        JButton btnRefresh = UIHelper.createOutlineButton("🔄 Refresh Records");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadRecordsData();
            }
        });
        btnBar.add(btnRefresh);
        card.add(btnBar, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tab 4: Doctor Profile & Schedule Management
     */
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel lblTitle = new JLabel("PHYSICIAN CREDENTIALS & OPD STATUS");
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.DOCTOR_TEAL);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        card.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        lblProfileName = addProfileRow(card, gbc, row++, "Physician Name:");
        lblProfileSpec = addProfileRow(card, gbc, row++, "Specialization:");
        lblProfileQual = addProfileRow(card, gbc, row++, "Qualifications:");
        lblProfileFee = addProfileRow(card, gbc, row++, "Consultation Fee:");
        lblProfileRoom = addProfileRow(card, gbc, row++, "OPD Room Number:");
        lblProfilePhone = addProfileRow(card, gbc, row++, "Contact Phone:");
        lblProfileEmail = addProfileRow(card, gbc, row++, "Email Address:");
        lblProfileStatus = addProfileRow(card, gbc, row++, "Consultation Availability:");

        // Action button to toggle availability
        btnToggleStatus = UIHelper.createTealButton("Toggle Availability Status");
        btnToggleStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDoctorAvailability();
            }
        });

        gbc.gridx = 1;
        gbc.gridy = row++;
        card.add(btnToggleStatus, gbc);

        panel.add(card, BorderLayout.NORTH);
        return panel;
    }

    private JLabel addProfileRow(JPanel parent, GridBagConstraints gbc, int row, String labelText) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_SUBHEADER);
        lbl.setForeground(Theme.TEXT_MUTED);
        parent.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel val = new JLabel("—");
        val.setFont(Theme.FONT_REGULAR);
        val.setForeground(Theme.TEXT_MAIN);
        parent.add(val, gbc);
        return val;
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

    // =========================================================================
    // Data Loading & Business Logic
    // =========================================================================

    public void refreshAllData() {
        loadDoctorProfileData();
        loadAppointmentsData();
        loadPatientsData();
        loadRecordsData();
    }

    private void loadDoctorProfileData() {
        if (currentDoctor == null) return;
        lblProfileName.setText(currentDoctor.getFullName());
        lblProfileSpec.setText(currentDoctor.getSpecialization());
        lblProfileQual.setText(currentDoctor.getQualification());
        lblProfileFee.setText(String.format("₹%.2f per consultation", currentDoctor.getConsultationFee()));
        lblProfileRoom.setText(currentDoctor.getRoomNumber());
        lblProfilePhone.setText(currentDoctor.getPhone());
        lblProfileEmail.setText(currentDoctor.getEmail());

        boolean isAvail = currentDoctor.isAvailable();
        lblProfileStatus.setText(isAvail ? "AVAILABLE (Accepting Appointments)" : "ON LEAVE / UNAVAILABLE");
        lblProfileStatus.setForeground(isAvail ? Theme.SUCCESS : Theme.ERROR);

        lblAvailabilityBadge.setText(isAvail ? "  Available for Consultation  " : "  On Leave / Unavailable  ");
        lblAvailabilityBadge.setForeground(isAvail ? new Color(0x06, 0x5F, 0x46) : new Color(0x99, 0x1B, 0x1B));
        lblAvailabilityBadge.setBackground(isAvail ? new Color(0xD1, 0xFA, 0xE5) : new Color(0xFE, 0xE2, 0xE2));
        lblAvailabilityBadge.setBorder(new LineBorder(isAvail ? new Color(0xA7, 0xF3, 0xD0) : new Color(0xFE, 0xCA, 0xCA), 1, true));

        btnToggleStatus.setText(isAvail ? "Mark as On Leave" : "Mark as Available");
    }

    private void loadAppointmentsData() {
        appointmentTableModel.setRowCount(0);
        if (currentDoctor == null) return;

        try {
            int docId = currentDoctor.getId();
            loadedAppointments = appointmentService.getAppointmentsByDoctor(docId);

            int total = loadedAppointments.size();
            int scheduled = 0;
            int completed = 0;

            for (Appointment appt : loadedAppointments) {
                if (appt.getStatus() == AppointmentStatus.SCHEDULED) {
                    scheduled++;
                } else if (appt.getStatus() == AppointmentStatus.COMPLETED) {
                    completed++;
                }

                String patientName = "Patient #" + appt.getPatientId();
                Patient p = patientDAO.findById(appt.getPatientId());
                if (p != null) {
                    patientName = p.getFullName();
                }

                appointmentTableModel.addRow(new Object[]{
                    appt.getId(),
                    appt.getPatientId(),
                    patientName,
                    appt.getAppointmentDate(),
                    appt.getAppointmentTime(),
                    appt.getReason(),
                    appt.getStatus().name(),
                    (appt.getNotes() != null ? appt.getNotes() : "")
                });
            }

            lblStatTotal.setText(String.valueOf(total));
            lblStatScheduled.setText(String.valueOf(scheduled));
            lblStatCompleted.setText(String.valueOf(completed));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterAppointments() {
        String keyword = txtSearchAppt.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadAppointmentsData();
            return;
        }

        appointmentTableModel.setRowCount(0);
        for (Appointment appt : loadedAppointments) {
            String patientName = "";
            try {
                Patient p = patientDAO.findById(appt.getPatientId());
                if (p != null) patientName = p.getFullName();
            } catch (Exception ignored) {}

            boolean match = String.valueOf(appt.getId()).contains(keyword) ||
                            patientName.toLowerCase().contains(keyword) ||
                            appt.getReason().toLowerCase().contains(keyword) ||
                            appt.getStatus().name().toLowerCase().contains(keyword);

            if (match) {
                appointmentTableModel.addRow(new Object[]{
                    appt.getId(),
                    appt.getPatientId(),
                    patientName,
                    appt.getAppointmentDate(),
                    appt.getAppointmentTime(),
                    appt.getReason(),
                    appt.getStatus().name(),
                    appt.getNotes()
                });
            }
        }
    }

    private void loadPatientsData() {
        patientTableModel.setRowCount(0);
        if (currentDoctor == null) return;

        try {
            // Find all unique patient IDs linked to this doctor's appointments
            Set<Integer> patientIds = new HashSet<>();
            for (Appointment appt : loadedAppointments) {
                patientIds.add(appt.getPatientId());
            }

            loadedPatients.clear();
            for (Integer pid : patientIds) {
                Patient p = patientDAO.findById(pid);
                if (p != null) {
                    loadedPatients.add(p);
                    patientTableModel.addRow(new Object[]{
                        p.getId(),
                        p.getFullName(),
                        p.getAge() + " yrs",
                        p.getGender(),
                        p.getPhone(),
                        p.getBloodGroup(),
                        p.getAddress(),
                        p.getEmergencyContact()
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecordsData() {
        recordTableModel.setRowCount(0);
        if (currentDoctor == null) return;

        try {
            loadedRecords = medicalRecordService.getRecordsByDoctor(currentDoctor.getId());
            for (MedicalRecord mr : loadedRecords) {
                String patientName = "Patient #" + mr.getPatientId();
                try {
                    Patient p = patientDAO.findById(mr.getPatientId());
                    if (p != null) patientName = p.getFullName();
                } catch (Exception ignored) {}

                String dateStr = mr.getRecordedDate() != null ? mr.getRecordedDate().toString() : "N/A";
                String rxBrief = mr.getPrescription();
                if (rxBrief != null && rxBrief.length() > 30) {
                    rxBrief = rxBrief.substring(0, 27) + "...";
                }

                recordTableModel.addRow(new Object[]{
                    mr.getId(),
                    mr.getPatientId(),
                    patientName,
                    dateStr,
                    mr.getDiagnosis(),
                    rxBrief
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSelectedRecordDetail() {
        int selectedRow = tblRecords.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= loadedRecords.size()) {
            return;
        }

        MedicalRecord mr = loadedRecords.get(selectedRow);
        String patientName = "Patient #" + mr.getPatientId();
        try {
            Patient p = patientDAO.findById(mr.getPatientId());
            if (p != null) patientName = p.getFullName();
        } catch (Exception ignored) {}

        String text = String.format(
            "========================================================================================\n" +
            " CLINICAL EXAMINATION & PRESCRIPTION DETAIL [Record ID #%d]\n" +
            "========================================================================================\n" +
            " Patient Name     : %s (ID #%d)\n" +
            " Attending Doctor : %s (%s)\n" +
            " Recorded Date    : %s\n\n" +
            " CLINICAL DIAGNOSIS:\n%s\n\n" +
            " PRESCRIPTION & DOSAGE DIRECTIVES:\n%s\n\n" +
            " PHYSICIAN TREATMENT NOTES / INSTRUCTIONS:\n%s\n" +
            "========================================================================================",
            mr.getId(),
            patientName,
            mr.getPatientId(),
            (currentDoctor != null ? currentDoctor.getFullName() : "Physician"),
            (currentDoctor != null ? currentDoctor.getSpecialization() : "OPD"),
            mr.getRecordedDate(),
            mr.getDiagnosis(),
            mr.getPrescription(),
            (mr.getTreatmentNotes() != null ? mr.getTreatmentNotes() : "None")
        );
        txtRecordDetail.setText(text);
        txtRecordDetail.setCaretPosition(0);
    }

    private void addDiagnosisForSelectedAppointment() {
        int row = tblAppointments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the queue first.", "Select Appointment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int apptId = (int) appointmentTableModel.getValueAt(row, 0);
        int patientId = (int) appointmentTableModel.getValueAt(row, 1);

        MedicalRecordDialog dialog = new MedicalRecordDialog(this, medicalRecordService, patientService, doctorService, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            // Ask doctor if they'd like to mark the appointment completed
            int opt = JOptionPane.showConfirmDialog(
                this,
                "Medical record saved successfully!\nWould you like to mark Appointment #" + apptId + " as COMPLETED?",
                "Appointment Completed",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    appointmentService.updateStatus(apptId, AppointmentStatus.COMPLETED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            refreshAllData();
        }
    }

    private void updateSelectedAppointmentStatus(AppointmentStatus newStatus) {
        int row = tblAppointments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the queue.", "Select Appointment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int apptId = (int) appointmentTableModel.getValueAt(row, 0);
        String prompt = "Are you sure you want to change Appointment #" + apptId + " status to " + newStatus.name() + "?";
        int confirm = JOptionPane.showConfirmDialog(this, prompt, "Confirm Status Update", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                appointmentService.updateStatus(apptId, newStatus);
                JOptionPane.showMessageDialog(this, "Appointment status updated to " + newStatus.name() + ".", "Updated", JOptionPane.INFORMATION_MESSAGE);
                loadAppointmentsData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewSelectedPatientDetails() {
        int row = tblAppointments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to view patient details.", "Select Appointment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId = (int) appointmentTableModel.getValueAt(row, 1);
        try {
            Patient p = patientDAO.findById(patientId);
            if (p != null) {
                JOptionPane.showMessageDialog(this, p.getDetails(), "Patient Profile #" + p.getId(), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewHistoryForSelectedPatient() {
        int row = tblPatients.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the list.", "Select Patient", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId = (int) patientTableModel.getValueAt(row, 0);
        try {
            Patient p = patientDAO.findById(patientId);
            List<MedicalRecord> records = medicalRecordService.getRecordsByPatient(patientId);
            StringBuilder sb = new StringBuilder();
            sb.append("PATIENT MEDICAL HISTORY: ").append(p != null ? p.getFullName() : "Patient #" + patientId).append("\n");
            sb.append("=========================================================================\n\n");

            if (records.isEmpty()) {
                sb.append("No prior clinical diagnosis records logged for this patient yet.\n");
            } else {
                for (MedicalRecord r : records) {
                    sb.append(String.format("Record #%d  |  Date: %s\n", r.getId(), r.getRecordedDate()));
                    sb.append("Diagnosis   : ").append(r.getDiagnosis()).append("\n");
                    sb.append("Prescription: ").append(r.getPrescription()).append("\n");
                    sb.append("Notes       : ").append(r.getTreatmentNotes() != null ? r.getTreatmentNotes() : "N/A").append("\n");
                    sb.append("-------------------------------------------------------------------------\n\n");
                }
            }

            JTextArea area = new JTextArea(sb.toString(), 16, 50);
            area.setFont(Theme.FONT_MONO);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Medical History", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading medical history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleDoctorAvailability() {
        if (currentDoctor == null) return;
        boolean newStatus = !currentDoctor.isAvailable();
        currentDoctor.setAvailable(newStatus);
        try {
            doctorService.updateDoctor(currentDoctor);
            loadDoctorProfileData();
            String msg = newStatus ? "You are now marked AVAILABLE for consultations." : "You are now marked ON LEAVE.";
            JOptionPane.showMessageDialog(this, msg, "Availability Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating availability: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out of the Doctor Portal?",
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
}
