package com.hospital.service;

import java.util.List;
import com.hospital.dao.DoctorDAO;
import com.hospital.exception.DatabaseException;
import com.hospital.exception.HospitalException;
import com.hospital.model.Doctor;
import com.hospital.util.ValidationUtil;

/**
 * Service encapsulating physician business logic, credentials, and scheduling availability.
 */
public class DoctorService {

    private final DoctorDAO doctorDAO;

    public DoctorService() {
        this.doctorDAO = new DoctorDAO();
    }

    public DoctorService(DoctorDAO doctorDAO) {
        this.doctorDAO = doctorDAO;
    }

    public Doctor addDoctor(Doctor doctor) throws HospitalException {
        validateDoctor(doctor);
        return doctorDAO.save(doctor);
    }

    public boolean updateDoctor(Doctor doctor) throws HospitalException {
        if (doctor.getId() == null || doctor.getId() <= 0) {
            throw new HospitalException("Cannot update doctor: Missing doctor ID.");
        }
        validateDoctor(doctor);
        return doctorDAO.update(doctor);
    }

    public boolean deleteDoctor(int doctorId) throws DatabaseException {
        if (doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be greater than zero.");
        }
        return doctorDAO.delete(doctorId);
    }

    public Doctor getDoctorById(int id) throws DatabaseException {
        return doctorDAO.findById(id);
    }

    public List<Doctor> getAllDoctors() throws DatabaseException {
        return doctorDAO.findAll();
    }

    public List<Doctor> getAvailableDoctors() throws DatabaseException {
        return doctorDAO.findAvailableDoctors();
    }

    public List<Doctor> searchDoctors(String keyword) throws DatabaseException {
        return doctorDAO.search(keyword);
    }

    public List<Doctor> filterBySpecialization(String specialization) throws DatabaseException {
        return doctorDAO.searchBySpecialization(specialization);
    }

    public int getDoctorCount() throws DatabaseException {
        return doctorDAO.count();
    }

    private void validateDoctor(Doctor doctor) throws HospitalException {
        if (doctor == null) {
            throw new HospitalException("Doctor data cannot be null.");
        }
        if (!ValidationUtil.isNotEmpty(doctor.getFullName())) {
            throw new HospitalException("Doctor full name is mandatory.");
        }
        if (!ValidationUtil.isNotEmpty(doctor.getSpecialization())) {
            throw new HospitalException("Medical specialization department is required.");
        }
        if (!ValidationUtil.isNotEmpty(doctor.getQualification())) {
            throw new HospitalException("Medical qualification (e.g. MBBS, MD) is required.");
        }
        if (!ValidationUtil.isValidPhone(doctor.getPhone())) {
            throw new HospitalException("Valid doctor contact phone number is required.");
        }
        if (ValidationUtil.isNotEmpty(doctor.getEmail()) && !ValidationUtil.isValidEmail(doctor.getEmail())) {
            throw new HospitalException("Invalid doctor email address.");
        }
        if (doctor.getConsultationFee() < 0.0) {
            throw new HospitalException("Consultation fee cannot be negative.");
        }
        if (!ValidationUtil.isNotEmpty(doctor.getRoomNumber())) {
            throw new HospitalException("OPD Room Number is required for outpatient scheduling.");
        }
    }
}
