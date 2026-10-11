package com.hospital.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import com.hospital.config.HospitalConstants;
import com.hospital.model.Bill;
import com.hospital.model.BillItem;
import com.hospital.ui.Theme;
import com.hospital.ui.UIHelper;
import com.hospital.util.FileUtil;

/**
 * Clean, printable clinical receipt and financial statement dialog.
 */
public class ReceiptDialog extends JDialog {

    private final Bill bill;
    private JTextArea txtReceipt;

    public ReceiptDialog(Frame owner, Bill bill) {
        super(owner, "Official Hospital Invoice Receipt #" + (bill != null ? bill.getId() : ""), true);
        this.bill = bill;

        initComponents();
        generateReceiptText();

        setSize(560, 640);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 12));
        contentPane.setBackground(Theme.BG);
        contentPane.setBorder(new EmptyBorder(16, 16, 16, 16));

        txtReceipt = new JTextArea();
        txtReceipt.setEditable(false);
        txtReceipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtReceipt.setBackground(Color.WHITE);
        txtReceipt.setForeground(Theme.TEXT_MAIN);
        txtReceipt.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(txtReceipt);
        scroll.setBorder(new LineBorder(Theme.BORDER));
        contentPane.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Theme.BG);

        JButton btnExport = UIHelper.createSecondaryButton("Export to File");
        btnExport.addActionListener(e -> exportReceipt());

        JButton btnClose = UIHelper.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnExport);
        buttonPanel.add(btnClose);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void generateReceiptText() {
        if (bill == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================\n");
        sb.append(String.format("          %s\n", HospitalConstants.HOSPITAL_NAME));
        sb.append(String.format("          %s\n", HospitalConstants.HOSPITAL_TAGLINE));
        sb.append(String.format("     %s | %s\n", HospitalConstants.HOSPITAL_ADDRESS, HospitalConstants.HOSPITAL_PHONE));
        sb.append("====================================================================\n\n");

        sb.append(String.format("INVOICE NUMBER   : INV-%05d\n", bill.getId()));
        sb.append(String.format("BILLING DATE     : %s\n", bill.getFormattedDate()));
        sb.append(String.format("PATIENT NAME     : %s (ID: #%d)\n", bill.getPatientName(), bill.getPatientId()));
        sb.append(String.format("CONTACT PHONE    : %s\n", bill.getPatientPhone()));
        sb.append(String.format("ATTENDING DOCTOR : %s\n", bill.getDoctorName()));
        sb.append(String.format("SETTLEMENT STATUS: [%s]  METHOD: %s\n", 
            bill.getPaymentStatus(), bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "UNPAID"));
        sb.append("--------------------------------------------------------------------\n");
        sb.append(String.format("%-42s %12s\n", "ITEM / SERVICE DESCRIPTION", "AMOUNT (₹)"));
        sb.append("--------------------------------------------------------------------\n");

        sb.append(String.format("%-42s %12.2f\n", "Physician Consultation Fee", bill.getConsultationFee()));

        if (!bill.getItems().isEmpty()) {
            for (BillItem item : bill.getItems()) {
                sb.append(String.format("%-42s %12.2f\n", item.getDescription(), item.getAmount()));
            }
        } else if (bill.getAdditionalCharges() > 0) {
            sb.append(String.format("%-42s %12.2f\n", "Additional Diagnostic / Pharmacy Charges", bill.getAdditionalCharges()));
        }

        sb.append("--------------------------------------------------------------------\n");
        double subtotal = bill.getConsultationFee() + bill.getAdditionalCharges();
        sb.append(String.format("%-42s %12.2f\n", "SUBTOTAL", subtotal));
        sb.append(String.format("%-42s %12.2f\n", "HEALTHCARE SURCHARGE / TAX (5%)", bill.getTaxAmount()));
        if (bill.getDiscount() > 0) {
            sb.append(String.format("%-42s -%11.2f\n", "CONCESSION / DISCOUNT", bill.getDiscount()));
        }
        sb.append("====================================================================\n");
        sb.append(String.format("%-42s %12.2f\n", "NET GRAND TOTAL PAYABLE (₹)", bill.getTotalAmount()));
        sb.append("====================================================================\n\n");
        sb.append("Computer Generated Tax Invoice. No signature required.\n");
        sb.append("Thank you for choosing ").append(HospitalConstants.HOSPITAL_NAME).append(".\n");

        txtReceipt.setText(sb.toString());
        txtReceipt.setCaretPosition(0);
    }

    private void exportReceipt() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Invoice_INV_" + bill.getId() + ".txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                File target = chooser.getSelectedFile();
                List<String> lines = new ArrayList<>();
                for (String line : txtReceipt.getText().split("\n")) {
                    lines.add(line);
                }
                FileUtil.exportToTextFile(target, "Official Hospital Invoice", lines);
                JOptionPane.showMessageDialog(this, "Invoice exported successfully to:\n" + target.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
