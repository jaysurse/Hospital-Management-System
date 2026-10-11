package com.hospital.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.AppointmentException;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.util.DateUtil;
import com.hospital.util.ValidationUtil;

/**
 * Service orchestrating appointment scheduling and conflict prevention.
 * Demonstrates business rule validation and notification integration (Adapter Pattern).
 */
public class AppointmentService {

    private final AppointmentDAO appointmentDAO;
    private final DoctorDAO doctorDAO;
    private final PatientDAO patientDAO;
    private NotificationService notificationService;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
        this.doctorDAO = new DoctorDAO();
        this.patientDAO = new PatientDAO();
        // Default to SMSNotificationAdapter demonstrating the Adapter Pattern
        this.notificationService = new SMSNotificationAdapter();
    }

    public AppointmentService(AppointmentDAO appointmentDAO, DoctorDAO doctorDAO, PatientDAO patientDAO, NotificationService notificationService) {
        this.appointmentDAO = appointmentDAO;
        this.doctorDAO = doctorDAO;
        this.patientDAO = patientDAO;
        this.notificationService = notificationService != null ? notificationService : new SMSNotificationAdapter();
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Schedules a new appointment after verifying doctor availability and slot conflict checks.
     */
    public Appointment scheduleAppointment(Appointment appointment) throws AppointmentException, DatabaseException {
        validateAppointment(appointment, false);

        Appointment saved = appointmentDAO.save(appointment);

        // Dispatch alert via NotificationAdapter (Adapter Pattern)
        if (notificationService != null && saved != null) {
            String msg = String.format(
                "Appointment confirmed for %s with %s on %s at %s. Ref: #%d",
                saved.getPatientName(), saved.getDoctorName(),
                DateUtil.formatDate(saved.getAppointmentDate()),
                DateUtil.formatTime(saved.getAppointmentTime()),
                saved.getId()
            );
            notificationService.sendNotification(saved.getPatientName(), msg);
        }

        return saved;
    }

    public boolean updateAppointment(Appointment appointment) throws AppointmentException, DatabaseException {
        if (appointment.getId() == null || appointment.getId() <= 0) {
            throw new AppointmentException("Cannot update appointment: Missing appointment ID.");
        }
        validateAppointment(appointment, true);
        return appointmentDAO.update(appointment);
    }

    public boolean cancelAppointment(int appointmentId) throws DatabaseException {
        Appointment apt = appointmentDAO.findById(appointmentId);
        boolean updated = appointmentDAO.updateStatus(appointmentId, AppointmentStatus.Cancelled);
        if (updated && apt != null && notificationService != null) {
            notificationService.sendNotification(
                apt.getPatientName(), 
                "Your appointment #" + appointmentId + " has been cancelled as requested."
            );
        }
        return updated;
    }

    public boolean completeAppointment(int appointmentId) throws DatabaseException {
        return appointmentDAO.updateStatus(appointmentId, AppointmentStatus.Completed);
    }

    public boolean deleteAppointment(int appointmentId) throws DatabaseException {
        return appointmentDAO.delete(appointmentId);
    }

    public Appointment getAppointmentById(int id) throws DatabaseException {
        return appointmentDAO.findById(id);
    }

    public List<Appointment> getAllAppointments() throws DatabaseException {
        return appointmentDAO.findAll();
    }

    public List<Appointment> getTodayAppointments() throws DatabaseException {
        return appointmentDAO.findTodayAppointments();
    }

    public int getTodayAppointmentsCount() throws DatabaseException {
        return appointmentDAO.countTodayAppointments();
    }

    public List<Appointment> searchAppointments(String query, String statusFilter, LocalDate fromDate, LocalDate toDate) throws DatabaseException {
        return appointmentDAO.search(query, statusFilter, fromDate, toDate);
    }

    public List<Appointment> getAppointmentsByPatientId(int patientId) throws DatabaseException {
        return appointmentDAO.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) throws DatabaseException {
        return appointmentDAO.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) throws DatabaseException {
        return appointmentDAO.findByDoctorId(doctorId);
    }

    public Appointment bookAppointment(int patientId, int doctorId, LocalDate date, LocalTime time, String reason) throws AppointmentException, DatabaseException {
        Appointment apt = new Appointment(patientId, doctorId, date, time, reason);
        return scheduleAppointment(apt);
    }

    public boolean updateStatus(int appointmentId, AppointmentStatus status) throws DatabaseException {
        return appointmentDAO.updateStatus(appointmentId, status);
    }

    private void validateAppointment(Appointment apt, boolean isUpdate) throws AppointmentException, DatabaseException {
        if (apt == null) {
            throw new AppointmentException("Appointment details cannot be null.");
        }
        if (apt.getPatientId() <= 0) {
            throw new AppointmentException("Please select an existing patient.");
        }
        if (apt.getDoctorId() <= 0) {
            throw new AppointmentException("Please select an assigned doctor.");
        }
        if (apt.getAppointmentDate() == null) {
            throw new AppointmentException("Appointment consultation date is required.");
        }
        if (apt.getAppointmentTime() == null) {
            throw new AppointmentException("Appointment consultation time is required.");
        }
        if (!ValidationUtil.isNotEmpty(apt.getReason())) {
            throw new AppointmentException("Chief complaint / reason for visit is required.");
        }

        // Verify patient existence
        Patient patient = patientDAO.findById(apt.getPatientId());
        if (patient == null) {
            throw new AppointmentException("Selected patient record not found in database.");
        }

        // Verify doctor existence and active status
        Doctor doctor = doctorDAO.findById(apt.getDoctorId());
        if (doctor == null) {
            throw new AppointmentException("Selected doctor not found in database.");
        }
        if (!doctor.isAvailable()) {
            throw new AppointmentException(String.format("Dr. %s is currently marked as unavailable / on leave.", doctor.getFullName()));
        }

        // Conflict Prevention: Check if the doctor already has a booking at this exact slot
        Integer excludeId = isUpdate ? apt.getId() : null;
        boolean available = appointmentDAO.isSlotAvailable(
            apt.getDoctorId(), 
            apt.getAppointmentDate(), 
            apt.getAppointmentTime(), 
            excludeId
        );

        if (!available) {
            throw new AppointmentException(String.format(
                "Scheduling Conflict: Dr. %s already has an active appointment on %s at %s. Please select another time slot.",
                doctor.getFullName(),
                DateUtil.formatDate(apt.getAppointmentDate()),
                DateUtil.formatTime(apt.getAppointmentTime())
            ));
        }
    }
}
