package com.election.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Security Utility for password hashing and validation
 */
public class SecurityUtil {

    /**
     * Hash a password/string using SHA-256
     */
    public static String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Validate phone number (10 digits)
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[6-9]\\d{9}$");
    }

    /**
     * Validate National ID (Aadhaar-like 12 digits)
     */
    public static boolean isValidNationalId(String id) {
        return id != null && id.matches("^\\d{12}$");
    }

    /**
     * Mask email for display: ab***@gmail.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) return local + domain;
        return local.substring(0, 2) + "***" + domain;
    }

    /**
     * Mask phone: ******7890
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return "******" + phone.substring(phone.length() - 4);
    }
}
