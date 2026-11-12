package ilep.codesubmission.studentsession;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ilep.codesubmission.configuration.CodeSubmissionProperties;
import ilep.codesubmission.error.ApiException;

/**
 * A student's work on one assignment: reading it, acknowledging the process-tracking notice,
 * autosaving and submitting.
 */
@Service
public class StudentWorkSessionService {

	/**
	 * Version of the in-app notice in docs/code-submission/syllabus-statement.md, Part 2.
	 * Increase it whenever the notice text changes, so students acknowledge the new text.
	 */
	public static final int currentNoticeVersion = 1;

	private final JdbcClient jdbcClient;
	private final CodeSubmissionProperties properties;

	public StudentWorkSessionService(JdbcClient jdbcClient, CodeSubmissionProperties properties) {
		this.jdbcClient = jdbcClient;
		this.properties = properties;
	}

	public record StudentWorkSessionView(
			String title,
			String instructions,
			String assignmentMode,
			String artificialIntelligenceUse,
			String artificialIntelligenceNote,
			OffsetDateTime dueAt,
			String currentCode,
			String status,
			OffsetDateTime lastSavedAt,
			OffsetDateTime submittedAt,
			boolean assignmentClosed,
			boolean noticeAcknowledged,
			int currentNoticeVersion) {
	}

	public StudentWorkSessionView viewWorkSession(UUID studentWorkSessionId) {
		return jdbcClient.sql("""
				select assignments.title, assignments.instructions, assignments.assignment_mode,
				       assignments.artificial_intelligence_use, assignments.artificial_intelligence_note, assignments.due_at,
				       sessions.current_code, sessions.last_saved_at, sessions.submitted_at,
				       assignments.closed_at is not null as assignment_closed,
				       exists (select 1 from notice_acknowledgements acknowledgements
				               where acknowledgements.student_work_session_id = sessions.student_work_session_id
				                 and acknowledgements.notice_version = :currentNoticeVersion) as notice_acknowledged
				from student_work_sessions sessions
				join assignments on assignments.assignment_id = sessions.assignment_id
				where sessions.student_work_session_id = :studentWorkSessionId
				""")
				.param("currentNoticeVersion", currentNoticeVersion)
				.param("studentWorkSessionId", studentWorkSessionId)
				.query((resultSet, rowNumber) -> {
					OffsetDateTime submittedAt = resultSet.getObject("submitted_at", OffsetDateTime.class);
					return new StudentWorkSessionView(
							resultSet.getString("title"),
							resultSet.getString("instructions"),
							resultSet.getString("assignment_mode"),
							resultSet.getString("artificial_intelligence_use"),
							resultSet.getString("artificial_intelligence_note"),
							resultSet.getObject("due_at", OffsetDateTime.class),
							resultSet.getString("current_code"),
							submittedAt != null ? "submitted" : "inProgress",
							resultSet.getObject("last_saved_at", OffsetDateTime.class),
							submittedAt,
							resultSet.getBoolean("assignment_closed"),
							resultSet.getBoolean("notice_acknowledged"),
							currentNoticeVersion);
				})
				.optional()
				.orElseThrow(() -> ApiException.notFound("Work session not found."));
	}

	public void acknowledgeNotice(UUID studentWorkSessionId, int noticeVersion) {
		if (noticeVersion != currentNoticeVersion) {
			throw ApiException.conflict("The notice has changed. Reload the page and read the current notice.");
		}
		jdbcClient.sql("""
				insert into notice_acknowledgements (student_work_session_id, notice_version)
				values (:studentWorkSessionId, :noticeVersion)
				on conflict (student_work_session_id, notice_version) do nothing
				""")
				.param("studentWorkSessionId", studentWorkSessionId)
				.param("noticeVersion", noticeVersion)
				.update();
	}

	@Transactional
	public OffsetDateTime saveCode(UUID studentWorkSessionId, String code) {
		ensureCodeLengthAllowed(code);
		ensureEditable(studentWorkSessionId);
		OffsetDateTime now = OffsetDateTime.now();
		jdbcClient.sql("""
				update student_work_sessions set current_code = :code, last_saved_at = :now
				where student_work_session_id = :studentWorkSessionId
				""")
				.param("code", code)
				.param("now", now)
				.param("studentWorkSessionId", studentWorkSessionId)
				.update();
		return now;
	}

	/** Saves the final code and locks the session. After this, saving and running return 409. */
	@Transactional
	public OffsetDateTime submit(UUID studentWorkSessionId, String finalCode) {
		ensureCodeLengthAllowed(finalCode);
		ensureEditable(studentWorkSessionId);
		OffsetDateTime now = OffsetDateTime.now();
		jdbcClient.sql("""
				update student_work_sessions set current_code = :code, last_saved_at = :now, submitted_at = :now
				where student_work_session_id = :studentWorkSessionId
				""")
				.param("code", finalCode)
				.param("now", now)
				.param("studentWorkSessionId", studentWorkSessionId)
				.update();
		return now;
	}

	/**
	 * Checks, with a row lock, that the notice is acknowledged, the session is not submitted and
	 * the assignment is not closed. Call inside a transaction before changing the session.
	 */
	public void ensureEditable(UUID studentWorkSessionId) {
		record EditableState(boolean submitted, boolean assignmentClosed, boolean noticeAcknowledged) {
		}
		EditableState editableState = jdbcClient.sql("""
				select sessions.submitted_at is not null as submitted,
				       assignments.closed_at is not null as assignment_closed,
				       exists (select 1 from notice_acknowledgements acknowledgements
				               where acknowledgements.student_work_session_id = sessions.student_work_session_id
				                 and acknowledgements.notice_version = :currentNoticeVersion) as notice_acknowledged
				from student_work_sessions sessions
				join assignments on assignments.assignment_id = sessions.assignment_id
				where sessions.student_work_session_id = :studentWorkSessionId
				for update of sessions
				""")
				.param("currentNoticeVersion", currentNoticeVersion)
				.param("studentWorkSessionId", studentWorkSessionId)
				.query(EditableState.class)
				.optional()
				.orElseThrow(() -> ApiException.notFound("Work session not found."));
		if (!editableState.noticeAcknowledged()) {
			throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN, "notice_not_acknowledged",
					"Read and acknowledge the notice before you start.");
		}
		if (editableState.submitted()) {
			throw ApiException.conflict("You already submitted this assignment.");
		}
		if (editableState.assignmentClosed()) {
			throw ApiException.conflict("This assignment is closed.");
		}
	}

	private void ensureCodeLengthAllowed(String code) {
		if (code.length() > properties.studentSession().maximumCodeLength()) {
			throw ApiException.validationFailed("Code is too long.");
		}
	}
}
