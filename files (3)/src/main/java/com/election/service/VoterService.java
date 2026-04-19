package com.election.service;

import com.election.dao.OTPDAO;
import com.election.dao.VoterDAO;
import com.election.model.OTPToken;
import com.election.model.Voter;
import com.election.util.OTPUtil;
import com.election.util.SecurityUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Voter Service - Business logic for voter registration, login, and OTP
 */
public class VoterService {

    private final VoterDAO voterDAO   = new VoterDAO();
    private final OTPDAO   otpDAO     = new OTPDAO();

    // ─── REGISTRATION ──────────────────────────────────────────────────────────

    /**
     * Step 1: Register voter (unverified), send OTP
     */
    public Voter registerVoter(String fullName, String email, String phone,
                               String nationalId, int age, String address) throws Exception {

        // Validations
        if (!SecurityUtil.isValidEmail(email))
            throw new IllegalArgumentException("Invalid email format.");
        if (!SecurityUtil.isValidPhone(phone))
            throw new IllegalArgumentException("Invalid phone number (10 digits starting with 6-9).");
        if (!SecurityUtil.isValidNationalId(nationalId))
            throw new IllegalArgumentException("Invalid National ID (12-digit Aadhaar number).");
        if (age < 18)
            throw new IllegalArgumentException("Voter must be at least 18 years old.");

        // Check duplicates
        if (voterDAO.findByEmail(email) != null)
            throw new IllegalArgumentException("Email already registered.");
        if (voterDAO.findByPhone(phone) != null)
            throw new IllegalArgumentException("Phone number already registered.");
        if (voterDAO.findByNationalId(nationalId) != null)
            throw new IllegalArgumentException("National ID already registered.");

        // Save voter (unverified)
        Voter voter = new Voter(fullName, email, phone, nationalId, age, address);
        voterDAO.registerVoter(voter);

        // Generate and send OTP
        sendOTP(voter.getVoterId(), email, phone, "REGISTRATION");
        return voter;
    }

    /**
     * Step 2: Verify OTP to activate voter account
     */
    public boolean verifyRegistrationOTP(int voterId, String enteredOTP) throws SQLException {
        OTPToken token = otpDAO.getValidOTP(voterId, "REGISTRATION");
        if (token == null)
            throw new IllegalStateException("No valid OTP found. Please request a new OTP.");
        if (!token.getOtpCode().equals(enteredOTP))
            throw new IllegalArgumentException("Invalid OTP entered.");

        otpDAO.markOTPUsed(token.getOtpId());
        voterDAO.markVoterVerified(voterId);
        return true;
    }

    // ─── LOGIN / AUTHENTICATION ────────────────────────────────────────────────

    /**
     * Step 1: Lookup voter by phone/email, send login OTP
     */
    public Voter initiateLogin(String identifier) throws Exception {
        Voter voter = voterDAO.findByEmail(identifier);
        if (voter == null) voter = voterDAO.findByPhone(identifier);
        if (voter == null)
            throw new IllegalArgumentException("No voter found with this email/phone.");
        if (!voter.isVerified())
            throw new IllegalStateException("Account not verified. Please complete registration OTP.");

        sendOTP(voter.getVoterId(), voter.getEmail(), voter.getPhone(), "LOGIN");
        return voter;
    }

    /**
     * Step 2: Verify login OTP
     */
    public boolean verifyLoginOTP(int voterId, String enteredOTP) throws SQLException {
        OTPToken token = otpDAO.getValidOTP(voterId, "LOGIN");
        if (token == null)
            throw new IllegalStateException("OTP expired or not found. Please login again.");
        if (!token.getOtpCode().equals(enteredOTP))
            throw new IllegalArgumentException("Incorrect OTP.");

        otpDAO.markOTPUsed(token.getOtpId());
        return true;
    }

    // ─── VOTING OTP ────────────────────────────────────────────────────────────

    /**
     * Issue OTP before casting vote
     */
    public void issueVotingOTP(Voter voter) throws Exception {
        sendOTP(voter.getVoterId(), voter.getEmail(), voter.getPhone(), "VOTING");
    }

    /**
     * Verify voting OTP
     */
    public boolean verifyVotingOTP(int voterId, String enteredOTP) throws SQLException {
        OTPToken token = otpDAO.getValidOTP(voterId, "VOTING");
        if (token == null)
            throw new IllegalStateException("Voting OTP expired. Please request again.");
        if (!token.getOtpCode().equals(enteredOTP))
            throw new IllegalArgumentException("Incorrect OTP. Vote not recorded.");

        otpDAO.markOTPUsed(token.getOtpId());
        return true;
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private void sendOTP(int voterId, String email, String phone, String purpose) throws SQLException {
        String otp = OTPUtil.generateOTP();
        LocalDateTime expiry = OTPUtil.getExpiryTime();

        OTPToken token = new OTPToken(voterId, otp, purpose, expiry);
        otpDAO.saveOTP(token);

        // Send via both email and SMS (simulated)
        OTPUtil.sendOTPtoEmail(email, otp, purpose);
        OTPUtil.sendOTPtoPhone(phone, otp, purpose);
    }

    public Voter getVoterById(int id) throws SQLException {
        return voterDAO.findById(id);
    }

    public int getTotalVoters() throws SQLException {
        return voterDAO.getTotalVoters();
    }

    public int getTotalVoted() throws SQLException {
        return voterDAO.getTotalVoted();
    }

    public java.util.List<Voter> getAllVoters() throws SQLException {
        return voterDAO.getAllVoters();
    }
}
