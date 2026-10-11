package com.hospital.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import com.hospital.service.ReportService;

/**
 * Hospital Reports & Administrative Audit Panel.
 * Generates clinical and financial summaries using the Iterator Pattern and exports to CSV / Text.
 */
public class ReportsPanel extends JPanel {

    private final ReportService reportService;
    private final MainDashboardFrame parentFrame;

    private JComboBox<String> cbReportType;
    private JTextArea txtSummaryMetrics;
    private DefaultTableModel tableModel;
    private JTable reportTable;
    private ReportService.ReportData currentReportData;

    public ReportsPanel(MainDashboardFrame parentFrame, ReportService reportService) {
        this.parentFrame = parentFrame;
        this.reportService = reportService;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initComponents();
        generateSelectedReport();
    }

    private void initComponents() {
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        // Top Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Theme.SURFACE);

        // Selector (Left)
        JPanel selectBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selectBox.setBackground(Theme.SURFACE);

        JLabel lblSelect = new JLabel("Select Report Type:");
        lblSelect.setFont(Theme.FONT_SUBHEADER);
        lblSelect.setForeground(Theme.TEXT_MAIN);

        cbReportType = UIHelper.createComboBox(new String[]{
            "Patient Demographics & Registration Report",
            "Clinical Appointment & Scheduling Report",
            "Hospital Revenue & Financial Audit Report"
        });
        cbReportType.setPreferredSize(new Dimension(340, 32));
        cbReportType.addActionListener(e -> generateSelectedReport());

        JButton btnRefresh = UIHelper.createOutlineButton("Generate / Refresh");
        btnRefresh.addActionListener(e -> generateSelectedReport());

        selectBox.add(lblSelect);
        selectBox.add(cbReportType);
        selectBox.add(btnRefresh);
        toolbar.add(selectBox, BorderLayout.WEST);

        // Export Actions (Right)
        JPanel exportActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exportActions.setBackground(Theme.SURFACE);

        JButton btnExportCsv = UIHelper.createPrimaryButton("Export to CSV (.csv)");
        btnExportCsv.addActionListener(e -> exportToCsv());

        JButton btnExportTxt = UIHelper.createSecondaryButton("Export to Text (.txt)");
        btnExportTxt.addActionListener(e -> exportToText());

        exportActions.add(btnExportCsv);
        exportActions.add(btnExportTxt);
        toolbar.add(exportActions, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Center Split / Summary + Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Theme.SURFACE);

        // Summary Statistics Banner
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(new java.awt.Color(0xF4, 0xF8, 0xFA));
        summaryPanel.setBorder(new LineBorder(Theme.BORDER));

        txtSummaryMetrics = new JTextArea(3, 40);
        txtSummaryMetrics.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        txtSummaryMetrics.setEditable(false);
        txtSummaryMetrics.setBackground(new java.awt.Color(0xF4, 0xF8, 0xFA));
        txtSummaryMetrics.setForeground(Theme.TEXT_MAIN);
        txtSummaryMetrics.setBorder(new EmptyBorder(8, 12, 8, 12));

        summaryPanel.add(txtSummaryMetrics, BorderLayout.CENTER);
        centerPanel.add(summaryPanel, BorderLayout.NORTH);

        // Report Table
        tableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        reportTable = UIHelper.createTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        card.add(centerPanel, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void generateSelectedReport() {
        int index = cbReportType.getSelectedIndex();
        try {
            switch (index) {
                case 0:
                    currentReportData = reportService.generatePatientReport();
                    break;
                case 1:
                    currentReportData = reportService.generateAppointmentReport();
                    break;
                case 2:
                    currentReportData = reportService.generateRevenueReport();
                    break;
                default:
                    currentReportData = reportService.generatePatientReport();
                    break;
            }

            // Update Summary
            StringBuilder sb = new StringBuilder();
            for (String line : currentReportData.getSummaryLines()) {
                sb.append(line).append("\n");
            }
            txtSummaryMetrics.setText(sb.toString().trim());

            // Update Table
            tableModel.setDataVector(
                toObjectArray(currentReportData.getRows()),
                currentReportData.getHeaders().toArray()
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to compile report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object[][] toObjectArray(List<List<String>> rows) {
        Object[][] data = new Object[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            data[i] = rows.get(i).toArray();
        }
        return data;
    }

    private void exportToCsv() {
        if (currentReportData == null) {
            JOptionPane.showMessageDialog(this, "No report data available to export.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String defaultName = currentReportData.getTitle().toLowerCase().replace(" ", "_") + ".csv";
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getParentFile(), target.getName() + ".csv");
            }
            try {
                reportService.exportToCSV(currentReportData, target);
                JOptionPane.showMessageDialog(this, "CSV Report exported successfully to:\n" + target.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "CSV Export failed: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToText() {
        if (currentReportData == null) {
            JOptionPane.showMessageDialog(this, "No report data available to export.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String defaultName = currentReportData.getTitle().toLowerCase().replace(" ", "_") + ".txt";
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".txt")) {
                target = new File(target.getParentFile(), target.getName() + ".txt");
            }
            try {
                reportService.exportToTextFile(currentReportData, target);
                JOptionPane.showMessageDialog(this, "Text Report exported successfully to:\n" + target.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Text Export failed: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
