package com.hospital.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import com.hospital.exception.AppointmentException;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.util.DateUtil;

/**
 * Data Access Object for clinical Appointments.
 * Enforces doctor scheduling conflict prevention and manages lifecycle transitions.
 */
public class AppointmentDAO implements GenericDAO<Appointment, Integer> {

    private static final String BASE_SELECT = 
        "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, a.appointment_time, " +
        "a.reason, a.status, a.notes, a.created_at, " +
        "p.full_name AS patient_name, d.full_name AS doctor_name, d.specialization AS doctor_specialization " +
        "FROM appointments a " +
        "JOIN patients p ON a.patient_id = p.patient_id " +
        "JOIN doctors d ON a.doctor_id = d.doctor_id ";

    @Override
    public Appointment save(Appointment appointment) throws DatabaseException {
        // Enforce conflict check
        if (!isSlotAvailable(appointment.getDoctorId(), appointment.getAppointmentDate(), 
                             appointment.getAppointmentTime(), null)) {
            throw new DatabaseException("Conflict detected: Doctor already has an active appointment at this date and time slot.");
        }

        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setDate(3, DateUtil.toSqlDate(appointment.getAppointmentDate()));
            ps.setTime(4, DateUtil.toSqlTime(appointment.getAppointmentTime()));
            ps.setString(5, appointment.getReason());
            ps.setString(6, appointment.getStatus().name());
            ps.setString(7, appointment.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    appointment.setId(rs.getInt(1));
                }
            }
            return findById(appointment.getId());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to schedule appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Appointment appointment) throws DatabaseException {
        // Check slot conflict excluding the current appointment itself
        if (!isSlotAvailable(appointment.getDoctorId(), appointment.getAppointmentDate(), 
                             appointment.getAppointmentTime(), appointment.getId())) {
            throw new DatabaseException("Conflict detected: Doctor already has an active appointment at this updated slot.");
        }

        String sql = "UPDATE appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, " +
                     "appointment_time = ?, reason = ?, status = ?, notes = ? WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setDate(3, DateUtil.toSqlDate(appointment.getAppointmentDate()));
            ps.setTime(4, DateUtil.toSqlTime(appointment.getAppointmentTime()));
            ps.setString(5, appointment.getReason());
            ps.setString(6, appointment.getStatus().name());
            ps.setString(7, appointment.getNotes());
            ps.setInt(8, appointment.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update appointment: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(int appointmentId, AppointmentStatus status) throws DatabaseException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update appointment status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Integer id) throws DatabaseException {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to cancel/delete appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public Appointment findById(Integer id) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE a.appointment_id = ?";
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
            throw new DatabaseException("Failed to retrieve appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Appointment> findAll() throws DatabaseException {
        String sql = BASE_SELECT + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve appointments list: " + e.getMessage(), e);
        }
    }

    public List<Appointment> findTodayAppointments() throws DatabaseException {
        String sql = BASE_SELECT + "WHERE a.appointment_date = CURRENT_DATE ORDER BY a.appointment_time ASC";
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve today's appointments: " + e.getMessage(), e);
        }
    }

    public List<Appointment> findByPatientId(int patientId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        List<Appointment> list = new ArrayList<>();
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
            throw new DatabaseException("Failed to retrieve patient appointments: " + e.getMessage(), e);
        }
    }

    public List<Appointment> findByDoctorId(int doctorId) throws DatabaseException {
        String sql = BASE_SELECT + "WHERE a.doctor_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        List<Appointment> list = new ArrayList<>();
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
            throw new DatabaseException("Failed to retrieve doctor appointments: " + e.getMessage(), e);
        }
    }

    public List<Appointment> search(String query, String statusFilter, LocalDate fromDate, LocalDate toDate) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (p.full_name LIKE ? OR d.full_name LIKE ? OR a.reason LIKE ?) ");
            String pat = "%" + query.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("All Statuses")) {
            sql.append("AND a.status = ? ");
            params.add(statusFilter.trim());
        }

        if (fromDate != null) {
            sql.append("AND a.appointment_date >= ? ");
            params.add(DateUtil.toSqlDate(fromDate));
        }

        if (toDate != null) {
            sql.append("AND a.appointment_date <= ? ");
            params.add(DateUtil.toSqlDate(toDate));
        }

        sql.append("ORDER BY a.appointment_date DESC, a.appointment_time DESC");

        List<Appointment> list = new ArrayList<>();
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
            throw new DatabaseException("Failed to search appointments: " + e.getMessage(), e);
        }
    }

    /**
     * Conflict Prevention: Checks whether a doctor has an existing non-cancelled appointment at this date/time.
     */
    public boolean isSlotAvailable(int doctorId, LocalDate date, LocalTime time, Integer excludeAppointmentId) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM appointments " +
            "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status != 'Cancelled' "
        );
        if (excludeAppointmentId != null) {
            sql.append("AND appointment_id != ?");
        }

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, doctorId);
            ps.setDate(2, DateUtil.toSqlDate(date));
            ps.setTime(3, DateUtil.toSqlTime(time));
            if (excludeAppointmentId != null) {
                ps.setInt(4, excludeAppointmentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (Exception e) {
            System.err.println("[AppointmentDAO] Slot check error: " + e.getMessage());
        }
        return false;
    }

    public int countTodayAppointments() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count today's appointments: " + e.getMessage(), e);
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Date appDate = rs.getDate("appointment_date");
        Time appTime = rs.getTime("appointment_time");

        Appointment apt = new Appointment(
            rs.getInt("appointment_id"),
            rs.getInt("patient_id"),
            rs.getInt("doctor_id"),
            appDate != null ? appDate.toLocalDate() : null,
            appTime != null ? appTime.toLocalTime() : null,
            rs.getString("reason"),
            AppointmentStatus.fromString(rs.getString("status")),
            rs.getString("notes"),
            rs.getTimestamp("created_at")
        );
        apt.setPatientName(rs.getString("patient_name"));
        apt.setDoctorName(rs.getString("doctor_name"));
        apt.setDoctorSpecialization(rs.getString("doctor_specialization"));
        return apt;
    }
}
