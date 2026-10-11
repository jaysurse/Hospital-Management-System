package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.ui.dialogs.AppointmentDialog;

/**
 * Hospital Dashboard Panel displaying real-time live metrics and today's schedule.
 * All information is retrieved dynamically from MySQL / JDBC DAOs.
 */
public class DashboardPanel extends JPanel {

    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;
    private final AppointmentDAO appointmentDAO;
    private final BillDAO billDAO;
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final MainDashboardFrame parentFrame;

    private JPanel statPatients;
    private JPanel statDoctors;
    private JPanel statAppointments;
    private JPanel statPendingBills;
    private JPanel statsContainer;

    private DefaultTableModel tableModel;
    private JTable appointmentsTable;

    public DashboardPanel(MainDashboardFrame parentFrame,
                          PatientDAO patientDAO, DoctorDAO doctorDAO, 
                          AppointmentDAO appointmentDAO, BillDAO billDAO,
                          AppointmentService appointmentService,
                          PatientService patientService,
                          DoctorService doctorService) {
        this.parentFrame = parentFrame;
        this.patientDAO = patientDAO;
        this.doctorDAO = doctorDAO;
        this.appointmentDAO = appointmentDAO;
        this.billDAO = billDAO;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        refreshDashboard();
    }

    private void initComponents() {
        // Top: Statistics Cards Row
        statsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        statsContainer.setBackground(Theme.BG);

        statPatients = UIHelper.createStatCard("Total Patients", "0", "Registered Inpatients/OPD", Theme.PRIMARY);
        statDoctors = UIHelper.createStatCard("Active Physicians", "0", "Consulting Medical Staff", Theme.SECONDARY);
        statAppointments = UIHelper.createStatCard("Today's Consultations", "0", "Scheduled Today", Theme.WARNING);
        statPendingBills = UIHelper.createStatCard("Pending Invoices", "0", "Unsettled Patient Bills", Theme.ERROR);

        statsContainer.add(statPatients);
        statsContainer.add(statDoctors);
        statsContainer.add(statAppointments);
        statsContainer.add(statPendingBills);

        add(statsContainer, BorderLayout.NORTH);

        // Center: Today's Appointments Table & Header
        JPanel centerCard = UIHelper.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 12));

        // Table Header & Action Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Theme.SURFACE);

        JPanel titleBlock = new JPanel(new BorderLayout(0, 2));
        titleBlock.setBackground(Theme.SURFACE);
        JLabel lblTableTitle = new JLabel("Today's Scheduled Consultations");
        lblTableTitle.setFont(Theme.FONT_HEADER);
        lblTableTitle.setForeground(Theme.PRIMARY);
        JLabel lblTableSub = new JLabel("Real-time clinical queue synchronized with MySQL database");
        lblTableSub.setFont(Theme.FONT_SMALL);
        lblTableSub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(lblTableTitle, BorderLayout.NORTH);
        titleBlock.add(lblTableSub, BorderLayout.SOUTH);
        toolbar.add(titleBlock, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Theme.SURFACE);

        JButton btnBook = UIHelper.createPrimaryButton("+ Book Appointment");
        btnBook.addActionListener(e -> {
            AppointmentDialog dialog = new AppointmentDialog(parentFrame, appointmentService, patientService, doctorService, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshDashboard();
            }
        });

        JButton btnRefresh = UIHelper.createOutlineButton("Refresh");
        btnRefresh.addActionListener(e -> refreshDashboard());

        actions.add(btnBook);
        actions.add(btnRefresh);
        toolbar.add(actions, BorderLayout.EAST);

        centerCard.add(toolbar, BorderLayout.NORTH);

        // Appointments JTable
        String[] columns = {"Appt ID", "Patient Name", "Consulting Doctor", "Specialization", "Date & Time", "Status", "Chief Complaint"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        appointmentsTable = UIHelper.createTable(tableModel);
        appointmentsTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        appointmentsTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        appointmentsTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        appointmentsTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        appointmentsTable.getColumnModel().getColumn(4).setPreferredWidth(160);
        appointmentsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        appointmentsTable.getColumnModel().getColumn(6).setPreferredWidth(220);

        appointmentsTable.getColumnModel().getColumn(5).setCellRenderer(UIHelper.createStatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        add(centerCard, BorderLayout.CENTER);
    }

    public void refreshDashboard() {
        try {
            int patients = patientDAO.count();
            int doctors = doctorDAO.count();
            int todayAppts = appointmentDAO.countTodayAppointments();
            int pendingBills = billDAO.countPendingBills();

            // Update stats cards
            updateStatCard(statPatients, "Total Patients", String.valueOf(patients), "Registered Inpatients/OPD", Theme.PRIMARY);
            updateStatCard(statDoctors, "Active Physicians", String.valueOf(doctors), "Consulting Medical Staff", Theme.SECONDARY);
            updateStatCard(statAppointments, "Today's Consultations", String.valueOf(todayAppts), "Scheduled Today", Theme.WARNING);
            updateStatCard(statPendingBills, "Pending Invoices", String.valueOf(pendingBills), "Unsettled Patient Bills", Theme.ERROR);

            // Populate table
            tableModel.setRowCount(0);
            List<Appointment> list = appointmentDAO.findTodayAppointments();
            if (list.isEmpty()) {
                // If no today appointments, fall back to recent appointments so table is populated
                list = appointmentDAO.findAll();
            }

            for (Appointment a : list) {
                tableModel.addRow(new Object[]{
                    "#" + a.getId(),
                    a.getPatientName(),
                    a.getDoctorName(),
                    a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "-",
                    a.getFormattedDateTime(),
                    a.getStatus().name(),
                    a.getReason()
                });
            }
        } catch (Exception e) {
            System.err.println("[DashboardPanel] Refresh error: " + e.getMessage());
        }
    }

    private void updateStatCard(JPanel card, String label, String value, String subtext, java.awt.Color color) {
        card.removeAll();
        card.setLayout(null);

        JLabel lblTitle = new JLabel(label.toUpperCase());
        lblTitle.setFont(Theme.FONT_SMALL_BOLD);
        lblTitle.setForeground(Theme.TEXT_MUTED);
        lblTitle.setBounds(18, 12, 200, 16);
        card.add(lblTitle);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new java.awt.Font(Theme.FONT_TITLE.getName(), java.awt.Font.BOLD, 24));
        lblValue.setForeground(Theme.TEXT_MAIN);
        lblValue.setBounds(18, 32, 200, 32);
        card.add(lblValue);

        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.TEXT_MUTED);
        lblSub.setBounds(18, 66, 200, 16);
        card.add(lblSub);

        card.revalidate();
        card.repaint();
    }
}
