# Code Submission: Policy Documents (Phase 0)

**Status:** Draft. Not yet reviewed by Information Technology Services, the Registrar or the Office of Legal Affairs. Do not use with real students until the open items in [policy-compliance.md](policy-compliance.md) are closed.

The Code Submission web extension adds a browser-based Java editor to ILEP's **Code Submission** class activity (`ClassActivity.CODE_SUBMISSION`). It records how students write their code, not just the final result: edits, pastes, runs and focus changes inside the editor. That recording is a student record, so this folder defines how the project handles it before any code is written.

## Governing policies

| Short name | Document | Revision |
|---|---|---|
| **DSCS** | Georgia Southern University, *Data Stewardship and Classification Standard* | December 1, 2025 |
| **AI Policy** | Georgia Southern University, *Use of Artificial Intelligence (AI) in Academic Contexts* | September 17, 2026 |

Section numbers in these documents (for example, DSCS §V.B) refer to those revisions. If either policy is revised, re-check every row in `policy-compliance.md`.

## Contents

| File | Purpose |
|---|---|
| [data-classification.md](data-classification.md) | Every data element the system touches, its class (I, II or III), and how it is stored, transmitted, retained and accessed. |
| [policy-compliance.md](policy-compliance.md) | Each policy requirement mapped to the design decision that meets it, with status and open questions. |
| [syllabus-statement.md](syllabus-statement.md) | Paste-ready syllabus language (AI use and process tracking) and the in-app student notice. |

## Ground rules for anyone working on this project

1. **No real student data in the repository.** Names, emails, Eagle IDs, CRN rosters and captured sessions never go into git. ILEP's `.gitignore` already blocks `data/`, `*.csv` and `appData/`.
2. **Synthetic data only** in tests, demos and documentation. Example: `Student One`, Eagle ID `900000001`, email `student.one@example.edu`.
3. **No real student data in AI tools**, including Claude, unless the tool is on the University's approved AI inventory (AI Policy §VI.D, §VI.E.1).
