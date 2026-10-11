package com.hospital.model;

import java.time.LocalDate;
import com.hospital.util.DateUtil;

/**
 * Concrete Doctor entity extending Person.
 *
 * OOP Principles Demonstrated:
 * 1. Inheritance: Subclasses Person, inheriting core personal details
 * 2. Encapsulation: Protects professional attributes, fee bounds, and room assignments
 * 3. Polymorphism: Overrides getDetails() and getRole()
 * 4. Method Overloading: Provides multiple constructors for different setup workflows
 */
public class Doctor extends Person {

    private String specialization;
    private String qualification;
    private double consultationFee;
    private boolean available;
    private String roomNumber;
    private LocalDate joiningDate;

    /**
     * Default constructor (Overloading)
     */
    public Doctor() {
        super();
        this.available = true;
        this.consultationFee = 500.00;
        this.joiningDate = LocalDate.now();
    }

    /**
     * Concise constructor (Overloading)
     */
    public Doctor(String fullName, String specialization, String phone, double consultationFee, String roomNumber) {
        super(fullName, phone, "N/A", "Hospital Staff Quarters");
        this.specialization = specialization;
        this.qualification = "MBBS";
        this.consultationFee = consultationFee;
        this.roomNumber = roomNumber;
        this.available = true;
        this.joiningDate = LocalDate.now();
        setEmail(fullName.toLowerCase().replaceAll("[^a-z0-9]", ".") + "@hospital.org");
    }

    /**
     * Full parameterized constructor (Overloading)
     */
    public Doctor(int id, String fullName, String specialization, String qualification, 
                  String phone, String email, double consultationFee, boolean available, 
                  String roomNumber, LocalDate joiningDate) {
        super(id, fullName, phone, email, "N/A", "Hospital Quarters");
        this.specialization = specialization;
        this.qualification = qualification;
        this.consultationFee = consultationFee;
        this.available = available;
        this.roomNumber = roomNumber;
        this.joiningDate = (joiningDate != null) ? joiningDate : LocalDate.now();
    }

    /**
     * Runtime Polymorphism: Overrides Person.getDetails()
     */
    @Override
    public String getDetails() {
        return String.format(
            "DOCTOR CREDENTIALS [ID #%d]\n" +
            "--------------------------------------------------\n" +
            "Full Name         : %s\n" +
            "Department/Spec   : %s\n" +
            "Qualifications    : %s\n" +
            "Consultation Fee  : ₹%.2f\n" +
            "Availability      : %s\n" +
            "OPD Room Number   : %s\n" +
            "Contact Phone     : %s\n" +
            "Email Address     : %s\n" +
            "Joined Service    : %s",
            getId(),
            getFullName(),
            specialization,
            qualification,
            consultationFee,
            (available ? "Available for Consultation" : "On Leave / Unavailable"),
            roomNumber,
            getPhone(),
            (getEmail() != null ? getEmail() : "N/A"),
            DateUtil.formatDate(joiningDate)
        );
    }

    /**
     * Runtime Polymorphism: Overrides Person.getRole()
     */
    @Override
    public String getRole() {
        return "Doctor";
    }

    // Getters and Setters

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        if (consultationFee < 0.0) {
            throw new IllegalArgumentException("Consultation fee cannot be negative");
        }
        this.consultationFee = consultationFee;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s, ₹%.0f)", 
            getFullName(), specialization, roomNumber, consultationFee);
    }
}
