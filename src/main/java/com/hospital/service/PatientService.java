package com.hospital.service;

import java.util.List;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.exception.InvalidPatientException;
import com.hospital.model.Patient;
import com.hospital.util.ValidationUtil;

/**
 * Service encapsulating patient business rules, data validation, and operations.
 */
public class PatientService {

    private final PatientDAO patientDAO;

    public PatientService() {
        this.patientDAO = new PatientDAO();
    }

    public PatientService(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    public Patient registerPatient(Patient patient) throws InvalidPatientException, DatabaseException {
        validatePatient(patient);
        return patientDAO.save(patient);
    }

    public boolean updatePatient(Patient patient) throws InvalidPatientException, DatabaseException {
        if (patient.getId() == null || patient.getId() <= 0) {
            throw new InvalidPatientException("Cannot update patient: Invalid or missing Patient ID.");
        }
        validatePatient(patient);
        return patientDAO.update(patient);
    }

    public boolean deletePatient(int patientId) throws DatabaseException {
        if (patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be greater than zero.");
        }
        return patientDAO.delete(patientId);
    }

    public Patient getPatientById(int id) throws DatabaseException {
        return patientDAO.findById(id);
    }

    public List<Patient> getAllPatients() throws DatabaseException {
        return patientDAO.findAll();
    }

    public List<Patient> searchPatients(String query) throws DatabaseException {
        return patientDAO.search(query);
    }

    public int getPatientCount() throws DatabaseException {
        return patientDAO.count();
    }

    /**
     * Business validation logic for patient profiles.
     */
    private void validatePatient(Patient patient) throws InvalidPatientException {
        if (patient == null) {
            throw new InvalidPatientException("Patient data cannot be null.");
        }
        if (!ValidationUtil.isNotEmpty(patient.getFullName())) {
            throw new InvalidPatientException("Patient full name is mandatory.");
        }
        if (patient.getFullName().trim().length() < 2) {
            throw new InvalidPatientException("Patient name must contain at least 2 characters.");
        }
        if (!ValidationUtil.isValidAge(patient.getAge())) {
            throw new InvalidPatientException("Patient age must be between 0 and 130 years.");
        }
        if (!ValidationUtil.isNotEmpty(patient.getGender())) {
            throw new InvalidPatientException("Gender selection is mandatory.");
        }
        if (!ValidationUtil.isValidPhone(patient.getPhone())) {
            throw new InvalidPatientException("Valid contact phone number is required (7 to 20 digits).");
        }
        if (ValidationUtil.isNotEmpty(patient.getEmail()) && !ValidationUtil.isValidEmail(patient.getEmail())) {
            throw new InvalidPatientException("Provided email format is invalid.");
        }
        if (!ValidationUtil.isNotEmpty(patient.getAddress())) {
            throw new InvalidPatientException("Residential address is required for hospital registration.");
        }
        if (!ValidationUtil.isValidBloodGroup(patient.getBloodGroup())) {
            throw new InvalidPatientException("Invalid blood group selected. Must be one of: A+, A-, B+, B-, AB+, AB-, O+, O-.");
        }
    }
}
