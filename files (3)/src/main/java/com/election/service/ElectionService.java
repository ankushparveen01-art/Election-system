package com.election.service;

import com.election.dao.ElectionDAO;
import com.election.dao.VoterDAO;
import com.election.model.Candidate;
import com.election.model.Election;
import com.election.model.Voter;

import java.sql.SQLException;
import java.util.List;

/**
 * Election Service - business logic for voting and results
 */
public class ElectionService {

    private final ElectionDAO electionDAO = new ElectionDAO();
    private final VoterDAO    voterDAO    = new VoterDAO();

    public List<Election> getActiveElections() throws SQLException {
        return electionDAO.getActiveElections();
    }

    public List<Election> getAllElections() throws SQLException {
        return electionDAO.getAllElections();
    }

    public List<Candidate> getCandidatesForElection(int electionId) throws SQLException {
        return electionDAO.getCandidatesForElection(electionId);
    }

    /**
     * Cast a vote — called AFTER OTP has been verified
     */
    public void castVote(int electionId, int voterId, int candidateId) throws Exception {
        Voter voter = voterDAO.findById(voterId);
        if (voter == null)           throw new IllegalArgumentException("Voter not found.");
        if (!voter.isVerified())     throw new IllegalStateException("Voter account not verified.");
        if (voter.isHasVoted())      throw new IllegalStateException("You have already voted.");

        if (electionDAO.hasVoterVotedInElection(voterId, electionId))
            throw new IllegalStateException("You have already voted in this election.");

        electionDAO.castVote(electionId, voterId, candidateId);
        voterDAO.markVoterVoted(voterId);

        // Audit trail
        electionDAO.logAction("VOTE_CAST",
                "voter_id=" + voterId,
                "Election=" + electionId + " Candidate=" + candidateId);
    }

    public List<Candidate> getResults(int electionId) throws SQLException {
        return electionDAO.getElectionResults(electionId);
    }

    public boolean createElection(Election e) throws SQLException {
        boolean result = electionDAO.createElection(e);
        if (result) electionDAO.logAction("ELECTION_CREATED", "admin", e.getElectionName());
        return result;
    }

    public boolean addCandidate(Candidate c) throws SQLException {
        return electionDAO.addCandidate(c);
    }

    public boolean addCandidateToElection(int electionId, int candidateId) throws SQLException {
        return electionDAO.addCandidateToElection(electionId, candidateId);
    }

    public boolean updateElectionStatus(int electionId, String status) throws SQLException {
        boolean result = electionDAO.updateElectionStatus(electionId, status);
        if (result) electionDAO.logAction("STATUS_UPDATED", "admin", "Election " + electionId + " -> " + status);
        return result;
    }
}
