import { useCallback, useEffect, useState } from "react";
import { EditorPage } from "./pages/EditorPage";
import { JoinPage } from "./pages/JoinPage";
import { NoticePage } from "./pages/NoticePage";
import {
  loadWorkSession,
  readStoredStudentToken,
  StudentApiError,
  storeStudentToken,
  type StudentWorkSession,
} from "./studentApi";

type PageState =
  | { page: "join"; message?: string }
  | { page: "loading" }
  | { page: "notice"; workSession: StudentWorkSession }
  | { page: "editor"; workSession: StudentWorkSession };

function joinCodeFromAddressBar(): string {
  return new URLSearchParams(window.location.search).get("joinCode") ?? "";
}

export function App() {
  const [studentToken, setStudentToken] = useState<string | null>(readStoredStudentToken);
  const [pageState, setPageState] = useState<PageState>(studentToken ? { page: "loading" } : { page: "join" });

  const leaveSession = useCallback((message?: string) => {
    storeStudentToken(null);
    setStudentToken(null);
    setPageState({ page: "join", message });
  }, []);

  const showWorkSession = useCallback(
    async (token: string) => {
      setPageState({ page: "loading" });
      try {
        const workSession = await loadWorkSession(token);
        setPageState({ page: workSession.noticeAcknowledged ? "editor" : "notice", workSession });
      } catch (error) {
        if (error instanceof StudentApiError && error.httpStatus === 401) {
          leaveSession("Your session ended. Join again to keep working; your code was saved.");
        } else {
          leaveSession(error instanceof StudentApiError ? error.message : "Could not load your assignment.");
        }
      }
    },
    [leaveSession],
  );

  useEffect(() => {
    if (studentToken) {
      void showWorkSession(studentToken);
    }
    // Only on first load; later transitions call showWorkSession directly.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleJoined(newStudentToken: string) {
    storeStudentToken(newStudentToken);
    setStudentToken(newStudentToken);
    void showWorkSession(newStudentToken);
  }

  if (pageState.page === "join" || !studentToken) {
    return (
      <JoinPage
        initialJoinCode={joinCodeFromAddressBar()}
        initialMessage={pageState.page === "join" ? pageState.message : undefined}
        onJoined={handleJoined}
      />
    );
  }
  if (pageState.page === "loading") {
    return (
      <main className="centered-page" aria-busy="true">
        <p className="muted">Loading your assignment…</p>
      </main>
    );
  }
  if (pageState.page === "notice") {
    return (
      <NoticePage
        studentToken={studentToken}
        workSession={pageState.workSession}
        onAcknowledged={() => void showWorkSession(studentToken)}
      />
    );
  }
  return (
    <EditorPage
      studentToken={studentToken}
      workSession={pageState.workSession}
      onSessionExpired={() => leaveSession("Your session ended. Join again to keep working; your code was saved.")}
    />
  );
}
