package com.hospital.service;

import java.util.List;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.exception.HospitalException;
import com.hospital.model.MedicalRecord;
import com.hospital.util.ValidationUtil;

/**
 * Service managing clinical diagnoses, prescriptions, and patient medical histories.
 */
public class MedicalRecordService {

    private final MedicalRecordDAO medicalRecordDAO;
    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;

    public MedicalRecordService() {
        this.medicalRecordDAO = new MedicalRecordDAO();
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
    }

    public MedicalRecordService(MedicalRecordDAO medicalRecordDAO, PatientDAO patientDAO, DoctorDAO doctorDAO) {
        this.medicalRecordDAO = medicalRecordDAO;
        this.patientDAO = patientDAO;
        this.doctorDAO = doctorDAO;
    }

    public MedicalRecord addRecord(MedicalRecord record) throws HospitalException {
        validateRecord(record);
        return medicalRecordDAO.save(record);
    }

    public boolean updateRecord(MedicalRecord record) throws HospitalException {
        if (record.getId() == null || record.getId() <= 0) {
            throw new HospitalException("Cannot update clinical record: Missing record ID.");
        }
        validateRecord(record);
        return medicalRecordDAO.update(record);
    }

    public boolean deleteRecord(int recordId) throws DatabaseException {
        if (recordId <= 0) {
            throw new IllegalArgumentException("Record ID must be greater than zero.");
        }
        return medicalRecordDAO.delete(recordId);
    }

    public MedicalRecord getRecordById(int recordId) throws DatabaseException {
        return medicalRecordDAO.findById(recordId);
    }

    public List<MedicalRecord> getAllRecords() throws DatabaseException {
        return medicalRecordDAO.findAll();
    }

    public List<MedicalRecord> getRecordsByPatientId(int patientId) throws DatabaseException {
        return medicalRecordDAO.findByPatientId(patientId);
    }

    public List<MedicalRecord> getRecordsByPatient(int patientId) throws DatabaseException {
        return medicalRecordDAO.findByPatientId(patientId);
    }

    public List<MedicalRecord> getRecordsByDoctor(int doctorId) throws DatabaseException {
        return medicalRecordDAO.findByDoctorId(doctorId);
    }

    public List<MedicalRecord> searchRecords(String keyword) throws DatabaseException {
        return medicalRecordDAO.search(keyword);
    }

    private void validateRecord(MedicalRecord record) throws HospitalException {
        if (record == null) {
            throw new HospitalException("Medical record cannot be null.");
        }
        if (record.getPatientId() <= 0) {
            throw new HospitalException("A valid patient must be associated with this medical record.");
        }
        if (record.getDoctorId() <= 0) {
            throw new HospitalException("A consulting physician must be assigned.");
        }
        if (!ValidationUtil.isNotEmpty(record.getDiagnosis())) {
            throw new HospitalException("Clinical diagnosis statement is required.");
        }
        if (!ValidationUtil.isNotEmpty(record.getPrescription())) {
            throw new HospitalException("Prescription details and medication instructions are required.");
        }

        // Verify patient and doctor existence
        if (patientDAO.findById(record.getPatientId()) == null) {
            throw new HospitalException("Patient record #" + record.getPatientId() + " does not exist.");
        }
        if (doctorDAO.findById(record.getDoctorId()) == null) {
            throw new HospitalException("Doctor record #" + record.getDoctorId() + " does not exist.");
        }
    }
}
