package com.hospital.config;

/**
 * Shared static constants for hospital administrative configuration.
 * Demonstrates static members and encapsulation of system-wide constants.
 */
public final class HospitalConstants {

    public static final String APP_NAME = "Hospital Management System (HMS)";
    public static final String APP_VERSION = "v1.0.0 (Enterprise OOP Edition)";
    public static final String HOSPITAL_NAME = "METROPOLITAN HEALTHCARE & RESEARCH CENTRE";
    public static final String HOSPITAL_TAGLINE = "Excellence in Clinical Care & Patient Service";
    public static final String HOSPITAL_ADDRESS = "Plot 42, Health City Avenue, Medical Enclave";
    public static final String HOSPITAL_PHONE = "+91 (020) 2765-4321 / Emergency: 108";
    public static final String HOSPITAL_EMAIL = "contact@metrohealth.org";

    public static final String[] SPECIALIZATIONS = {
        "Cardiology",
        "Pediatrics",
        "Orthopedics",
        "Neurology",
        "General Medicine",
        "Dermatology",
        "Oncology",
        "Gynecology & Obstetrics",
        "ENT (Otolaryngology)",
        "Ophthalmology",
        "Pulmonology",
        "Psychiatry"
    };

    public static final String[] BLOOD_GROUPS = {
        "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    public static final String[] GENDERS = {
        "Male", "Female", "Other"
    };

    public static final String[] APPOINTMENT_STATUSES = {
        "Scheduled", "Completed", "Cancelled"
    };

    public static final String[] PAYMENT_METHODS = {
        "CASH", "CARD", "UPI", "INSURANCE", "NET_BANKING"
    };

    private HospitalConstants() {
        // Prevent instantiation
    }
}
