package com.election.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Election Entity Model
 */
public class Election {
    private int electionId;
    private String electionName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; // UPCOMING, ACTIVE, CLOSED
    private LocalDateTime createdAt;
    private List<Candidate> candidates;

    public Election() {}

    public Election(String electionName, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.electionName = electionName;
        this.description  = description;
        this.startDate    = startDate;
        this.endDate      = endDate;
        this.status       = "UPCOMING";
    }

    // Getters and Setters
    public int getElectionId()                          { return electionId; }
    public void setElectionId(int electionId)           { this.electionId = electionId; }
    public String getElectionName()                     { return electionName; }
    public void setElectionName(String electionName)    { this.electionName = electionName; }
    public String getDescription()                      { return description; }
    public void setDescription(String description)      { this.description = description; }
    public LocalDateTime getStartDate()                 { return startDate; }
    public void setStartDate(LocalDateTime startDate)   { this.startDate = startDate; }
    public LocalDateTime getEndDate()                   { return endDate; }
    public void setEndDate(LocalDateTime endDate)       { this.endDate = endDate; }
    public String getStatus()                           { return status; }
    public void setStatus(String status)                { this.status = status; }
    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
    public List<Candidate> getCandidates()              { return candidates; }
    public void setCandidates(List<Candidate> candidates){ this.candidates = candidates; }

    @Override
    public String toString() {
        return String.format("Election[ID=%d, Name=%s, Status=%s, Start=%s, End=%s]",
                electionId, electionName, status, startDate, endDate);
    }
}
