package com.election.ui;

import com.election.model.Candidate;
import com.election.model.Election;
import com.election.model.Voter;
import com.election.service.ElectionService;
import com.election.service.VoterService;
import com.election.util.SecurityUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ElectionGUI extends JFrame {

    // ── Palette ──────────────────────────────────────────────
    static final Color BG         = new Color(10,  12,  20);
    static final Color SURFACE    = new Color(18,  22,  35);
    static final Color SURFACE2   = new Color(25,  30,  48);
    static final Color ACCENT     = new Color(0,  198, 137);
    static final Color ACCENT2    = new Color(255,180,  40);
    static final Color TEXT       = new Color(230,235, 250);
    static final Color TEXT_DIM   = new Color(130,140, 170);
    static final Color DANGER     = new Color(255, 80,  80);
    static final Color BORDER_CLR = new Color(40,  50,  80);

    // ── Fonts ────────────────────────────────────────────────
    static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD,  28);
    static final Font FONT_HEAD  = new Font("Segoe UI", Font.BOLD,  16);
    static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font FONT_MONO  = new Font("Consolas", Font.BOLD,  22);

    private final VoterService    voterService    = new VoterService();
    private final ElectionService electionService = new ElectionService();

    private JPanel    cardRoot;
    private CardLayout cards;
    private Voter     loggedInVoter;

    public ElectionGUI() {
        super("Secure Election System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        UIManager.put("OptionPane.background",        SURFACE);
        UIManager.put("Panel.background",             SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.background",            SURFACE2);
        UIManager.put("Button.foreground",            TEXT);

        cards    = new CardLayout();
        cardRoot = new JPanel(cards);
        cardRoot.setBackground(BG);

        cardRoot.add(buildWelcomeCard(),  "WELCOME");
        cardRoot.add(buildRegisterCard(), "REGISTER");
        cardRoot.add(buildOTPCard(),      "OTP_VERIFY");
        cardRoot.add(buildLoginCard(),    "LOGIN");
        cardRoot.add(new BackgroundPanel(), "LOGIN_OTP");
        cardRoot.add(buildVoteCard(),     "VOTE");
        cardRoot.add(new BackgroundPanel(), "VOTE_OTP");
        cardRoot.add(buildAdminCard(),    "ADMIN");

        add(cardRoot);
        setVisible(true);
    }

    // ── Helpers ──────────────────────────────────────────────

    JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 18)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(32, 40, 32, 40));
        if (title != null) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(FONT_HEAD);
            lbl.setForeground(ACCENT);
            lbl.setBorder(new EmptyBorder(0,0,4,0));
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    JTextField field(String ph) {
        JTextField tf = new JTextField(22) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(TEXT_DIM);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    g2.drawString(ph, ins.left+2, getHeight()/2 + getFont().getSize()/3);
                }
            }
        };
        tf.setBackground(SURFACE2); tf.setForeground(TEXT);
        tf.setCaretColor(ACCENT); tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_CLR, 1), new EmptyBorder(8,12,8,12)));
        return tf;
    }

    JPasswordField passField() {
        JPasswordField pf = new JPasswordField(22);
        pf.setBackground(SURFACE2); pf.setForeground(TEXT);
        pf.setCaretColor(ACCENT); pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_CLR, 1), new EmptyBorder(8,12,8,12)));
        return pf;
    }

    JButton btn(String text, Color color) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isRollover() ? color.brighter() : color;
                g2.setColor(getModel().isPressed() ? base.darker() : base);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setFont(FONT_BODY.deriveFont(Font.BOLD));
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(200,42));
        return b;
    }

    JButton btnAccent(String t) { return btn(t, ACCENT.darker()); }
    JButton btnAmber(String t)  { return btn(t, new Color(180,120,0)); }

    JButton btnGhost(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_SMALL); b.setForeground(ACCENT);
        b.setBackground(new Color(0,0,0,0));
        b.setBorderPainted(false); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    JButton buildTabBtn(String label) {
        JButton b = new JButton(label);
        b.setFont(FONT_BODY.deriveFont(Font.BOLD)); b.setForeground(TEXT_DIM);
        b.setBackground(SURFACE2); b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_CLR, 1), new EmptyBorder(7,18,7,18)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    JLabel lbl(String t, Font f, Color c) { JLabel l=new JLabel(t); l.setFont(f); l.setForeground(c); return l; }

    void showMsg(String msg, boolean ok) {
        JOptionPane.showMessageDialog(this, msg, ok?"Success":"Error",
                ok?JOptionPane.INFORMATION_MESSAGE:JOptionPane.ERROR_MESSAGE);
    }

    void go(String c) { cards.show(cardRoot, c); }

    JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12,0));
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(FONT_SMALL); l.setForeground(TEXT_DIM);
        l.setPreferredSize(new Dimension(170,36));
        row.add(l, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // ═══════════════════════════════════════════════════════
    //  WELCOME — only Voter Portal + Admin Portal, NO results
    // ═══════════════════════════════════════════════════════
    JPanel buildWelcomeCard() {
        JPanel bg = new BackgroundPanel();
        bg.setLayout(new GridBagLayout());

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel icon  = lbl("Secure Election System", FONT_TITLE, TEXT);
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = lbl("Java  ·  MySQL  ·  OTP Verification", FONT_SMALL, TEXT_DIM);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        sep.setMaximumSize(new Dimension(400,1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnRow.setOpaque(false);
        JButton bVoter = btnAccent("  Voter Portal  ");
        JButton bAdmin = btnAmber( "  Admin Portal  ");
        bVoter.setPreferredSize(new Dimension(200,46));
        bAdmin.setPreferredSize(new Dimension(200,46));
        btnRow.add(bVoter);
        btnRow.add(bAdmin);

        JLabel notice = lbl("Results are restricted to Admin only", FONT_SMALL, TEXT_DIM);
        notice.setAlignmentX(CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(icon);
        center.add(Box.createVerticalStrut(8));
        center.add(sub);
        center.add(Box.createVerticalStrut(30));
        center.add(sep);
        center.add(Box.createVerticalStrut(30));
        center.add(btnRow);
        center.add(Box.createVerticalStrut(14));
        center.add(notice);
        center.add(Box.createVerticalGlue());

        bVoter.addActionListener(e -> go("REGISTER"));
        bAdmin.addActionListener(e -> go("ADMIN"));

        bg.add(center);
        return bg;
    }

    // ═══════════════════════════════════════════════════════
    //  REGISTER
    // ═══════════════════════════════════════════════════════
    JTextField regName, regEmail, regPhone, regId, regAge, regAddr;

    JPanel buildRegisterCard() {
        JPanel bg = new BackgroundPanel();
        bg.setLayout(new GridBagLayout());

        JPanel wrap = card("Voter Registration");
        wrap.setPreferredSize(new Dimension(640,560));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        regName  = field("Full Name");
        regEmail = field("Email Address");
        regPhone = field("Phone (10 digits)");
        regId    = field("Aadhaar / National ID (12 digits)");
        regAge   = field("Age (18+)");
        regAddr  = field("Address");

        int sp = 10;
        form.add(formRow("Full Name",   regName));  form.add(Box.createVerticalStrut(sp));
        form.add(formRow("Email",       regEmail)); form.add(Box.createVerticalStrut(sp));
        form.add(formRow("Phone",       regPhone)); form.add(Box.createVerticalStrut(sp));
        form.add(formRow("National ID", regId));    form.add(Box.createVerticalStrut(sp));
        form.add(formRow("Age",         regAge));   form.add(Box.createVerticalStrut(sp));
        form.add(formRow("Address",     regAddr));

        JButton submit = btnAccent("Register & Get OTP");
        submit.setAlignmentX(CENTER_ALIGNMENT);
        submit.setMaximumSize(new Dimension(280,44));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER,20,0));
        bottom.setOpaque(false);
        JButton back  = btnGhost("<- Back");
        JButton login = btnGhost("Already registered? Login");
        bottom.add(back); bottom.add(login);
        back .addActionListener(e -> go("WELCOME"));
        login.addActionListener(e -> go("LOGIN"));

        wrap.add(form, BorderLayout.CENTER);
        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(Box.createVerticalStrut(18));
        south.add(submit);
        south.add(Box.createVerticalStrut(10));
        south.add(bottom);
        wrap.add(south, BorderLayout.SOUTH);
        submit.addActionListener(e -> doRegister());

        bg.add(wrap);
        return bg;
    }

    void doRegister() {
        try {
            int age = Integer.parseInt(regAge.getText().trim());
            Voter v = voterService.registerVoter(
                    regName.getText().trim(), regEmail.getText().trim(),
                    regPhone.getText().trim(), regId.getText().trim(),
                    age, regAddr.getText().trim());
            pendingVoterRef.set(v);
            otpPurposeRef.set("REGISTRATION");
            otpHeaderLabel.setText("OTP sent to " +
                    SecurityUtil.maskEmail(v.getEmail()) + " / " +
                    SecurityUtil.maskPhone(v.getPhone()));
            otpField.setText(""); go("OTP_VERIFY");
        } catch (Exception ex) { showMsg(ex.getMessage(), false); }
    }

    // ═══════════════════════════════════════════════════════
    //  OTP VERIFY
    // ═══════════════════════════════════════════════════════
    JLabel otpHeaderLabel;
    JTextField otpField;
    AtomicReference<Voter>  pendingVoterRef  = new AtomicReference<>();
    AtomicReference<String> otpPurposeRef    = new AtomicReference<>("REGISTRATION");

    JPanel buildOTPCard() {
        JPanel bg = new BackgroundPanel();
        bg.setLayout(new GridBagLayout());

        JPanel wrap = card(null);
        wrap.setPreferredSize(new Dimension(480,380));
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JLabel title = lbl("Verify Your Identity", FONT_HEAD, ACCENT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        otpHeaderLabel = lbl("OTP sent to your registered contact", FONT_SMALL, TEXT_DIM);
        otpHeaderLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hint = lbl("Enter the 6-digit OTP from your email", FONT_SMALL, TEXT_DIM);
        hint.setAlignmentX(CENTER_ALIGNMENT);

        otpField = new JTextField(6);
        otpField.setHorizontalAlignment(JTextField.CENTER);
        otpField.setFont(FONT_MONO);
        otpField.setBackground(SURFACE2); otpField.setForeground(ACCENT);
        otpField.setCaretColor(ACCENT);
        otpField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, ACCENT, 2), new EmptyBorder(12,20,12,20)));
        otpField.setMaximumSize(new Dimension(200,60));
        otpField.setAlignmentX(CENTER_ALIGNMENT);

        JButton verify = btnAccent("Verify OTP");
        verify.setAlignmentX(CENTER_ALIGNMENT);
        verify.setMaximumSize(new Dimension(220,44));

        JButton back = btnGhost("<- Back");
        back.setAlignmentX(CENTER_ALIGNMENT);

        wrap.add(Box.createVerticalStrut(10));
        wrap.add(title);        wrap.add(Box.createVerticalStrut(12));
        wrap.add(otpHeaderLabel); wrap.add(Box.createVerticalStrut(6));
        wrap.add(hint);         wrap.add(Box.createVerticalStrut(28));
        wrap.add(otpField);     wrap.add(Box.createVerticalStrut(28));
        wrap.add(verify);       wrap.add(Box.createVerticalStrut(12));
        wrap.add(back);

        verify.addActionListener(e -> doOTPVerify());
        back.addActionListener(e -> go("REGISTER"));
        otpField.addActionListener(e -> doOTPVerify());

        bg.add(wrap);
        return bg;
    }

    void doOTPVerify() {
        Voter v = pendingVoterRef.get();
        if (v == null) return;
        String purpose = otpPurposeRef.get();
        String entered = otpField.getText().trim();
        try {
            switch (purpose) {
                case "REGISTRATION" -> {
                    voterService.verifyRegistrationOTP(v.getVoterId(), entered);
                    showMsg("Account verified! You can now login to vote.", true);
                    go("LOGIN");
                }
                case "LOGIN" -> {
                    voterService.verifyLoginOTP(v.getVoterId(), entered);
                    loggedInVoter = voterService.getVoterById(v.getVoterId());
                    refreshVotePanel();
                    go("VOTE");
                }
                case "VOTING" -> {
                    voterService.verifyVotingOTP(v.getVoterId(), entered);
                    electionService.castVote(pendingElectionId.get(), v.getVoterId(), pendingCandidateId.get());
                    showMsg("Your vote has been recorded!\nThank you for participating.", true);
                    loggedInVoter = null;
                    go("WELCOME");   // back to home — NO results shown to voter
                }
            }
        } catch (Exception ex) { showMsg(ex.getMessage(), false); }
    }

    // ═══════════════════════════════════════════════════════
    //  LOGIN
    // ═══════════════════════════════════════════════════════
    JTextField loginIdField;

    JPanel buildLoginCard() {
        JPanel bg = new BackgroundPanel();
        bg.setLayout(new GridBagLayout());

        JPanel wrap = card("Voter Login");
        wrap.setPreferredSize(new Dimension(460,300));
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JLabel sub = lbl("Enter your registered email or phone number", FONT_SMALL, TEXT_DIM);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        loginIdField = field("Email or Phone");
        loginIdField.setMaximumSize(new Dimension(400,42));
        loginIdField.setAlignmentX(CENTER_ALIGNMENT);

        JButton submit = btnAccent("Send Login OTP");
        submit.setAlignmentX(CENTER_ALIGNMENT);
        submit.setMaximumSize(new Dimension(260,44));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER,20,0));
        row.setOpaque(false);
        JButton back = btnGhost("<- Back");
        JButton reg  = btnGhost("New voter? Register");
        row.add(back); row.add(reg);
        back.addActionListener(e -> go("WELCOME"));
        reg .addActionListener(e -> go("REGISTER"));
        submit.addActionListener(e -> doLogin());
        loginIdField.addActionListener(e -> doLogin());

        wrap.add(sub);                       wrap.add(Box.createVerticalStrut(22));
        wrap.add(loginIdField);              wrap.add(Box.createVerticalStrut(22));
        wrap.add(submit);                    wrap.add(Box.createVerticalStrut(12));
        wrap.add(row);

        bg.add(wrap);
        return bg;
    }

    void doLogin() {
        try {
            Voter v = voterService.initiateLogin(loginIdField.getText().trim());
            pendingVoterRef.set(v);
            otpPurposeRef.set("LOGIN");
            otpHeaderLabel.setText("OTP sent to " +
                    SecurityUtil.maskEmail(v.getEmail()) + " / " +
                    SecurityUtil.maskPhone(v.getPhone()));
            otpField.setText(""); go("OTP_VERIFY");
        } catch (Exception ex) { showMsg(ex.getMessage(), false); }
    }

    // ═══════════════════════════════════════════════════════
    //  VOTE
    // ═══════════════════════════════════════════════════════
    JPanel    voteCenterPanel;
    JLabel    voterNameLabel;
    AtomicInteger pendingElectionId  = new AtomicInteger(-1);
    AtomicInteger pendingCandidateId = new AtomicInteger(-1);

    JPanel buildVoteCard() {
        JPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout(16,0));
        top.setBackground(SURFACE);
        top.setBorder(new EmptyBorder(14,30,14,30));
        JLabel title = lbl("Cast Your Vote", FONT_HEAD, ACCENT);
        voterNameLabel = lbl("", FONT_SMALL, TEXT_DIM);
        JButton logout = btnGhost("Logout");
        logout.addActionListener(e -> { loggedInVoter = null; go("WELCOME"); });
        top.add(title, BorderLayout.WEST);
        top.add(voterNameLabel, BorderLayout.CENTER);
        top.add(logout, BorderLayout.EAST);

        voteCenterPanel = new JPanel();
        voteCenterPanel.setBackground(BG);
        voteCenterPanel.setLayout(new BoxLayout(voteCenterPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(voteCenterPanel);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);

        bg.add(top, BorderLayout.NORTH);
        bg.add(scroll, BorderLayout.CENTER);
        return bg;
    }

    void refreshVotePanel() {
        voteCenterPanel.removeAll();
        if (loggedInVoter == null) return;
        voterNameLabel.setText("Logged in as: " + loggedInVoter.getFullName());

        try {
            if (loggedInVoter.isHasVoted()) {
                JPanel msg = new JPanel(new GridBagLayout());
                msg.setBackground(BG);
                JPanel inner = card("Already Voted");
                inner.setPreferredSize(new Dimension(480,200));
                JLabel l = lbl("You have already cast your vote. Thank you!", FONT_BODY, TEXT);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                JButton home = btnAccent("Go to Home");
                home.addActionListener(e -> { loggedInVoter = null; go("WELCOME"); });
                inner.add(l, BorderLayout.CENTER);
                inner.add(home, BorderLayout.SOUTH);
                msg.add(inner);
                voteCenterPanel.add(msg);
                voteCenterPanel.revalidate();
                voteCenterPanel.repaint();
                return;
            }

            List<Election> elections = electionService.getActiveElections();
            if (elections.isEmpty()) {
                JPanel msg = new JPanel(new GridBagLayout());
                msg.setBackground(BG);
                msg.add(lbl("No active elections at this time.", FONT_BODY, TEXT_DIM));
                voteCenterPanel.add(msg);
                voteCenterPanel.revalidate();
                return;
            }

            for (Election election : elections) {
                List<Candidate> candidates = electionService.getCandidatesForElection(election.getElectionId());

                JPanel elPanel = new JPanel();
                elPanel.setOpaque(false);
                elPanel.setLayout(new BoxLayout(elPanel, BoxLayout.Y_AXIS));
                elPanel.setBorder(new EmptyBorder(24,40,0,40));

                JLabel elTitle = lbl(election.getElectionName(), FONT_HEAD, TEXT);
                elTitle.setAlignmentX(LEFT_ALIGNMENT);
                elPanel.add(elTitle);
                elPanel.add(Box.createVerticalStrut(16));

                JPanel grid = new JPanel(new GridLayout(0,2,16,16));
                grid.setOpaque(false);
                grid.setAlignmentX(LEFT_ALIGNMENT);

                ButtonGroup bg2 = new ButtonGroup();
                AtomicInteger sel = new AtomicInteger(-1);

                for (Candidate c : candidates) {
                    grid.add(buildCandidateCard(c, bg2, sel));
                }

                elPanel.add(grid);
                elPanel.add(Box.createVerticalStrut(20));

                JButton vote = btnAccent("Cast Vote for This Election");
                vote.setAlignmentX(LEFT_ALIGNMENT);
                vote.setMaximumSize(new Dimension(300,44));
                vote.addActionListener(e -> {
                    int cid = sel.get();
                    if (cid == -1) { showMsg("Please select a candidate first.", false); return; }
                    String cName = candidates.stream()
                            .filter(x -> x.getCandidateId() == cid)
                            .map(Candidate::getFullName).findFirst().orElse("?");
                    int ok = JOptionPane.showConfirmDialog(this,
                            "Confirm vote for: " + cName +
                            "\n\nAn OTP will be sent to your email to confirm.",
                            "Confirm Vote", JOptionPane.YES_NO_OPTION);
                    if (ok != JOptionPane.YES_OPTION) return;
                    pendingElectionId.set(election.getElectionId());
                    pendingCandidateId.set(cid);
                    try {
                        voterService.issueVotingOTP(loggedInVoter);
                        pendingVoterRef.set(loggedInVoter);
                        otpPurposeRef.set("VOTING");
                        otpHeaderLabel.setText("Voting OTP sent to " +
                                SecurityUtil.maskEmail(loggedInVoter.getEmail()));
                        otpField.setText(""); go("OTP_VERIFY");
                    } catch (Exception ex) { showMsg(ex.getMessage(), false); }
                });

                elPanel.add(vote);
                elPanel.add(Box.createVerticalStrut(8));
                voteCenterPanel.add(elPanel);
            }
        } catch (Exception ex) { showMsg(ex.getMessage(), false); }
        voteCenterPanel.revalidate();
        voteCenterPanel.repaint();
    }

    JPanel buildCandidateCard(Candidate c, ButtonGroup bg, AtomicInteger sel) {
        JPanel p = new JPanel(new BorderLayout(12,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE2);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16,16,16,16));

        JRadioButton rb = new JRadioButton();
        rb.setOpaque(false); rb.setForeground(ACCENT);
        bg.add(rb);
        rb.addActionListener(e -> sel.set(c.getCandidateId()));

        JLabel symbol = new JLabel(c.getSymbol() != null ? c.getSymbol() : "O", SwingConstants.CENTER);
        symbol.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        symbol.setForeground(ACCENT2);
        symbol.setPreferredSize(new Dimension(52,52));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(lbl(c.getFullName(),  FONT_BODY.deriveFont(Font.BOLD), TEXT));
        info.add(Box.createVerticalStrut(3));
        info.add(lbl(c.getPartyName(), FONT_SMALL, TEXT_DIM));

        p.add(rb, BorderLayout.WEST);
        p.add(symbol, BorderLayout.EAST);
        p.add(info, BorderLayout.CENTER);
        p.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { rb.doClick(); }
        });
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return p;
    }

    // ═══════════════════════════════════════════════════════
    //  ADMIN — Login → Voter List tab + Results tab
    // ═══════════════════════════════════════════════════════
    JTextField   adminUser;
    JPasswordField adminPass;
    JPanel       adminContent;

    JPanel buildAdminCard() {
        JPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout(16,0));
        top.setBackground(SURFACE);
        top.setBorder(new EmptyBorder(14,30,14,30));
        JLabel title = lbl("Admin Portal", FONT_HEAD, ACCENT2);
        JButton back = btnGhost("<- Home");
        back.addActionListener(e -> go("WELCOME"));
        top.add(title, BorderLayout.WEST);
        top.add(back,  BorderLayout.EAST);
        bg.add(top, BorderLayout.NORTH);

        // login panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(BG);
        JPanel loginCard = card("Admin Login");
        loginCard.setPreferredSize(new Dimension(420,300));
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));

        adminUser = field("Username");
        adminPass = passField();
        adminUser.setMaximumSize(new Dimension(400,42));
        adminPass.setMaximumSize(new Dimension(400,42));
        adminUser.setAlignmentX(CENTER_ALIGNMENT);
        adminPass.setAlignmentX(CENTER_ALIGNMENT);

        JButton loginBtn = btnAmber("Login as Admin");
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(220,44));

        loginCard.add(formRow("Username", adminUser));
        loginCard.add(Box.createVerticalStrut(12));
        loginCard.add(formRow("Password", adminPass));
        loginCard.add(Box.createVerticalStrut(20));
        loginCard.add(loginBtn);
        loginPanel.add(loginCard);

        // dashboard panel
        adminContent = new JPanel();
        adminContent.setBackground(BG);
        adminContent.setLayout(new BoxLayout(adminContent, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(adminContent);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);

        JPanel inner = new JPanel(new CardLayout());
        inner.setBackground(BG);
        inner.add(loginPanel, "LOGIN");
        inner.add(scroll,     "DASHBOARD");

        loginBtn.addActionListener(e -> {
            try {
                com.election.dao.AdminDAO dao = new com.election.dao.AdminDAO();
                if (!dao.authenticateAdmin(adminUser.getText().trim(),
                        new String(adminPass.getPassword()))) {
                    showMsg("Invalid admin credentials.", false); return;
                }
                loadAdminDashboard();
                ((CardLayout)inner.getLayout()).show(inner, "DASHBOARD");
            } catch (Exception ex) { showMsg(ex.getMessage(), false); }
        });

        bg.add(inner, BorderLayout.CENTER);
        return bg;
    }

    void loadAdminDashboard() {
        adminContent.removeAll();

        // ── Stats row ─────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1,3,16,0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(24,40,0,40));
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        try {
            int total = voterService.getTotalVoters();
            int voted = voterService.getTotalVoted();
            double pct = total > 0 ? voted * 100.0 / total : 0;
            statsRow.add(statCard("Registered Voters", String.valueOf(total), ACCENT));
            statsRow.add(statCard("Votes Cast",        String.valueOf(voted),  ACCENT2));
            statsRow.add(statCard("Voter Turnout",      String.format("%.1f%%", pct), new Color(100,160,255)));
        } catch (Exception ignored) {}
        adminContent.add(statsRow);
        adminContent.add(Box.createVerticalStrut(20));

        // ── Tab bar ────────────────────────────────────────
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        tabBar.setOpaque(false);
        tabBar.setBorder(new EmptyBorder(0,40,10,40));

        JButton tabVoters  = buildTabBtn("Voter List");
        JButton tabResults = buildTabBtn("Election Results");

        JPanel voterPanel   = buildVoterListPanel();
        JPanel resultsPanel = buildAdminResultsPanel();

        JPanel tabContent = new JPanel(new CardLayout());
        tabContent.setOpaque(false);
        tabContent.add(voterPanel,   "VOTERS");
        tabContent.add(resultsPanel, "RESULTS");

        tabVoters.setForeground(ACCENT);
        tabVoters.addActionListener(e -> {
            ((CardLayout)tabContent.getLayout()).show(tabContent, "VOTERS");
            tabVoters.setForeground(ACCENT); tabResults.setForeground(TEXT_DIM);
        });
        tabResults.addActionListener(e -> {
            ((CardLayout)tabContent.getLayout()).show(tabContent, "RESULTS");
            tabResults.setForeground(ACCENT); tabVoters.setForeground(TEXT_DIM);
        });

        tabBar.add(tabVoters);
        tabBar.add(tabResults);
        adminContent.add(tabBar);
        adminContent.add(tabContent);
        adminContent.revalidate();
        adminContent.repaint();
    }

    // ── Voter List (admin only, full details) ──────────────
    JPanel buildVoterListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0,10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0,40,20,40));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = lbl("All Registered Voters  (Admin Only)", FONT_HEAD, TEXT);
        JButton refresh = btnGhost("Refresh");
        refresh.addActionListener(e -> { adminContent.removeAll(); loadAdminDashboard(); });
        header.add(title,   BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID","Full Name","Email","Phone","Age","National ID","Address","Verified","Has Voted"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try {
            for (Voter v : voterService.getAllVoters()) {
                model.addRow(new Object[]{
                    v.getVoterId(), v.getFullName(), v.getEmail(),
                    v.getPhone(), v.getAge(), v.getNationalId(), v.getAddress(),
                    v.isVerified() ? "YES" : "NO",
                    v.isHasVoted() ? "YES" : "NO"
                });
            }
        } catch (Exception ignored) {}

        JTable table = new JTable(model);
        styleTable(table);

        // Color "Has Voted" column
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                l.setForeground("YES".equals(val) ? ACCENT : DANGER);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });
        // Color "Verified" column
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                l.setForeground("YES".equals(val) ? ACCENT : ACCENT2);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new RoundedBorder(10, BORDER_CLR, 1));
        scroll.setBackground(SURFACE);
        scroll.getViewport().setBackground(SURFACE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Results (admin only) ───────────────────────────────
    JPanel buildAdminResultsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0,40,20,40));

        JLabel title = lbl("Election Results  (Admin Only)", FONT_HEAD, TEXT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(16));

        try {
            for (Election election : electionService.getAllElections()) {
                List<Candidate> results = electionService.getResults(election.getElectionId());
                int total = results.stream().mapToInt(Candidate::getVoteCount).sum();

                JLabel elLbl = lbl(election.getElectionName() +
                        "  [" + election.getStatus() + "]",
                        FONT_BODY.deriveFont(Font.BOLD), ACCENT);
                elLbl.setAlignmentX(LEFT_ALIGNMENT);

                JLabel voteLbl = lbl("Total votes: " + total, FONT_SMALL, TEXT_DIM);
                voteLbl.setAlignmentX(LEFT_ALIGNMENT);

                panel.add(elLbl);
                panel.add(Box.createVerticalStrut(4));
                panel.add(voteLbl);
                panel.add(Box.createVerticalStrut(10));

                for (int i = 0; i < results.size(); i++) {
                    Candidate c = results.get(i);
                    double pct = total > 0 ? c.getVoteCount() * 100.0 / total : 0;
                    JPanel row = buildResultRow(c, pct, i == 0 && total > 0, total);
                    row.setAlignmentX(LEFT_ALIGNMENT);
                    panel.add(row);
                    panel.add(Box.createVerticalStrut(8));
                }
                panel.add(Box.createVerticalStrut(20));
            }
        } catch (Exception ignored) {}
        return panel;
    }

    // ── Result bar row ──────────────────────────────────────
    JPanel buildResultRow(Candidate c, double pct, boolean winner, int total) {
        JPanel row = new JPanel(new BorderLayout(14,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(winner ? new Color(0,80,60) : SURFACE2);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12,16,12,16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(lbl((winner?"[1st] ":"") + c.getFullName(), FONT_BODY.deriveFont(Font.BOLD), winner ? ACCENT : TEXT));
        left.add(Box.createVerticalStrut(2));
        left.add(lbl(c.getPartyName(), FONT_SMALL, TEXT_DIM));

        JProgressBar bar = new JProgressBar(0,100) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fill(new RoundRectangle2D.Float(0,4,getWidth(),getHeight()-8,8,8));
                int fillW = (int)(getWidth() * getValue() / 100.0);
                g2.setColor(winner ? ACCENT : new Color(80,120,200));
                if (fillW > 0) g2.fill(new RoundRectangle2D.Float(0,4,fillW,getHeight()-8,8,8));
                g2.dispose();
            }
        };
        bar.setValue((int)pct);
        bar.setOpaque(false); bar.setBorderPainted(false); bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(0,28));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel vl = lbl(String.valueOf(c.getVoteCount()), FONT_HEAD, winner ? ACCENT : TEXT);
        JLabel pl = lbl(String.format("%.1f%%", pct), FONT_SMALL, TEXT_DIM);
        vl.setAlignmentX(RIGHT_ALIGNMENT);
        pl.setAlignmentX(RIGHT_ALIGNMENT);
        right.setPreferredSize(new Dimension(90,50));
        right.add(vl); right.add(pl);

        row.add(left, BorderLayout.WEST);
        row.add(bar,  BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    // ── stat card ───────────────────────────────────────────
    JPanel statCard(String label, String value, Color accent) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE2);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.setColor(accent); g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,14,14));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16,20,16,20));
        JLabel val = lbl(value, new Font("Segoe UI", Font.BOLD, 32), accent);
        val.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl2 = lbl(label, FONT_SMALL, TEXT_DIM);
        lbl2.setAlignmentX(LEFT_ALIGNMENT);
        p.add(val); p.add(Box.createVerticalStrut(4)); p.add(lbl2);
        return p;
    }

    void styleTable(JTable t) {
        t.setBackground(SURFACE); t.setForeground(TEXT); t.setFont(FONT_SMALL);
        t.setRowHeight(34); t.setGridColor(BORDER_CLR);
        t.setSelectionBackground(new Color(0,120,80)); t.setSelectionForeground(Color.WHITE);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0,1));
        JTableHeader hdr = t.getTableHeader();
        hdr.setBackground(SURFACE2); hdr.setForeground(ACCENT);
        hdr.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_CLR));
        hdr.setReorderingAllowed(false);
    }

    // ═══════════════════════════════════════════════════════
    //  INNER CLASSES
    // ═══════════════════════════════════════════════════════

    static class BackgroundPanel extends JPanel {
        BackgroundPanel() { setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GradientPaint gp = new GradientPaint(0,0,BG,getWidth(),getHeight(),new Color(12,20,38));
            g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(new Color(255,255,255,8));
            for (int x=0; x<getWidth(); x+=40) g2.drawLine(x,0,x,getHeight());
            for (int y=0; y<getHeight(); y+=40) g2.drawLine(0,y,getWidth(),y);
            g2.dispose();
        }
    }

    static class RoundedBorder extends AbstractBorder {
        private final int radius, thick;
        private final Color color;
        RoundedBorder(int r, Color c, int t) { radius=r; color=c; thick=t; }
        @Override public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(thick));
            g2.draw(new RoundRectangle2D.Float(x,y,w-1,h-1,radius,radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(thick,thick,thick,thick); }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(ElectionGUI::new);
    }
}
