package com.hospital.ui;

/**
 * Receptionist & Front Desk Portal Window.
 * 
 * Demonstrates Core OOP Principles:
 * 1. Inheritance: Subclasses MainDashboardFrame to reuse the layered administrative architecture
 * 2. Polymorphism: Can be treated as MainDashboardFrame across all embedded panels and dialogs
 * 3. Role-Based Customization: Displays the Receptionist Orange theme (#F59E0B) and front-desk access:
 *    - Fast patient intake & registration
 *    - Doctor directory & availability check
 *    - Appointment scheduling & slot conflict management
 *    - Billing & payment collection
 *    - Analytical reports & CSV/TXT data export
 */
public class ReceptionistDashboardFrame extends MainDashboardFrame {

    /**
     * Initializes the Receptionist Portal using receptionistMode = true.
     */
    public ReceptionistDashboardFrame() {
        super(true);
    }
}
