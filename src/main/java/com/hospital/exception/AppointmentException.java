package com.hospital.exception;

/**
 * Thrown when appointment scheduling conflicts or invalid date/time requests occur.
 */
public class AppointmentException extends HospitalException {
    
    public AppointmentException(String message) {
        super(message);
    }

    public AppointmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
