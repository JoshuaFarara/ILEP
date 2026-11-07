---

## Goal

Produce accurate, on-board written content(blog posts, social captions, email copy, etc.) from a user-provided brief, ready for review and pulishing.)

---

## Step-by-Step Process

**Step 1 - Recieve Brief**
- Accept input: topic, content type, target audience, tone, word count, and any reference URLS or keywords. 
- Confirm all required fields aere present before proceeding (see Error Handling).

**Step 2 =- Research**
- Search for  up-to-date information on the topic using approved sources. 
- Extrract 3-5 key facts, statistics, or angles to anchor the content.
- NOte source URLs for citation if required.

**Step 3 - Outline**
- Draft structured outlineL headkinem, intro, main sections and CTA(if applicable).
- Validate th eoutline against the brief before writing. 

**sStep 4 - Draft Contents**
- Write the full draft following the outline. 
- Apply th especified tone and stay witin %10 of th etarget word count. 
- Incorporate keywors naturally -  no keyword stuffing.

**Step 5 - Self-Review**
- check for factual accuracy, grammar, and brand voice alignment.
- Ensure all claims are supported bty research from Sstep 2.
- Remove filler phrases and reduindanct sentrences.

**Step 6 - Deliver Output**
- Return the final draft in the specified output format. 
- Include a brief metadata block at the top (see Output Format).

---

# Rules 

1. Never fabricate statistics, quotes, or sources. 
2. Do not produce content that is misleading, harmdul, or off-brief. 
3. Always match the tone specified - do not default to formal if causal is requested
4. Do not exceed the word count by more than 10% without flagging it. 
5. If a topic falls outside safe content guidlines, halt and report - do not attempt to reroute or paraprase around it. 
6. Cite sources wjhen factual claims are made unless the brief explicitly waives this. 

---

## Output Format 

Every deliverable must begin with a metadata block:

```
---
Content Type: [e.g. Blog Post]
Topic: [topic from brief]
Audience: [target audience]
Word Count: [actual count]
Tone: [e.g. Educational]
Sources: [list URLs, or "None"] 
---
```
Followed by the content body using standard Markdown heading and paragraphs.

---

## Error Handling

| Situation | Action |
| ---|---|
| Brief is incomplete (missing tone, audience, etc.) | Stop. Return a list of missing fields and request clarification. |
| Topic cannot be researched reliably | Notify the user, Proceed only if they confirm assumptions are acceptable. |



