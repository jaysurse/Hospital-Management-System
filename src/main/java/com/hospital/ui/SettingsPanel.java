package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import com.hospital.config.AppConfig;
import com.hospital.config.HospitalConstants;
import com.hospital.dao.DBConnection;
import com.hospital.dao.DatabaseInitializer;
import com.hospital.dao.UserDAO;
import com.hospital.model.User;
import com.hospital.service.AuthService;

/**
 * Hospital System Configuration, Database Diagnostics, and Account Security Panel.
 */
public class SettingsPanel extends JPanel {

    private final MainDashboardFrame parentFrame;
    private final AuthService authService;
    private final UserDAO userDAO;

    private JLabel lblDbStatus;
    private JLabel lblDbType;
    private JLabel lblUserSession;
    private JLabel lblUserRole;

    public SettingsPanel(MainDashboardFrame parentFrame, AuthService authService) {
        this.parentFrame = parentFrame;
        this.authService = authService;
        this.userDAO = new UserDAO();

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        refreshStatus();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel(new GridBagLayout());
        scrollContent.setBackground(Theme.BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        int row = 0;

        // Section 1: Database & Persistence Engine
        JPanel dbCard = UIHelper.createCardPanel();
        dbCard.setLayout(new BorderLayout(0, 10));
        dbCard.setBorder(new TitledBorder(new LineBorder(Theme.BORDER), "Database Engine & Persistence Diagnostics", TitledBorder.LEFT, TitledBorder.TOP, Theme.FONT_HEADER, Theme.PRIMARY));

        JPanel dbInfo = new JPanel(new GridBagLayout());
        dbInfo.setBackground(Theme.SURFACE);
        GridBagConstraints gbcDb = new GridBagConstraints();
        gbcDb.insets = new Insets(6, 6, 6, 6);
        gbcDb.fill = GridBagConstraints.HORIZONTAL;

        lblDbStatus = new JLabel("Checking connection...");
        lblDbStatus.setFont(Theme.FONT_SUBHEADER);
        lblDbStatus.setForeground(Theme.SUCCESS);

        lblDbType = new JLabel(DBConnection.getInstance().getActiveDatabaseInfo());
        lblDbType.setFont(Theme.FONT_REGULAR);
        lblDbType.setForeground(Theme.TEXT_MAIN);

        addGridRow(dbInfo, gbcDb, 0, "Connection State:", lblDbStatus);
        addGridRow(dbInfo, gbcDb, 1, "Active Database:", lblDbType);

        JPanel dbActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        dbActions.setBackground(Theme.SURFACE);

        JButton btnTestDb = UIHelper.createPrimaryButton("Test Connection");
        btnTestDb.addActionListener(e -> testDbConnection());

        JButton btnReconnect = UIHelper.createSecondaryButton("Reconnect Database");
        btnReconnect.addActionListener(e -> reconnectDb());

        JButton btnResetDb = UIHelper.createDangerButton("Reset Demo Seed Data");
        btnResetDb.addActionListener(e -> resetDatabase());

        dbActions.add(btnTestDb);
        dbActions.add(btnReconnect);
        dbActions.add(btnResetDb);

        dbCard.add(dbInfo, BorderLayout.CENTER);
        dbCard.add(dbActions, BorderLayout.SOUTH);

        gbc.gridy = row++;
        scrollContent.add(dbCard, gbc);

        // Section 2: User Account & Security
        JPanel userCard = UIHelper.createCardPanel();
        userCard.setLayout(new BorderLayout(0, 10));
        userCard.setBorder(new TitledBorder(new LineBorder(Theme.BORDER), "User Authentication & Credentials", TitledBorder.LEFT, TitledBorder.TOP, Theme.FONT_HEADER, Theme.PRIMARY));

        JPanel userInfo = new JPanel(new GridBagLayout());
        userInfo.setBackground(Theme.SURFACE);
        GridBagConstraints gbcUser = new GridBagConstraints();
        gbcUser.insets = new Insets(6, 6, 6, 6);
        gbcUser.fill = GridBagConstraints.HORIZONTAL;

        lblUserSession = new JLabel("Administrator");
        lblUserSession.setFont(Theme.FONT_SUBHEADER);
        lblUserSession.setForeground(Theme.PRIMARY);

        lblUserRole = new JLabel("ADMIN");
        lblUserRole.setFont(Theme.FONT_REGULAR);

        addGridRow(userInfo, gbcUser, 0, "Logged-In User:", lblUserSession);
        addGridRow(userInfo, gbcUser, 1, "Access Role:", lblUserRole);

        JPanel userActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        userActions.setBackground(Theme.SURFACE);

        JButton btnChangePassword = UIHelper.createPrimaryButton("Change Password");
        btnChangePassword.addActionListener(e -> promptChangePassword());

        JButton btnLogout = UIHelper.createOutlineButton("Logout");
        btnLogout.addActionListener(e -> parentFrame.performLogout());

        userActions.add(btnChangePassword);
        userActions.add(btnLogout);

        userCard.add(userInfo, BorderLayout.CENTER);
        userCard.add(userActions, BorderLayout.SOUTH);

        gbc.gridy = row++;
        scrollContent.add(userCard, gbc);

        // Section 3: Academic Viva & OOP Architecture Reference
        JPanel oopCard = UIHelper.createCardPanel();
        oopCard.setLayout(new BorderLayout(0, 8));
        oopCard.setBorder(new TitledBorder(new LineBorder(Theme.BORDER), "Academic OOP Course Implementation Reference", TitledBorder.LEFT, TitledBorder.TOP, Theme.FONT_HEADER, Theme.PRIMARY));

        JTextArea txtOop = new JTextArea(8, 60);
        txtOop.setEditable(false);
        txtOop.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        txtOop.setBackground(new java.awt.Color(0xF8, 0xFA, 0xFC));
        txtOop.setForeground(Theme.TEXT_MAIN);
        txtOop.setText(
            "========================================================================================\n" +
            "  OBJECT-ORIENTED PROGRAMMING (OOP) PRINCIPLES IMPLEMENTED IN THIS SYSTEM\n" +
            "========================================================================================\n" +
            "1. Encapsulation        : Private fields with validated getters/setters across all models.\n" +
            "2. Inheritance          : Abstract Person superclass extended by Patient and Doctor.\n" +
            "3. Abstraction          : Abstract methods getDetails(), getRole(), GenericDAO<T, ID>.\n" +
            "4. Method Overloading   : Overloaded constructors in Patient/Doctor, overloaded search() in DAOs.\n" +
            "5. Method Overriding    : Polymorphic getDetails(), getRole(), toString() across subclasses.\n" +
            "6. Runtime Polymorphism : Person references invoke overridden subclass behaviors dynamically.\n" +
            "7. Association          : Patient, Doctor, and Appointment interconnected.\n" +
            "8. Aggregation          : Appointment aggregates independent Patient and Doctor instances.\n" +
            "9. Composition          : Bill composes BillItem line items; cannot exist without Bill.\n" +
            "10. Static Members      : System-wide HospitalConstants, Theme palette, AppConfig.\n" +
            "11. Exception Handling  : Hierarchical custom exceptions (InvalidPatientException, etc.).\n" +
            "12. Generics            : GenericDAO<T, ID>, Identifiable<T>, List<T>, Map<K, V>.\n" +
            "13. File I/O Handling   : Character stream report export (CSV and TXT) via FileUtil.\n" +
            "14. Singleton Pattern   : DBConnection, AppConfig, and AuthService manage single instances.\n" +
            "15. Iterator Pattern    : Explicit Iterator<T> traversals in ReportService and FileUtil.\n" +
            "16. Adapter Pattern     : SMSNotificationAdapter adapts incompatible third-party SMS API.\n" +
            "========================================================================================"
        );

        JScrollPane scrollOop = new JScrollPane(txtOop);
        scrollOop.setBorder(new LineBorder(Theme.BORDER));
        oopCard.add(scrollOop, BorderLayout.CENTER);

        gbc.gridy = row++;
        scrollContent.add(oopCard, gbc);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);
    }

    private void addGridRow(JPanel panel, GridBagConstraints gbc, int row, String label, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.25;
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SUBHEADER);
        l.setForeground(Theme.TEXT_MAIN);
        panel.add(l, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.75;
        panel.add(valueLabel, gbc);
    }

    public void refreshStatus() {
        boolean valid = DBConnection.getInstance().testConnection();
        if (valid) {
            lblDbStatus.setText("Connected & Operational (Healthy)");
            lblDbStatus.setForeground(Theme.SUCCESS);
        } else {
            lblDbStatus.setText("Connection Failed / Offline");
            lblDbStatus.setForeground(Theme.ERROR);
        }
        lblDbType.setText(DBConnection.getInstance().getActiveDatabaseInfo());

        User current = authService.getCurrentUser();
        if (current != null) {
            lblUserSession.setText(current.getFullName() + " (" + current.getUsername() + ")");
            lblUserRole.setText(current.getRole().getDisplayName());
        }
    }

    private void testDbConnection() {
        boolean valid = DBConnection.getInstance().testConnection();
        if (valid) {
            JOptionPane.showMessageDialog(this, 
                "Database connection test successful!\nActive DB: " + DBConnection.getInstance().getActiveDatabaseInfo(), 
                "Connection Test", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Database connection test failed.\nPlease check your MySQL service and db.properties settings.", 
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshStatus();
    }

    private void reconnectDb() {
        DBConnection.getInstance().reconnect();
        refreshStatus();
        parentFrame.refreshDashboardMetrics();
        JOptionPane.showMessageDialog(this, "Database reconnected.\nActive: " + DBConnection.getInstance().getActiveDatabaseInfo(), "Reconnect", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to reset all tables and re-apply standard seed data from hospital_management.sql?\n" +
            "Any custom records added during this session will be reset to factory demonstration data.",
            "Confirm Database Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getInstance().getConnection()) {
                DatabaseInitializer.initializeDatabase(conn);
                refreshStatus();
                parentFrame.refreshDashboardMetrics();
                JOptionPane.showMessageDialog(this, "Database schema and demonstration seed data reloaded successfully!", "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Reset failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void promptChangePassword() {
        User user = authService.getCurrentUser();
        if (user == null) return;

        JPanel p = new JPanel(new java.awt.GridLayout(3, 2, 8, 8));
        JPasswordField pfCurrent = new JPasswordField();
        JPasswordField pfNew = new JPasswordField();
        JPasswordField pfConfirm = new JPasswordField();

        p.add(new JLabel("Current Password:"));
        p.add(pfCurrent);
        p.add(new JLabel("New Password:"));
        p.add(pfNew);
        p.add(new JLabel("Confirm New Password:"));
        p.add(pfConfirm);

        int res = JOptionPane.showConfirmDialog(this, p, "Change Password - " + user.getUsername(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String curr = new String(pfCurrent.getPassword()).trim();
            String newPwd = new String(pfNew.getPassword()).trim();
            String conf = new String(pfConfirm.getPassword()).trim();

            if (curr.isEmpty() || newPwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be blank.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPwd.equals(conf)) {
                JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newPwd.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Verify current password
                User verified = userDAO.authenticate(user.getUsername(), curr);
                if (verified == null) {
                    JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                userDAO.updatePassword(user.getUserId(), newPwd);
                JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error updating password: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
