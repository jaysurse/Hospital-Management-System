package com.hospital.exception;

/**
 * Base custom checked exception for all Hospital Management System domain errors.
 * Demonstrates user-defined exception hierarchy and robust error handling in OOP.
 */
public class HospitalException extends Exception {
    
    public HospitalException(String message) {
        super(message);
    }

    public HospitalException(String message, Throwable cause) {
        super(message, cause);
    }
}
