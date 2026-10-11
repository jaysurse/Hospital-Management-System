package com.hospital.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import com.hospital.util.DateUtil;

/**
 * Appointment entity representing a scheduled clinical consultation.
 *
 * OOP Principles Demonstrated:
 * 1. Association & Aggregation: Associates Patient and Doctor entities
 * 2. Encapsulation: Strict accessor/mutator methods for time slots and status transitions
 */
public class Appointment implements Identifiable<Integer> {

    private int appointmentId;
    private int patientId;
    private int doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String reason;
    private AppointmentStatus status;
    private String notes;
    private Timestamp createdAt;

    // Aggregated Domain Entity References (Association)
    private Patient patient;
    private Doctor doctor;

    // Optional cached display properties for convenient JTable rendering
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;

    public Appointment() {
        this.status = AppointmentStatus.Scheduled;
        this.appointmentDate = LocalDate.now();
        this.appointmentTime = LocalTime.of(10, 0);
    }

    public Appointment(int patientId, int doctorId, LocalDate appointmentDate, LocalTime appointmentTime, String reason) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = AppointmentStatus.Scheduled;
    }

    public Appointment(int appointmentId, int patientId, int doctorId, LocalDate appointmentDate, 
                       LocalTime appointmentTime, String reason, AppointmentStatus status, 
                       String notes, Timestamp createdAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = (status != null) ? status : AppointmentStatus.Scheduled;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    @Override
    public Integer getId() {
        return appointmentId;
    }

    @Override
    public void setId(Integer id) {
        this.appointmentId = (id == null) ? 0 : id;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patientId = patient.getId();
            this.patientName = patient.getFullName();
        }
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.doctorId = doctor.getId();
            this.doctorName = doctor.getFullName();
            this.doctorSpecialization = doctor.getSpecialization();
        }
    }

    public String getPatientName() {
        return (patient != null) ? patient.getFullName() : (patientName != null ? patientName : "Patient #" + patientId);
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return (doctor != null) ? doctor.getFullName() : (doctorName != null ? doctorName : "Dr. #" + doctorId);
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return (doctor != null) ? doctor.getSpecialization() : doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getFormattedDateTime() {
        return DateUtil.formatDate(appointmentDate) + " at " + DateUtil.formatTime(appointmentTime);
    }

    @Override
    public String toString() {
        return String.format("Apt #%d: %s with %s on %s [%s]",
            appointmentId, getPatientName(), getDoctorName(), getFormattedDateTime(), status);
    }
}
