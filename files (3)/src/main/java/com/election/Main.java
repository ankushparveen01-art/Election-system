package com.election;

import com.election.ui.AdminPortal;
import com.election.ui.ElectionGUI;
import com.election.ui.VoterPortal;
import com.election.util.DatabaseConnection;

import javax.swing.*;

/**
 * ╔═══════════════════════════════════════════╗
 * ║     SECURE ELECTION SYSTEM - MAIN APP     ║
 * ╚═══════════════════════════════════════════╝
 *
 * Features:
 *  - Voter Registration with OTP Email/SMS Verification
 *  - OTP-based Login Authentication
 *  - OTP Confirmation before casting vote
 *  - Secure vote storage in MySQL with audit trail
 *  - Admin portal for election management
 *  - Real-time election results with percentage
 *  - Voter turnout statistics
 *  - One vote per voter per election enforcement
 *  - Age 18+ validation
 *  - Duplicate voter prevention (email/phone/nationalId)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // Test DB connection
        try {
            DatabaseConnection.getInstance();
        } catch (Exception e) {
            String msg = "Database connection failed:\n" + e.getMessage()
                    + "\n\nConfigure DatabaseConnection.java with your MySQL credentials.";
            System.err.println(msg);
            try { JOptionPane.showMessageDialog(null, msg, "DB Error", JOptionPane.ERROR_MESSAGE); }
            catch (Exception ignored) {}
            System.exit(1);
        }

        String mode = args.length > 0 ? args[0].toLowerCase() : "choose";

        if (mode.equals("gui"))  { SwingUtilities.invokeLater(ElectionGUI::new); return; }
        if (mode.equals("cli"))  { launchCLI(); return; }

        // Offer choice dialog
        try {
            Object[] options = {"🖥  Launch GUI", "⌨  Launch CLI"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Welcome to the Secure Election System\n\nChoose your interface:",
                    "🗳  Election System v2.0",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (choice == 0) SwingUtilities.invokeLater(ElectionGUI::new);
            else             launchCLI();
        } catch (Exception e) { launchCLI(); }
    }

    static void launchCLI() {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("\n╔═══════════════════════════════════════════╗");
        System.out.println("║    🗳  SECURE ELECTION SYSTEM v2.0  🗳     ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        boolean running = true;
        while (running) {
            System.out.println("\n 1. Voter Portal   2. Admin Portal   3. Exit");
            System.out.print(" Choose: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> new VoterPortal().start();
                case "2" -> new AdminPortal().start();
                case "3" -> { running = false; System.out.println(" Goodbye!"); }
                default  -> System.out.println(" Invalid.");
            }
        }
    }
}
