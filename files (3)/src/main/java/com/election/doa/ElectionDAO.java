package com.election.dao;

import com.election.model.Candidate;
import com.election.model.Election;
import com.election.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Election and Candidate operations
 */
public class ElectionDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ─── ELECTIONS ─────────────────────────────────────────────────────────────

    public List<Election> getActiveElections() throws SQLException {
        List<Election> list = new ArrayList<>();
        String sql = "SELECT * FROM elections WHERE status = 'ACTIVE' ORDER BY start_date";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapElection(rs));
        }
        return list;
    }

    public List<Election> getAllElections() throws SQLException {
        List<Election> list = new ArrayList<>();
        String sql = "SELECT * FROM elections ORDER BY start_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapElection(rs));
        }
        return list;
    }

    public Election findElectionById(int id) throws SQLException {
        String sql = "SELECT * FROM elections WHERE election_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapElection(rs);
        }
        return null;
    }

    public boolean createElection(Election e) throws SQLException {
        String sql = "INSERT INTO elections (election_name, description, start_date, end_date, status) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getElectionName());
            ps.setString(2, e.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(e.getStartDate()));
            ps.setTimestamp(4, Timestamp.valueOf(e.getEndDate()));
            ps.setString(5, e.getStatus());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) e.setElectionId(rs.getInt(1));
                return true;
            }
            return false;
        }
    }

    public boolean updateElectionStatus(int electionId, String status) throws SQLException {
        String sql = "UPDATE elections SET status = ? WHERE election_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, electionId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── CANDIDATES ────────────────────────────────────────────────────────────

    public List<Candidate> getCandidatesForElection(int electionId) throws SQLException {
        List<Candidate> list = new ArrayList<>();
        String sql = """
                SELECT c.* FROM candidates c
                JOIN election_candidates ec ON c.candidate_id = ec.candidate_id
                WHERE ec.election_id = ? AND c.is_active = TRUE
                ORDER BY c.candidate_id
                """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, electionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapCandidate(rs));
        }
        return list;
    }

    public boolean addCandidate(Candidate c) throws SQLException {
        String sql = "INSERT INTO candidates (full_name, party_name, constituency, symbol, description) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getPartyName());
            ps.setString(3, c.getConstituency());
            ps.setString(4, c.getSymbol());
            ps.setString(5, c.getDescription());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) c.setCandidateId(rs.getInt(1));
                return true;
            }
            return false;
        }
    }

    public boolean addCandidateToElection(int electionId, int candidateId) throws SQLException {
        String sql = "INSERT INTO election_candidates (election_id, candidate_id) VALUES (?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, electionId);
            ps.setInt(2, candidateId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── VOTING ────────────────────────────────────────────────────────────────

    public boolean castVote(int electionId, int voterId, int candidateId) throws SQLException {
        String sql = "INSERT INTO votes (election_id, voter_id, candidate_id) VALUES (?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, electionId);
            ps.setInt(2, voterId);
            ps.setInt(3, candidateId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hasVoterVotedInElection(int voterId, int electionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM votes WHERE voter_id = ? AND election_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, voterId);
            ps.setInt(2, electionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    // ─── RESULTS ───────────────────────────────────────────────────────────────

    public List<Candidate> getElectionResults(int electionId) throws SQLException {
        List<Candidate> results = new ArrayList<>();
        String sql = """
                SELECT c.candidate_id, c.full_name, c.party_name, c.constituency, c.symbol,
                       COUNT(v.vote_id) AS vote_count
                FROM candidates c
                JOIN election_candidates ec ON c.candidate_id = ec.candidate_id
                LEFT JOIN votes v ON v.candidate_id = c.candidate_id AND v.election_id = ?
                WHERE ec.election_id = ?
                GROUP BY c.candidate_id
                ORDER BY vote_count DESC
                """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, electionId);
            ps.setInt(2, electionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Candidate c = mapCandidate(rs);
                c.setVoteCount(rs.getInt("vote_count"));
                results.add(c);
            }
        }
        return results;
    }

    // ─── AUDIT LOG ─────────────────────────────────────────────────────────────

    public void logAction(String action, String performedBy, String details) throws SQLException {
        String sql = "INSERT INTO audit_log (action, performed_by, details) VALUES (?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, performedBy);
            ps.setString(3, details);
            ps.executeUpdate();
        }
    }

    // ─── MAPPERS ───────────────────────────────────────────────────────────────

    private Election mapElection(ResultSet rs) throws SQLException {
        Election e = new Election();
        e.setElectionId(rs.getInt("election_id"));
        e.setElectionName(rs.getString("election_name"));
        e.setDescription(rs.getString("description"));
        Timestamp start = rs.getTimestamp("start_date");
        Timestamp end   = rs.getTimestamp("end_date");
        if (start != null) e.setStartDate(start.toLocalDateTime());
        if (end   != null) e.setEndDate(end.toLocalDateTime());
        e.setStatus(rs.getString("status"));
        return e;
    }

    private Candidate mapCandidate(ResultSet rs) throws SQLException {
        Candidate c = new Candidate();
        c.setCandidateId(rs.getInt("candidate_id"));
        c.setFullName(rs.getString("full_name"));
        c.setPartyName(rs.getString("party_name"));
        c.setConstituency(rs.getString("constituency"));
        c.setSymbol(rs.getString("symbol"));
        try { c.setDescription(rs.getString("description")); } catch (SQLException ignored) {}
        return c;
    }
}
