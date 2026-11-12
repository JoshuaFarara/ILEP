import { useCallback, useEffect, useRef, useState } from "react";
import { JavaCodeEditor } from "../components/JavaCodeEditor";
import { artificialIntelligenceRuleText } from "../artificialIntelligenceRules";
import {
  runCode,
  saveCode,
  StudentApiError,
  submitCode,
  type CodeRunResult,
  type StudentWorkSession,
} from "../studentApi";

interface EditorPageProps {
  studentToken: string;
  workSession: StudentWorkSession;
  onSessionExpired: () => void;
}

type SaveState =
  | { kind: "saved"; savedAt?: string }
  | { kind: "unsaved" }
  | { kind: "saving" }
  | { kind: "failed"; message: string };

const autosaveDebounceMilliseconds = 2_000;
const autosaveIntervalMilliseconds = 10_000;

export function EditorPage({ studentToken, workSession, onSessionExpired }: EditorPageProps) {
  const [submittedAt, setSubmittedAt] = useState(workSession.submittedAt);
  const [saveState, setSaveState] = useState<SaveState>({ kind: "saved", savedAt: workSession.lastSavedAt });
  const [standardInput, setStandardInput] = useState("");
  const [running, setRunning] = useState(false);
  const [codeRunResult, setCodeRunResult] = useState<CodeRunResult>();
  const [runErrorMessage, setRunErrorMessage] = useState<string>();
  const [confirmingSubmit, setConfirmingSubmit] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitErrorMessage, setSubmitErrorMessage] = useState<string>();

  const currentCode = useRef(workSession.currentCode);
  const lastSavedCode = useRef(workSession.currentCode);
  const debounceTimer = useRef<number>(undefined);
  const saveInFlight = useRef(false);
  const submitDialog = useRef<HTMLDialogElement>(null);

  const locked = Boolean(submittedAt) || workSession.assignmentClosed;
  const artificialIntelligenceRule = artificialIntelligenceRuleText[workSession.artificialIntelligenceUse];

  const handleApiError = useCallback(
    (error: unknown, fallbackMessage: string): string => {
      if (error instanceof StudentApiError) {
        if (error.httpStatus === 401) {
          onSessionExpired();
        }
        return error.message;
      }
      return fallbackMessage;
    },
    [onSessionExpired],
  );

  const saveIfChanged = useCallback(async () => {
    if (locked || saveInFlight.current || currentCode.current === lastSavedCode.current) {
      return;
    }
    const codeBeingSaved = currentCode.current;
    saveInFlight.current = true;
    setSaveState({ kind: "saving" });
    try {
      const saveResult = await saveCode(studentToken, codeBeingSaved);
      lastSavedCode.current = codeBeingSaved;
      setSaveState(
        currentCode.current === codeBeingSaved ? { kind: "saved", savedAt: saveResult.savedAt } : { kind: "unsaved" },
      );
    } catch (error) {
      setSaveState({ kind: "failed", message: handleApiError(error, "Couldn't save. Retrying…") });
    } finally {
      saveInFlight.current = false;
    }
  }, [handleApiError, locked, studentToken]);

  const handleCodeChange = useCallback(
    (code: string) => {
      currentCode.current = code;
      setSaveState({ kind: "unsaved" });
      window.clearTimeout(debounceTimer.current);
      debounceTimer.current = window.setTimeout(saveIfChanged, autosaveDebounceMilliseconds);
    },
    [saveIfChanged],
  );

  useEffect(() => {
    const intervalTimer = window.setInterval(saveIfChanged, autosaveIntervalMilliseconds);
    const saveBeforeLeaving = () => {
      if (document.visibilityState === "hidden") {
        void saveIfChanged();
      }
    };
    document.addEventListener("visibilitychange", saveBeforeLeaving);
    return () => {
      window.clearInterval(intervalTimer);
      window.clearTimeout(debounceTimer.current);
      document.removeEventListener("visibilitychange", saveBeforeLeaving);
    };
  }, [saveIfChanged]);

  async function handleRun() {
    setRunning(true);
    setRunErrorMessage(undefined);
    try {
      const codeBeingRun = currentCode.current;
      const result = await runCode(studentToken, codeBeingRun, standardInput);
      // Running also saves the code on the server.
      lastSavedCode.current = codeBeingRun;
      if (currentCode.current === codeBeingRun) {
        setSaveState({ kind: "saved", savedAt: new Date().toISOString() });
      }
      setCodeRunResult(result);
    } catch (error) {
      setRunErrorMessage(handleApiError(error, "The code could not be run. Please try again."));
    } finally {
      setRunning(false);
    }
  }

  function openSubmitDialog() {
    setSubmitErrorMessage(undefined);
    setConfirmingSubmit(true);
    submitDialog.current?.showModal();
  }

  function closeSubmitDialog() {
    setConfirmingSubmit(false);
    submitDialog.current?.close();
  }

  async function handleSubmit() {
    setSubmitting(true);
    try {
      window.clearTimeout(debounceTimer.current);
      const submitResult = await submitCode(studentToken, currentCode.current);
      lastSavedCode.current = currentCode.current;
      setSubmittedAt(submitResult.submittedAt);
      setSaveState({ kind: "saved", savedAt: submitResult.submittedAt });
      closeSubmitDialog();
    } catch (error) {
      setSubmitErrorMessage(handleApiError(error, "Could not submit. Please try again."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="editor-layout">
      <header className="editor-header">
        <div>
          <p className="eyebrow">ILEP Code Submission</p>
          <h1>{workSession.title}</h1>
          {workSession.dueAt && <p className="muted">Due {formatDateTime(workSession.dueAt)}</p>}
        </div>
        <div className="header-actions">
          <SaveStatus saveState={saveState} submittedAt={submittedAt} assignmentClosed={workSession.assignmentClosed} />
          <button className="primary-button" onClick={openSubmitDialog} disabled={locked}>
            {submittedAt ? "Submitted" : "Submit"}
          </button>
        </div>
      </header>

      <div className={`rule-banner rule-${workSession.artificialIntelligenceUse}`} role="note">
        <strong>{artificialIntelligenceRule.label}.</strong> {artificialIntelligenceRule.summary}
        {workSession.artificialIntelligenceNote && <span> {workSession.artificialIntelligenceNote}</span>}
      </div>

      <div className="workspace">
        <section className="instructions-panel" aria-labelledby="instructions-heading">
          <h2 id="instructions-heading">Instructions</h2>
          <div className="instructions-text">{workSession.instructions || "No instructions provided."}</div>
        </section>

        <section className="code-panel" aria-labelledby="code-heading">
          <h2 id="code-heading" className="visually-hidden">
            Your code
          </h2>
          {submittedAt && (
            <p className="locked-message" role="status">
              Submitted {formatDateTime(submittedAt)}. Your code is now read-only.
            </p>
          )}
          {!submittedAt && workSession.assignmentClosed && (
            <p className="locked-message" role="status">
              This assignment is closed. Your code is read-only.
            </p>
          )}
          <JavaCodeEditor initialCode={workSession.currentCode} readOnly={locked} onCodeChange={handleCodeChange} />
        </section>

        <section className="run-panel" aria-labelledby="run-heading">
          <h2 id="run-heading">Run</h2>
          <label htmlFor="standard-input">Program input (standard input)</label>
          <textarea
            id="standard-input"
            value={standardInput}
            onChange={(event) => setStandardInput(event.target.value)}
            rows={3}
            spellCheck={false}
            disabled={locked}
            placeholder="Text your program reads with Scanner"
          />
          <button className="secondary-button" onClick={handleRun} disabled={running || locked}>
            {running ? "Running…" : "Run ▶"}
          </button>
          <div className="run-output" aria-live="polite">
            {runErrorMessage && <p className="form-error">{runErrorMessage}</p>}
            {codeRunResult && <RunOutput codeRunResult={codeRunResult} />}
          </div>
        </section>
      </div>

      <dialog ref={submitDialog} className="card submit-dialog" onClose={() => setConfirmingSubmit(false)}>
        {confirmingSubmit && (
          <>
            <h2>Submit your code?</h2>
            <p>After you submit, you can't change your code.</p>
            {submitErrorMessage && (
              <p className="form-error" role="alert">
                {submitErrorMessage}
              </p>
            )}
            <div className="dialog-actions">
              <button className="secondary-button" onClick={closeSubmitDialog} disabled={submitting}>
                Keep working
              </button>
              <button className="primary-button" onClick={handleSubmit} disabled={submitting}>
                {submitting ? "Submitting…" : "Submit"}
              </button>
            </div>
          </>
        )}
      </dialog>
    </div>
  );
}

function SaveStatus({
  saveState,
  submittedAt,
  assignmentClosed,
}: {
  saveState: SaveState;
  submittedAt?: string;
  assignmentClosed: boolean;
}) {
  let text: string;
  if (submittedAt) {
    text = "Submitted";
  } else if (assignmentClosed) {
    text = "Closed";
  } else if (saveState.kind === "saving") {
    text = "Saving…";
  } else if (saveState.kind === "unsaved") {
    text = "Unsaved changes";
  } else if (saveState.kind === "failed") {
    text = saveState.message;
  } else {
    text = saveState.savedAt ? `Saved ${formatTime(saveState.savedAt)}` : "No changes yet";
  }
  return (
    <span className={`save-status save-${submittedAt ? "submitted" : saveState.kind}`} role="status" aria-live="polite">
      {text}
    </span>
  );
}

function RunOutput({ codeRunResult }: { codeRunResult: CodeRunResult }) {
  if (!codeRunResult.compileSucceeded) {
    return (
      <>
        <p className="output-label output-error-label">Compile errors</p>
        <pre className="output-text output-error">{codeRunResult.standardError}</pre>
      </>
    );
  }
  return (
    <>
      <p className="output-label">
        Output
        <span className="muted">
          {" "}
          · {codeRunResult.timedOut ? "stopped: time limit reached" : `exit code ${codeRunResult.exitCode}`} ·{" "}
          {(codeRunResult.durationMilliseconds / 1000).toFixed(1)} s
        </span>
      </p>
      <pre className="output-text">{codeRunResult.standardOutput || "(no output)"}</pre>
      {codeRunResult.standardError && <pre className="output-text output-error">{codeRunResult.standardError}</pre>}
      {codeRunResult.outputTruncated && <p className="muted">Output was too long and was cut off.</p>}
    </>
  );
}

function formatDateTime(isoDateTime: string) {
  return new Date(isoDateTime).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });
}

function formatTime(isoDateTime: string) {
  return new Date(isoDateTime).toLocaleTimeString(undefined, { hour: "numeric", minute: "2-digit" });
}
