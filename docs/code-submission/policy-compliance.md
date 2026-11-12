# Policy Compliance Map: Code Submission

**Classification of this document:** Class III - Unrestricted. **Date:** 2026-11-12. **Status:** Draft.

Each row is one requirement from a governing policy, the design decision that meets it, and its status.

**Status key:** **Designed** = in the design, not yet built. **Pending approval** = needs a University office to decide. **Open** = not yet resolved.

## 1. Data Stewardship and Classification Standard (DSCS)

| # | Requirement | Section | How the design meets it | Status |
|---|---|---|---|---|
| D1 | Classify all institutional data as Class I, II or III. | §V.B | [data-classification.md](data-classification.md) classifies every data element. Student work and process data are Class I. | Designed |
| D2 | Class I electronic records stored only by University-approved methods; NIST 800-171 controls. | §V.B, Class I | No real student use until Information Technology Services approves hosting, through the Third Party Risk Review if a commercial cloud is used. Otherwise deploy on University infrastructure. | **Pending approval** |
| D3 | Class I not disclosed without written authorization from the Data Trustee. | §V.B, Class I | Access is need-to-know: the instructor, and the student for their own record. No sharing features. Any other access needs written authorization. | Designed |
| D4 | Class I never on a public website; Class II never on an unrestricted website. | §V.B | Every page needs a login or a signed token. No public share links. Instructor viewer links are one-time and expire in 5 minutes. | Designed |
| D5 | Class I never sent by unencrypted email. | §V.B, Class I | The service sends no email that contains student data. All traffic uses TLS. | Designed |
| D6 | Protect Class I/II from loss, theft and unauthorized access; encrypt as needed. | §V.B | Encryption in transit (TLS) and at rest (database, backups, ILEP `appData/`). Student identifiers stay in ILEP; the server holds pseudonyms only. Raw Eagle ID is discarded at join and never logged. | Designed |
| D7 | Minimize identifiable data on systems that do not need it. | §V.B (protection of Class I) | Server stores `studentReference` and `joinHash`, never names, emails, Eagle IDs or CRNs. | Designed |
| D8 | Reports inherit the most restrictive class; label each page with class, source and date. | §V.C | Per-student exports are labeled "Class I - Confidential" with source and date. Server exports are pseudonymous; names are joined only inside ILEP. | Designed |
| D9 | Reports distributed only through channels approved by Data Trustees. | §V.C | No in-app distribution. Named reports exist only on the instructor's device. | Designed |
| D10 | Destroy Class I/II data per the Records Retention Policy, using NIST 800-88 methods. | §V.D | Scheduled deletion after the retention period; backups expire on the same schedule. | **Open**: retention period not yet set |
| D11 | Notify the Office of Information Security within 24 hours of a suspected loss or disclosure of Class I/II data. | §V.F | Incident-response procedure with contacts. | **Open**: document deferred to a later phase |
| D12 | Data Stewards and Data Managers implement training and awareness. | §V.E | The instructor completes required University data and AI training before launch. | Open |
| D13 | No exclusions to the policy. | §III | Applies to every environment that holds real data, including development and testing. | Designed |
| D14 | Keep student data out of version control. | §V.B (Class I storage) | ILEP `.gitignore` blocks `data/`, `*.csv` and `appData/`. Tests use synthetic data only. A planned pre-commit check blocks roster files. | Designed |

## 2. Use of Artificial Intelligence (AI) in Academic Contexts (AI Policy)

| # | Requirement | Section | How the design meets it | Status |
|---|---|---|---|---|
| A1 | Faculty syllabus must define AI for the course and state forbidden, restricted and permitted uses. | §VI.B.1 | [syllabus-statement.md](syllabus-statement.md) gives the definition and all three levels, with the course default set to **restricted**. Each assignment chooses `forbidden`, `restricted` or `permitted`. | Designed |
| A2 | Syllabus must state requirements for process-tracking software and tell students which tools may be used to address unethical AI use. | §VI.B.1; §V "Process Tracking Software" | The syllabus statement names the Code Submission editor as process-tracking software and lists what it records. Students acknowledge an in-app notice before their first session. | Designed |
| A3 | Syllabus must set expectations for transparency and citation of AI use. | §VI.B.1 | When an assignment allows AI, the editor shows an "AI use disclosure" field (tool, purpose, extent) that is saved with the submission. | Designed |
| A4 | Syllabus must identify consequences for violations. | §VI.B.1 | The statement points to the Academic Dishonesty Policy and Student Code of Conduct. The instructor fills in course-specific consequences. | Designed (instructor to complete) |
| A5 | Faculty are encouraged to use the Flexible AI Statement Builder. | §VI.B.2 | The syllabus statement says to check its wording against the Builder before publishing. | Open |
| A6 | Using agentic AI to complete assessments without human interaction is cheating. | §VI.C | Signals for automated input: near-constant key timing, edits with no cursor activity, typing speed beyond human range, input while the window is hidden. | Designed |
| A7 | Concerns about inappropriate AI use go through existing processes (Faculty Handbook, Student Code of Conduct). | §III; §VI.H | Flags are labeled as indicators for a conversation, never as findings of misconduct. The UI names the Office of Student Conduct as the point of contact. | Designed |
| A8 | Only University-approved AI tools may be used to assess student work; tell students when AI is used for this. | §VI.B.3 | The app contains **no AI features**. Any future AI feature needs approved-inventory status, is opt-in and is disclosed to students. | Designed |
| A9 | Student and University data must not be entered into public AI tools. Class I/II data goes only to AI tools on the approved inventory. | §III; §VI.D; §VI.E.1 | Development uses synthetic data only. Real student data is never given to Claude or any other AI tool unless that tool is confirmed to be on the approved inventory. | **Pending approval**: confirm Claude's inventory status |
| A10 | AI tools and systems must be accessible to all users, including those with disabilities. | §VI.E.1 | Editor and viewer target WCAG 2.1 AA. An accommodation setting turns off typing-cadence signals so dictation or switch input is not flagged. | Designed |
| A11 | Human oversight: AI supports, not replaces, human judgment. | §VI.E.1 | No automated verdicts. Every flag needs instructor review and links to the replay so the evidence can be checked. | Designed |
| A12 | Students must complete required AI training and follow course AI guidelines. | §VI.C | The syllabus statement reminds students of the requirement. | Designed |

## 3. Open questions for University offices

Answer these before any real student uses the system.

| # | Question | Office | Blocks |
|---|---|---|---|
| Q1 | Is the proposed hosting approved for Class I student records? If a commercial cloud is used, has it passed the Third Party Risk Review? | Information Technology Services | D2, launch |
| Q2 | What is the retention period for keystroke logs, code and submissions? | Registrar or departmental Data Manager | D10 |
| Q3 | Is Claude (or any AI tool used in development) on the approved AI inventory? | Information Technology Services | A9 |
| Q4 | Who is the departmental Data Steward for course data, and must they approve this system? | Department chair | D3, D12 |
| Q5 | Does the syllabus statement match the Flexible AI Statement Builder's approved language? | Instructor, checking against the Builder | A5 |
