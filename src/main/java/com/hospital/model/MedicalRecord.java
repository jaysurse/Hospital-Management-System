package com.hospital.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import com.hospital.util.DateUtil;

/**
 * Clinical Medical Record storing diagnostic notes, prescriptions, and physician comments.
 */
public class MedicalRecord implements Identifiable<Integer> {

    private int recordId;
    private Integer appointmentId;
    private int patientId;
    private int doctorId;
    private String diagnosis;
    private String prescription;
    private String treatmentNotes;
    private LocalDateTime recordedDate;

    // Display helpers for clinical history views
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;

    public MedicalRecord() {
        this.recordedDate = LocalDateTime.now();
    }

    public MedicalRecord(Integer appointmentId, int patientId, int doctorId, 
                         String diagnosis, String prescription, String treatmentNotes) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.treatmentNotes = treatmentNotes;
        this.recordedDate = LocalDateTime.now();
    }

    public MedicalRecord(int recordId, Integer appointmentId, int patientId, int doctorId, 
                         String diagnosis, String prescription, String treatmentNotes, 
                         LocalDateTime recordedDate) {
        this.recordId = recordId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.treatmentNotes = treatmentNotes;
        this.recordedDate = (recordedDate != null) ? recordedDate : LocalDateTime.now();
    }

    @Override
    public Integer getId() {
        return recordId;
    }

    @Override
    public void setId(Integer id) {
        this.recordId = (id == null) ? 0 : id;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
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

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getTreatmentNotes() {
        return treatmentNotes;
    }

    public void setTreatmentNotes(String treatmentNotes) {
        this.treatmentNotes = treatmentNotes;
    }

    public LocalDateTime getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(LocalDateTime recordedDate) {
        this.recordedDate = recordedDate;
    }

    public String getPatientName() {
        return patientName != null ? patientName : "Patient #" + patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName != null ? doctorName : "Dr. #" + doctorId;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getFormattedDate() {
        return DateUtil.formatTimestamp(DateUtil.toSqlTimestamp(recordedDate));
    }

    @Override
    public String toString() {
        return String.format("Record #%d for %s by %s [%s]",
            recordId, getPatientName(), getDoctorName(), getFormattedDate());
    }
}
