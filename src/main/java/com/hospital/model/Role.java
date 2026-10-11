package com.hospital.model;

/**
 * User roles for access control and authorization.
 */
public enum Role {
    ADMIN("Administrator"),
    DOCTOR("Doctor"),
    RECEPTIONIST("Receptionist / Front Desk"),
    PATIENT("Patient");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String text) {
        if (text == null) return RECEPTIONIST;
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(text.trim())) {
                return r;
            }
        }
        return RECEPTIONIST;
    }
}
