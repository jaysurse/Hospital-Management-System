package com.hospital.service;

/**
 * Target interface for dispatching patient and administrative alerts.
 * Demonstrates the Adapter Design Pattern in OOP.
 */
public interface NotificationService {
    void sendNotification(String recipient, String message);
}
