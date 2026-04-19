-- ============================================
-- ELECTION SYSTEM DATABASE SCHEMA
-- ============================================

CREATE DATABASE IF NOT EXISTS election_system;
USE election_system;

-- ============================================
-- TABLE: voters
-- ============================================
CREATE TABLE IF NOT EXISTS voters (
    voter_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(100)        NOT NULL,
    email          VARCHAR(150)        UNIQUE NOT NULL,
    phone          VARCHAR(15)         UNIQUE NOT NULL,
    national_id    VARCHAR(20)         UNIQUE NOT NULL,
    age            INT                 NOT NULL CHECK (age >= 18),
    address        TEXT,
    is_verified    BOOLEAN             DEFAULT FALSE,
    has_voted      BOOLEAN             DEFAULT FALSE,
    created_at     TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_age CHECK (age >= 18)
);

-- ============================================
-- TABLE: candidates
-- ============================================
CREATE TABLE IF NOT EXISTS candidates (
    candidate_id   INT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(100)        NOT NULL,
    party_name     VARCHAR(100)        NOT NULL,
    constituency   VARCHAR(100)        NOT NULL,
    symbol         VARCHAR(50),
    description    TEXT,
    is_active      BOOLEAN             DEFAULT TRUE,
    created_at     TIMESTAMP           DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE: elections
-- ============================================
CREATE TABLE IF NOT EXISTS elections (
    election_id    INT AUTO_INCREMENT PRIMARY KEY,
    election_name  VARCHAR(200)        NOT NULL,
    description    TEXT,
    start_date     DATETIME            NOT NULL,
    end_date       DATETIME            NOT NULL,
    status         ENUM('UPCOMING','ACTIVE','CLOSED') DEFAULT 'UPCOMING',
    created_at     TIMESTAMP           DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE: election_candidates  (Many-to-Many)
-- ============================================
CREATE TABLE IF NOT EXISTS election_candidates (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    election_id    INT                 NOT NULL,
    candidate_id   INT                 NOT NULL,
    FOREIGN KEY (election_id) REFERENCES elections(election_id) ON DELETE CASCADE,
    FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id) ON DELETE CASCADE,
    UNIQUE KEY uq_election_candidate (election_id, candidate_id)
);

-- ============================================
-- TABLE: votes
-- ============================================
CREATE TABLE IF NOT EXISTS votes (
    vote_id        INT AUTO_INCREMENT PRIMARY KEY,
    election_id    INT                 NOT NULL,
    voter_id       INT                 NOT NULL,
    candidate_id   INT                 NOT NULL,
    voted_at       TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
    ip_address     VARCHAR(50),
    FOREIGN KEY (election_id)  REFERENCES elections(election_id),
    FOREIGN KEY (voter_id)     REFERENCES voters(voter_id),
    FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id),
    UNIQUE KEY uq_voter_election (voter_id, election_id)   -- one vote per election
);

-- ============================================
-- TABLE: otp_tokens
-- ============================================
CREATE TABLE IF NOT EXISTS otp_tokens (
    otp_id         INT AUTO_INCREMENT PRIMARY KEY,
    voter_id       INT                 NOT NULL,
    otp_code       VARCHAR(6)          NOT NULL,
    purpose        ENUM('REGISTRATION','LOGIN','VOTING') NOT NULL,
    is_used        BOOLEAN             DEFAULT FALSE,
    expires_at     DATETIME            NOT NULL,
    created_at     TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voter_id) REFERENCES voters(voter_id) ON DELETE CASCADE
);

-- ============================================
-- TABLE: audit_log
-- ============================================
CREATE TABLE IF NOT EXISTS audit_log (
    log_id         INT AUTO_INCREMENT PRIMARY KEY,
    action         VARCHAR(100)        NOT NULL,
    performed_by   VARCHAR(150),
    details        TEXT,
    ip_address     VARCHAR(50),
    logged_at      TIMESTAMP           DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE: admins
-- ============================================
CREATE TABLE IF NOT EXISTS admins (
    admin_id       INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)         UNIQUE NOT NULL,
    password_hash  VARCHAR(256)        NOT NULL,
    email          VARCHAR(150)        UNIQUE NOT NULL,
    created_at     TIMESTAMP           DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- SAMPLE DATA
-- ============================================
INSERT INTO admins (username, password_hash, email) VALUES
('admin', SHA2('Admin@1234', 256), 'admin@election.gov.in');

INSERT INTO candidates (full_name, party_name, constituency, symbol, description) VALUES
('Rajesh Kumar',   'National Progress Party', 'Central Delhi',  'Lotus',   'Experienced leader with 10 years in public service'),
('Priya Sharma',   'Democratic Alliance',     'Central Delhi',  'Hand',    'Advocate for education and womens rights'),
('Amit Verma',     'People First Party',      'Central Delhi',  'Cycle',   'Focus on rural development and farmers welfare'),
('Sunita Patel',   'Green Future Party',      'Central Delhi',  'Tree',    'Environmental activist and social reformer');

INSERT INTO elections (election_name, description, start_date, end_date, status) VALUES
('General Election 2026', 'National General Election for Parliament', '2026-03-21 08:00:00', '2026-03-21 18:00:00', 'ACTIVE');

INSERT INTO election_candidates (election_id, candidate_id) VALUES (1,1),(1,2),(1,3),(1,4);
