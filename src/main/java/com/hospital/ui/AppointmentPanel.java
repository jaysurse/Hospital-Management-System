package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.ui.dialogs.AppointmentDialog;
import com.hospital.util.DateUtil;

/**
 * Clinical Appointment Management Panel.
 * Handles consultation scheduling, doctor conflict prevention, and appointment lifecycle.
 */
public class AppointmentPanel extends JPanel {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final MainDashboardFrame parentFrame;

    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;
    private DefaultTableModel tableModel;
    private JTable appointmentsTable;
    private List<Appointment> currentAppointments;

    public AppointmentPanel(MainDashboardFrame parentFrame,
                            AppointmentService appointmentService, 
                            PatientService patientService, 
                            DoctorService doctorService) {
        this.parentFrame = parentFrame;
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        loadAppointments();
    }

    private void initComponents() {
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Theme.SURFACE);

        // Filters (Left)
        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setBackground(Theme.SURFACE);

        JLabel lblFilter = new JLabel("Status Filter:");
        lblFilter.setFont(Theme.FONT_SUBHEADER);
        lblFilter.setForeground(Theme.TEXT_MAIN);

        cbStatusFilter = UIHelper.createComboBox(new String[]{"All Statuses", "Scheduled", "Completed", "Cancelled"});
        cbStatusFilter.addActionListener(e -> performSearch());

        txtSearch = UIHelper.createTextField();
        txtSearch.setPreferredSize(new Dimension(180, 32));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = UIHelper.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = UIHelper.createOutlineButton("Clear");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbStatusFilter.setSelectedIndex(0);
            loadAppointments();
        });

        filterBox.add(lblFilter);
        filterBox.add(cbStatusFilter);
        filterBox.add(txtSearch);
        filterBox.add(btnSearch);
        filterBox.add(btnReset);
        toolbar.add(filterBox, BorderLayout.WEST);

        // Actions (Right)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Theme.SURFACE);

        JButton btnBook = UIHelper.createPrimaryButton("+ Book Appointment");
        btnBook.addActionListener(e -> openBookDialog());

        JButton btnEdit = UIHelper.createOutlineButton("Reschedule / Edit");
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnComplete = UIHelper.createSuccessButton("Mark Completed");
        btnComplete.addActionListener(e -> markCompleted());

        JButton btnCancel = UIHelper.createDangerButton("Cancel Appt");
        btnCancel.addActionListener(e -> cancelAppointment());

        actions.add(btnBook);
        actions.add(btnEdit);
        actions.add(btnComplete);
        actions.add(btnCancel);
        toolbar.add(actions, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {
            "Appt ID", "Patient Name", "Consulting Doctor", "Specialization", "Consultation Date", "Time Slot", "Status", "Reason / Chief Complaint", "Clinical Notes"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        appointmentsTable = UIHelper.createTable(tableModel);
        appointmentsTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        appointmentsTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        appointmentsTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        appointmentsTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        appointmentsTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        appointmentsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        appointmentsTable.getColumnModel().getColumn(6).setPreferredWidth(95);
        appointmentsTable.getColumnModel().getColumn(7).setPreferredWidth(210);
        appointmentsTable.getColumnModel().getColumn(8).setPreferredWidth(160);

        appointmentsTable.getColumnModel().getColumn(6).setCellRenderer(UIHelper.createStatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadAppointments() {
        try {
            currentAppointments = appointmentService.getAllAppointments();
            populateTable(currentAppointments);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load appointments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        String status = (String) cbStatusFilter.getSelectedItem();
        try {
            currentAppointments = appointmentService.searchAppointments(query, status, null, null);
            populateTable(currentAppointments);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Appointment> list) {
        tableModel.setRowCount(0);
        for (Appointment a : list) {
            tableModel.addRow(new Object[]{
                "#" + a.getId(),
                a.getPatientName(),
                a.getDoctorName(),
                a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "-",
                DateUtil.formatDate(a.getAppointmentDate()),
                DateUtil.formatTime(a.getAppointmentTime()),
                a.getStatus().name(),
                a.getReason(),
                a.getNotes() != null ? a.getNotes() : "-"
            });
        }
    }

    private void openBookDialog() {
        AppointmentDialog dialog = new AppointmentDialog(parentFrame, appointmentService, patientService, doctorService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadAppointments();
            parentFrame.refreshDashboardMetrics();
        }
    }

    private void openEditDialog() {
        Appointment selected = getSelectedAppointment();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table to reschedule or edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AppointmentDialog dialog = new AppointmentDialog(parentFrame, appointmentService, patientService, doctorService, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadAppointments();
            parentFrame.refreshDashboardMetrics();
        }
    }

    private void markCompleted() {
        Appointment selected = getSelectedAppointment();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to mark as Completed.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.getStatus() == AppointmentStatus.Completed) {
            JOptionPane.showMessageDialog(this, "This consultation is already marked as Completed.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            appointmentService.completeAppointment(selected.getId());
            loadAppointments();
            parentFrame.refreshDashboardMetrics();
            JOptionPane.showMessageDialog(this, "Consultation marked as Completed.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating appointment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelAppointment() {
        Appointment selected = getSelectedAppointment();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.getStatus() == AppointmentStatus.Cancelled) {
            JOptionPane.showMessageDialog(this, "This appointment is already cancelled.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel appointment #" + selected.getId() + " for " + selected.getPatientName() + "?\n" +
            "The doctor's time slot will be immediately freed for new bookings.",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                appointmentService.cancelAppointment(selected.getId());
                loadAppointments();
                parentFrame.refreshDashboardMetrics();
                JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to cancel appointment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Appointment getSelectedAppointment() {
        int row = appointmentsTable.getSelectedRow();
        if (row < 0 || currentAppointments == null || row >= currentAppointments.size()) {
            return null;
        }
        return currentAppointments.get(row);
    }
}
