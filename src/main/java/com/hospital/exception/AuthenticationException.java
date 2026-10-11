package com.hospital.exception;

/**
 * Thrown when login authentication or user authorization checks fail.
 */
public class AuthenticationException extends HospitalException {
    
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
