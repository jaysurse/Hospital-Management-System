package com.hospital.model;

import java.time.LocalDate;
import com.hospital.util.DateUtil;

/**
 * Concrete Patient entity inheriting from abstract Person.
 * 
 * OOP Principles Demonstrated:
 * 1. Inheritance: Extends Person, inheriting core identity fields
 * 2. Encapsulation: Protects clinical fields (blood group, age, emergency contact)
 * 3. Polymorphism: Overrides getDetails() and getRole()
 * 4. Method Overloading: Provides multiple overloaded constructors
 */
public class Patient extends Person {

    private int age;
    private String bloodGroup;
    private String emergencyContact;
    private LocalDate registrationDate;

    /**
     * Default constructor (Overloading)
     */
    public Patient() {
        super();
        this.registrationDate = LocalDate.now();
    }

    /**
     * Parameterized constructor for quick patient intake (Overloading)
     */
    public Patient(String fullName, int age, String gender, String phone, String address, String bloodGroup) {
        super(fullName, phone, gender, address);
        this.age = age;
        this.bloodGroup = bloodGroup;
        this.registrationDate = LocalDate.now();
    }

    /**
     * Full parameterized constructor (Overloading)
     */
    public Patient(int id, String fullName, int age, String gender, String phone, String email, 
                   String address, String bloodGroup, String emergencyContact, LocalDate registrationDate) {
        super(id, fullName, phone, email, gender, address);
        this.age = age;
        this.bloodGroup = bloodGroup;
        this.emergencyContact = emergencyContact;
        this.registrationDate = (registrationDate != null) ? registrationDate : LocalDate.now();
    }

    /**
     * Runtime Polymorphism: Overrides Person.getDetails()
     */
    @Override
    public String getDetails() {
        return String.format(
            "PATIENT PROFILE [ID #%d]\n" +
            "--------------------------------------------------\n" +
            "Full Name         : %s\n" +
            "Age / Gender      : %d yrs / %s\n" +
            "Blood Group       : %s\n" +
            "Contact Phone     : %s\n" +
            "Email Address     : %s\n" +
            "Residential Addr  : %s\n" +
            "Emergency Contact : %s\n" +
            "Registered Since  : %s",
            getId(),
            getFullName(),
            age,
            getGender(),
            (bloodGroup != null ? bloodGroup : "N/A"),
            getPhone(),
            (getEmail() != null && !getEmail().isEmpty() ? getEmail() : "N/A"),
            getAddress(),
            (emergencyContact != null && !emergencyContact.isEmpty() ? emergencyContact : "None"),
            DateUtil.formatDate(registrationDate)
        );
    }

    /**
     * Runtime Polymorphism: Overrides Person.getRole()
     */
    @Override
    public String getRole() {
        return "Patient";
    }

    // Getters and Setters

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 0 || age > 130) {
            throw new IllegalArgumentException("Invalid patient age: " + age);
        }
        this.age = age;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d, %s, %d yrs, BG: %s)", 
            getFullName(), getId(), getGender(), age, bloodGroup);
    }
}
