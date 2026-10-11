package com.hospital.model;

import java.util.Objects;

/**
 * Abstract base class representing a human individual within the hospital domain.
 * 
 * OOP Principles Demonstrated:
 * 1. Abstraction: Provides abstract contract methods getDetails() and getRole()
 * 2. Encapsulation: Strict private fields with public validated accessors/mutators
 * 3. Inheritance: Serves as superclass for Patient and Doctor entities
 * 4. Polymorphism: Allows parent references to invoke overridden subclass behaviors
 */
public abstract class Person implements Identifiable<Integer> {

    private int id;
    private String fullName;
    private String phone;
    private String email;
    private String gender;
    private String address;

    /**
     * Default constructor (Method Overloading)
     */
    public Person() {
    }

    /**
     * Parameterized constructor for new entities without pre-assigned ID (Method Overloading)
     */
    public Person(String fullName, String phone, String gender, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.gender = gender;
        this.address = address;
    }

    /**
     * Full parameterized constructor (Method Overloading)
     */
    public Person(int id, String fullName, String phone, String email, String gender, String address) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.address = address;
    }

    /**
     * Abstract method to be overridden by subclasses (Runtime Polymorphism).
     * Returns a formatted summary of individual clinical or professional details.
     */
    public abstract String getDetails();

    /**
     * Abstract method returning the person's functional role in the hospital.
     */
    public abstract String getRole();

    // Getters and Setters (Encapsulation)

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = (id == null) ? 0 : id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return fullName + " (ID: " + id + ", Phone: " + phone + ")";
    }
}
