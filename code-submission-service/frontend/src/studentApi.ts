// Calls to the Code Submission service's student API.
// The session token lives in sessionStorage: it is cleared when the tab closes.

const studentTokenStorageKey = "codeSubmission.studentToken";

export type ArtificialIntelligenceUse = "forbidden" | "restricted" | "permitted";

export interface StudentWorkSession {
  title: string;
  instructions: string;
  assignmentMode: "assignment" | "exam";
  artificialIntelligenceUse: ArtificialIntelligenceUse;
  artificialIntelligenceNote: string;
  dueAt?: string;
  currentCode: string;
  status: "inProgress" | "submitted";
  lastSavedAt?: string;
  submittedAt?: string;
  assignmentClosed: boolean;
  noticeAcknowledged: boolean;
  currentNoticeVersion: number;
}

export interface CodeRunResult {
  compileSucceeded: boolean;
  standardOutput: string;
  standardError: string;
  exitCode?: number;
  timedOut: boolean;
  outputTruncated: boolean;
  durationMilliseconds: number;
}

/** An error from the service, with its machine-readable code and a message safe to show. */
export class StudentApiError extends Error {
  constructor(
    readonly httpStatus: number,
    readonly errorCode: string,
    message: string,
  ) {
    super(message);
  }
}

export function readStoredStudentToken(): string | null {
  try {
    return sessionStorage.getItem(studentTokenStorageKey);
  } catch {
    return null;
  }
}

export function storeStudentToken(studentToken: string | null): void {
  try {
    if (studentToken) {
      sessionStorage.setItem(studentTokenStorageKey, studentToken);
    } else {
      sessionStorage.removeItem(studentTokenStorageKey);
    }
  } catch {
    // Storage can be blocked (private browsing). The session then lasts only while the page is open.
  }
}

async function callStudentApi<ResponseBody>(
  method: string,
  path: string,
  studentToken: string | null,
  requestBody?: unknown,
): Promise<ResponseBody> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (requestBody !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (studentToken) {
    headers["Authorization"] = `Bearer ${studentToken}`;
  }

  let response: Response;
  try {
    response = await fetch(path, {
      method,
      headers,
      body: requestBody === undefined ? undefined : JSON.stringify(requestBody),
      cache: "no-store",
    });
  } catch {
    throw new StudentApiError(0, "network_error", "Can't reach the server. Check your connection and try again.");
  }

  if (response.status === 204) {
    return undefined as ResponseBody;
  }
  const responseBody = await response.json().catch(() => null);
  if (!response.ok) {
    throw new StudentApiError(
      response.status,
      responseBody?.error ?? "unknown_error",
      responseBody?.message ?? "Something went wrong. Please try again.",
    );
  }
  return responseBody as ResponseBody;
}

export function joinAssignment(joinCode: string, eagleIdentifier: string) {
  return callStudentApi<{ studentToken: string; expiresAt: string }>("POST", "/api/v1/join", null, {
    joinCode,
    eagleIdentifier,
  });
}

export function loadWorkSession(studentToken: string) {
  return callStudentApi<StudentWorkSession>("GET", "/api/v1/student/session", studentToken);
}

export function acknowledgeNotice(studentToken: string, noticeVersion: number) {
  return callStudentApi<void>("POST", "/api/v1/student/session/notice-acknowledgement", studentToken, {
    noticeVersion,
  });
}

export function saveCode(studentToken: string, code: string) {
  return callStudentApi<{ savedAt: string }>("PUT", "/api/v1/student/session/code", studentToken, { code });
}

export function runCode(studentToken: string, code: string, standardInput: string) {
  return callStudentApi<CodeRunResult>("POST", "/api/v1/student/session/runs", studentToken, {
    code,
    standardInput,
  });
}

export function submitCode(studentToken: string, code: string) {
  return callStudentApi<{ submittedAt: string }>("POST", "/api/v1/student/session/submit", studentToken, { code });
}
