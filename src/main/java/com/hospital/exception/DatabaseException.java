package com.hospital.exception;

/**
 * Wraps low-level JDBC SQLExceptions to isolate DAO and DB layers from business logic.
 */
public class DatabaseException extends HospitalException {
    
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
