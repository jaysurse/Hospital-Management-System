package com.hospital.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Bill;
import com.hospital.model.BillItem;
import com.hospital.model.PaymentStatus;

/**
 * Data Access Object for Hospital Invoicing and Billing.
 * 
 * Database Requirements Demonstrated:
 * 1. Transactions: Uses conn.setAutoCommit(false), commit(), and rollback() 
 *    to atomically persist a Bill and all its composed BillItems.
 * 2. Referential Integrity: Synchronizes parent-child records cleanly.
 */
public class BillDAO implements GenericDAO<Bill, Integer> {

    private static final String BASE_SELECT = 
        "SELECT b.bill_id, b.appointment_id, b.patient_id, b.consultation_fee, " +
        "b.additional_charges, b.tax_amount, b.discount, b.total_amount, " +
        "b.payment_status, b.payment_method, b.bill_date, b.paid_date, " +
        "p.full_name AS patient_name, p.phone AS patient_phone, " +
        "d.full_name AS doctor_name " +
        "FROM bills b " +
        "JOIN patients p ON b.patient_id = p.patient_id " +
        "LEFT JOIN appointments a ON b.appointment_id = a.appointment_id " +
        "LEFT JOIN doctors d ON a.doctor_id = d.doctor_id ";

    /**
     * Atomically saves a Bill and all composed BillItems within a single JDBC transaction.
     */
    @Override
    public Bill save(Bill bill) throws DatabaseException {
        String insertBillSql = 
            "INSERT INTO bills (appointment_id, patient_id, consultation_fee, additional_charges, " +
            "tax_amount, discount, total_amount, payment_status, payment_method, bill_date, paid_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String insertItemSql = "INSERT INTO bill_items (bill_id, description, amount) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            try (PreparedStatement psBill = conn.prepareStatement(insertBillSql, Statement.RETURN_GENERATED_KEYS)) {
                if (bill.getAppointmentId() != null) {
                    psBill.setInt(1, bill.getAppointmentId());
                } else {
                    psBill.setNull(1, Types.INTEGER);
                }
                psBill.setInt(2, bill.getPatientId());
                psBill.setDouble(3, bill.getConsultationFee());
                psBill.setDouble(4, bill.getAdditionalCharges());
                psBill.setDouble(5, bill.getTaxAmount());
                psBill.setDouble(6, bill.getDiscount());
                psBill.setDouble(7, bill.getTotalAmount());
                psBill.setString(8, bill.getPaymentStatus().name());
                psBill.setString(9, bill.getPaymentMethod());
                psBill.setTimestamp(10, Timestamp.valueOf(bill.getBillDate()));
                psBill.setTimestamp(11, bill.getPaidDate() != null ? Timestamp.valueOf(bill.getPaidDate()) : null);

                psBill.executeUpdate();
                try (ResultSet rs = psBill.getGeneratedKeys()) {
                    if (rs.next()) {
                        bill.setId(rs.getInt(1));
                    }
                }
            }

            // Persist Composed BillItems within the same transaction
            if (!bill.getItems().isEmpty()) {
                try (PreparedStatement psItem = conn.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (BillItem item : bill.getItems()) {
                        psItem.setInt(1, bill.getId());
                        psItem.setString(2, item.getDescription());
                        psItem.setDouble(3, item.getAmount());
                        psItem.executeUpdate();
                        try (ResultSet rs = psItem.getGeneratedKeys()) {
                            if (rs.next()) {
                                item.setId(rs.getInt(1));
                            }
                        }
                    }
                }
            }

            conn.commit(); // Commit Transaction Atomically
            return findById(bill.getId());
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("[BillDAO] Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw new DatabaseException("Transaction failed: Unable to save bill and line items: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset connection state
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public boolean update(Bill bill) throws DatabaseException {
        String sql = "UPDATE bills SET appointment_id = ?, patient_id = ?, consultation_fee = ?, " +
                     "additional_charges = ?, tax_amount = ?, discount = ?, total_amount = ?, " +
                     "payment_status = ?, payment_method = ?, paid_date = ? WHERE bill_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (bill.getAppointmentId() != null) {
                ps.setInt(1, bill.getAppointmentId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, bill.getPatientId());
            ps.setDouble(3, bill.getConsultationFee());
            ps.setDouble(4, bill.getAdditionalCharges());
            ps.setDouble(5, bill.getTaxAmount());
            ps.setDouble(6, bill.getDiscount());
            ps.setDouble(7, bill.getTotalAmount());
            ps.setString(8, bill.getPaymentStatus().name());
            ps.setString(9, bill.getPaymentMethod());
            ps.setTimestamp(10, bill.getPaidDate() != null ? Timestamp.valueOf(bill.getPaidDate()) : null);
            ps.setInt(11, bill.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update bill: " + e.getMessage(), e);
        }
    }

    public boolean markAsPaid(int billId, String paymentMethod) throws DatabaseException {
        String sql = "UPDATE bills SET payment_status = 'PAID', payment_method = ?, paid_date = CURRENT_TIMESTAMP WHERE bill_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, (paymentMethod != null && !paymentMethod.isEmpty()) ? paymentMethod : "CASH");
            ps.setInt(2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to mark bill as paid: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DatabaseException {
        String sql = "DELETE FROM bills WHERE bill_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete bill: " + e.getMessage(), e);
        }
    }

    @Override
    public Bill findById(Integer id) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE b.bill_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Bill bill = mapRow(rs);
                    loadBillItems(conn, bill);
                    return bill;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find bill by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Bill> findAll() throws DatabaseException {
        String sql = BASE_SELECT + "ORDER BY b.bill_date DESC";
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve bills: " + e.getMessage(), e);
        }
    }

    public List<Bill> findPendingBills() throws DatabaseException {
        String sql = BASE_SELECT + "WHERE b.payment_status = 'PENDING' ORDER BY b.bill_date DESC";
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve pending bills: " + e.getMessage(), e);
        }
    }

    public List<Bill> findByPatientId(int patientId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE b.patient_id = ? ORDER BY b.bill_date DESC";
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve bills for patient: " + e.getMessage(), e);
        }
    }

    public List<Bill> search(String query, String statusFilter) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (p.full_name LIKE ? OR p.phone LIKE ? OR CAST(b.bill_id AS CHAR) LIKE ?) ");
            String pat = "%" + query.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("All Statuses")) {
            sql.append("AND b.payment_status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }

        sql.append("ORDER BY b.bill_date DESC");

        List<Bill> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search bills: " + e.getMessage(), e);
        }
    }

    public int countPendingBills() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM bills WHERE payment_status = 'PENDING'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count pending bills: " + e.getMessage(), e);
        }
    }

    public double sumCollectedRevenue() throws DatabaseException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.0) FROM bills WHERE payment_status = 'PAID'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate total collected revenue: " + e.getMessage(), e);
        }
    }

    public double sumPendingAmount() throws DatabaseException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.0) FROM bills WHERE payment_status = 'PENDING'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate pending amount: " + e.getMessage(), e);
        }
    }

    private void loadBillItems(Connection conn, Bill bill) {
        String sql = "SELECT item_id, bill_id, description, amount FROM bill_items WHERE bill_id = ? ORDER BY item_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bill.getId());
            try (ResultSet rs = ps.executeQuery()) {
                bill.clearItems();
                while (rs.next()) {
                    bill.addItem(new BillItem(
                        rs.getInt("item_id"),
                        rs.getInt("bill_id"),
                        rs.getString("description"),
                        rs.getDouble("amount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] Warning: Failed to load bill items: " + e.getMessage());
        }
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Timestamp billTs = rs.getTimestamp("bill_date");
        Timestamp paidTs = rs.getTimestamp("paid_date");
        int aptId = rs.getInt("appointment_id");
        Integer appointmentId = rs.wasNull() ? null : aptId;

        Bill bill = new Bill(
            rs.getInt("bill_id"),
            appointmentId,
            rs.getInt("patient_id"),
            rs.getDouble("consultation_fee"),
            rs.getDouble("additional_charges"),
            rs.getDouble("tax_amount"),
            rs.getDouble("discount"),
            rs.getDouble("total_amount"),
            PaymentStatus.fromString(rs.getString("payment_status")),
            rs.getString("payment_method"),
            billTs != null ? billTs.toLocalDateTime() : null,
            paidTs != null ? paidTs.toLocalDateTime() : null
        );
        bill.setPatientName(rs.getString("patient_name"));
        bill.setPatientPhone(rs.getString("patient_phone"));
        bill.setDoctorName(rs.getString("doctor_name"));
        return bill;
    }
}
