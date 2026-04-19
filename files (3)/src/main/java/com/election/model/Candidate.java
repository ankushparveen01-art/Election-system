package com.election.model;

import java.time.LocalDateTime;

/**
 * Candidate Entity Model
 */
public class Candidate {
    private int candidateId;
    private String fullName;
    private String partyName;
    private String constituency;
    private String symbol;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;
    private int voteCount; // used for results display (not stored)

    public Candidate() {}

    public Candidate(String fullName, String partyName, String constituency, String symbol, String description) {
        this.fullName     = fullName;
        this.partyName    = partyName;
        this.constituency = constituency;
        this.symbol       = symbol;
        this.description  = description;
        this.isActive     = true;
    }

    // Getters and Setters
    public int getCandidateId()                         { return candidateId; }
    public void setCandidateId(int candidateId)         { this.candidateId = candidateId; }
    public String getFullName()                         { return fullName; }
    public void setFullName(String fullName)            { this.fullName = fullName; }
    public String getPartyName()                        { return partyName; }
    public void setPartyName(String partyName)          { this.partyName = partyName; }
    public String getConstituency()                     { return constituency; }
    public void setConstituency(String constituency)    { this.constituency = constituency; }
    public String getSymbol()                           { return symbol; }
    public void setSymbol(String symbol)                { this.symbol = symbol; }
    public String getDescription()                      { return description; }
    public void setDescription(String description)      { this.description = description; }
    public boolean isActive()                           { return isActive; }
    public void setActive(boolean active)               { isActive = active; }
    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
    public int getVoteCount()                           { return voteCount; }
    public void setVoteCount(int voteCount)             { this.voteCount = voteCount; }

    @Override
    public String toString() {
        return String.format("Candidate[ID=%d, Name=%-20s Party=%-25s Symbol=%s]",
                candidateId, fullName, partyName, symbol);
    }
}
