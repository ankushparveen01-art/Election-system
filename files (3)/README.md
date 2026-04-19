# 🗳 Secure Election System — Java + MySQL + OTP

A full-featured, secure election management system built with **Java 17**, **MySQL**, and **OTP-based verification**.

---

## 📁 Project Structure

```
ElectionSystem/
├── pom.xml
└── src/main/java/com/election/
    ├── Main.java                          ← Entry point
    ├── model/
    │   ├── Voter.java
    │   ├── Candidate.java
    │   ├── Election.java
    │   └── OTPToken.java
    ├── dao/
    │   ├── VoterDAO.java                  ← CRUD for voters table
    │   ├── ElectionDAO.java               ← CRUD for elections, votes, results
    │   ├── OTPDAO.java                    ← OTP store/retrieve/invalidate
    │   └── AdminDAO.java                  ← Admin auth
    ├── service/
    │   ├── VoterService.java              ← Registration + login + OTP flow
    │   └── ElectionService.java           ← Voting + results business logic
    └── util/
        ├── DatabaseConnection.java        ← Singleton JDBC connection
        ├── OTPUtil.java                   ← OTP generate / send / validate
        └── SecurityUtil.java              ← SHA-256 hash, validators, masking
└── src/main/resources/
    └── schema.sql                         ← Full DB schema + sample data
```

---

## 🗄️ Database Tables

| Table                  | Purpose                                      |
|------------------------|----------------------------------------------|
| `voters`               | Registered voters with verification status  |
| `candidates`           | Election candidates                          |
| `elections`            | Election events (UPCOMING / ACTIVE / CLOSED)|
| `election_candidates`  | Many-to-many: elections ↔ candidates        |
| `votes`                | Cast votes (one per voter per election)     |
| `otp_tokens`           | OTP codes with expiry and purpose           |
| `audit_log`            | Full audit trail of all actions             |
| `admins`               | Admin accounts with SHA-256 passwords       |

---

## 🔐 Security Features

| Feature                        | Implementation                                  |
|--------------------------------|-------------------------------------------------|
| OTP on Registration            | 6-digit OTP, 5-min expiry, stored in DB        |
| OTP on Login                   | Fresh OTP required each login session          |
| OTP before Vote                | Third OTP layer before vote is recorded        |
| Password Hashing               | SHA-256 for admin passwords                    |
| One Vote Enforcement           | DB UNIQUE constraint + application check       |
| Duplicate Voter Prevention     | Unique email + phone + nationalId              |
| Age Validation                 | 18+ enforced at service + DB constraint level  |
| Audit Logging                  | Every vote and admin action logged             |
| OTP Invalidation               | Old OTPs auto-invalidated on new request       |
| Email/Phone Masking            | Sensitive data masked in console output        |

---

## ⚙️ Setup Instructions

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 2. Configure Database
```bash
mysql -u root -p
```
```sql
source path/to/schema.sql;
```

### 3. Update DB Credentials
Edit `src/main/java/com/election/util/DatabaseConnection.java`:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/election_system?useSSL=false&serverTimezone=UTC";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_mysql_password";
```

### 4. Build the Project
```bash
cd ElectionSystem
mvn clean package
```

### 5. Run the Application
```bash
java -jar target/election-system-1.0.0-jar-with-dependencies.jar
```

---

## 🧭 Usage Flow

### Voter Flow
```
1. Main Menu → Voter Portal
2. Register (name, email, phone, Aadhaar, age, address)
3. Enter REGISTRATION OTP (sent to email + phone)
4. Login with email or phone
5. Enter LOGIN OTP
6. Select active election
7. Select candidate
8. Enter VOTING OTP
9. ✅ Vote recorded!
```

### Admin Flow
```
1. Main Menu → Admin Portal
2. Login (default: admin / Admin@1234)
3. Create Elections, Add Candidates, Assign Candidates
4. Manage election status (UPCOMING → ACTIVE → CLOSED)
5. View all voters, results, statistics
```

---

## 📧 OTP Integration (Production)

The current implementation **simulates** OTP delivery to the console.
For production, replace `OTPUtil.sendOTPtoEmail()` with:

**Email (JavaMail / SendGrid)**:
```java
// Add dependency: jakarta.mail or sendgrid-java
Session session = Session.getInstance(props, new Authenticator() {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
});
Message message = new MimeMessage(session);
message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
message.setText("Your OTP: " + otp);
Transport.send(message);
```

**SMS (Twilio)**:
```java
// Add dependency: com.twilio.sdk
Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
Message.creator(new PhoneNumber(phone), new PhoneNumber(FROM), "OTP: " + otp).create();
```

---

## 🛡️ Sample Admin Credentials
| Username | Password   |
|----------|------------|
| admin    | Admin@1234 |

> **Change this in production** by updating the `admins` table with a new SHA-256 hash.

---

## 📊 Sample Election Data (Pre-loaded)
- **Election**: General Election 2026 (Status: ACTIVE)
- **Candidates**: Rajesh Kumar, Priya Sharma, Amit Verma, Sunita Patel

---

## 🔧 Extending the System

- **Real Email/SMS**: Plug in JavaMail + Twilio in `OTPUtil.java`
- **Web Frontend**: Wrap services with Spring Boot REST APIs
- **Encryption**: Add AES encryption for stored votes
- **2FA**: Add biometric or TOTP (Google Authenticator) alongside OTP
- **Multi-constituency**: Filter candidates by voter's registered constituency
