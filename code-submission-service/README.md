# Code Submission Service

A browser-based Java editor for ILEP's **Code Submission** class activity. Students join with a code, write Java, run it in a sandbox, and submit. Their work saves automatically.

**Status: Phase 1 (walking skeleton). Synthetic data only.** Do not use with real students until the open questions in [`docs/code-submission/policy-compliance.md`](../docs/code-submission/policy-compliance.md) are answered: hosting approval, retention period and AI-tool approval.

## How it fits together

```
ILEP (instructor laptop)            Code Submission service                 Student browser
  roster: names, Eagle IDs  ──►  pseudonymous roster:                ◄──  join code + Eagle ID
  (never leave the laptop)        studentReference + joinHash           (ID checked, then discarded)
                                  assignments, code, runs                editor, run, submit
                                        │
                                        ▼
                                  Docker sandbox (one container per run)
```

- **Instructor API** (`Authorization: Bearer <instructor API key>`):
  - `PUT /api/v1/courses/{courseReference}/roster`: replace the roster. Idempotent; students left out lose access.
  - `POST /api/v1/courses/{courseReference}/assignments`: returns `assignmentId`, `joinCode`, `joinUrl`.
  - `POST /api/v1/assignments/{assignmentId}/close`
  - `GET /api/v1/assignments/{assignmentId}/summary`: per-student status, times and run count.
- **Student API** (token from `POST /api/v1/join`): `GET /api/v1/student/session`, `POST .../notice-acknowledgement`, `PUT .../code` (autosave), `POST .../runs`, `POST .../submit`.
- **Errors** always look like `{"error": "validation_failed", "message": "..."}`.

### Join hash (ILEP must compute it the same way)

```
courseSecret  = 32 random bytes, sent as 64 hex characters
normalizedId  = Eagle ID with spaces and hyphens removed, uppercased
joinHash      = hex( HMAC-SHA256(key = courseSecret, message = UTF-8(normalizedId)) )
```

The server stores the course secret encrypted (AES-256-GCM) and the join hash. It never stores the Eagle ID.

## Run it locally

Requirements: JDK 25, Docker, Node.js 22+.

```bash
cd code-submission-service
```

```bash
docker build -t ilep-java-runner:25 runner-image
```

```bash
docker compose up -d
```

```bash
cd backend
INSTRUCTOR_API_KEY="$(openssl rand -hex 24)" COURSE_SECRET_ENCRYPTION_KEY="$(openssl rand -base64 32)" ./gradlew bootRun
```

Keep the `INSTRUCTOR_API_KEY` value; you need it to create a class. Then, in another terminal:

```bash
INSTRUCTOR_API_KEY=<the same key> ./scripts/create-synthetic-class.sh
```

Open the printed `joinUrl` and join with a synthetic Eagle ID from `900000001` to `900000005`.

To work on the editor with live reload, run `npm install && npm run dev` in `frontend/` and open http://localhost:5173 (API calls are forwarded to port 8080). `./gradlew bootJar` builds one jar that includes the editor.

### Configuration

| Variable | Purpose |
|---|---|
| `COURSE_SECRET_ENCRYPTION_KEY` | **Required.** 32 random bytes, base64. Encrypts course secrets at rest. |
| `INSTRUCTOR_API_KEY` | Development only: registers an instructor with this key at startup. |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | PostgreSQL connection. |
| `PUBLIC_BASE_URL` | Address students use; join links are built from it. |

Never commit keys, `.env` files or real student data.

## Tests

```bash
cd backend && ./gradlew test
```

42 tests run, covering:
- unit tests, plus API integration tests against a throwaway PostgreSQL container (Testcontainers)
- sandbox escape checks in real containers, tagged `docker`
- a **data-stewardship test**: after a full synthetic class session, no Eagle ID appears anywhere in the database or the logs

Skip the sandbox tests with `-PexcludeDockerTests`.

Browser test of the whole student flow (needs a running service and a synthetic class):

```bash
cd frontend && NODE_PATH="$(npm root -g)" node e2e/student-flow.cjs http://localhost:8080 <join-code>
```

## Sandbox limits

Each run gets a new container that is removed afterwards. Limits:
- no network
- read-only root file system, with a 32 MB writable `/workspace`
- 256 MB memory, no extra swap
- half a CPU, 64 processes
- all Linux capabilities dropped, no privilege escalation, user `nobody`
- 10 s program time limit, 15 s wall-clock limit
- 64 KB output per stream

At most 4 runs at once across the service, and one per student.

Known limits, to address in Phase 5:
- Docker shares the host kernel; consider gVisor (`--runtime=runsc`) for stronger isolation.
- The service starts containers through the host Docker daemon, which is root-equivalent. In production, put the runner on a separate host.

## Not in Phase 1

- Keystroke, paste and focus capture; replay and heat maps (Phases 3-4).
- The ILEP panel (Phase 2).
- AI-use disclosure field, exam mode, multiple devices per student, exports.
