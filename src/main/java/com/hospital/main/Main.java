package com.hospital.main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.hospital.config.HospitalConstants;
import com.hospital.dao.DBConnection;
import com.hospital.ui.RoleSelectionFrame;

/**
 * Main application entry point for the Hospital Management System (HMS).
 * 
 * System Architecture:
 * - Desktop GUI: Pure Java Swing (AWT/Swing)
 * - Layered Architecture: UI -> Service -> DAO -> MySQL JDBC
 * - OOP Paradigm: Polymorphism, Inheritance, Encapsulation, Abstraction, Generics
 * - Design Patterns: Singleton, Iterator, Composition, Adapter
 */
public class Main {

    public static void main(String[] args) {
        // Configure UI Look and Feel
        setupLookAndFeel();

        System.out.println("====================================================================");
        System.out.println("  Starting " + HospitalConstants.HOSPITAL_NAME);
        System.out.println("  " + HospitalConstants.APP_NAME + " - " + HospitalConstants.APP_VERSION);
        System.out.println("====================================================================");

        // Pre-initialize database connection & schema
        try {
            DBConnection db = DBConnection.getInstance();
            System.out.println("[Database Engine] " + db.getActiveDatabaseInfo());
        } catch (Exception e) {
            System.err.println("[Database Engine] Initial warning: " + e.getMessage());
        }

        // Launch GUI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                RoleSelectionFrame roleFrame = new RoleSelectionFrame();
                roleFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void setupLookAndFeel() {
        try {
            // Attempt FlatLaf modern clinical theme
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLafClass.getMethod("setup").invoke(null);
        } catch (Throwable t) {
            // Fallback to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // High-DPI and anti-aliasing text hints
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }
}
