package com.election.ui;

import com.election.model.Candidate;
import com.election.model.Election;
import com.election.model.Voter;
import com.election.service.ElectionService;
import com.election.service.VoterService;
import com.election.util.SecurityUtil;

import java.util.List;
import java.util.Scanner;

/**
 * Voter Portal - Command Line Interface
 */
public class VoterPortal {

    private final VoterService    voterService    = new VoterService();
    private final ElectionService electionService = new ElectionService();
    private final Scanner         scanner         = new Scanner(System.in);

    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║      VOTER PORTAL MENU       ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Register as Voter        ║");
            System.out.println("║  2. Login & Cast Vote        ║");
            System.out.println("║  3. View Election Results    ║");
            System.out.println("║  4. Exit                     ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("  Choose option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> registerVoter();
                case "2" -> loginAndVote();
                case "3" -> viewResults();
                case "4" -> { running = false; System.out.println("  Goodbye! 🇮🇳"); }
                default  -> System.out.println("  ⚠ Invalid option.");
            }
        }
    }

    // ─── REGISTRATION ──────────────────────────────────────────────────────────

    private void registerVoter() {
        System.out.println("\n  ── VOTER REGISTRATION ──────────────────");
        try {
            System.out.print("  Full Name     : ");
            String name = scanner.nextLine().trim();

            System.out.print("  Email         : ");
            String email = scanner.nextLine().trim();

            System.out.print("  Phone (10-digit): ");
            String phone = scanner.nextLine().trim();

            System.out.print("  Aadhaar/National ID (12 digits): ");
            String nationalId = scanner.nextLine().trim();

            System.out.print("  Age           : ");
            int age = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("  Address       : ");
            String address = scanner.nextLine().trim();

            Voter voter = voterService.registerVoter(name, email, phone, nationalId, age, address);
            System.out.println("\n  ✅ Registration submitted! Voter ID: " + voter.getVoterId());
            System.out.println("  📩 OTP sent to " + SecurityUtil.maskEmail(email) +
                               " and " + SecurityUtil.maskPhone(phone));

            // OTP Verification
            System.out.print("\n  Enter OTP to verify your account: ");
            String otp = scanner.nextLine().trim();

            voterService.verifyRegistrationOTP(voter.getVoterId(), otp);
            System.out.println("\n  🎉 Account verified successfully! You can now login to vote.");

        } catch (NumberFormatException e) {
            System.out.println("  ❌ Invalid age entered.");
        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }

    // ─── LOGIN + VOTE ──────────────────────────────────────────────────────────

    private void loginAndVote() {
        System.out.println("\n  ── VOTER LOGIN ─────────────────────────");
        try {
            System.out.print("  Enter Email or Phone: ");
            String identifier = scanner.nextLine().trim();

            Voter voter = voterService.initiateLogin(identifier);
            System.out.println("\n  👤 Welcome, " + voter.getFullName());
            System.out.println("  📩 OTP sent to " + SecurityUtil.maskEmail(voter.getEmail()) +
                               " / " + SecurityUtil.maskPhone(voter.getPhone()));

            System.out.print("  Enter Login OTP: ");
            String loginOTP = scanner.nextLine().trim();
            voterService.verifyLoginOTP(voter.getVoterId(), loginOTP);
            System.out.println("  ✅ Login successful!");

            // Check if already voted
            if (voter.isHasVoted()) {
                System.out.println("  ℹ  You have already cast your vote.");
                return;
            }

            // Show active elections
            List<Election> elections = electionService.getActiveElections();
            if (elections.isEmpty()) {
                System.out.println("  ⚠ No active elections at this time.");
                return;
            }

            System.out.println("\n  ── ACTIVE ELECTIONS ────────────────────");
            for (int i = 0; i < elections.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, elections.get(i).getElectionName());
            }
            System.out.print("  Select Election: ");
            int eChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (eChoice < 0 || eChoice >= elections.size()) {
                System.out.println("  ❌ Invalid selection.");
                return;
            }
            Election selectedElection = elections.get(eChoice);

            // Show candidates
            List<Candidate> candidates = electionService.getCandidatesForElection(selectedElection.getElectionId());
            if (candidates.isEmpty()) {
                System.out.println("  ⚠ No candidates listed for this election.");
                return;
            }

            System.out.println("\n  ── CANDIDATES ──────────────────────────");
            System.out.printf("  %-4s %-22s %-25s %-10s%n", "No.", "Name", "Party", "Symbol");
            System.out.println("  " + "─".repeat(65));
            for (int i = 0; i < candidates.size(); i++) {
                Candidate c = candidates.get(i);
                System.out.printf("  %-4d %-22s %-25s %-10s%n",
                        i + 1, c.getFullName(), c.getPartyName(), c.getSymbol());
            }

            System.out.print("\n  Enter candidate number to vote for: ");
            int cChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (cChoice < 0 || cChoice >= candidates.size()) {
                System.out.println("  ❌ Invalid candidate selection.");
                return;
            }
            Candidate chosen = candidates.get(cChoice);

            // Confirmation
            System.out.printf("%n  ⚠  You are about to vote for:%n");
            System.out.printf("     %s (%s) — %s%n", chosen.getFullName(), chosen.getPartyName(), chosen.getSymbol());
            System.out.print("  Confirm? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes")) {
                System.out.println("  ❌ Vote cancelled.");
                return;
            }

            // Issue Voting OTP
            voterService.issueVotingOTP(voter);
            System.out.println("\n  📩 Voting OTP sent to your registered email/phone.");
            System.out.print("  Enter Voting OTP: ");
            String votingOTP = scanner.nextLine().trim();
            voterService.verifyVotingOTP(voter.getVoterId(), votingOTP);

            // Cast Vote
            electionService.castVote(selectedElection.getElectionId(), voter.getVoterId(), chosen.getCandidateId());
            System.out.println("\n  ✅ ====================================");
            System.out.println("     VOTE CAST SUCCESSFULLY!");
            System.out.println("     Thank you for participating in");
            System.out.println("     the democratic process. 🇮🇳");
            System.out.println("  ====================================");

        } catch (NumberFormatException e) {
            System.out.println("  ❌ Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }

    // ─── VIEW RESULTS ──────────────────────────────────────────────────────────

    private void viewResults() {
        System.out.println("\n  ── ELECTION RESULTS ────────────────────");
        try {
            List<Election> elections = electionService.getAllElections();
            if (elections.isEmpty()) {
                System.out.println("  No elections found.");
                return;
            }

            for (int i = 0; i < elections.size(); i++) {
                Election e = elections.get(i);
                System.out.printf("  %d. [%s] %s%n", i + 1, e.getStatus(), e.getElectionName());
            }
            System.out.print("  Select election to view results: ");
            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (choice < 0 || choice >= elections.size()) {
                System.out.println("  ❌ Invalid selection.");
                return;
            }

            Election election = elections.get(choice);
            List<Candidate> results = electionService.getResults(election.getElectionId());

            int totalVotes = results.stream().mapToInt(Candidate::getVoteCount).sum();

            System.out.printf("%n  ══ RESULTS: %s ══%n", election.getElectionName());
            System.out.printf("  %-4s %-22s %-22s %-10s %-8s%n", "Rank", "Candidate", "Party", "Votes", "(%%)");
            System.out.println("  " + "─".repeat(72));

            for (int i = 0; i < results.size(); i++) {
                Candidate c = results.get(i);
                double pct = totalVotes > 0 ? (c.getVoteCount() * 100.0 / totalVotes) : 0;
                String medal = (i == 0 && totalVotes > 0) ? "🏆" : "  ";
                System.out.printf("  %s%-3d %-22s %-22s %-10d %.1f%%%n",
                        medal, i + 1, c.getFullName(), c.getPartyName(), c.getVoteCount(), pct);
            }
            System.out.printf("%n  Total Votes Cast: %d%n", totalVotes);

        } catch (NumberFormatException e) {
            System.out.println("  ❌ Invalid input.");
        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }

    private void printBanner() {
        System.out.println("\n╔═══════════════════════════════════════════╗");
        System.out.println("║    🗳  SECURE ELECTION SYSTEM v1.0  🗳     ║");
        System.out.println("║    Powered by Java + MySQL + OTP Auth     ║");
        System.out.println("╚═══════════════════════════════════════════╝");
    }
}
