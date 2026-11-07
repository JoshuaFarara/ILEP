# Research Workflow

> **Research workflow:** a gated process that turns a topic into a cited, structured report. *Gated* means the agent stops at fixed checkpoints and waits for your approval before continuing.

---

## Goal

Produce an accurate, well-sourced research report on a user-provided topic, pitched at the right audience and depth, and saved to `/outputs/research`.

---

## Step-by-Step Process

**Step 1 - Receive Topic**
- Input arrives as `/research <topic>`, or as "follow the research workflow on <topic>".

**Step 2 - Clarify (Checkpoint 1)**
- Ask at least three clarifying questions:
  - **Scope:** what is in and out of bounds?
  - **Audience:** who will read it (e.g., first-year CS students, the instructor, admins)?
  - **Depth:** overview, working knowledge, or deep dive?
- Ask about length, deadline, or required sources only when they matter.
- **Stop and wait for answers.**

**Step 3 - Plan (Checkpoint 2)**
- Present a short written plan:
  - 3-6 research questions the report will answer
  - Proposed section headings
  - Sources to check (local files and source types)
  - Output filename
- **Stop and wait for approval.** Revise and re-present if asked.

**Step 4 - Research**
- Check `/resources` first for relevant local material.
- Then search the web for current information.
- Record a source URL (or file path) for every factual claim.
- Prefer primary sources: official documentation, specifications, peer-reviewed papers, and vendor docs, over blogs and forums.

**Step 5 - Organize**
- Fill in `templates/research-report-template.md`.
- Define each term before you use it.
- Include at least one practical, real-world example (code where relevant).

**Step 6 - Self-Review**
- Every claim is backed by a cited source.
- The depth and vocabulary match the agreed audience.
- The length matches what was agreed, and there is no filler.
- Where sources conflict, the conflict is flagged rather than hidden.

**Step 7 - Save and Report**
- Save to `outputs/research/<topic>-research-report.md`, with the topic hyphenated and in lowercase.
- Reply with a 2-3 line summary and the file path.

---

## Rules

1. Never fabricate sources, statistics, or quotes.
2. Never skip a checkpoint. Approval is required before Step 4.
3. Ask instead of assuming when important information is missing.
4. Flag low-confidence or conflicting findings explicitly.
5. Never commit, publish, or send anything without asking first.

---

## Error Handling

| Situation | Action |
|---|---|
| Topic is too broad | Propose 2-3 narrower angles and ask which one to pursue. |
| Few reliable sources exist | Say so, and ask before continuing with weaker sources. |
| Sources contradict each other | Present both positions with citations in Findings. |
| Answers to Step 2 are incomplete | Re-ask only the missing questions. |
