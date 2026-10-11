package com.hospital.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Doctor;
import com.hospital.util.DateUtil;

/**
 * Data Access Object for Doctor management.
 * Demonstrates PreparedStatement usage and Method Overloading.
 */
public class DoctorDAO implements GenericDAO<Doctor, Integer> {

    @Override
    public Doctor save(Doctor doctor) throws DatabaseException {
        String sql = "INSERT INTO doctors (full_name, specialization, qualification, phone, email, consultation_fee, is_available, room_number, joining_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, doctor.getFullName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getQualification());
            ps.setString(4, doctor.getPhone());
            String email = doctor.getEmail();
            if (email == null || email.trim().isEmpty()) {
                email = "doctor." + System.currentTimeMillis() + "@hospital.org";
            }
            ps.setString(5, email);
            ps.setDouble(6, doctor.getConsultationFee());
            ps.setBoolean(7, doctor.isAvailable());
            ps.setString(8, doctor.getRoomNumber());
            ps.setDate(9, DateUtil.toSqlDate(doctor.getJoiningDate()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    doctor.setId(rs.getInt(1));
                }
            }
            return doctor;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Doctor doctor) throws DatabaseException {
        String sql = "UPDATE doctors SET full_name = ?, specialization = ?, qualification = ?, phone = ?, email = ?, " +
                     "consultation_fee = ?, is_available = ?, room_number = ?, joining_date = ? " +
                     "WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.getFullName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getQualification());
            ps.setString(4, doctor.getPhone());
            String email = doctor.getEmail();
            if (email == null || email.trim().isEmpty()) {
                email = "doctor." + doctor.getId() + "@hospital.org";
            }
            ps.setString(5, email);
            ps.setDouble(6, doctor.getConsultationFee());
            ps.setBoolean(7, doctor.isAvailable());
            ps.setString(8, doctor.getRoomNumber());
            ps.setDate(9, DateUtil.toSqlDate(doctor.getJoiningDate()));
            ps.setInt(10, doctor.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update doctor profile: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DatabaseException {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public Doctor findById(Integer id) throws DatabaseException {
        String sql = "SELECT doctor_id, full_name, specialization, qualification, phone, email, consultation_fee, is_available, room_number, joining_date " +
                     "FROM doctors WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find doctor by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Doctor> findAll() throws DatabaseException {
        String sql = "SELECT doctor_id, full_name, specialization, qualification, phone, email, consultation_fee, is_available, room_number, joining_date " +
                     "FROM doctors ORDER BY full_name";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve doctors list: " + e.getMessage(), e);
        }
    }

    public List<Doctor> findAvailableDoctors() throws DatabaseException {
        String sql = "SELECT doctor_id, full_name, specialization, qualification, phone, email, consultation_fee, is_available, room_number, joining_date " +
                     "FROM doctors WHERE is_available = TRUE ORDER BY specialization, full_name";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve available doctors: " + e.getMessage(), e);
        }
    }

    /**
     * Method Overloading: Search doctors by name, specialization, or phone.
     */
    public List<Doctor> search(String keyword) throws DatabaseException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT doctor_id, full_name, specialization, qualification, phone, email, consultation_fee, is_available, room_number, joining_date " +
                     "FROM doctors WHERE full_name LIKE ? OR specialization LIKE ? OR phone LIKE ? " +
                     "ORDER BY full_name";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error searching doctors: " + e.getMessage(), e);
        }
    }

    /**
     * Method Overloading: Filter doctors strictly by specialization department.
     */
    public List<Doctor> searchBySpecialization(String specialization) throws DatabaseException {
        if (specialization == null || specialization.equalsIgnoreCase("All Departments")) {
            return findAll();
        }
        String sql = "SELECT doctor_id, full_name, specialization, qualification, phone, email, consultation_fee, is_available, room_number, joining_date " +
                     "FROM doctors WHERE specialization = ? ORDER BY full_name";
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, specialization.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error filtering doctors by specialization: " + e.getMessage(), e);
        }
    }

    /**
     * Total doctor count for the dashboard.
     */
    public int count() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM doctors";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count doctors: " + e.getMessage(), e);
        }
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        Date joinDate = rs.getDate("joining_date");
        return new Doctor(
            rs.getInt("doctor_id"),
            rs.getString("full_name"),
            rs.getString("specialization"),
            rs.getString("qualification"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getDouble("consultation_fee"),
            rs.getBoolean("is_available"),
            rs.getString("room_number"),
            joinDate != null ? joinDate.toLocalDate() : null
        );
    }
}
