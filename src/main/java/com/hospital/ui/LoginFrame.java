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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import com.hospital.config.HospitalConstants;
import com.hospital.exception.AuthenticationException;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.service.AuthService;

/**
 * Authentication and Login Screen for Hospital Management System.
 * Connects directly to MySQL database and verifies SHA-256 password hash + salt.
 * Supports customized presentation based on selected target role.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final Role targetRole;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;

    public LoginFrame() {
        this(Role.RECEPTIONIST);
    }

    public LoginFrame(Role targetRole) {
        this.authService = AuthService.getInstance();
        this.targetRole = (targetRole != null) ? targetRole : Role.RECEPTIONIST;
        initComponents();
    }

    private void initComponents() {
        String roleTitle = "Staff Authentication";
        Color bannerColor = Theme.PRIMARY;
        String bannerSub = "Internal Staff Access Portal • Security Clearance Required";

        if (targetRole == Role.DOCTOR) {
            roleTitle = "Doctor Portal - Medical Staff Access";
            bannerColor = Theme.DOCTOR_TEAL;
            bannerSub = "Physician Consultation & Clinical Records Authorization";
        } else if (targetRole == Role.RECEPTIONIST) {
            roleTitle = "Receptionist Portal - Front Desk Access";
            bannerColor = Theme.RECEPTIONIST_ORANGE;
            bannerSub = "Patient Intake, Scheduling & Cashier Authorization";
        } else if (targetRole == Role.ADMIN) {
            roleTitle = "Administrator Portal - Full System Access";
            bannerColor = Theme.NAVY_BLUE;
            bannerSub = "Hospital Administrative Governance & IT Access Clearance";
        }

        setTitle(HospitalConstants.HOSPITAL_NAME + " - " + roleTitle);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(540, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel rootPane = new JPanel(new BorderLayout());
        rootPane.setBackground(Theme.BG);

        // 1. Top Clinical Banner
        JPanel banner = new JPanel(new BorderLayout(14, 0));
        banner.setBackground(bannerColor);
        banner.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel lblLogo = new JLabel(UIHelper.createMedicalCrossIcon(36, Color.WHITE));
        JPanel bannerText = new JPanel(new GridLayout(2, 1, 0, 4));
        bannerText.setBackground(bannerColor);

        JLabel lblTitle = new JLabel(roleTitle.toUpperCase());
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(bannerSub);
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(new Color(0xBD, 0xD4, 0xE0));

        bannerText.add(lblTitle);
        bannerText.add(lblSub);

        banner.add(lblLogo, BorderLayout.WEST);
        banner.add(bannerText, BorderLayout.CENTER);

        // Back to Role Selection Button
        JButton btnBack = new JButton("← Back");
        btnBack.setFont(Theme.FONT_SMALL_BOLD);
        btnBack.setForeground(bannerColor);
        btnBack.setBackground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setBorder(new CompoundBorder(
            new LineBorder(Color.WHITE, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new RoleSelectionFrame().setVisible(true);
            }
        });
        banner.add(btnBack, BorderLayout.EAST);

        rootPane.add(banner, BorderLayout.NORTH);

        // 2. Center Card Panel
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(Theme.BG);
        centerContainer.setBorder(new EmptyBorder(20, 28, 16, 28));

        JPanel formCard = UIHelper.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = UIHelper.createTextField();
        txtUsername.setPreferredSize(new Dimension(220, 34));
        txtPassword = UIHelper.createPasswordField();
        txtPassword.setPreferredSize(new Dimension(220, 34));

        // Submit on Enter key
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        lblError = new JLabel(" ");
        lblError.setFont(Theme.FONT_SMALL_BOLD);
        lblError.setForeground(Theme.ERROR);

        int row = 0;

        JLabel lblInstructions = new JLabel("Please enter your database credentials to sign in:");
        lblInstructions.setFont(Theme.FONT_SUBHEADER);
        lblInstructions.setForeground(Theme.TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formCard.add(lblInstructions, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(Theme.FONT_SUBHEADER);
        lblUser.setForeground(Theme.TEXT_MAIN);
        formCard.add(lblUser, gbc);

        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 0.65;
        formCard.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(Theme.FONT_SUBHEADER);
        lblPass.setForeground(Theme.TEXT_MAIN);
        formCard.add(lblPass, gbc);

        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 0.65;
        formCard.add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formCard.add(lblError, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Theme.SURFACE);

        JButton btnReset = UIHelper.createOutlineButton("Clear");
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtUsername.setText("");
                txtPassword.setText("");
                lblError.setText(" ");
            }
        });

        final Color loginButtonColor = bannerColor;

        JButton btnLogin = new JButton("Authenticate & Sign In") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover() ? loginButtonColor.darker() : loginButtonColor;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(Theme.FONT_BOLD);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        btnPanel.add(btnReset);
        btnPanel.add(btnLogin);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formCard.add(btnPanel, gbc);

        centerContainer.add(formCard, BorderLayout.CENTER);

        // 3. Quick Demo Login Credentials Bar (Ideal for College Viva Evaluation)
        JPanel demoBox = new JPanel(new BorderLayout(0, 6));
        demoBox.setBackground(Theme.BG);
        demoBox.setBorder(new TitledBorder(
            new LineBorder(Theme.BORDER),
            "Viva / Demo Quick Sign-in",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            Theme.FONT_SMALL_BOLD,
            Theme.TEXT_MUTED
        ));

        JPanel demoBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        demoBtns.setBackground(Theme.BG);

        if (targetRole == Role.DOCTOR) {
            JButton btnDoc1 = createQuickFillButton("Dr. Arun Sharma (Cardio)", "dr_sharma", "doctor123", bannerColor);
            JButton btnDoc2 = createQuickFillButton("Dr. Priya Patel (Pedia)", "dr_patel", "doctor123", bannerColor);
            demoBtns.add(btnDoc1);
            demoBtns.add(btnDoc2);
        } else if (targetRole == Role.RECEPTIONIST) {
            JButton btnRecep = createQuickFillButton("Receptionist (Sunita Rao)", "receptionist", "recep123", bannerColor);
            demoBtns.add(btnRecep);
        } else if (targetRole == Role.ADMIN) {
            JButton btnAdmin = createQuickFillButton("Administrator (Dr. Roy)", "admin", "admin123", bannerColor);
            demoBtns.add(btnAdmin);
        } else {
            JButton btnDemoAdmin = createQuickFillButton("Admin", "admin", "admin123", bannerColor);
            JButton btnDemoDoctor = createQuickFillButton("Doctor", "dr_sharma", "doctor123", bannerColor);
            JButton btnDemoRecep = createQuickFillButton("Receptionist", "receptionist", "recep123", bannerColor);
            demoBtns.add(btnDemoAdmin);
            demoBtns.add(btnDemoDoctor);
            demoBtns.add(btnDemoRecep);
        }

        demoBox.add(demoBtns, BorderLayout.CENTER);
        centerContainer.add(demoBox, BorderLayout.SOUTH);
        rootPane.add(centerContainer, BorderLayout.CENTER);

        // Bottom Footer
        JLabel lblFooter = new JLabel("Hospital Management System • OOP Academic Project • All Rights Reserved", SwingConstants.CENTER);
        lblFooter.setFont(Theme.FONT_SMALL);
        lblFooter.setForeground(Theme.TEXT_MUTED);
        lblFooter.setBorder(new EmptyBorder(0, 0, 12, 0));
        rootPane.add(lblFooter, BorderLayout.SOUTH);

        setContentPane(rootPane);
    }

    private JButton createQuickFillButton(String label, String user, String pass, Color accentColor) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.FONT_SMALL);
        btn.setBackground(Color.WHITE);
        btn.setForeground(accentColor);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtUsername.setText(user);
                txtPassword.setText(pass);
                lblError.setText(" ");
                performLogin();
            }
        });
        return btn;
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user != null) {
                dispose();
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (user.getRole() == Role.DOCTOR) {
                            DoctorDashboardFrame docFrame = new DoctorDashboardFrame();
                            docFrame.setVisible(true);
                        } else if (user.getRole() == Role.RECEPTIONIST) {
                            ReceptionistDashboardFrame recepFrame = new ReceptionistDashboardFrame();
                            recepFrame.setVisible(true);
                        } else {
                            MainDashboardFrame adminFrame = new MainDashboardFrame();
                            adminFrame.setVisible(true);
                        }
                    }
                });
            }
        } catch (AuthenticationException ex) {
            lblError.setText(ex.getMessage());
            txtPassword.setText("");
            txtPassword.requestFocus();
        } catch (Exception ex) {
            lblError.setText("System error: " + ex.getMessage());
        }
    }
}
