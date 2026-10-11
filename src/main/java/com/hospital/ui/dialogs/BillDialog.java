package com.hospital.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import com.hospital.config.HospitalConstants;
import com.hospital.model.Bill;
import com.hospital.model.BillItem;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.PaymentStatus;
import com.hospital.service.BillingService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.ui.Theme;
import com.hospital.ui.UIHelper;

/**
 * Modal dialog for generating and itemizing hospital bills.
 * Demonstrates real-time financial calculations and line-item composition.
 */
public class BillDialog extends JDialog {

    private final BillingService billingService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private Bill bill;
    private boolean saved = false;

    private JComboBox<PatientOption> cbPatient;
    private JComboBox<DoctorOption> cbDoctor;
    private JTextField txtConsultationFee;
    private JTextField txtAdditionalCharges;
    private JTextField txtDiscount;
    private JLabel lblTaxAmount;
    private JLabel lblGrandTotal;
    private JComboBox<String> cbPaymentStatus;
    private JComboBox<String> cbPaymentMethod;

    private DefaultTableModel itemsTableModel;
    private JTable itemsTable;

    public BillDialog(Frame owner, BillingService billingService, 
                      PatientService patientService, DoctorService doctorService, 
                      Bill existing) {
        super(owner, existing == null ? "Generate Medical Invoice" : "Edit Invoice Details", true);
        this.billingService = billingService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.bill = existing;

        initComponents();
        loadDropdownData();
        if (existing != null) {
            populateFields(existing);
        } else {
            recalculateTotals();
        }

        setSize(680, 680);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 14));
        contentPane.setBackground(Theme.SURFACE);
        contentPane.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.SURFACE);
        JLabel lblTitle = new JLabel(bill == null ? "Generate Outpatient / Inpatient Invoice" : "Modify Invoice #" + bill.getId());
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.PRIMARY);
        JLabel lblSub = new JLabel("Calculates subtotal, healthcare tax (5%), discounts, and line-item charges.");
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.TEXT_MUTED);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Center: Form + Line Items Panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setBackground(Theme.SURFACE);

        // Top Form Info
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Theme.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbPatient = new JComboBox<>();
        cbPatient.setFont(Theme.FONT_REGULAR);

        cbDoctor = new JComboBox<>();
        cbDoctor.setFont(Theme.FONT_REGULAR);
        cbDoctor.addActionListener(e -> {
            DoctorOption doc = (DoctorOption) cbDoctor.getSelectedItem();
            if (doc != null && (bill == null)) {
                txtConsultationFee.setText(String.format("%.2f", doc.fee));
                recalculateTotals();
            }
        });

        txtConsultationFee = UIHelper.createTextField();
        txtConsultationFee.setText("500.00");
        txtConsultationFee.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                recalculateTotals();
            }
        });

        txtAdditionalCharges = UIHelper.createTextField();
        txtAdditionalCharges.setText("0.00");
        txtAdditionalCharges.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                recalculateTotals();
            }
        });

        txtDiscount = UIHelper.createTextField();
        txtDiscount.setText("0.00");
        txtDiscount.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                recalculateTotals();
            }
        });

        cbPaymentStatus = UIHelper.createComboBox(new String[]{"PENDING", "PAID"});
        cbPaymentMethod = UIHelper.createComboBox(HospitalConstants.PAYMENT_METHODS);

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Select Patient *", cbPatient);
        addFormRow(formPanel, gbc, row++, "Consulting Doctor", cbDoctor);
        addFormRow(formPanel, gbc, row++, "Consultation Fee (₹) *", txtConsultationFee);
        addFormRow(formPanel, gbc, row++, "Additional Charges (₹)", txtAdditionalCharges);
        addFormRow(formPanel, gbc, row++, "Concession / Discount (₹)", txtDiscount);
        addFormRow(formPanel, gbc, row++, "Settlement Status", cbPaymentStatus);
        addFormRow(formPanel, gbc, row++, "Payment Method", cbPaymentMethod);

        centerPanel.add(formPanel, BorderLayout.NORTH);

        // Line Items Table (Composition)
        JPanel itemsPanel = new JPanel(new BorderLayout(0, 6));
        itemsPanel.setBackground(Theme.SURFACE);
        itemsPanel.setBorder(new TitledBorder(new LineBorder(Theme.BORDER), "Itemized Services & Medication Breakdown", TitledBorder.LEFT, TitledBorder.TOP, Theme.FONT_SUBHEADER, Theme.PRIMARY));

        itemsTableModel = new DefaultTableModel(new String[]{"Service Description", "Amount (₹)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        itemsTable = UIHelper.createTable(itemsTableModel);
        itemsTable.setPreferredScrollableViewportSize(new Dimension(0, 110));
        JScrollPane scrollItems = new JScrollPane(itemsTable);
        itemsPanel.add(scrollItems, BorderLayout.CENTER);

        // Item Buttons
        JPanel itemBtnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        itemBtnBar.setBackground(Theme.SURFACE);

        JButton btnAddItem = UIHelper.createOutlineButton("+ Add Service Item");
        btnAddItem.addActionListener(e -> promptAddBillItem());

        JButton btnRemoveItem = UIHelper.createOutlineButton("- Remove Selected");
        btnRemoveItem.addActionListener(e -> {
            int sel = itemsTable.getSelectedRow();
            if (sel >= 0) {
                itemsTableModel.removeRow(sel);
                syncAdditionalChargesFromItems();
            }
        });

        itemBtnBar.add(btnAddItem);
        itemBtnBar.add(btnRemoveItem);
        itemsPanel.add(itemBtnBar, BorderLayout.SOUTH);

        centerPanel.add(itemsPanel, BorderLayout.CENTER);

        // Summary Totals Banner
        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 6));
        totalsPanel.setBackground(new Color(0xF4, 0xF8, 0xFA));
        totalsPanel.setBorder(new LineBorder(Theme.BORDER));

        lblTaxAmount = new JLabel("Tax (5%): ₹0.00");
        lblTaxAmount.setFont(Theme.FONT_SUBHEADER);
        lblTaxAmount.setForeground(Theme.TEXT_MUTED);

        lblGrandTotal = new JLabel("Grand Total: ₹0.00");
        lblGrandTotal.setFont(new java.awt.Font(Theme.FONT_TITLE.getName(), java.awt.Font.BOLD, 16));
        lblGrandTotal.setForeground(Theme.PRIMARY);

        totalsPanel.add(lblTaxAmount);
        totalsPanel.add(lblGrandTotal);
        centerPanel.add(totalsPanel, BorderLayout.SOUTH);

        contentPane.add(centerPanel, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Theme.SURFACE);

        JButton btnCancel = UIHelper.createOutlineButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UIHelper.createPrimaryButton(bill == null ? "Generate Invoice" : "Save Changes");
        btnSave.addActionListener(e -> saveBill());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, javax.swing.JComponent comp) {
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
                cbPatient.addItem(new PatientOption(p.getId(), p.getFullName(), p.getPhone()));
            }

            List<Doctor> doctors = doctorService.getAllDoctors();
            for (Doctor d : doctors) {
                cbDoctor.addItem(new DoctorOption(d.getId(), d.getFullName(), d.getSpecialization(), d.getConsultationFee()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load patient or doctor data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void promptAddBillItem() {
        String desc = JOptionPane.showInputDialog(this, "Enter Service / Medication Description:", "Add Line Item", JOptionPane.PLAIN_MESSAGE);
        if (desc == null || desc.trim().isEmpty()) return;

        String amtStr = JOptionPane.showInputDialog(this, "Enter Amount (₹):", "Add Line Item", JOptionPane.PLAIN_MESSAGE);
        if (amtStr == null || amtStr.trim().isEmpty()) return;

        try {
            double amt = Double.parseDouble(amtStr.trim());
            if (amt < 0) throw new NumberFormatException();
            itemsTableModel.addRow(new Object[]{desc.trim(), String.format("%.2f", amt)});
            syncAdditionalChargesFromItems();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void syncAdditionalChargesFromItems() {
        double sum = 0.0;
        for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
            try {
                sum += Double.parseDouble(itemsTableModel.getValueAt(i, 1).toString());
            } catch (Exception ignored) {
            }
        }
        txtAdditionalCharges.setText(String.format("%.2f", sum));
        recalculateTotals();
    }

    private void recalculateTotals() {
        double fee = parseDouble(txtConsultationFee.getText());
        double add = parseDouble(txtAdditionalCharges.getText());
        double disc = parseDouble(txtDiscount.getText());

        double subtotal = fee + add;
        double tax = Math.round(subtotal * 0.05 * 100.0) / 100.0;
        double grand = Math.max(0.0, Math.round((subtotal + tax - disc) * 100.0) / 100.0);

        lblTaxAmount.setText(String.format("Tax (5%%): ₹%.2f", tax));
        lblGrandTotal.setText(String.format("Grand Total: ₹%.2f", grand));
    }

    private double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void populateFields(Bill b) {
        for (int i = 0; i < cbPatient.getItemCount(); i++) {
            if (cbPatient.getItemAt(i).id == b.getPatientId()) {
                cbPatient.setSelectedIndex(i);
                break;
            }
        }
        txtConsultationFee.setText(String.format("%.2f", b.getConsultationFee()));
        txtAdditionalCharges.setText(String.format("%.2f", b.getAdditionalCharges()));
        txtDiscount.setText(String.format("%.2f", b.getDiscount()));
        cbPaymentStatus.setSelectedItem(b.getPaymentStatus().name());
        if (b.getPaymentMethod() != null) {
            cbPaymentMethod.setSelectedItem(b.getPaymentMethod());
        }

        // Populate items
        for (BillItem item : b.getItems()) {
            itemsTableModel.addRow(new Object[]{item.getDescription(), String.format("%.2f", item.getAmount())});
        }
        recalculateTotals();
    }

    private void saveBill() {
        PatientOption patientOpt = (PatientOption) cbPatient.getSelectedItem();
        if (patientOpt == null) {
            JOptionPane.showMessageDialog(this, "Please select an assigned patient.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double fee = parseDouble(txtConsultationFee.getText());
        double add = parseDouble(txtAdditionalCharges.getText());
        double disc = parseDouble(txtDiscount.getText());
        PaymentStatus status = PaymentStatus.fromString((String) cbPaymentStatus.getSelectedItem());
        String method = (String) cbPaymentMethod.getSelectedItem();

        try {
            if (bill == null) {
                bill = new Bill(null, patientOpt.id, fee, add);
                bill.setDiscount(disc);
                bill.setPaymentStatus(status);
                bill.setPaymentMethod(status == PaymentStatus.PAID ? method : null);

                // Add composed line items
                for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
                    String desc = (String) itemsTableModel.getValueAt(i, 0);
                    double amount = parseDouble((String) itemsTableModel.getValueAt(i, 1));
                    bill.addItem(new BillItem(desc, amount));
                }

                billingService.generateBill(bill);
                JOptionPane.showMessageDialog(this, "Invoice generated successfully! Invoice Ref: #" + bill.getId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                bill.setPatientId(patientOpt.id);
                bill.setConsultationFee(fee);
                bill.setAdditionalCharges(add);
                bill.setDiscount(disc);
                bill.setPaymentStatus(status);
                bill.setPaymentMethod(status == PaymentStatus.PAID ? method : null);
                billingService.updateBill(bill);
                JOptionPane.showMessageDialog(this, "Invoice updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Billing error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Bill getBill() {
        return bill;
    }

    private static class PatientOption {
        final int id;
        final String name;
        final String phone;

        PatientOption(int id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }

        @Override
        public String toString() {
            return String.format("#%d - %s (%s)", id, name, phone);
        }
    }

    private static class DoctorOption {
        final int id;
        final String name;
        final String spec;
        final double fee;

        DoctorOption(int id, String name, String spec, double fee) {
            this.id = id;
            this.name = name;
            this.spec = spec;
            this.fee = fee;
        }

        @Override
        public String toString() {
            return String.format("%s - %s (Fee: ₹%.0f)", name, spec, fee);
        }
    }
}
