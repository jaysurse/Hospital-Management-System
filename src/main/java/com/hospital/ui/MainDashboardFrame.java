package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import com.hospital.config.HospitalConstants;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillDAO;
import com.hospital.dao.DBConnection;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.AuthService;
import com.hospital.service.BillingService;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import com.hospital.service.ReportService;

/**
 * Main Hospital Administration Desktop Window.
 * Implements clinical layout with Deep Blue Sidebar, Top Header, and CardLayout views.
 */
public class MainDashboardFrame extends JFrame {

    private final AuthService authService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final BillingService billingService;
    private final ReportService reportService;

    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;
    private final AppointmentDAO appointmentDAO;
    private final BillDAO billDAO;
    private final MedicalRecordDAO medicalRecordDAO;

    // UI Panels & Layout
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JLabel lblPageTitle;
    private JLabel lblBreadcrumb;
    private JLabel lblLiveDateTime;
    private JLabel lblDbStatusBadge;

    private DashboardPanel dashboardPanel;
    private PatientPanel patientPanel;
    private DoctorPanel doctorPanel;
    private AppointmentPanel appointmentPanel;
    private MedicalRecordPanel medicalRecordPanel;
    private BillingPanel billingPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    private final List<NavButton> navButtons = new ArrayList<>();
    private String currentCard = "dashboard";
    protected final boolean receptionistMode;

    public MainDashboardFrame() {
        this(false);
    }

    public MainDashboardFrame(boolean receptionistMode) {
        this.receptionistMode = receptionistMode;
        this.authService = AuthService.getInstance();
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.billDAO = new BillDAO();
        this.medicalRecordDAO = new MedicalRecordDAO();

        this.patientService = new PatientService(patientDAO);
        this.doctorService = new DoctorService(doctorDAO);
        this.appointmentService = new AppointmentService(appointmentDAO, doctorDAO, patientDAO, null);
        this.medicalRecordService = new MedicalRecordService(medicalRecordDAO, patientDAO, doctorDAO);
        this.billingService = new BillingService(billDAO, patientDAO, null);
        this.reportService = new ReportService(patientDAO, doctorDAO, appointmentDAO, billDAO);

        initFrame();
    }

    private void initFrame() {
        if (receptionistMode) {
            setTitle(HospitalConstants.HOSPITAL_NAME + " - Front Desk & Reception Portal");
        } else {
            setTitle(HospitalConstants.HOSPITAL_NAME + " - " + HospitalConstants.APP_NAME);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel rootPane = new JPanel(new BorderLayout());
        rootPane.setBackground(Theme.BG);

        // 1. Left Sidebar
        rootPane.add(createSidebar(), BorderLayout.WEST);

        // 2. Right Workspace (Header + Main Cards)
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(Theme.BG);
        workspace.add(createTopHeader(), BorderLayout.NORTH);

        // 3. CardLayout Content
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Theme.BG);

        dashboardPanel = new DashboardPanel(this, patientDAO, doctorDAO, appointmentDAO, billDAO,
                                           appointmentService, patientService, doctorService);
        patientPanel = new PatientPanel(this, patientService, medicalRecordService);
        doctorPanel = new DoctorPanel(this, doctorService);
        appointmentPanel = new AppointmentPanel(this, appointmentService, patientService, doctorService);
        medicalRecordPanel = new MedicalRecordPanel(this, medicalRecordService, patientService, doctorService);
        billingPanel = new BillingPanel(this, billingService, patientService, doctorService);
        reportsPanel = new ReportsPanel(this, reportService);
        settingsPanel = new SettingsPanel(this, authService);

        mainContentPanel.add(dashboardPanel, "dashboard");
        mainContentPanel.add(patientPanel, "patients");
        mainContentPanel.add(doctorPanel, "doctors");
        mainContentPanel.add(appointmentPanel, "appointments");
        mainContentPanel.add(medicalRecordPanel, "records");
        mainContentPanel.add(billingPanel, "billing");
        mainContentPanel.add(reportsPanel, "reports");
        mainContentPanel.add(settingsPanel, "settings");

        workspace.add(mainContentPanel, BorderLayout.CENTER);
        rootPane.add(workspace, BorderLayout.CENTER);

        setContentPane(rootPane);

        // Start Clock Timer
        Timer clockTimer = new Timer(1000, e -> updateDateTime());
        clockTimer.start();
        updateDateTime();

        switchView("dashboard", "HOSPITAL CLINICAL DASHBOARD", "Hospital Administration > Overview");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(0x15, 0x37, 0x4A)));

        // Top Brand Header
        JPanel brandPanel = new JPanel(new BorderLayout(12, 0));
        brandPanel.setBackground(Theme.SIDEBAR_BG);
        brandPanel.setBorder(new EmptyBorder(22, 18, 22, 18));

        JLabel lblLogo = new JLabel(UIHelper.createMedicalCrossIcon(26, receptionistMode ? Theme.RECEPTIONIST_ORANGE : Color.WHITE));
        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 2));
        brandText.setBackground(Theme.SIDEBAR_BG);

        JLabel lblHospital = new JLabel(receptionistMode ? "CITY GENERAL" : "METROPOLITAN");
        lblHospital.setFont(Theme.FONT_HEADER);
        lblHospital.setForeground(Color.WHITE);

        JLabel lblTag = new JLabel(receptionistMode ? "Front Desk Portal" : "Healthcare ERP v1.0");
        lblTag.setFont(Theme.FONT_SMALL);
        lblTag.setForeground(receptionistMode ? Theme.RECEPTIONIST_ORANGE : Theme.SIDEBAR_TEXT_MUTED);

        brandText.add(lblHospital);
        brandText.add(lblTag);

        brandPanel.add(lblLogo, BorderLayout.WEST);
        brandPanel.add(brandText, BorderLayout.CENTER);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Center Navigation Items
        JPanel navContainer = new JPanel();
        navContainer.setLayout(new javax.swing.BoxLayout(navContainer, javax.swing.BoxLayout.Y_AXIS));
        navContainer.setBackground(Theme.SIDEBAR_BG);
        navContainer.setBorder(new EmptyBorder(8, 0, 8, 0));

        addNavItem(navContainer, "Dashboard", "dashboard", "HOSPITAL CLINICAL DASHBOARD", "Hospital Administration > Overview");
        addNavItem(navContainer, "Patients", "patients", "PATIENT DIRECTORY & PROFILES", "Hospital Administration > Patient Management");
        addNavItem(navContainer, "Doctors", "doctors", "CONSULTING PHYSICIANS & OPD", "Hospital Administration > Doctor Staff");
        addNavItem(navContainer, "Appointments", "appointments", "APPOINTMENT SCHEDULING & QUEUE", "Clinical Services > Appointment Scheduling");
        if (!receptionistMode) {
            addNavItem(navContainer, "Medical Records", "records", "DIAGNOSTIC & CLINICAL RECORDS", "Clinical Services > Medical History & Diagnoses");
        }
        addNavItem(navContainer, "Billing & Invoices", "billing", "HOSPITAL BILLING & INVOICING", "Accounts & Finance > Patient Billing");
        addNavItem(navContainer, "Reports & Audit", "reports", "CLINICAL & FINANCIAL REPORTS", "Analytics & Reporting > Performance Audit");
        if (!receptionistMode) {
            addNavItem(navContainer, "System Settings", "settings", "SYSTEM SETTINGS & DIAGNOSTICS", "Administration > System Configuration");
        }

        sidebar.add(navContainer, BorderLayout.CENTER);

        // Bottom User Session Profile
        JPanel userPanel = new JPanel(new BorderLayout(8, 0));
        userPanel.setBackground(new Color(0x15, 0x36, 0x48));
        userPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        userInfo.setBackground(new Color(0x15, 0x36, 0x48));

        User user = authService.getCurrentUser();
        String name = user != null ? user.getFullName() : (receptionistMode ? "Front Desk" : "Administrator");
        String role = user != null ? user.getRole().getDisplayName() : (receptionistMode ? "Receptionist" : "Admin");

        JLabel lblName = new JLabel(name);
        lblName.setFont(Theme.FONT_SUBHEADER);
        lblName.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel(receptionistMode ? "FRONT DESK OFFICER" : role.toUpperCase());
        lblRole.setFont(Theme.FONT_SMALL_BOLD);
        lblRole.setForeground(receptionistMode ? Theme.RECEPTIONIST_ORANGE : Theme.SECONDARY);

        userInfo.add(lblName);
        userInfo.add(lblRole);
        userPanel.add(userInfo, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Exit");
        btnLogout.setFont(Theme.FONT_SMALL_BOLD);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(Theme.ERROR);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> performLogout());
        userPanel.add(btnLogout, BorderLayout.EAST);

        sidebar.add(userPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel container, String title, String cardKey, String pageTitle, String breadcrumb) {
        NavButton btn = new NavButton(title, cardKey, pageTitle, breadcrumb);
        navButtons.add(btn);
        container.add(btn);
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setPreferredSize(new Dimension(0, 64));
        header.setBackground(Theme.SURFACE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setBorder(new javax.swing.border.CompoundBorder(
            new MatteBorder(0, 0, 1, 0, Theme.BORDER),
            new EmptyBorder(10, 24, 10, 24)
        ));

        // Left Titles
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setBackground(Theme.SURFACE);

        lblPageTitle = new JLabel("HOSPITAL CLINICAL DASHBOARD");
        lblPageTitle.setFont(Theme.FONT_TITLE);
        lblPageTitle.setForeground(Theme.PRIMARY);

        lblBreadcrumb = new JLabel("Hospital Administration > Overview");
        lblBreadcrumb.setFont(Theme.FONT_SMALL);
        lblBreadcrumb.setForeground(Theme.TEXT_MUTED);

        titleBox.add(lblPageTitle);
        titleBox.add(lblBreadcrumb);
        header.add(titleBox, BorderLayout.WEST);

        // Right Meta: Date/Time + Database Badge
        JPanel metaBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 4));
        metaBox.setBackground(Theme.SURFACE);

        lblDbStatusBadge = new JLabel(DBConnection.getInstance().isUsingFallback() ? "Standalone Demo Mode" : "MySQL Connected");
        lblDbStatusBadge.setFont(Theme.FONT_SMALL_BOLD);
        lblDbStatusBadge.setForeground(DBConnection.getInstance().isUsingFallback() ? Theme.WARNING : Theme.SUCCESS);
        lblDbStatusBadge.setBorder(new javax.swing.border.CompoundBorder(
            new javax.swing.border.LineBorder(DBConnection.getInstance().isUsingFallback() ? Theme.WARNING : Theme.SUCCESS, 1, true),
            new EmptyBorder(3, 8, 3, 8)
        ));

        lblLiveDateTime = new JLabel();
        lblLiveDateTime.setFont(Theme.FONT_REGULAR);
        lblLiveDateTime.setForeground(Theme.TEXT_MAIN);

        JButton btnQuickRefresh = UIHelper.createOutlineButton("↻ Sync");
        btnQuickRefresh.addActionListener(e -> refreshActiveView());

        metaBox.add(lblDbStatusBadge);
        metaBox.add(lblLiveDateTime);
        metaBox.add(btnQuickRefresh);

        header.add(metaBox, BorderLayout.EAST);

        return header;
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy | hh:mm:ss a");
        lblLiveDateTime.setText(now.format(fmt));
    }

    public void switchView(String cardKey, String pageTitle, String breadcrumb) {
        this.currentCard = cardKey;
        cardLayout.show(mainContentPanel, cardKey);
        lblPageTitle.setText(pageTitle);
        lblBreadcrumb.setText(breadcrumb);

        for (NavButton btn : navButtons) {
            btn.setActive(btn.cardKey.equals(cardKey));
        }

        refreshActiveView();
    }

    public void refreshActiveView() {
        if ("dashboard".equals(currentCard)) {
            dashboardPanel.refreshDashboard();
        } else if ("patients".equals(currentCard)) {
            patientPanel.loadPatients();
        } else if ("doctors".equals(currentCard)) {
            doctorPanel.loadDoctors();
        } else if ("appointments".equals(currentCard)) {
            appointmentPanel.loadAppointments();
        } else if ("records".equals(currentCard)) {
            medicalRecordPanel.loadRecords();
        } else if ("billing".equals(currentCard)) {
            billingPanel.loadBills();
        } else if ("reports".equals(currentCard)) {
            reportsPanel.generateSelectedReport();
        } else if ("settings".equals(currentCard)) {
            settingsPanel.refreshStatus();
        }
    }

    public void refreshDashboardMetrics() {
        dashboardPanel.refreshDashboard();
        billingPanel.loadBills();
    }

    public void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out of the Hospital Management System?",
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

    /**
     * Inner class representing a clinical sidebar navigation button with active indicator.
     */
    private class NavButton extends JPanel {
        private final String title;
        private final String cardKey;
        private final String pageTitle;
        private final String breadcrumb;
        private boolean active = false;
        private boolean hovered = false;

        NavButton(String title, String cardKey, String pageTitle, String breadcrumb) {
            this.title = title;
            this.cardKey = cardKey;
            this.pageTitle = pageTitle;
            this.breadcrumb = breadcrumb;

            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(240, 42));
            setMaximumSize(new Dimension(240, 42));
            setBackground(Theme.SIDEBAR_BG);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lbl = new JLabel("  " + title);
            lbl.setFont(Theme.FONT_SUBHEADER);
            lbl.setForeground(Theme.SIDEBAR_TEXT);
            lbl.setBorder(new EmptyBorder(0, 16, 0, 0));
            add(lbl, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    switchView(cardKey, pageTitle, breadcrumb);
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                g2.setColor(Theme.SIDEBAR_ACTIVE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Left 4px highlight bar
                g2.setColor(receptionistMode ? Theme.RECEPTIONIST_ORANGE : Theme.SECONDARY);
                g2.fillRect(0, 0, 4, getHeight());
            } else if (hovered) {
                g2.setColor(Theme.SIDEBAR_HOVER);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
        }
    }
}
