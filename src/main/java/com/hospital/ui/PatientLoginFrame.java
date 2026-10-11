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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import com.hospital.config.HospitalConstants;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.AuthenticationException;
import com.hospital.model.Patient;
import com.hospital.service.AuthService;
import com.hospital.service.PatientService;
import com.hospital.ui.dialogs.PatientDialog;

/**
 * Dedicated Patient Portal Identity Verification Screen.
 * 
 * Authenticates patients against their Patient ID and registered phone number
 * stored in the MySQL database (patients table).
 */
public class PatientLoginFrame extends JFrame {

    private final AuthService authService;
    private final PatientService patientService;
    private JTextField txtPatientId;
    private JTextField txtPhone;
    private JLabel lblError;

    public PatientLoginFrame() {
        this.authService = AuthService.getInstance();
        this.patientService = new PatientService(new PatientDAO());
        initComponents();
    }

    private void initComponents() {
        setTitle(HospitalConstants.HOSPITAL_NAME + " - Patient Portal Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel rootPane = new JPanel(new BorderLayout());
        rootPane.setBackground(Theme.BG);

        // 1. Top Green Clinical Banner
        JPanel banner = new JPanel(new BorderLayout(14, 0));
        banner.setBackground(Theme.PATIENT_GREEN);
        banner.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel lblLogo = new JLabel(UIHelper.createMedicalCrossIcon(36, Color.WHITE));
        JPanel bannerText = new JPanel(new GridLayout(2, 1, 0, 4));
        bannerText.setBackground(Theme.PATIENT_GREEN);

        JLabel lblTitle = new JLabel("PATIENT & FAMILY PORTAL");
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Secure Medical Access • Verify Patient ID & Registered Phone");
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(new Color(0xE0, 0xF2, 0xFE));

        bannerText.add(lblTitle);
        bannerText.add(lblSub);

        banner.add(lblLogo, BorderLayout.WEST);
        banner.add(bannerText, BorderLayout.CENTER);

        // Back button to Role Selection
        JButton btnBack = new JButton("← Back");
        btnBack.setFont(Theme.FONT_SMALL_BOLD);
        btnBack.setForeground(Theme.PATIENT_GREEN);
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

        // 2. Center Card Form Panel
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(Theme.BG);
        centerContainer.setBorder(new EmptyBorder(20, 28, 16, 28));

        JPanel formCard = UIHelper.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPatientId = UIHelper.createTextField();
        txtPatientId.setPreferredSize(new Dimension(220, 34));
        txtPhone = UIHelper.createTextField();
        txtPhone.setPreferredSize(new Dimension(220, 34));

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performPatientLogin();
                }
            }
        };
        txtPatientId.addKeyListener(enterListener);
        txtPhone.addKeyListener(enterListener);

        lblError = new JLabel(" ");
        lblError.setFont(Theme.FONT_SMALL_BOLD);
        lblError.setForeground(Theme.ERROR);

        int row = 0;

        JLabel lblInstructions = new JLabel("Enter your hospital registration details to sign in:");
        lblInstructions.setFont(Theme.FONT_SUBHEADER);
        lblInstructions.setForeground(Theme.TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formCard.add(lblInstructions, gbc);

        gbc.gridwidth = 1;

        // Patient ID
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lblId = new JLabel("Patient ID #:");
        lblId.setFont(Theme.FONT_SUBHEADER);
        lblId.setForeground(Theme.TEXT_MAIN);
        formCard.add(lblId, gbc);

        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 0.65;
        formCard.add(txtPatientId, gbc);

        // Phone Number
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lblPhone = new JLabel("Registered Phone:");
        lblPhone.setFont(Theme.FONT_SUBHEADER);
        lblPhone.setForeground(Theme.TEXT_MAIN);
        formCard.add(lblPhone, gbc);

        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 0.65;
        formCard.add(txtPhone, gbc);

        // Error message label
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formCard.add(lblError, gbc);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Theme.SURFACE);

        JButton btnRegister = UIHelper.createOutlineButton("+ Register New Patient");
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistrationDialog();
            }
        });

        JButton btnLogin = UIHelper.createGreenButton("Sign In to Portal");
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performPatientLogin();
            }
        });

        btnPanel.add(btnRegister);
        btnPanel.add(btnLogin);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formCard.add(btnPanel, gbc);

        centerContainer.add(formCard, BorderLayout.CENTER);

        // 3. Quick Demo Buttons for Examination/Viva
        JPanel demoBox = new JPanel(new BorderLayout(0, 6));
        demoBox.setBackground(Theme.BG);
        demoBox.setBorder(new TitledBorder(
            new LineBorder(Theme.BORDER),
            "Viva / Demo One-Click Sign In",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            Theme.FONT_SMALL_BOLD,
            Theme.TEXT_MUTED
        ));

        JPanel demoBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        demoBtns.setBackground(Theme.BG);

        JButton btnDemo1 = createQuickFillButton("Patient #1 (Ramesh)", "1", "+91 97111 22334");
        JButton btnDemo2 = createQuickFillButton("Patient #2 (Anita)", "2", "+91 97222 33445");
        JButton btnDemo3 = createQuickFillButton("Patient #3 (Karan)", "3", "+91 97333 44556");

        demoBtns.add(btnDemo1);
        demoBtns.add(btnDemo2);
        demoBtns.add(btnDemo3);
        demoBox.add(demoBtns, BorderLayout.CENTER);

        centerContainer.add(demoBox, BorderLayout.SOUTH);
        rootPane.add(centerContainer, BorderLayout.CENTER);

        // Footer
        JLabel lblFooter = new JLabel("Hospital Management System • Patient Privacy & HIPAA Protected", SwingConstants.CENTER);
        lblFooter.setFont(Theme.FONT_SMALL);
        lblFooter.setForeground(Theme.TEXT_MUTED);
        lblFooter.setBorder(new EmptyBorder(0, 0, 12, 0));
        rootPane.add(lblFooter, BorderLayout.SOUTH);

        setContentPane(rootPane);
    }

    private JButton createQuickFillButton(String label, String id, String phone) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.FONT_SMALL);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Theme.PATIENT_GREEN_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtPatientId.setText(id);
                txtPhone.setText(phone);
                lblError.setText(" ");
                performPatientLogin();
            }
        });
        return btn;
    }

    private void performPatientLogin() {
        String idStr = txtPatientId.getText().trim();
        String phone = txtPhone.getText().trim();

        if (idStr.isEmpty() || phone.isEmpty()) {
            lblError.setText("Please enter both Patient ID and registered Phone number.");
            return;
        }

        int patientId;
        try {
            patientId = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            lblError.setText("Patient ID must be a numeric value (e.g. 1).");
            return;
        }

        try {
            Patient patient = authService.loginPatient(patientId, phone);
            if (patient != null) {
                dispose();
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        PatientDashboardFrame dashboard = new PatientDashboardFrame();
                        dashboard.setVisible(true);
                    }
                });
            }
        } catch (AuthenticationException ex) {
            lblError.setText(ex.getMessage());
        } catch (Exception ex) {
            lblError.setText("System error: " + ex.getMessage());
        }
    }

    private void openRegistrationDialog() {
        PatientDialog dialog = new PatientDialog(this, patientService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Patient saved = dialog.getPatient();
            if (saved != null) {
                txtPatientId.setText(String.valueOf(saved.getId()));
                txtPhone.setText(saved.getPhone());
                JOptionPane.showMessageDialog(
                    this,
                    "Registration successful!\n\nYour Patient ID is #" + saved.getId() + "\n" +
                    "Use this ID and your phone number (" + saved.getPhone() + ") to log in.",
                    "Patient Registered",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }
}
