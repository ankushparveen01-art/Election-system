package com.election.model;

import java.time.LocalDateTime;

/**
 * Voter Entity Model
 */
public class Voter {
    private int voterId;
    private String fullName;
    private String email;
    private String phone;
    private String nationalId;
    private int age;
    private String address;
    private boolean isVerified;
    private boolean hasVoted;
    private LocalDateTime createdAt;

    public Voter() {}

    public Voter(String fullName, String email, String phone, String nationalId, int age, String address) {
        this.fullName   = fullName;
        this.email      = email;
        this.phone      = phone;
        this.nationalId = nationalId;
        this.age        = age;
        this.address    = address;
    }

    // Getters and Setters
    public int getVoterId()                         { return voterId; }
    public void setVoterId(int voterId)             { this.voterId = voterId; }
    public String getFullName()                     { return fullName; }
    public void setFullName(String fullName)        { this.fullName = fullName; }
    public String getEmail()                        { return email; }
    public void setEmail(String email)              { this.email = email; }
    public String getPhone()                        { return phone; }
    public void setPhone(String phone)              { this.phone = phone; }
    public String getNationalId()                   { return nationalId; }
    public void setNationalId(String nationalId)    { this.nationalId = nationalId; }
    public int getAge()                             { return age; }
    public void setAge(int age)                     { this.age = age; }
    public String getAddress()                      { return address; }
    public void setAddress(String address)          { this.address = address; }
    public boolean isVerified()                     { return isVerified; }
    public void setVerified(boolean verified)       { isVerified = verified; }
    public boolean isHasVoted()                     { return hasVoted; }
    public void setHasVoted(boolean hasVoted)       { this.hasVoted = hasVoted; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Voter[ID=%d, Name=%s, Email=%s, Phone=%s, Verified=%s, Voted=%s]",
                voterId, fullName, email, phone, isVerified, hasVoted);
    }
}
