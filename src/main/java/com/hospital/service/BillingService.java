package com.hospital.service;

import java.util.List;
import com.hospital.dao.BillDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.BillingException;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Bill;
import com.hospital.model.BillItem;
import com.hospital.model.PaymentStatus;

/**
 * Service managing invoicing, medical charges, and revenue settlements.
 * Demonstrates transactional consistency and adapter pattern alerts.
 */
public class BillingService {

    private final BillDAO billDAO;
    private final PatientDAO patientDAO;
    private NotificationService notificationService;

    public BillingService() {
        this.billDAO = new BillDAO();
        this.patientDAO = new PatientDAO();
        this.notificationService = new SMSNotificationAdapter();
    }

    public BillingService(BillDAO billDAO, PatientDAO patientDAO, NotificationService notificationService) {
        this.billDAO = billDAO;
        this.patientDAO = patientDAO;
        this.notificationService = notificationService != null ? notificationService : new SMSNotificationAdapter();
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Generates a new invoice with atomic itemized composition.
     */
    public Bill generateBill(Bill bill) throws BillingException, DatabaseException {
        if (bill == null) {
            throw new BillingException("Bill data cannot be null.");
        }
        if (bill.getPatientId() <= 0) {
            throw new BillingException("Patient must be selected for invoice generation.");
        }
        if (patientDAO.findById(bill.getPatientId()) == null) {
            throw new BillingException("Selected patient record does not exist.");
        }

        // Recalculate totals before saving
        bill.calculateTotal();

        Bill saved = billDAO.save(bill);

        if (notificationService != null && saved != null) {
            notificationService.sendNotification(
                saved.getPatientName(),
                String.format("Invoice #%d generated. Grand Total: ₹%.2f (Status: %s)",
                    saved.getId(), saved.getTotalAmount(), saved.getPaymentStatus())
            );
        }

        return saved;
    }

    /**
     * Marks an existing invoice as settled/paid.
     */
    public boolean markAsPaid(int billId, String paymentMethod) throws DatabaseException {
        if (billId <= 0) {
            throw new IllegalArgumentException("Bill ID must be greater than zero.");
        }
        boolean updated = billDAO.markAsPaid(billId, paymentMethod);
        if (updated) {
            Bill bill = billDAO.findById(billId);
            if (bill != null && notificationService != null) {
                notificationService.sendNotification(
                    bill.getPatientName(),
                    String.format("Payment of ₹%.2f received via %s for Invoice #%d. Thank you!",
                        bill.getTotalAmount(), paymentMethod, billId)
                );
            }
        }
        return updated;
    }

    public boolean processPayment(int billId, String paymentMethod) throws DatabaseException {
        return markAsPaid(billId, paymentMethod);
    }

    public List<Bill> getBillsByPatient(int patientId) throws DatabaseException {
        return billDAO.findByPatientId(patientId);
    }

    public boolean updateBill(Bill bill) throws BillingException, DatabaseException {
        if (bill.getId() == null || bill.getId() <= 0) {
            throw new BillingException("Cannot update invoice: Missing bill ID.");
        }
        bill.calculateTotal();
        return billDAO.update(bill);
    }

    public boolean deleteBill(int billId) throws DatabaseException {
        return billDAO.delete(billId);
    }

    public Bill getBillById(int id) throws DatabaseException {
        return billDAO.findById(id);
    }

    public List<Bill> getAllBills() throws DatabaseException {
        return billDAO.findAll();
    }

    public List<Bill> getPendingBills() throws DatabaseException {
        return billDAO.findPendingBills();
    }

    public List<Bill> searchBills(String query, String status) throws DatabaseException {
        return billDAO.search(query, status);
    }

    public int getPendingBillsCount() throws DatabaseException {
        return billDAO.countPendingBills();
    }

    public double getTotalCollectedRevenue() throws DatabaseException {
        return billDAO.sumCollectedRevenue();
    }

    public double getTotalPendingAmount() throws DatabaseException {
        return billDAO.sumPendingAmount();
    }
}
