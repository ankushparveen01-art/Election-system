package com.election.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection — creates a fresh connection for every call.
 * This avoids stale connection bugs when multiple DAOs share one socket.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/election_system"
            + "?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Ankush@123";   // ← PUT YOUR MYSQL PASSWORD HERE

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found!", e);
        }
    }

    /**
     * Returns a brand-new connection every time.
     * Each DAO closes its own connection via try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    // ── Legacy singleton support (keeps old getInstance() calls working) ──────
    private static DatabaseConnection instance;
    private DatabaseConnection() {}
    public static DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }
    public void closeConnection() { /* nothing to close — connections are per-query */ }
}
