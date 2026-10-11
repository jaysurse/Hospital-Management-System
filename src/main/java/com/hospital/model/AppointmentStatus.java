package com.hospital.model;

/**
 * Lifecycle states of an appointment.
 */
public enum AppointmentStatus {
    Scheduled("Scheduled"),
    Completed("Completed"),
    Cancelled("Cancelled"),

    // Uppercase enum aliases
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AppointmentStatus fromString(String text) {
        if (text == null) return Scheduled;
        for (AppointmentStatus s : AppointmentStatus.values()) {
            if (s.name().equalsIgnoreCase(text.trim()) || s.label.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return Scheduled;
    }
}
