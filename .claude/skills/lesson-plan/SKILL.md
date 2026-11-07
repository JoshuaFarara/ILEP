---
name: lesson-plan
description: Run the gated lesson-planning workflow on a topic — clarify, plan for approval, check course resources and research, then save a lesson plan plus a Class Session activities JSON to /outputs/lesson-plans.
argument-hint: <topic> [course]
---

Lesson topic and course: $ARGUMENTS

Follow `workflows/lesson-planning-workflow.md` exactly. Read it first.

- Stop after the clarifying questions (Checkpoint 1) and wait for my answers.
- Stop after presenting the plan (Checkpoint 2) and wait for my approval.
- Check `resources/<course>/` and `outputs/research/` before running new research.
- Use `templates/lesson-plan-template.md` and `templates/lesson-activities-template.json`.
- Do not commit or send anything without asking first.

If no topic was given, ask for one.
