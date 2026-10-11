package com.hospital.model;

/**
 * Payment settlement status for medical invoices.
 */
public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid");

    private final String display;

    PaymentStatus(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public static PaymentStatus fromString(String text) {
        if (text == null) return PENDING;
        for (PaymentStatus p : PaymentStatus.values()) {
            if (p.name().equalsIgnoreCase(text.trim()) || p.display.equalsIgnoreCase(text.trim())) {
                return p;
            }
        }
        return PENDING;
    }
}
