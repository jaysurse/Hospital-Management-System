package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import com.hospital.config.HospitalConstants;
import com.hospital.dao.DBConnection;
import com.hospital.model.Role;

/**
 * First Screen — Select Your Role
 * 
 * Provides an attractive, professional welcome screen with three large cards:
 * 1. Doctor (Teal theme #0D9488)
 * 2. Patient (Green theme #10B981)
 * 3. Receptionist (Orange theme #F59E0B)
 * 
 * Also includes direct access for System Administrator.
 */
public class RoleSelectionFrame extends JFrame {

    public RoleSelectionFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle(HospitalConstants.HOSPITAL_NAME + " - Select Role");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 680);
        setMinimumSize(new Dimension(920, 600));
        setLocationRelativeTo(null);

        JPanel rootPane = new JPanel(new BorderLayout());
        rootPane.setBackground(Theme.BG);

        // 1. Top Clinical Header Banner (Navy Blue Theme)
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setBackground(Theme.NAVY_BLUE);
        headerPanel.setBorder(new EmptyBorder(22, 32, 22, 32));

        // Medical Cross Emblem
        JLabel lblLogo = new JLabel(UIHelper.createMedicalCrossIcon(40, Color.WHITE));

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
        headerText.setBackground(Theme.NAVY_BLUE);

        JLabel lblTitle = new JLabel(HospitalConstants.HOSPITAL_NAME);
        lblTitle.setFont(new Font(Theme.FONT_TITLE.getName(), Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("City General Healthcare Network • Enterprise Hospital Management Portal");
        lblSubtitle.setFont(Theme.FONT_SMALL);
        lblSubtitle.setForeground(new Color(0xBD, 0xD4, 0xE0));

        headerText.add(lblTitle);
        headerText.add(lblSubtitle);

        headerPanel.add(lblLogo, BorderLayout.WEST);
        headerPanel.add(headerText, BorderLayout.CENTER);

        // Database status pill badge
        String dbText = DBConnection.getInstance().isUsingFallback() ? "Standalone Demo Mode" : "MySQL Connected";
        JLabel lblDbBadge = new JLabel("  " + dbText + "  ");
        lblDbBadge.setFont(Theme.FONT_SMALL_BOLD);
        lblDbBadge.setForeground(Color.WHITE);
        lblDbBadge.setBackground(new Color(0x2A, 0x55, 0x8C));
        lblDbBadge.setOpaque(true);
        lblDbBadge.setBorder(new LineBorder(new Color(0x4A, 0x75, 0xAC), 1, true));
        headerPanel.add(lblDbBadge, BorderLayout.EAST);

        rootPane.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content: Instruction + Three Large Role Cards
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setBackground(Theme.BG);
        centerPanel.setBorder(new EmptyBorder(24, 32, 16, 32));

        // Greeting and Instruction
        JPanel greetingPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        greetingPanel.setBackground(Theme.BG);

        JLabel lblWelcome = new JLabel("Welcome to Hospital Administration", SwingConstants.CENTER);
        lblWelcome.setFont(new Font(Theme.FONT_TITLE.getName(), Font.BOLD, 20));
        lblWelcome.setForeground(Theme.TEXT_MAIN);

        JLabel lblSelect = new JLabel("Please select your portal to continue with identity verification:", SwingConstants.CENTER);
        lblSelect.setFont(Theme.FONT_REGULAR);
        lblSelect.setForeground(Theme.TEXT_MUTED);

        greetingPanel.add(lblWelcome);
        greetingPanel.add(lblSelect);
        centerPanel.add(greetingPanel, BorderLayout.NORTH);

        // Cards Container: 3 Columns
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsGrid.setBackground(Theme.BG);

        // Card 1: Doctor (Teal Theme)
        JPanel doctorCard = createRoleCard(
            "DOCTOR",
            "Medical Specialist",
            "Manage appointments, view authorized patient details, and maintain consultation records.",
            new String[]{"• View scheduled OPD appointments", "• Access assigned patient history", "• Write diagnoses & prescriptions", "• Update doctor availability status"},
            Theme.DOCTOR_TEAL,
            Theme.DOCTOR_TEAL_DARK,
            "Doctor Portal →",
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openDoctorLogin();
                }
            }
        );

        // Card 2: Patient (Green Theme)
        JPanel patientCard = createRoleCard(
            "PATIENT",
            "Patient & Family",
            "Book appointments, view personal medical history, and check bills.",
            new String[]{"• Schedule doctor consultations", "• View past diagnoses & treatments", "• Check invoices & pay online", "• Download printable receipts"},
            Theme.PATIENT_GREEN,
            Theme.PATIENT_GREEN_DARK,
            "Patient Portal →",
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openPatientLogin();
                }
            }
        );

        // Card 3: Receptionist (Orange Theme)
        JPanel receptionistCard = createRoleCard(
            "RECEPTIONIST",
            "Front Desk & Cashier",
            "Register patients, schedule appointments, and manage billing.",
            new String[]{"• Fast patient intake & registration", "• Book & reschedule appointments", "• Generate bills & process payments", "• Export hospital analytics reports"},
            Theme.RECEPTIONIST_ORANGE,
            Theme.RECEPTIONIST_ORANGE_DARK,
            "Front Desk Portal →",
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openReceptionistLogin();
                }
            }
        );

        cardsGrid.add(doctorCard);
        cardsGrid.add(patientCard);
        cardsGrid.add(receptionistCard);

        centerPanel.add(cardsGrid, BorderLayout.CENTER);
        rootPane.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Footer with Admin Option
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Theme.BG);
        bottomPanel.setBorder(new EmptyBorder(8, 32, 16, 32));

        // Administrator Access Button
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        adminPanel.setBackground(Theme.BG);

        JLabel lblAdminPrompt = new JLabel("System Management:");
        lblAdminPrompt.setFont(Theme.FONT_SMALL);
        lblAdminPrompt.setForeground(Theme.TEXT_MUTED);

        JButton btnAdmin = new JButton("Administrator Sign In");
        btnAdmin.setFont(Theme.FONT_SMALL_BOLD);
        btnAdmin.setForeground(Theme.PRIMARY);
        btnAdmin.setBackground(Color.WHITE);
        btnAdmin.setFocusPainted(false);
        btnAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdmin.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(4, 12, 4, 12)
        ));
        btnAdmin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAdminLogin();
            }
        });

        adminPanel.add(lblAdminPrompt);
        adminPanel.add(btnAdmin);
        bottomPanel.add(adminPanel, BorderLayout.CENTER);

        JLabel lblCopyright = new JLabel("Hospital Management System • Academic OOP Project • Java Swing & JDBC", SwingConstants.CENTER);
        lblCopyright.setFont(Theme.FONT_SMALL);
        lblCopyright.setForeground(Theme.TEXT_MUTED);
        lblCopyright.setBorder(new EmptyBorder(8, 0, 0, 0));
        bottomPanel.add(lblCopyright, BorderLayout.SOUTH);

        rootPane.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(rootPane);
    }

    /**
     * Helper method to construct a professional, interactive role card.
     */
    private JPanel createRoleCard(String roleTitle, String roleSubtitle, String description,
                                  String[] features, Color accentColor, Color darkColor,
                                  String buttonText, ActionListener action) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Top colored accent banner (8px)
                g2.setColor(accentColor);
                g2.fillRect(0, 0, getWidth(), 8);
                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setBackground(Theme.SURFACE);
        card.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(16, 20, 20, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Click on entire card opens portal
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(new ActionEvent(card, ActionEvent.ACTION_PERFORMED, null));
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(new CompoundBorder(
                    new LineBorder(accentColor, 2, true),
                    new EmptyBorder(15, 19, 19, 19)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(new CompoundBorder(
                    new LineBorder(Theme.BORDER, 1, true),
                    new EmptyBorder(16, 20, 20, 20)
                ));
            }
        });

        // Inner vertical content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.SURFACE);

        // Badge pill
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgePanel.setBackground(Theme.SURFACE);
        JLabel badge = new JLabel("  " + roleSubtitle.toUpperCase() + "  ");
        badge.setFont(Theme.FONT_SMALL_BOLD);
        badge.setForeground(accentColor);
        badge.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 25));
        badge.setOpaque(true);
        badge.setBorder(new LineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60), 1, true));
        badgePanel.add(badge);
        content.add(badgePanel);

        content.add(Box.createVerticalStrut(10));

        // Title
        JLabel lblRole = new JLabel(roleTitle);
        lblRole.setFont(new Font(Theme.FONT_TITLE.getName(), Font.BOLD, 22));
        lblRole.setForeground(accentColor);
        content.add(lblRole);

        content.add(Box.createVerticalStrut(8));

        // Description
        JLabel lblDesc = new JLabel("<html><body style='width: 200px; color: #263746; line-height: 1.4;'>" + description + "</body></html>");
        lblDesc.setFont(Theme.FONT_REGULAR);
        content.add(lblDesc);

        content.add(Box.createVerticalStrut(14));

        // Bullet feature points
        for (String feat : features) {
            JLabel lblFeat = new JLabel(feat);
            lblFeat.setFont(Theme.FONT_SMALL);
            lblFeat.setForeground(Theme.TEXT_MUTED);
            content.add(lblFeat);
            content.add(Box.createVerticalStrut(4));
        }

        card.add(content, BorderLayout.CENTER);

        // Action button at bottom
        JButton btnAction = new JButton(buttonText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(darkColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(darkColor);
                } else {
                    g2.setColor(accentColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAction.setFont(Theme.FONT_BOLD);
        btnAction.setForeground(Color.WHITE);
        btnAction.setContentAreaFilled(false);
        btnAction.setFocusPainted(false);
        btnAction.setBorderPainted(false);
        btnAction.setOpaque(false);
        btnAction.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAction.setBorder(new EmptyBorder(10, 16, 10, 16));
        btnAction.addActionListener(action);

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setBackground(Theme.SURFACE);
        btnWrapper.setBorder(new EmptyBorder(12, 0, 0, 0));
        btnWrapper.add(btnAction, BorderLayout.CENTER);

        card.add(btnWrapper, BorderLayout.SOUTH);

        return card;
    }

    private void openDoctorLogin() {
        dispose();
        LoginFrame loginFrame = new LoginFrame(Role.DOCTOR);
        loginFrame.setVisible(true);
    }

    private void openPatientLogin() {
        dispose();
        PatientLoginFrame patientLoginFrame = new PatientLoginFrame();
        patientLoginFrame.setVisible(true);
    }

    private void openReceptionistLogin() {
        dispose();
        LoginFrame loginFrame = new LoginFrame(Role.RECEPTIONIST);
        loginFrame.setVisible(true);
    }

    private void openAdminLogin() {
        dispose();
        LoginFrame loginFrame = new LoginFrame(Role.ADMIN);
        loginFrame.setVisible(true);
    }
}
