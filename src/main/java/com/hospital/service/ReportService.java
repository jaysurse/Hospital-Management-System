package com.hospital.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Bill;
import com.hospital.model.Patient;
import com.hospital.model.PaymentStatus;
import com.hospital.util.DateUtil;
import com.hospital.util.FileUtil;

/**
 * Service generating clinical and administrative audit reports.
 * 
 * OOP Principles Demonstrated:
 * 1. Iterator Pattern: Uses explicit Java Iterator<T> instances to traverse patient, appointment,
 *    and billing collections when compiling statistics.
 * 2. File I/O: Interfaces with FileUtil for CSV and formatted text export.
 */
public class ReportService {

    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;
    private final AppointmentDAO appointmentDAO;
    private final BillDAO billDAO;

    public ReportService() {
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.billDAO = new BillDAO();
    }

    public ReportService(PatientDAO patientDAO, DoctorDAO doctorDAO, AppointmentDAO appointmentDAO, BillDAO billDAO) {
        this.patientDAO = patientDAO;
        this.doctorDAO = doctorDAO;
        this.appointmentDAO = appointmentDAO;
        this.billDAO = billDAO;
    }

    /**
     * Compiles Patient Registration Report data using the Iterator Pattern.
     */
    public ReportData generatePatientReport() throws DatabaseException {
        List<Patient> patients = patientDAO.findAll();
        List<String> headers = Arrays.asList(
            "Patient ID", "Full Name", "Age", "Gender", "Phone", "Blood Group", "Address", "Registration Date"
        );

        List<List<String>> rows = new ArrayList<>();
        Map<String, Integer> bloodGroupCounts = new HashMap<>();

        // Traversal using the Iterator Pattern
        Iterator<Patient> it = patients.iterator();
        while (it.hasNext()) {
            Patient p = it.next();
            List<String> row = Arrays.asList(
                String.valueOf(p.getId()),
                p.getFullName(),
                String.valueOf(p.getAge()),
                p.getGender(),
                p.getPhone(),
                p.getBloodGroup(),
                p.getAddress(),
                DateUtil.formatDate(p.getRegistrationDate())
            );
            rows.add(row);

            // Tally metrics
            String bg = p.getBloodGroup();
            bloodGroupCounts.put(bg, bloodGroupCounts.getOrDefault(bg, 0) + 1);
        }

        List<String> summaryLines = new ArrayList<>();
        summaryLines.add(String.format("Total Registered Patients: %d", patients.size()));
        summaryLines.add("Blood Group Distribution:");
        for (Map.Entry<String, Integer> entry : bloodGroupCounts.entrySet()) {
            summaryLines.add(String.format("  - %-4s : %d patients", entry.getKey(), entry.getValue()));
        }

        return new ReportData("Patient Registration & Demographics Report", headers, rows, summaryLines);
    }

    /**
     * Compiles Appointment Scheduling Report using the Iterator Pattern.
     */
    public ReportData generateAppointmentReport() throws DatabaseException {
        List<Appointment> appointments = appointmentDAO.findAll();
        List<String> headers = Arrays.asList(
            "Appt ID", "Patient Name", "Doctor Name", "Specialization", "Date", "Time", "Status", "Reason"
        );

        List<List<String>> rows = new ArrayList<>();
        int scheduledCount = 0;
        int completedCount = 0;
        int cancelledCount = 0;

        // Traversal using the Iterator Pattern
        Iterator<Appointment> it = appointments.iterator();
        while (it.hasNext()) {
            Appointment a = it.next();
            List<String> row = Arrays.asList(
                String.valueOf(a.getId()),
                a.getPatientName(),
                a.getDoctorName(),
                a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "-",
                DateUtil.formatDate(a.getAppointmentDate()),
                DateUtil.formatTime(a.getAppointmentTime()),
                a.getStatus().name(),
                a.getReason()
            );
            rows.add(row);

            if (a.getStatus() == AppointmentStatus.Scheduled) scheduledCount++;
            else if (a.getStatus() == AppointmentStatus.Completed) completedCount++;
            else if (a.getStatus() == AppointmentStatus.Cancelled) cancelledCount++;
        }

        List<String> summaryLines = new ArrayList<>();
        summaryLines.add(String.format("Total Consultations Recorded : %d", appointments.size()));
        summaryLines.add(String.format("  - Currently Scheduled      : %d", scheduledCount));
        summaryLines.add(String.format("  - Successfully Completed   : %d", completedCount));
        summaryLines.add(String.format("  - Cancelled Bookings       : %d", cancelledCount));

        return new ReportData("Clinical Appointment & Scheduling Report", headers, rows, summaryLines);
    }

    /**
     * Compiles Revenue & Financial Summary Report using the Iterator Pattern.
     */
    public ReportData generateRevenueReport() throws DatabaseException {
        List<Bill> bills = billDAO.findAll();
        List<String> headers = Arrays.asList(
            "Bill ID", "Patient Name", "Consultation (₹)", "Additional (₹)", "Tax (₹)", "Discount (₹)", "Total (₹)", "Status", "Method", "Date"
        );

        List<List<String>> rows = new ArrayList<>();
        double totalBilled = 0.0;
        double totalPaid = 0.0;
        double totalPending = 0.0;
        int paidInvoices = 0;
        int pendingInvoices = 0;

        // Traversal using the Iterator Pattern
        Iterator<Bill> it = bills.iterator();
        while (it.hasNext()) {
            Bill b = it.next();
            List<String> row = Arrays.asList(
                String.valueOf(b.getId()),
                b.getPatientName(),
                String.format("%.2f", b.getConsultationFee()),
                String.format("%.2f", b.getAdditionalCharges()),
                String.format("%.2f", b.getTaxAmount()),
                String.format("%.2f", b.getDiscount()),
                String.format("%.2f", b.getTotalAmount()),
                b.getPaymentStatus().name(),
                b.getPaymentMethod() != null ? b.getPaymentMethod() : "-",
                b.getFormattedDate()
            );
            rows.add(row);

            totalBilled += b.getTotalAmount();
            if (b.getPaymentStatus() == PaymentStatus.PAID) {
                totalPaid += b.getTotalAmount();
                paidInvoices++;
            } else {
                totalPending += b.getTotalAmount();
                pendingInvoices++;
            }
        }

        List<String> summaryLines = new ArrayList<>();
        summaryLines.add(String.format("Total Invoices Generated : %d", bills.size()));
        summaryLines.add(String.format("Gross Billed Revenue     : ₹%.2f", totalBilled));
        summaryLines.add(String.format("Collected Revenue (Paid) : ₹%.2f (%d invoices)", totalPaid, paidInvoices));
        summaryLines.add(String.format("Outstanding Receivables  : ₹%.2f (%d pending)", totalPending, pendingInvoices));

        return new ReportData("Hospital Revenue & Financial Audit Report", headers, rows, summaryLines);
    }

    public void exportToCSV(ReportData report, File targetFile) throws IOException {
        FileUtil.exportToCSV(targetFile, report.getHeaders(), report.getRows());
    }

    public void exportToTextFile(ReportData report, File targetFile) throws IOException {
        List<String> content = new ArrayList<>(report.getSummaryLines());
        content.add("");
        content.add("TABLE DETAILS:");
        content.add(String.join(" | ", report.getHeaders()));
        content.add("--------------------------------------------------------------------------");
        for (List<String> row : report.getRows()) {
            content.add(String.join(" | ", row));
        }
        FileUtil.exportToTextFile(targetFile, report.getTitle(), content);
    }

    /**
     * DTO containing aggregated report contents.
     */
    public static class ReportData {
        private final String title;
        private final List<String> headers;
        private final List<List<String>> rows;
        private final List<String> summaryLines;

        public ReportData(String title, List<String> headers, List<List<String>> rows, List<String> summaryLines) {
            this.title = title;
            this.headers = headers;
            this.rows = rows;
            this.summaryLines = summaryLines;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public List<List<String>> getRows() {
            return rows;
        }

        public List<String> getSummaryLines() {
            return summaryLines;
        }
    }
}
