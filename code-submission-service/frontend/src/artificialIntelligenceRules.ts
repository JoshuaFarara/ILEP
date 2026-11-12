import type { ArtificialIntelligenceUse } from "./studentApi";

// Wording follows docs/code-submission/syllabus-statement.md (course default: restricted).
export const artificialIntelligenceRuleText: Record<ArtificialIntelligenceUse, { label: string; summary: string }> = {
  forbidden: {
    label: "AI forbidden",
    summary: "No AI use of any kind on this assignment.",
  },
  restricted: {
    label: "AI restricted",
    summary:
      "AI is allowed only for the purposes your instructor lists. Writing or completing your solution code with AI is not allowed.",
  },
  permitted: {
    label: "AI permitted",
    summary: "AI is allowed for any part of this assignment, as long as you disclose how you used it.",
  },
};
