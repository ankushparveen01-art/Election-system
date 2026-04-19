package com.election.dao;

import com.election.util.DatabaseConnection;
import com.election.util.SecurityUtil;

import java.sql.*;

/**
 * Data Access Object for Admin authentication
 */
public class AdminDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Authenticate admin with username and hashed password
     */
    public boolean authenticateAdmin(String username, String password) throws SQLException {
        String passwordHash = SecurityUtil.sha256Hash(password);
        String sql = "SELECT COUNT(*) FROM admins WHERE username = ? AND password_hash = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    /**
     * Get admin email by username
     */
    public String getAdminEmail(String username) throws SQLException {
        String sql = "SELECT email FROM admins WHERE username = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("email");
        }
        return null;
    }
}
