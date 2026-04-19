package com.election.model;

import java.time.LocalDateTime;

/**
 * OTP Token Entity Model
 */
public class OTPToken {
    private int otpId;
    private int voterId;
    private String otpCode;
    private String purpose; // REGISTRATION, LOGIN, VOTING
    private boolean isUsed;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public OTPToken() {}

    public OTPToken(int voterId, String otpCode, String purpose, LocalDateTime expiresAt) {
        this.voterId   = voterId;
        this.otpCode   = otpCode;
        this.purpose   = purpose;
        this.expiresAt = expiresAt;
        this.isUsed    = false;
    }

    // Getters and Setters
    public int getOtpId()                           { return otpId; }
    public void setOtpId(int otpId)                 { this.otpId = otpId; }
    public int getVoterId()                         { return voterId; }
    public void setVoterId(int voterId)             { this.voterId = voterId; }
    public String getOtpCode()                      { return otpCode; }
    public void setOtpCode(String otpCode)          { this.otpCode = otpCode; }
    public String getPurpose()                      { return purpose; }
    public void setPurpose(String purpose)          { this.purpose = purpose; }
    public boolean isUsed()                         { return isUsed; }
    public void setUsed(boolean used)               { isUsed = used; }
    public LocalDateTime getExpiresAt()             { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt){ this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}
