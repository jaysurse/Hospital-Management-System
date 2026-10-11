package com.hospital.service;

/**
 * Third-Party SMS Gateway SDK simulation (The Adaptee).
 * Has an incompatible method signature: sendTextMessage(String, String).
 */
class ThirdPartySmsService {
    public void sendTextMessage(String cellNumber, String text) {
        System.out.printf("[SMS GATEWAY -> %s]: %s%n", cellNumber, text);
    }
}

/**
 * Adapter Pattern Implementation:
 * Adapts ThirdPartySmsService to the application's standard NotificationService interface.
 */
public class SMSNotificationAdapter implements NotificationService {

    private final ThirdPartySmsService smsService;

    public SMSNotificationAdapter() {
        this.smsService = new ThirdPartySmsService();
    }

    public SMSNotificationAdapter(ThirdPartySmsService smsService) {
        this.smsService = smsService != null ? smsService : new ThirdPartySmsService();
    }

    @Override
    public void sendNotification(String recipient, String message) {
        if (recipient == null || recipient.trim().isEmpty()) {
            recipient = "Front-Desk";
        }
        smsService.sendTextMessage(recipient, message);
    }
}
