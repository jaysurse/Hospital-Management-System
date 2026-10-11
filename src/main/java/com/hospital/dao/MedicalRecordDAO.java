package com.hospital.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import com.hospital.exception.DatabaseException;
import com.hospital.model.MedicalRecord;

/**
 * Data Access Object for Clinical Medical Records and Diagnostic History.
 */
public class MedicalRecordDAO implements GenericDAO<MedicalRecord, Integer> {

    private static final String BASE_SELECT = 
        "SELECT m.record_id, m.appointment_id, m.patient_id, m.doctor_id, " +
        "m.diagnosis, m.prescription, m.treatment_notes, m.recorded_date, " +
        "p.full_name AS patient_name, d.full_name AS doctor_name, d.specialization AS doctor_specialization " +
        "FROM medical_records m " +
        "JOIN patients p ON m.patient_id = p.patient_id " +
        "JOIN doctors d ON m.doctor_id = d.doctor_id ";

    @Override
    public MedicalRecord save(MedicalRecord record) throws DatabaseException {
        String sql = "INSERT INTO medical_records (appointment_id, patient_id, doctor_id, diagnosis, prescription, treatment_notes, recorded_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (record.getAppointmentId() != null) {
                ps.setInt(1, record.getAppointmentId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, record.getPatientId());
            ps.setInt(3, record.getDoctorId());
            ps.setString(4, record.getDiagnosis());
            ps.setString(5, record.getPrescription());
            ps.setString(6, record.getTreatmentNotes());
            ps.setTimestamp(7, Timestamp.valueOf(record.getRecordedDate()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setId(rs.getInt(1));
                }
            }
            return findById(record.getId());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save clinical medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(MedicalRecord record) throws DatabaseException {
        String sql = "UPDATE medical_records SET appointment_id = ?, patient_id = ?, doctor_id = ?, " +
                     "diagnosis = ?, prescription = ?, treatment_notes = ? WHERE record_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (record.getAppointmentId() != null) {
                ps.setInt(1, record.getAppointmentId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, record.getPatientId());
            ps.setInt(3, record.getDoctorId());
            ps.setString(4, record.getDiagnosis());
            ps.setString(5, record.getPrescription());
            ps.setString(6, record.getTreatmentNotes());
            ps.setInt(7, record.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DatabaseException {
        String sql = "DELETE FROM medical_records WHERE record_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public MedicalRecord findById(Integer id) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE m.record_id = ?";
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
            throw new DatabaseException("Failed to find medical record by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MedicalRecord> findAll() throws DatabaseException {
        String sql = BASE_SELECT + "ORDER BY m.recorded_date DESC";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load medical records: " + e.getMessage(), e);
        }
    }

    public List<MedicalRecord> findByPatientId(int patientId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE m.patient_id = ? ORDER BY m.recorded_date DESC";
        List<MedicalRecord> list = new ArrayList<>();
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
            throw new DatabaseException("Failed to load medical history for patient: " + e.getMessage(), e);
        }
    }

    public List<MedicalRecord> findByDoctorId(int doctorId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE m.doctor_id = ? ORDER BY m.recorded_date DESC";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load records for doctor: " + e.getMessage(), e);
        }
    }

    public List<MedicalRecord> search(String keyword) throws DatabaseException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String pattern = "%" + keyword.trim() + "%";
        String sql = BASE_SELECT + "WHERE p.full_name LIKE ? OR d.full_name LIKE ? OR m.diagnosis LIKE ? OR m.prescription LIKE ? " +
                     "ORDER BY m.recorded_date DESC";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search medical records: " + e.getMessage(), e);
        }
    }

    private MedicalRecord mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("recorded_date");
        int aptId = rs.getInt("appointment_id");
        Integer appointmentId = rs.wasNull() ? null : aptId;

        MedicalRecord record = new MedicalRecord(
            rs.getInt("record_id"),
            appointmentId,
            rs.getInt("patient_id"),
            rs.getInt("doctor_id"),
            rs.getString("diagnosis"),
            rs.getString("prescription"),
            rs.getString("treatment_notes"),
            ts != null ? ts.toLocalDateTime() : null
        );
        record.setPatientName(rs.getString("patient_name"));
        record.setDoctorName(rs.getString("doctor_name"));
        record.setDoctorSpecialization(rs.getString("doctor_specialization"));
        return record;
    }
}
