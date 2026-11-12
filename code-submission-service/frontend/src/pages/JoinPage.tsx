import { useState, type FormEvent } from "react";
import { joinAssignment, StudentApiError } from "../studentApi";

interface JoinPageProps {
  initialJoinCode: string;
  initialMessage?: string;
  onJoined: (studentToken: string) => void;
}

export function JoinPage({ initialJoinCode, initialMessage, onJoined }: JoinPageProps) {
  const [joinCode, setJoinCode] = useState(initialJoinCode);
  const [eagleIdentifier, setEagleIdentifier] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | undefined>(initialMessage);
  const [joining, setJoining] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setJoining(true);
    setErrorMessage(undefined);
    try {
      const joinResult = await joinAssignment(joinCode.trim(), eagleIdentifier.trim());
      setEagleIdentifier("");
      onJoined(joinResult.studentToken);
    } catch (error) {
      setErrorMessage(error instanceof StudentApiError ? error.message : "Could not join. Please try again.");
    } finally {
      setJoining(false);
    }
  }

  return (
    <main className="centered-page">
      <form className="card join-card" onSubmit={handleSubmit} aria-describedby="join-help">
        <p className="eyebrow">ILEP Code Submission</p>
        <h1>Join your assignment</h1>
        <p id="join-help" className="muted">
          Enter the join code your instructor shared and your Eagle ID.
        </p>

        <label htmlFor="join-code">Join code</label>
        <input
          id="join-code"
          name="joinCode"
          value={joinCode}
          onChange={(event) => setJoinCode(event.target.value.toUpperCase())}
          placeholder="ABCD-2345"
          autoComplete="off"
          autoCapitalize="characters"
          spellCheck={false}
          required
          maxLength={20}
        />

        <label htmlFor="eagle-identifier">Eagle ID</label>
        <input
          id="eagle-identifier"
          name="eagleIdentifier"
          value={eagleIdentifier}
          onChange={(event) => setEagleIdentifier(event.target.value)}
          inputMode="numeric"
          autoComplete="off"
          spellCheck={false}
          required
          maxLength={40}
        />

        {errorMessage && (
          <p className="form-error" role="alert">
            {errorMessage}
          </p>
        )}

        <button type="submit" className="primary-button" disabled={joining}>
          {joining ? "Joining…" : "Join"}
        </button>
        <p className="fine-print">
          Your Eagle ID is used only to confirm you're enrolled. It is not stored by this service.
        </p>
      </form>
    </main>
  );
}
