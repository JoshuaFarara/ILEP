-- Code Submission service, initial schema.
-- Data classification: see docs/code-submission/data-classification.md.
-- This schema stores NO student names, emails, Eagle IDs or CRNs. Students are known only by
-- an opaque student_reference created by ILEP, and a join_hash (HMAC of the Eagle ID).

create table instructors (
    instructor_id    bigint generated always as identity primary key,
    display_label    text        not null,
    api_key_hash     bytea       not null unique,
    created_at       timestamptz not null default now()
);

create table courses (
    course_id                bigint generated always as identity primary key,
    instructor_id            bigint      not null references instructors (instructor_id),
    course_reference         text        not null unique,
    label                    text        not null,
    encrypted_course_secret  bytea       not null,
    created_at               timestamptz not null default now(),
    updated_at               timestamptz not null default now()
);

create table enrollments (
    enrollment_id      bigint generated always as identity primary key,
    course_id          bigint      not null references courses (course_id) on delete cascade,
    student_reference  text        not null,
    join_hash          bytea       not null,
    active             boolean     not null default true,
    updated_at         timestamptz not null default now(),
    unique (course_id, student_reference)
);
create index enrollments_course_join_hash on enrollments (course_id, join_hash);

create table assignments (
    assignment_id                 uuid        primary key,
    course_id                     bigint      not null references courses (course_id) on delete cascade,
    title                         text        not null,
    instructions                  text        not null default '',
    starter_code                  text        not null default '',
    assignment_mode               text        not null check (assignment_mode in ('assignment', 'exam')),
    artificial_intelligence_use   text        not null check (artificial_intelligence_use in ('forbidden', 'restricted', 'permitted')),
    artificial_intelligence_note  text        not null default '',
    opens_at                      timestamptz,
    due_at                        timestamptz,
    join_code                     text        not null unique,
    closed_at                     timestamptz,
    created_at                    timestamptz not null default now()
);

create table student_work_sessions (
    student_work_session_id  uuid        primary key,
    assignment_id            uuid        not null references assignments (assignment_id) on delete cascade,
    enrollment_id            bigint      not null references enrollments (enrollment_id) on delete cascade,
    current_code             text        not null,
    started_at               timestamptz not null default now(),
    last_saved_at            timestamptz,
    submitted_at             timestamptz,
    unique (assignment_id, enrollment_id)
);

create table student_session_tokens (
    token_hash               bytea       primary key,
    student_work_session_id  uuid        not null references student_work_sessions (student_work_session_id) on delete cascade,
    expires_at               timestamptz not null,
    created_at               timestamptz not null default now()
);

create table notice_acknowledgements (
    notice_acknowledgement_id  bigint generated always as identity primary key,
    student_work_session_id    uuid        not null references student_work_sessions (student_work_session_id) on delete cascade,
    notice_version             integer     not null,
    acknowledged_at            timestamptz not null default now(),
    unique (student_work_session_id, notice_version)
);

create table code_runs (
    code_run_id              bigint generated always as identity primary key,
    student_work_session_id  uuid        not null references student_work_sessions (student_work_session_id) on delete cascade,
    code_sha256              bytea       not null,
    compile_succeeded        boolean     not null,
    exit_code                integer,
    timed_out                boolean     not null,
    output_truncated         boolean     not null,
    standard_output          text        not null,
    standard_error           text        not null,
    duration_milliseconds    integer     not null,
    ran_at                   timestamptz not null default now()
);
create index code_runs_session on code_runs (student_work_session_id);
