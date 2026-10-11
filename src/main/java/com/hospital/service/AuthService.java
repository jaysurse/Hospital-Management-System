package com.hospital.service;

import java.util.List;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.UserDAO;
import com.hospital.exception.AuthenticationException;
import com.hospital.exception.DatabaseException;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.Role;
import com.hospital.model.User;

/**
 * Service managing user authentication, sessions, and role checks.
 * Supports both staff credentials (users table) and patient verification (patients table).
 */
public class AuthService {

    private static volatile AuthService instance;
    private final UserDAO userDAO;
    private final DoctorDAO doctorDAO;
    private final PatientDAO patientDAO;
    private User currentUser;
    private Doctor currentDoctor;
    private Patient currentPatient;

    private AuthService() {
        this.userDAO = new UserDAO();
        this.doctorDAO = new DoctorDAO();
        this.patientDAO = new PatientDAO();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            synchronized (AuthService.class) {
                if (instance == null) {
                    instance = new AuthService();
                }
            }
        }
        return instance;
    }

    /**
     * Authenticates staff user against MySQL users table.
     *
     * @param username Entered username
     * @param password Plain-text password
     * @return Authenticated User
     * @throws AuthenticationException on invalid credentials
     */
    public synchronized User login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Username and password are required.");
        }

        try {
            User user = userDAO.authenticate(username.trim(), password.trim());
            if (user == null) {
                throw new AuthenticationException("Invalid username or password. Please check your credentials.");
            }
            this.currentUser = user;
            this.currentPatient = null;
            this.currentDoctor = null;

            // If the user is a doctor, link to the doctor record
            if (user.getRole() == Role.DOCTOR) {
                linkDoctorProfile(user);
            }

            return user;
        } catch (DatabaseException e) {
            throw new AuthenticationException("Authentication service error: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates a patient using their Patient ID and registered Phone number.
     * Connects directly to the MySQL patients table.
     */
    public synchronized Patient loginPatient(int patientId, String phone) throws AuthenticationException {
        if (patientId <= 0) {
            throw new AuthenticationException("Please enter a valid Patient ID number.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new AuthenticationException("Please enter your registered contact phone number.");
        }

        try {
            Patient patient = patientDAO.findById(patientId);
            if (patient == null) {
                throw new AuthenticationException("Patient #" + patientId + " not found in hospital records.");
            }

            // Clean digits to ensure reliable comparison regardless of formatting
            String storedDigits = patient.getPhone().replaceAll("[^0-9]", "");
            String inputDigits = phone.trim().replaceAll("[^0-9]", "");

            boolean matched = storedDigits.equals(inputDigits) ||
                             (storedDigits.length() >= 10 && inputDigits.length() >= 10 &&
                              (storedDigits.endsWith(inputDigits) || inputDigits.endsWith(storedDigits)));

            if (!matched) {
                throw new AuthenticationException("Phone number does not match registered records for Patient #" + patientId + ".");
            }

            this.currentPatient = patient;
            this.currentDoctor = null;

            // Create patient user session
            User pUser = new User();
            pUser.setUserId(patient.getId());
            pUser.setFullName(patient.getFullName());
            pUser.setEmail(patient.getEmail());
            pUser.setRole(Role.PATIENT);
            pUser.setAssociatedPatientId(patient.getId());
            this.currentUser = pUser;

            return patient;
        } catch (DatabaseException e) {
            throw new AuthenticationException("Patient verification error: " + e.getMessage(), e);
        }
    }

    private void linkDoctorProfile(User user) {
        try {
            List<Doctor> doctors = doctorDAO.findAll();
            for (Doctor d : doctors) {
                // Match by email or full name
                if (d.getEmail() != null && d.getEmail().equalsIgnoreCase(user.getEmail())) {
                    this.currentDoctor = d;
                    return;
                }
                if (d.getFullName() != null && d.getFullName().equalsIgnoreCase(user.getFullName())) {
                    this.currentDoctor = d;
                    return;
                }
            }
            // Fallback: search by user full name
            List<Doctor> matched = doctorDAO.search(user.getFullName());
            if (!matched.isEmpty()) {
                this.currentDoctor = matched.get(0);
            }
        } catch (Exception ignored) {
        }
    }

    public synchronized void logout() {
        this.currentUser = null;
        this.currentDoctor = null;
        this.currentPatient = null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized Doctor getCurrentDoctor() {
        return currentDoctor;
    }

    public synchronized Patient getCurrentPatient() {
        return currentPatient;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public synchronized boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
}
