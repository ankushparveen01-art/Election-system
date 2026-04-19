package com.election.dao;

import com.election.model.OTPToken;
import com.election.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object for OTP operations — fresh connection per query
 */
public class OTPDAO {

    public boolean saveOTP(OTPToken token) throws SQLException {
        invalidateExistingOTPs(token.getVoterId(), token.getPurpose());

        String sql = "INSERT INTO otp_tokens (voter_id, otp_code, purpose, expires_at) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, token.getVoterId());
            ps.setString(2, token.getOtpCode());
            ps.setString(3, token.getPurpose());
            ps.setTimestamp(4, Timestamp.valueOf(token.getExpiresAt()));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) token.setOtpId(rs.getInt(1));
                return true;
            }
            return false;
        }
    }

    /**
     * Retrieve latest valid OTP — compares expires_at against Java time
     * to avoid MySQL timezone mismatch issues
     */
    public OTPToken getValidOTP(int voterId, String purpose) throws SQLException {
        // Fetch all unused OTPs for this voter+purpose, check expiry in Java
        String sql = "SELECT * FROM otp_tokens " +
                     "WHERE voter_id = ? AND purpose = ? AND is_used = FALSE " +
                     "ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            ps.setString(2, purpose);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                OTPToken t = new OTPToken();
                t.setOtpId(rs.getInt("otp_id"));
                t.setVoterId(rs.getInt("voter_id"));
                t.setOtpCode(rs.getString("otp_code"));
                t.setPurpose(rs.getString("purpose"));
                t.setUsed(rs.getBoolean("is_used"));
                Timestamp exp = rs.getTimestamp("expires_at");
                if (exp != null) {
                    LocalDateTime expiresAt = exp.toLocalDateTime();
                    t.setExpiresAt(expiresAt);
                    // Check expiry in Java (avoids MySQL timezone mismatch)
                    if (LocalDateTime.now().isAfter(expiresAt)) {
                        System.out.println("  [OTP expired at " + expiresAt + "]");
                        return null;
                    }
                }
                return t;
            }
        }
        return null;
    }

    public boolean markOTPUsed(int otpId) throws SQLException {
        String sql = "UPDATE otp_tokens SET is_used = TRUE WHERE otp_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, otpId);
            return ps.executeUpdate() > 0;
        }
    }

    private void invalidateExistingOTPs(int voterId, String purpose) throws SQLException {
        String sql = "UPDATE otp_tokens SET is_used = TRUE " +
                     "WHERE voter_id = ? AND purpose = ? AND is_used = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            ps.setString(2, purpose);
            ps.executeUpdate();
        }
    }
}
