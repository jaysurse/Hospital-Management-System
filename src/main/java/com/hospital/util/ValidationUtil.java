package com.hospital.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Robust input validation utility for clinical and administrative data.
 * Validates names, phones, emails, blood groups, and numerical fields.
 */
public final class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s\\-]{7,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Set<String> VALID_BLOOD_GROUPS = new HashSet<>(
        Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "UNKNOWN")
    );

    private ValidationUtil() {
        // Prevent instantiation
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return true; // Optional field
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidBloodGroup(String bg) {
        if (bg == null) return false;
        return VALID_BLOOD_GROUPS.contains(bg.trim().toUpperCase());
    }

    public static boolean isValidAge(int age) {
        return age >= 0 && age <= 130;
    }

    public static boolean isPositiveDecimal(double value) {
        return value >= 0.0;
    }

    public static String sanitize(String input) {
        return input == null ? "" : input.trim();
    }
}
