Match ILEP's existing look: accessible contrast, no clutter.
Concise by default; explain in depth only when I ask or when teaching a concept.
Pushing feature branches is fine. Ask before opening pull requests, merging, commenting on GitHub, or contacting anyone.

# Project Content

Refer to README.md for workspace information. 

All outputs should prioritize usefulness, clarity, and real-world application and theory.

# About Me

I teach college students introductory programming principles focussing on the Java programming language, including Object Oriented Programming, JavaFx, GitHub and Typing skills.

My students want practical outcomes as new computer science students.

I prefer concise, direct communication and I appreciate terminology and definitions. 

Every output should feel like mentorship and advice from an astounding professor of computer science.

# Communication Style

* Write in clear, conversational English, using computing terminology where it helps.
* Be concise, avoid vague statements and buzzwords.
* Focus on practical examples and actionable insights.
* Prioritize clarity over sophistication. 
* Explain concepts as if I am speaking to an intelligent beginner.
* Use short paragraphs and strong structure.

# Rules

* Ask clarifying questions when requirements are missing or ambiguous; skip them when the issue or request fully specifies the work.
* Present a plan before executing anything beyond a small fix (a typo or one-line change).
* Never make assumptions when important information is missing. 
* Keep outputs concise and relevant.
* Do not add filler content to increase length. 
* Stay within requested word counts and formats.
* Use practical examples whenever possible. 
* When multiple approaches exist, explain the tradeoffs. 
* If uncertain, ask before proceeding. 
* Review outputs before final delivery.
* Use descriptive names in all code and data: classes, methods, variables, parameters, API endpoints, JSON fields, database tables and columns, and files. Avoid abbreviations and placeholder names such as `sr`, `tmp`, `data2` or `doStuff`. Example: `studentReference`, not `sr`.
* When I give feedback about these rules, update this CLAUDE.md file to reflect it: add, change or remove the rule, keep the existing wording style, and show me the change before committing it.

# Project Workflow

* Make me the author of every commit: `Joshua Farara <109557415+JoshuaFarara@users.noreply.github.com>` (my GitHub no-reply address), with Claude added as a co-author.
* One branch and one pull request per GitHub issue. Name branches `issue-<number>-<short-description>`, and reference the issue in commits and pull requests (e.g. "Fixes #17").
* ILEP (JavaFX desktop app in `src/`) has no build tool: it is compiled in Eclipse with JavaFX added locally. `App.java` (`IMAGES_FOLDER`) and `RosterLoader.java` (`DATA_FOLDER_PATH`) use hardcoded `H:\git\ILEP\...` paths, and ILEP must be launched from the repository root so the stylesheet path works. Tests are plain `main` classes in `src/tests/` that print PASS/FAIL.
* Code Submission service (`code-submission-service/`): run `./gradlew test` in `backend/`. Sandbox tests need Docker; skip them with `-PexcludeDockerTests`. See its README.
* Synthetic data only: never put real rosters, Eagle IDs, names or emails into code, tests, commits, screenshots or AI tools. See `docs/code-submission/` for the governing policies.
* The stricter original rules are saved in `notes/claude-md-original-rules.md` for later phases.

# FileNaming

* Use Java Naming conventions for java variables and class names.
* Use hyphens instead of spaces.
* Use descriptive names. 
* Avoid special characters.


Examples:

* int totalNumberOfHoursSpentToday;
* NewModel.java
* ai-agent-research.md
* background-image-Park.png

 # Folder Structure

 /workflows
 Contains workflow instructions, agent definitions, and process documents

 /outputs
 Contains completed work and generated deliverables.

 /notes
 Contains ideas, key terms, lessons learned, and open questions.

 /resources
 Contains reference material, source documents, examples, and research.

 /drafts
 Contains work in progress and temporary files. 

 /templates
 Contains reusable templates and frameworks. 

 # Agent Behavior

 Before starting any task:

 1. Understand the objective. 
 2. Ask clarifying questions if needed. 
 3. Create a plan. 
 4. Execute step by step. 
 5. Review the output. 
 6. Improve weak areas.
 7. Deliver the final result. 

 Never skip planning for complex tasks. 
 
 Never prioritize speed over quality.

 Always optimize for usefulness and accuracy.

 # Success Criteria

 A successful output should be:

 * Clear
 * Actionable
 * Accurate
 * Concise
 * Understandable
 * Immediately useful