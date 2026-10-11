package com.hospital.exception;

/**
 * Thrown when patient validation or constraint checks fail.
 */
public class InvalidPatientException extends HospitalException {
    
    public InvalidPatientException(String message) {
        super(message);
    }

    public InvalidPatientException(String message, Throwable cause) {
        super(message, cause);
    }
}
