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
import com.hospital.model.Patient;
import com.hospital.util.DateUtil;

/**
 * Data Access Object for Patient entity management.
 * 
 * OOP Principles Demonstrated:
 * 1. Method Overloading: Provides multiple overloaded search() methods
 * 2. PreparedStatement: Eliminates SQL injection risk across all operations
 */
public class PatientDAO implements GenericDAO<Patient, Integer> {

    @Override
    public Patient save(Patient patient) throws DatabaseException {
        String sql = "INSERT INTO patients (full_name, age, gender, phone, email, address, blood_group, emergency_contact, registration_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, patient.getFullName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setString(5, patient.getEmail());
            ps.setString(6, patient.getAddress());
            ps.setString(7, patient.getBloodGroup());
            ps.setString(8, patient.getEmergencyContact());
            ps.setDate(9, DateUtil.toSqlDate(patient.getRegistrationDate()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    patient.setId(rs.getInt(1));
                }
            }
            return patient;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to register patient: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Patient patient) throws DatabaseException {
        String sql = "UPDATE patients SET full_name = ?, age = ?, gender = ?, phone = ?, email = ?, " +
                     "address = ?, blood_group = ?, emergency_contact = ?, registration_date = ? " +
                     "WHERE patient_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getFullName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setString(5, patient.getEmail());
            ps.setString(6, patient.getAddress());
            ps.setString(7, patient.getBloodGroup());
            ps.setString(8, patient.getEmergencyContact());
            ps.setDate(9, DateUtil.toSqlDate(patient.getRegistrationDate()));
            ps.setInt(10, patient.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update patient: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DatabaseException {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete patient. Ensure related records are cleared: " + e.getMessage(), e);
        }
    }

    @Override
    public Patient findById(Integer id) throws DatabaseException {
        String sql = "SELECT patient_id, full_name, age, gender, phone, email, address, blood_group, emergency_contact, registration_date " +
                     "FROM patients WHERE patient_id = ?";
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
            throw new DatabaseException("Failed to locate patient by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Patient> findAll() throws DatabaseException {
        String sql = "SELECT patient_id, full_name, age, gender, phone, email, address, blood_group, emergency_contact, registration_date " +
                     "FROM patients ORDER BY patient_id DESC";
        List<Patient> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve patients list: " + e.getMessage(), e);
        }
    }

    /**
     * Method Overloading: Search patients by a unified keyword across ID, Name, or Phone.
     */
    public List<Patient> search(String keyword) throws DatabaseException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT patient_id, full_name, age, gender, phone, email, address, blood_group, emergency_contact, registration_date " +
                     "FROM patients WHERE full_name LIKE ? OR phone LIKE ? OR CAST(patient_id AS CHAR) LIKE ? " +
                     "ORDER BY full_name";
        List<Patient> list = new ArrayList<>();
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
            throw new DatabaseException("Error searching patients: " + e.getMessage(), e);
        }
    }

    /**
     * Method Overloading: Search patients by both Name and Phone filters.
     */
    public List<Patient> search(String name, String phone) throws DatabaseException {
        String sql = "SELECT patient_id, full_name, age, gender, phone, email, address, blood_group, emergency_contact, registration_date " +
                     "FROM patients WHERE full_name LIKE ? AND phone LIKE ? ORDER BY full_name";
        List<Patient> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + (name != null ? name.trim() : "") + "%");
            ps.setString(2, "%" + (phone != null ? phone.trim() : "") + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error searching patients by name and phone: " + e.getMessage(), e);
        }
    }

    /**
     * Returns total registered patient count for the dashboard.
     */
    public int count() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count patients: " + e.getMessage(), e);
        }
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        Date regDate = rs.getDate("registration_date");
        return new Patient(
            rs.getInt("patient_id"),
            rs.getString("full_name"),
            rs.getInt("age"),
            rs.getString("gender"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("address"),
            rs.getString("blood_group"),
            rs.getString("emergency_contact"),
            regDate != null ? regDate.toLocalDate() : null
        );
    }
}
