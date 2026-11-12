# Data Classification: Code Submission

**Classification of this document:** Class III - Unrestricted (contains no student data).
**Source:** ILEP Code Submission project. **Date:** 2026-11-12. **Status:** Draft.

## 1. The three classes (DSCS §V.B)

| Class | Name | Meaning | Key handling rules |
|---|---|---|---|
| **I** | Confidential | Protected by law, regulation or policy. Includes "student records and other non-public student data" (FERPA). | No disclosure without written authorization from the Data Trustee. No unencrypted email. Electronic records stored only by University-approved methods. Never on a public website. NIST 800-171 controls. |
| **II** | Sensitive | Restricted to University members with a legitimate purpose. | Protect from loss, theft and unauthorized access. Never on an unrestricted website. Encrypt as sensitivity requires. |
| **III** | Unrestricted | May be public. | Normal care. |

**Key conclusion.** Everything a student produces in the editor (keystroke events, code, run output, flags, analytics) is a **student record**, so it is **Class I**, even when stored under a pseudonym. Pseudonymization limits the damage of a breach; it does not lower the class.

## 2. Where data lives

The system has three locations. Each row in section 3 names one or more of them.

- **ILEP (laptop):** the instructor's JavaFX app, on a University-managed or encrypted device. Holds the only link between a real student and their pseudonym.
- **Server:** the Code Submission web service and its PostgreSQL database. Holds pseudonymous records only. **Hosting is pending approval** (see section 5).
- **Browser:** the student's or instructor's web browser. Holds a short-term event buffer and session token.

## 3. Data elements

Retention value `[RETENTION]` means the period set by the University Records Retention Policy, to be confirmed with the Registrar (DSCS §V.D).

### 3.1 Roster and identity

| Data element | Synthetic example | Class | Stored | Transmitted | Retention | Access |
|---|---|---|---|---|---|---|
| Student name (first, last, preferred) | Student One | I | ILEP only | Never leaves ILEP | Per ILEP roster handling | Instructor |
| Student email | student.one@example.edu | I | ILEP only | Never leaves ILEP | Per ILEP roster handling | Instructor |
| Eagle ID | 900000001 | I | ILEP only; typed by the student at join | Sent once over TLS at join; server compares it and discards it immediately, never stores or logs it | Not retained on server | Instructor; the student |
| Course Reference Number (CRN) and roster file | 00000-Fall 2026-CSCI1301A.csv | I | ILEP only (gitignored `data/`) | Never leaves ILEP | Per ILEP roster handling | Instructor |
| `studentReference` to student mapping | `stu_7f3a…` → Student One | I | ILEP only (gitignored `appData/`), encrypted at rest | Never leaves ILEP | `[RETENTION]` | Instructor |
| `studentReference` | `stu_7f3a…` | I | ILEP and server | TLS | `[RETENTION]` | Instructor; system |
| `joinHash` (HMAC of Eagle ID) | `9c1e…` | I | Server | TLS | Until course ends | System only |
| Course secret (HMAC key) | random 32 bytes | I | ILEP (`appData/`) and server, encrypted | TLS, once per assignment | Until course ends | System only |
| `courseReference` | `crs_51b0…` | II | ILEP and server | TLS | `[RETENTION]` | Instructor |
| Course label | CSCI 1301 Section A Fall 2026 | II | Server | TLS | `[RETENTION]` | Instructor; enrolled students |

### 3.2 Assignment content

| Data element | Synthetic example | Class | Stored | Transmitted | Retention | Access |
|---|---|---|---|---|---|---|
| Assignment title and instructions | Lab 4: Loops | II (III if published publicly) | Server | TLS | Course materials schedule | Instructor; enrolled students |
| Starter code | `public class Main { … }` | II (III if published publicly) | Server | TLS | Course materials schedule | Instructor; enrolled students |
| Assignment settings (mode, dates, paste allowed, AI use level, Java version) | `artificialIntelligenceUse: restricted` | II | Server | TLS | `[RETENTION]` | Instructor; students see their rules |
| Exam answer keys and test cases (future) | hidden JUnit tests | II | Server | TLS | Course materials schedule | Instructor only |

### 3.3 Student work and process data

| Data element | Synthetic example | Class | Stored | Transmitted | Retention | Access |
|---|---|---|---|---|---|---|
| Edit events (insert, delete, undo, autocomplete) | `{from: 120, to: 120, insert: "for"}` | I | Browser buffer (IndexedDB, until sent); server | TLS (WebSocket) | `[RETENTION]` | Instructor; the student (own record) |
| Paste, copy and cut events (size, hash, internal or external) | `{length: 412, internal: false}` | I | Browser buffer; server | TLS | `[RETENTION]` | Instructor; the student |
| Focus, blur and tab-visibility events | `{type: "blur", durationMs: 95000}` | I | Browser buffer; server | TLS | `[RETENTION]` | Instructor; the student |
| Typing-timing data (key class and interval only, no extra content) | `{keyClass: "letter", intervalMs: 140}` | I | Server | TLS | `[RETENTION]` | Instructor; the student |
| Code snapshots and final submission | full `.java` source | I | Server | TLS | `[RETENTION]` | Instructor; the student |
| Run output, compile errors, exit codes | `Main.java:7: error: ';' expected` | I | Server | TLS | `[RETENTION]` | Instructor; the student |
| AI use disclosure (tool, purpose, extent) | "Used an approved AI tool to explain a compiler error" | I | Server | TLS | `[RETENTION]` | Instructor; the student |
| Notice acknowledgement (who, when, notice version) | `{acknowledgedAt, noticeVersion: 1}` | I | Server | TLS | `[RETENTION]` | Instructor; the student |
| Accommodation flag (suppresses typing-cadence signals) | `cadenceSignalsSuppressed: true` | I | Server (flag only, never the reason) | TLS | Until course ends | Instructor only |

### 3.4 Derived analytics

| Data element | Synthetic example | Class | Stored | Transmitted | Retention | Access |
|---|---|---|---|---|---|---|
| Flags (external paste, large insertion, automated-input signals, focus loss) | `largeExternalPaste` | I | Server | TLS | `[RETENTION]` | Instructor; the student on request |
| Heat-map scores, active time, paste ratio, run counts | `pasteRatio: 0.12` | I | Server | TLS | `[RETENTION]` | Instructor; the student on request |
| Class-wide aggregates (no per-student rows) | "Line 14 hardest for 60% of class" | II | Server | TLS | `[RETENTION]` | Instructor |

### 3.5 System and security

| Data element | Synthetic example | Class | Stored | Transmitted | Retention | Access |
|---|---|---|---|---|---|---|
| Instructor API key | random token | I (credential) | ILEP `appData/`; server stores only a hash | TLS, header only | Rotate each term | Instructor; system |
| Student session token | signed, short-lived | I (credential) | Browser memory; server validates | TLS | Hours | System only |
| Audit log (who viewed which student record, when) | `{viewer, studentReference, viewedAt}` | I | Server, append-only | TLS | `[RETENTION]`, at least as long as the records it covers | Instructor; auditors |
| Application logs | request timing, error codes | II, and must contain **no** Class I values | Server | Internal | 90 days `[confirm with ITS]` | System administrators |
| Client details (browser, screen size) | `Chrome 140, 1440×900` | I (tied to a student session) | Server | TLS | `[RETENTION]` | Instructor |

## 4. Reports and exports (DSCS §V.C)

A report inherits the most restrictive class of the data it uses. Every per-student export (CSV, PDF, HTML) is therefore **Class I** and must carry:

- the label **"Class I - Confidential"** on every page
- its source, e.g. "Georgia Southern University, Department of Computer Science, CSCI 1301"
- its date

Exports from the server are pseudonymous. ILEP joins names in locally, so a named report exists only on the instructor's device and is distributed only through channels the Data Trustee authorizes. A class-wide summary with no per-student rows may be Class II.

## 5. Open items

| Item | Owner | Placeholder used above |
|---|---|---|
| Retention period for student work and process data | Registrar or departmental Data Manager | `[RETENTION]` |
| Approved hosting for Class I data (University infrastructure, or a cloud provider that passes the Third Party Risk Review) | Information Technology Services | — |
| Application-log retention | Information Technology Services | `[confirm with ITS]` |
| Whether the course label may include section and term | Departmental Data Steward | — |
