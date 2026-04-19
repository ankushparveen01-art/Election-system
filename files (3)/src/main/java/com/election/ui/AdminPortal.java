package com.election.ui;

import com.election.dao.AdminDAO;
import com.election.model.Candidate;
import com.election.model.Election;
import com.election.model.Voter;
import com.election.service.ElectionService;
import com.election.service.VoterService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Admin Portal - Command Line Interface
 */
public class AdminPortal {

    private final AdminDAO        adminDAO        = new AdminDAO();
    private final ElectionService electionService = new ElectionService();
    private final VoterService    voterService    = new VoterService();
    private final Scanner         scanner         = new Scanner(System.in);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void start() {
        System.out.println("\n  ── ADMIN LOGIN ─────────────────────────");
        try {
            System.out.print("  Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("  Password: ");
            String password = scanner.nextLine().trim();

            if (!adminDAO.authenticateAdmin(username, password)) {
                System.out.println("  ❌ Invalid credentials.");
                return;
            }
            System.out.println("  ✅ Admin authenticated: " + username);

        } catch (Exception e) {
            System.out.println("  ❌ Login error: " + e.getMessage());
            return;
        }

        adminMenu();
    }

    private void adminMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║      ADMIN PANEL MENU        ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Create Election          ║");
            System.out.println("║  2. Add Candidate            ║");
            System.out.println("║  3. Assign Candidate to Election ║");
            System.out.println("║  4. Update Election Status   ║");
            System.out.println("║  5. View All Voters          ║");
            System.out.println("║  6. View Election Results    ║");
            System.out.println("║  7. View Voter Statistics    ║");
            System.out.println("║  8. Back to Main Menu        ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("  Choose option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createElection();
                case "2" -> addCandidate();
                case "3" -> assignCandidateToElection();
                case "4" -> updateElectionStatus();
                case "5" -> viewAllVoters();
                case "6" -> viewResults();
                case "7" -> viewStats();
                case "8" -> running = false;
                default  -> System.out.println("  ⚠ Invalid option.");
            }
        }
    }

    private void createElection() {
        System.out.println("\n  ── CREATE ELECTION ─────────────────────");
        try {
            System.out.print("  Election Name : ");
            String name = scanner.nextLine().trim();

            System.out.print("  Description   : ");
            String desc = scanner.nextLine().trim();

            System.out.print("  Start (yyyy-MM-dd HH:mm): ");
            LocalDateTime start = LocalDateTime.parse(scanner.nextLine().trim(), DTF);

            System.out.print("  End   (yyyy-MM-dd HH:mm): ");
            LocalDateTime end = LocalDateTime.parse(scanner.nextLine().trim(), DTF);

            System.out.print("  Status (UPCOMING/ACTIVE/CLOSED): ");
            String status = scanner.nextLine().trim().toUpperCase();

            Election e = new Election(name, desc, start, end);
            e.setStatus(status);
            electionService.createElection(e);
            System.out.println("  ✅ Election created with ID: " + e.getElectionId());

        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }

    private void addCandidate() {
        System.out.println("\n  ── ADD CANDIDATE ───────────────────────");
        try {
            System.out.print("  Full Name    : ");
            String name = scanner.nextLine().trim();

            System.out.print("  Party Name   : ");
            String party = scanner.nextLine().trim();

            System.out.print("  Constituency : ");
            String constituency = scanner.nextLine().trim();

            System.out.print("  Symbol       : ");
            String symbol = scanner.nextLine().trim();

            System.out.print("  Description  : ");
            String desc = scanner.nextLine().trim();

            Candidate c = new Candidate(name, party, constituency, symbol, desc);
            electionService.addCandidate(c);
            System.out.println("  ✅ Candidate added with ID: " + c.getCandidateId());

        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }

    private void assignCandidateToElection() {
        System.out.println("\n  ── ASSIGN CANDIDATE TO ELECTION ────────");
        try {
            System.out.print("  Election ID  : ");
            int eId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("  Candidate ID : ");
            int cId = Integer.parseInt(scanner.nextLine().trim());

            electionService.addCandidateToElection(eId, cId);
            System.out.println("  ✅ Candidate " + cId + " assigned to Election " + eId);

        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }

    private void updateElectionStatus() {
        System.out.println("\n  ── UPDATE ELECTION STATUS ──────────────");
        try {
            List<Election> elections = electionService.getAllElections();
            for (Election e : elections)
                System.out.printf("  ID:%-3d [%-8s] %s%n", e.getElectionId(), e.getStatus(), e.getElectionName());

            System.out.print("  Election ID : ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("  New Status (UPCOMING/ACTIVE/CLOSED): ");
            String status = scanner.nextLine().trim().toUpperCase();

            electionService.updateElectionStatus(id, status);
            System.out.println("  ✅ Status updated.");

        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }

    private void viewAllVoters() {
        System.out.println("\n  ── ALL REGISTERED VOTERS ───────────────");
        try {
            List<Voter> voters = voterService.getAllVoters();
            System.out.printf("  %-5s %-20s %-25s %-15s %-10s %-8s%n",
                    "ID", "Name", "Email", "Phone", "Verified", "Voted");
            System.out.println("  " + "─".repeat(90));
            for (Voter v : voters) {
                System.out.printf("  %-5d %-20s %-25s %-15s %-10s %-8s%n",
                        v.getVoterId(), v.getFullName(), v.getEmail(),
                        v.getPhone(), v.isVerified() ? "YES" : "NO",
                        v.isHasVoted() ? "YES" : "NO");
            }
            System.out.println("  Total: " + voters.size());
        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }

    private void viewResults() {
        System.out.println("\n  ── ELECTION RESULTS ────────────────────");
        try {
            List<Election> elections = electionService.getAllElections();
            for (int i = 0; i < elections.size(); i++)
                System.out.printf("  %d. [%s] %s%n", i+1, elections.get(i).getStatus(), elections.get(i).getElectionName());

            System.out.print("  Select: ");
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= elections.size()) { System.out.println("  Invalid."); return; }

            Election e = elections.get(idx);
            List<Candidate> results = electionService.getResults(e.getElectionId());
            int total = results.stream().mapToInt(Candidate::getVoteCount).sum();

            System.out.printf("%n  ══ %s (Total Votes: %d) ══%n", e.getElectionName(), total);
            System.out.printf("  %-4s %-22s %-22s %6s%n", "Rank", "Candidate", "Party", "Votes");
            System.out.println("  " + "─".repeat(60));
            for (int i = 0; i < results.size(); i++) {
                Candidate c = results.get(i);
                String bar = "█".repeat(Math.max(1, c.getVoteCount()));
                if (bar.length() > 20) bar = bar.substring(0, 20);
                double pct = total > 0 ? (c.getVoteCount() * 100.0 / total) : 0;
                System.out.printf("  %-4d %-22s %-22s %6d  (%.1f%%)%n",
                        i + 1, c.getFullName(), c.getPartyName(), c.getVoteCount(), pct);
            }
        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }

    private void viewStats() {
        System.out.println("\n  ── VOTER STATISTICS ────────────────────");
        try {
            int total = voterService.getTotalVoters();
            int voted = voterService.getTotalVoted();
            double turnout = total > 0 ? (voted * 100.0 / total) : 0;
            System.out.println("  Total Registered Voters : " + total);
            System.out.println("  Total Votes Cast        : " + voted);
            System.out.printf("  Voter Turnout           : %.1f%%%n", turnout);
        } catch (Exception e) {
            System.out.println("  ❌ Error: " + e.getMessage());
        }
    }
}
