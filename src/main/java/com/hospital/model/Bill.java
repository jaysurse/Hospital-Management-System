package com.hospital.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hospital.util.DateUtil;

/**
 * Hospital Medical Invoice entity.
 *
 * OOP Principles Demonstrated:
 * 1. Composition: Manages lifecycle of composed BillItem entities
 * 2. Encapsulation: Protects monetary calculations and payment state transitions
 * 3. Aggregation: Aggregates references to Patient and Appointment
 */
public class Bill implements Identifiable<Integer> {

    private int billId;
    private Integer appointmentId;
    private int patientId;
    private double consultationFee;
    private double additionalCharges;
    private double taxAmount;
    private double discount;
    private double totalAmount;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDateTime billDate;
    private LocalDateTime paidDate;

    // Composition: Line items that belong exclusively to this bill
    private final List<BillItem> items = new ArrayList<>();

    // Cached display helpers
    private String patientName;
    private String patientPhone;
    private String doctorName;

    public Bill() {
        this.paymentStatus = PaymentStatus.PENDING;
        this.billDate = LocalDateTime.now();
    }

    public Bill(Integer appointmentId, int patientId, double consultationFee, double additionalCharges) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.consultationFee = consultationFee;
        this.additionalCharges = additionalCharges;
        this.paymentStatus = PaymentStatus.PENDING;
        this.billDate = LocalDateTime.now();
        calculateTotal();
    }

    public Bill(int billId, Integer appointmentId, int patientId, double consultationFee, 
                double additionalCharges, double taxAmount, double discount, double totalAmount, 
                PaymentStatus paymentStatus, String paymentMethod, LocalDateTime billDate, 
                LocalDateTime paidDate) {
        this.billId = billId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.consultationFee = consultationFee;
        this.additionalCharges = additionalCharges;
        this.taxAmount = taxAmount;
        this.discount = discount;
        this.totalAmount = totalAmount;
        this.paymentStatus = (paymentStatus != null) ? paymentStatus : PaymentStatus.PENDING;
        this.paymentMethod = paymentMethod;
        this.billDate = (billDate != null) ? billDate : LocalDateTime.now();
        this.paidDate = paidDate;
    }

    /**
     * Composition method: Adds a line item to the bill and updates totals.
     */
    public void addItem(BillItem item) {
        if (item != null) {
            item.setBillId(this.billId);
            items.add(item);
        }
    }

    public void addItems(List<BillItem> newItems) {
        if (newItems != null) {
            for (BillItem item : newItems) {
                addItem(item);
            }
        }
    }

    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void clearItems() {
        items.clear();
    }

    /**
     * Automatic Total Calculation:
     * Subtotal = Consultation Fee + Additional Charges
     * Tax = 5% of Subtotal (rounded)
     * Grand Total = Subtotal + Tax - Discount
     */
    public void calculateTotal() {
        double subtotal = consultationFee + additionalCharges;
        if (subtotal < 0) subtotal = 0.0;
        
        // 5% standard healthcare surcharge / service tax
        this.taxAmount = Math.round(subtotal * 0.05 * 100.0) / 100.0;
        double net = subtotal + this.taxAmount - discount;
        this.totalAmount = Math.max(0.0, Math.round(net * 100.0) / 100.0);
    }

    @Override
    public Integer getId() {
        return billId;
    }

    @Override
    public void setId(Integer id) {
        this.billId = (id == null) ? 0 : id;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
        for (BillItem item : items) {
            item.setBillId(billId);
        }
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = Math.max(0.0, consultationFee);
    }

    public double getAdditionalCharges() {
        return additionalCharges;
    }

    public void setAdditionalCharges(double additionalCharges) {
        this.additionalCharges = Math.max(0.0, additionalCharges);
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = Math.max(0.0, discount);
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public String getPatientName() {
        return patientName != null ? patientName : "Patient #" + patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone != null ? patientPhone : "-";
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getDoctorName() {
        return doctorName != null ? doctorName : "-";
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getFormattedDate() {
        return DateUtil.formatTimestamp(DateUtil.toSqlTimestamp(billDate));
    }

    @Override
    public String toString() {
        return String.format("Invoice #%d: %s - Total: ₹%.2f [%s]",
            billId, getPatientName(), totalAmount, paymentStatus);
    }
}
