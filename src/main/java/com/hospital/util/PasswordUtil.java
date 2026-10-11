package com.hospital.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility for cryptographic password hashing and salt generation using SHA-256.
 * Demonstrates secure credential handling in Java applications.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Hashes a plain-text password concatenated with a salt using SHA-256.
     *
     * @param password Plain-text password
     * @param salt Unique per-user salt
     * @return Hex-encoded 64-character SHA-256 hash
     */
    public static String hashPassword(String password, String salt) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (salt == null) {
            salt = "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available on this JVM", e);
        }
    }

    /**
     * Generates a random hexadecimal salt string.
     *
     * @return 16-byte random hex string (32 chars)
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Verifies whether an entered password matches the stored hash for a given salt.
     */
    public static boolean verifyPassword(String enteredPassword, String salt, String expectedHash) {
        if (enteredPassword == null || expectedHash == null) {
            return false;
        }
        String calculated = hashPassword(enteredPassword, salt);
        return MessageDigest.isEqual(
            calculated.getBytes(StandardCharsets.UTF_8),
            expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
