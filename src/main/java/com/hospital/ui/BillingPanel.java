package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import com.hospital.config.HospitalConstants;
import com.hospital.model.Bill;
import com.hospital.model.PaymentStatus;
import com.hospital.service.BillingService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.ui.dialogs.BillDialog;
import com.hospital.ui.dialogs.ReceiptDialog;

/**
 * Hospital Billing, Invoicing, and Revenue Settlement Panel.
 * Manages itemized invoices, payment tracking, and receipt generation.
 */
public class BillingPanel extends JPanel {

    private final BillingService billingService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final MainDashboardFrame parentFrame;

    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;
    private DefaultTableModel tableModel;
    private JTable billTable;
    private List<Bill> currentBills;

    private JLabel lblTotalCollected;
    private JLabel lblTotalPending;
    private JLabel lblPendingCount;

    public BillingPanel(MainDashboardFrame parentFrame,
                        BillingService billingService, 
                        PatientService patientService, 
                        DoctorService doctorService) {
        this.parentFrame = parentFrame;
        this.billingService = billingService;
        this.patientService = patientService;
        this.doctorService = doctorService;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        loadBills();
    }

    private void initComponents() {
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Top Toolbar & Filters
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Theme.SURFACE);

        // Search & Filter (Left)
        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBox.setBackground(Theme.SURFACE);

        JLabel lblFilter = new JLabel("Status Filter:");
        lblFilter.setFont(Theme.FONT_SUBHEADER);
        lblFilter.setForeground(Theme.TEXT_MAIN);

        cbStatusFilter = UIHelper.createComboBox(new String[]{"All Statuses", "PENDING", "PAID"});
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
            loadBills();
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

        JButton btnGenerate = UIHelper.createPrimaryButton("+ Generate Bill");
        btnGenerate.addActionListener(e -> openGenerateDialog());

        JButton btnMarkPaid = UIHelper.createSuccessButton("Mark as Paid");
        btnMarkPaid.addActionListener(e -> markBillAsPaid());

        JButton btnReceipt = UIHelper.createSecondaryButton("Print / View Receipt");
        btnReceipt.addActionListener(e -> viewReceipt());

        JButton btnEdit = UIHelper.createOutlineButton("Edit Bill");
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnDelete = UIHelper.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteBill());

        actions.add(btnGenerate);
        actions.add(btnMarkPaid);
        actions.add(btnReceipt);
        actions.add(btnEdit);
        actions.add(btnDelete);
        toolbar.add(actions, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Invoices Table
        String[] columns = {
            "Bill ID", "Patient Name", "Consulting Doctor", "Consult Fee (₹)", "Additional (₹)", "Tax (5%)", "Discount (₹)", "Grand Total (₹)", "Status", "Payment Method", "Billing Date"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        billTable = UIHelper.createTable(tableModel);
        billTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        billTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        billTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        billTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        billTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        billTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        billTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        billTable.getColumnModel().getColumn(7).setPreferredWidth(110);
        billTable.getColumnModel().getColumn(8).setPreferredWidth(90);
        billTable.getColumnModel().getColumn(9).setPreferredWidth(110);
        billTable.getColumnModel().getColumn(10).setPreferredWidth(140);

        billTable.getColumnModel().getColumn(8).setCellRenderer(UIHelper.createStatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(billTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        // Financial Metrics Banner at Bottom
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 8));
        banner.setBackground(new java.awt.Color(0xF4, 0xF8, 0xFA));
        banner.setBorder(new LineBorder(Theme.BORDER));

        lblTotalCollected = new JLabel("Collected Revenue: ₹0.00");
        lblTotalCollected.setFont(Theme.FONT_SUBHEADER);
        lblTotalCollected.setForeground(Theme.SUCCESS);

        lblTotalPending = new JLabel("Pending Amount: ₹0.00");
        lblTotalPending.setFont(Theme.FONT_SUBHEADER);
        lblTotalPending.setForeground(Theme.WARNING);

        lblPendingCount = new JLabel("Pending Invoices: 0");
        lblPendingCount.setFont(Theme.FONT_SUBHEADER);
        lblPendingCount.setForeground(Theme.ERROR);

        banner.add(lblTotalCollected);
        banner.add(lblTotalPending);
        banner.add(lblPendingCount);

        card.add(banner, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    public void loadBills() {
        try {
            currentBills = billingService.getAllBills();
            populateTable(currentBills);
            updateSummaryMetrics();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load invoices: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        String status = (String) cbStatusFilter.getSelectedItem();
        try {
            currentBills = billingService.searchBills(query, status);
            populateTable(currentBills);
            if (currentBills.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No invoices matched your criteria.", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Bill> list) {
        tableModel.setRowCount(0);
        for (Bill b : list) {
            tableModel.addRow(new Object[]{
                "#" + b.getId(),
                b.getPatientName(),
                b.getDoctorName() != null ? b.getDoctorName() : "-",
                String.format("%.2f", b.getConsultationFee()),
                String.format("%.2f", b.getAdditionalCharges()),
                String.format("%.2f", b.getTaxAmount()),
                String.format("%.2f", b.getDiscount()),
                String.format("%.2f", b.getTotalAmount()),
                b.getPaymentStatus().name(),
                b.getPaymentMethod() != null ? b.getPaymentMethod() : "UNPAID",
                b.getFormattedDate()
            });
        }
    }

    private void updateSummaryMetrics() {
        try {
            double collected = billingService.getTotalCollectedRevenue();
            double pending = billingService.getTotalPendingAmount();
            int pendingCount = billingService.getPendingBillsCount();

            lblTotalCollected.setText(String.format("Collected Revenue: ₹%.2f", collected));
            lblTotalPending.setText(String.format("Pending Receivables: ₹%.2f", pending));
            lblPendingCount.setText(String.format("Pending Invoices: %d", pendingCount));
        } catch (Exception e) {
            System.err.println("[BillingPanel] Metric update warning: " + e.getMessage());
        }
    }

    private void openGenerateDialog() {
        BillDialog dialog = new BillDialog(parentFrame, billingService, patientService, doctorService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadBills();
            parentFrame.refreshDashboardMetrics();
        }
    }

    private void openEditDialog() {
        Bill selected = getSelectedBill();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BillDialog dialog = new BillDialog(parentFrame, billingService, patientService, doctorService, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadBills();
            parentFrame.refreshDashboardMetrics();
        }
    }

    private void markBillAsPaid() {
        Bill selected = getSelectedBill();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to settle.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.getPaymentStatus() == PaymentStatus.PAID) {
            JOptionPane.showMessageDialog(this, "This invoice is already settled and marked as PAID.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String method = (String) JOptionPane.showInputDialog(
            this,
            "Select Payment Method for settlement of ₹" + String.format("%.2f", selected.getTotalAmount()) + ":",
            "Settle Medical Invoice #" + selected.getId(),
            JOptionPane.QUESTION_MESSAGE,
            null,
            HospitalConstants.PAYMENT_METHODS,
            "CASH"
        );

        if (method != null) {
            try {
                billingService.markAsPaid(selected.getId(), method);
                loadBills();
                parentFrame.refreshDashboardMetrics();
                JOptionPane.showMessageDialog(this, "Invoice #" + selected.getId() + " settled successfully via " + method + "!", "Payment Completed", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to update payment status: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewReceipt() {
        Bill selected = getSelectedBill();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to view/print receipt.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ReceiptDialog dialog = new ReceiptDialog(parentFrame, selected);
        dialog.setVisible(true);
    }

    private void deleteBill() {
        Bill selected = getSelectedBill();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an invoice to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete Invoice #" + selected.getId() + " for " + selected.getPatientName() + "?\n" +
            "Amount: ₹" + String.format("%.2f", selected.getTotalAmount()) + "\n\n" +
            "This action cannot be undone.",
            "Confirm Invoice Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                billingService.deleteBill(selected.getId());
                loadBills();
                parentFrame.refreshDashboardMetrics();
                JOptionPane.showMessageDialog(this, "Invoice deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting invoice: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Bill getSelectedBill() {
        int row = billTable.getSelectedRow();
        if (row < 0 || currentBills == null || row >= currentBills.size()) {
            return null;
        }
        return currentBills.get(row);
    }
}
