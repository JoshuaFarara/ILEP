import { useState } from "react";
import { artificialIntelligenceRuleText } from "../artificialIntelligenceRules";
import { acknowledgeNotice, StudentApiError, type StudentWorkSession } from "../studentApi";

interface NoticePageProps {
  studentToken: string;
  workSession: StudentWorkSession;
  onAcknowledged: () => void;
}

/** The in-app notice from docs/code-submission/syllabus-statement.md, Part 2 (notice version 1). */
export function NoticePage({ studentToken, workSession, onAcknowledged }: NoticePageProps) {
  const [understood, setUnderstood] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>();
  const artificialIntelligenceRule = artificialIntelligenceRuleText[workSession.artificialIntelligenceUse];

  async function handleContinue() {
    setSaving(true);
    setErrorMessage(undefined);
    try {
      await acknowledgeNotice(studentToken, workSession.currentNoticeVersion);
      onAcknowledged();
    } catch (error) {
      setErrorMessage(error instanceof StudentApiError ? error.message : "Could not save. Please try again.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="centered-page">
      <section className="card notice-card" aria-labelledby="notice-heading">
        <p className="eyebrow">{workSession.title}</p>
        <h1 id="notice-heading">Before you start</h1>

        <p>
          This editor records your work as you write it, <strong>inside this editor only</strong>: your typing and
          edits, pastes, runs, and when you switch away from this window. It does not see other tabs, other apps, your
          camera or your screen.
        </p>
        <p>
          Your instructor uses this record to understand how you solved the problem and where you got stuck. It is a
          confidential education record (FERPA) and only your instructor can view it.
        </p>

        <div className={`rule-banner rule-${workSession.artificialIntelligenceUse}`}>
          <strong>This assignment's AI rule: {artificialIntelligenceRule.label}.</strong>{" "}
          {artificialIntelligenceRule.summary}
          {workSession.artificialIntelligenceNote && <span> {workSession.artificialIntelligenceNote}</span>}
        </div>

        <p>
          A highlighted pattern, such as a large paste, is not an accusation. Questions? Ask your instructor. Use
          assistive input? Tell your instructor so it isn't flagged.
        </p>

        <label className="checkbox-row">
          <input type="checkbox" checked={understood} onChange={(event) => setUnderstood(event.target.checked)} />
          <span>I understand</span>
        </label>

        {errorMessage && (
          <p className="form-error" role="alert">
            {errorMessage}
          </p>
        )}

        <button className="primary-button" disabled={!understood || saving} onClick={handleContinue}>
          {saving ? "Saving…" : "Continue to the editor"}
        </button>
        <p className="fine-print">Notice version {workSession.currentNoticeVersion}</p>
      </section>
    </main>
  );
}
