# Lesson Planning Workflow

> **Lesson-Plan Workflow:** a repeatable, gated sequence of steps an AI agent follows to turn a topic into a finished lesson plan. *Gated* means the agent stops at fixed checkpoints and waits for your approval.

---

## Goal

Produce a one-session Java lesson plan for intro programming students, plus an activities file the JavaFX Class Session screen can load.

---

## Inputs

| Input | Example |
|---|---|
| Course | CSCI1301, CSCI1302, IT2531 |
| Topic | <topic> in Java |
| Session length | 85 minutes or 55 minutes |
| Prior knowledge | Previous chapter concepts, passed assignments, resources |

Input arrives as `/lesson-plan <topic> [course]`, or as "follow the lesson-plan workflow on <topic>".

---

## Step-by-Step Process

**Step 1 - Clarify (Checkpoint 1)**
- Capture the topic and, if given, the course.
- Ask at least three clarifying questions:
  - **Course and section:** which course, and which section if it matters?
  - **Session length:** 85 or 55 minutes?
  - **Prior knowledge:** which chapters and assignments are done?
  - **App activities:** which to include: `SLIDE_ACTIVITIES`, `QUIZ`, `CODE_SUBMISSION`, `PRESENTATION`?
  - **Research depth:** overview or working knowledge, if new research is needed.
- **Stop and wait for answers.**

**Step 2 - Plan (Checkpoint 2)**
- Present a short written plan:
  - 2-4 learning goals
  - Key terms
  - Agenda with timings that sum to the session length
  - Activity list (type and title, each tied to a goal)
  - Sources to check (resource folders, existing reports, web)
  - Output paths
- **Stop and wait for approval.** Revise and re-present if asked.

**Step 3 - Research**
- Scan `resources/<course>/` first, then all of `/resources`. Read `.pptx`, `.pdf`, and `.docx` files with the matching document skills. List every file used.
- Look in `outputs/research/` for a report on the topic. Reuse it if one exists.
- Otherwise run Steps 4-6 of `workflows/research-workflow.md` inline. Its checkpoints are already covered by this workflow's two.

**Step 4 - Draft**
- Fill `templates/lesson-plan-template.md`, including one runnable Java example.
- Build the activities from the approved plan and fill `templates/lesson-activities-template.json`.
- Keep working files in `/drafts`.

**Step 5 - Review**
- Every term is defined before it is used.
- The Java example compiles: run `javac` on it in `/drafts`.
- Agenda timings add up to the session length.
- Every activity ties to a learning goal.
- The JSON is valid, and every `type` matches a `ClassActivity` constant.

**Step 6 - Deliver**
- Save `outputs/lesson-plans/<topic>-lesson-plan.md` and `outputs/lesson-plans/<topic>-activities.json`, with the topic hyphenated and in lowercase.
- If new research ran, also save `outputs/research/<topic>-research-report.md`.
- Clear this run's files from `/drafts`.
- Reply with a 2-3 line summary and the file paths.

---

## Rules

1. Never fabricate sources, statistics, or quotes.
2. Never skip a checkpoint. Approval is required before Step 3.
3. Ask instead of assuming when important information is missing.
4. Use only `ClassActivity` constant names as activity types.
5. Never commit, publish, or send anything without asking first.

---

## Output Format

The lesson plan begins with this metadata block:

```
---
Topic: [topic]
Course: [course]
Session length: [85 | 55] minutes
Prior knowledge: [chapters, assignments]
Research report: [path, or "None"]
Resources used: [file paths, or "None"]
Date: [YYYY-MM-DD]
---
```

The activities file follows `templates/lesson-activities-template.json`.

---

## Error Handling

| Situation | Action |
|---|---|
| Topic is too broad for one session | Propose 2-3 narrower topics and ask which one to teach. |
| No course materials found | Say so, and ask whether to continue with research only. |
| A resource file cannot be read | Name the file, skip it, and note it under Sources. |
| A requested activity type is not in `ClassActivity` | Flag it and offer the closest supported type. |
| Answers to Step 1 are incomplete | Re-ask only the missing questions. |

---

## Quality Checklist
- [ ] Every term is defined before it is used
- [ ] Code example compiles and runs
- [ ] Activities fit the session length
- [ ] Activities JSON is valid and loadable
