package com.hospital.exception;

/**
 * Thrown when financial, invoice, or payment transaction errors occur.
 */
public class BillingException extends HospitalException {
    
    public BillingException(String message) {
        super(message);
    }

    public BillingException(String message, Throwable cause) {
        super(message, cause);
    }
}
