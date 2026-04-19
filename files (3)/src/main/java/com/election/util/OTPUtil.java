package com.election.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * OTP Utility — sends real OTP to voter's email via Gmail SMTP
 * No API key needed — just Gmail + App Password
 */
public class OTPUtil {

    private static final int OTP_LENGTH      = 6;
    private static final int OTP_EXPIRY_MINS = 5;
    private static final SecureRandom random  = new SecureRandom();

    // ─── Gmail Configuration ───────────────────────────────────────────────────
    // Step 1: Use YOUR Gmail address below
    // Step 2: Generate App Password → Google Account → Security → 2-Step → App Passwords
    //         (search "App Passwords" in your Google Account settings)
    // Step 3: Paste the 16-character app password below (no spaces)
    private static final String GMAIL_USER     = "";   // ← your Gmail
    private static final String GMAIL_PASSWORD = "";   // ← 16-char app password

    private static String lastOTP = "";

    // ─── GENERATE ──────────────────────────────────────────────────────────────

    public static String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        lastOTP = otp.toString();
        return lastOTP;
    }

    public static String getLastOTP()           { return lastOTP; }
    public static LocalDateTime getExpiryTime() { return LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINS); }
    public static boolean isOTPValid(LocalDateTime expiry) { return LocalDateTime.now().isBefore(expiry); }

    // ─── SEND OTP EMAIL ────────────────────────────────────────────────────────

    public static void sendOTPtoEmail(String toEmail, String otp, String purpose) {
        System.out.println("\n  Sending OTP email to: " + toEmail + " ...");

        try {
            // Gmail SMTP properties
            Properties props = new Properties();
            props.put("mail.smtp.host",            "smtp.gmail.com");
            props.put("mail.smtp.port",            "587");
            props.put("mail.smtp.auth",            "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout",           "10000");

            // Authenticator with Gmail credentials
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(GMAIL_USER, GMAIL_PASSWORD);
                }
            });

            // Build the email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(GMAIL_USER, "Election System"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your OTP — " + purpose);

            // HTML email body
            String html = buildEmailHTML(otp, purpose);
            message.setContent(html, "text/html; charset=utf-8");

            // Send
            Transport.send(message);

            System.out.println("  ✅ OTP email sent successfully to: " + toEmail);
            System.out.println("  Purpose : " + purpose);
            System.out.println("  OTP Code: " + otp);
            System.out.println("  Valid for: " + OTP_EXPIRY_MINS + " minutes\n");

        } catch (Exception e) {
            System.out.println("  ❌ Email failed: " + e.getMessage());
            System.out.println("  ℹ  OTP for testing: " + otp);
            System.out.println("  Check GMAIL_USER and GMAIL_PASSWORD in OTPUtil.java\n");
        }
    }

    // ─── SMS — terminal only (no API) ─────────────────────────────────────────

    public static void sendOTPtoPhone(String phone, String otp, String purpose) {
        // Logged to terminal only — SMS requires paid API
        System.out.println("  [SMS skipped] OTP sent via email instead.");
    }

    // ─── HTML EMAIL TEMPLATE ───────────────────────────────────────────────────

    private static String buildEmailHTML(String otp, String purpose) {
        return "<!DOCTYPE html><html><body style='"
                + "margin:0;padding:0;background:#0B0D17;font-family:Segoe UI,Arial,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'>"
                + "<tr><td align='center' style='padding:40px 20px;'>"
                + "<table width='480' style='background:#141828;border-radius:16px;overflow:hidden;"
                + "border:1px solid #272F4A;'>"

                // Header
                + "<tr><td style='background:#0F6E56;padding:28px 36px;text-align:center;'>"
                + "<p style='margin:0;font-size:28px;'>🗳</p>"
                + "<h1 style='margin:8px 0 0;color:#ffffff;font-size:20px;font-weight:600;'>"
                + "Secure Election System</h1>"
                + "</td></tr>"

                // Body
                + "<tr><td style='padding:36px;'>"
                + "<p style='color:#9CA3C0;font-size:14px;margin:0 0 8px;text-transform:uppercase;"
                + "letter-spacing:1px;'>" + purpose + " Verification</p>"
                + "<p style='color:#E5E9FF;font-size:15px;margin:0 0 28px;line-height:1.6;'>"
                + "Use the code below to verify your identity. "
                + "Do not share this code with anyone.</p>"

                // OTP Box
                + "<div style='background:#1C2235;border:2px solid #00C882;border-radius:12px;"
                + "padding:24px;text-align:center;margin-bottom:28px;'>"
                + "<p style='margin:0 0 6px;color:#6C758F;font-size:12px;'>Your OTP Code</p>"
                + "<p style='margin:0;font-size:48px;font-weight:700;letter-spacing:12px;"
                + "color:#00C882;font-family:Consolas,monospace;'>" + otp + "</p>"
                + "<p style='margin:8px 0 0;color:#6C758F;font-size:12px;'>Valid for "
                + OTP_EXPIRY_MINS + " minutes only</p>"
                + "</div>"

                + "<p style='color:#6C758F;font-size:12px;margin:0;line-height:1.6;'>"
                + "If you did not request this, please ignore this email.<br>"
                + "This is an automated message — do not reply.</p>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='background:#0D1020;padding:18px 36px;text-align:center;'>"
                + "<p style='margin:0;color:#3D4460;font-size:12px;'>"
                + "© 2026 Secure Election System</p>"
                + "</td></tr>"

                + "</table></td></tr></table></body></html>";
    }
}
