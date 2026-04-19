package com.election.dao;

import com.election.model.Voter;
import com.election.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Voter operations
 */
public class VoterDAO {

    // ─── CREATE ────────────────────────────────────────────────────────────────

    public boolean registerVoter(Voter voter) throws SQLException {
        String sql = "INSERT INTO voters (full_name, email, phone, national_id, age, address) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, voter.getFullName());
            ps.setString(2, voter.getEmail());
            ps.setString(3, voter.getPhone());
            ps.setString(4, voter.getNationalId());
            ps.setInt(5, voter.getAge());
            ps.setString(6, voter.getAddress());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) voter.setVoterId(rs.getInt(1));
                return true;
            }
            return false;
        }
    }

    // ─── READ ──────────────────────────────────────────────────────────────────

    public Voter findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM voters WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public Voter findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM voters WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public Voter findByNationalId(String nationalId) throws SQLException {
        String sql = "SELECT * FROM voters WHERE national_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nationalId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public Voter findById(int voterId) throws SQLException {
        String sql = "SELECT * FROM voters WHERE voter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Voter> getAllVoters() throws SQLException {
        List<Voter> list = new ArrayList<>();
        String sql = "SELECT * FROM voters ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────

    public boolean markVoterVerified(int voterId) throws SQLException {
        String sql = "UPDATE voters SET is_verified = TRUE WHERE voter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markVoterVoted(int voterId) throws SQLException {
        String sql = "UPDATE voters SET has_voted = TRUE WHERE voter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── STATS ─────────────────────────────────────────────────────────────────

    public int getTotalVoters() throws SQLException {
        String sql = "SELECT COUNT(*) FROM voters";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getTotalVoted() throws SQLException {
        String sql = "SELECT COUNT(*) FROM voters WHERE has_voted = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ─── MAPPER ────────────────────────────────────────────────────────────────

    private Voter mapRow(ResultSet rs) throws SQLException {
        Voter v = new Voter();
        v.setVoterId(rs.getInt("voter_id"));
        v.setFullName(rs.getString("full_name"));
        v.setEmail(rs.getString("email"));
        v.setPhone(rs.getString("phone"));
        v.setNationalId(rs.getString("national_id"));
        v.setAge(rs.getInt("age"));
        v.setAddress(rs.getString("address"));
        v.setVerified(rs.getBoolean("is_verified"));
        v.setHasVoted(rs.getBoolean("has_voted"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) v.setCreatedAt(ts.toLocalDateTime());
        return v;
    }
}
