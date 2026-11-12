package ilep.codesubmission.assignment;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import ilep.codesubmission.assignment.AssignmentRequests.CreateAssignmentRequest;
import ilep.codesubmission.assignment.AssignmentRequests.CreateAssignmentResponse;
import ilep.codesubmission.assignment.AssignmentRequests.StudentSummary;
import ilep.codesubmission.configuration.CodeSubmissionProperties;
import ilep.codesubmission.error.ApiException;

/** Creating, closing and summarizing assignments. Every method checks that the instructor owns the course. */
@Service
public class AssignmentService {

	private static final int maximumJoinCodeAttempts = 5;

	private final JdbcClient jdbcClient;
	private final JoinCodeGenerator joinCodeGenerator;
	private final CodeSubmissionProperties properties;

	public AssignmentService(JdbcClient jdbcClient, JoinCodeGenerator joinCodeGenerator,
			CodeSubmissionProperties properties) {
		this.jdbcClient = jdbcClient;
		this.joinCodeGenerator = joinCodeGenerator;
		this.properties = properties;
	}

	// Not transactional on purpose: a join-code collision aborts a PostgreSQL transaction,
	// which would prevent the retry. Each insert is a single statement and atomic on its own.
	public CreateAssignmentResponse createAssignment(long instructorId, String courseReference,
			CreateAssignmentRequest request) {
		if (request.opensAt() != null && request.dueAt() != null && request.dueAt().isBefore(request.opensAt())) {
			throw ApiException.validationFailed("dueAt must be after opensAt.");
		}
		long courseId = jdbcClient.sql("""
				select course_id from courses where course_reference = :courseReference and instructor_id = :instructorId
				""")
				.param("courseReference", courseReference)
				.param("instructorId", instructorId)
				.query(Long.class)
				.optional()
				.orElseThrow(() -> ApiException.notFound("Course not found. Synchronize its roster first."));

		UUID assignmentId = UUID.randomUUID();
		for (int attempt = 1; attempt <= maximumJoinCodeAttempts; attempt++) {
			String joinCode = joinCodeGenerator.newJoinCode();
			try {
				jdbcClient.sql("""
						insert into assignments (assignment_id, course_id, title, instructions, starter_code,
						  assignment_mode, artificial_intelligence_use, artificial_intelligence_note, opens_at, due_at, join_code)
						values (:assignmentId, :courseId, :title, :instructions, :starterCode,
						  :assignmentMode, :artificialIntelligenceUse, :artificialIntelligenceNote, :opensAt, :dueAt, :joinCode)
						""")
						.param("assignmentId", assignmentId)
						.param("courseId", courseId)
						.param("title", request.title())
						.param("instructions", Objects.requireNonNullElse(request.instructions(), ""))
						.param("starterCode", Objects.requireNonNullElse(request.starterCode(), ""))
						.param("assignmentMode", Objects.requireNonNullElse(request.assignmentMode(), "assignment"))
						.param("artificialIntelligenceUse", request.settings().artificialIntelligenceUse())
						.param("artificialIntelligenceNote",
								Objects.requireNonNullElse(request.settings().artificialIntelligenceNote(), ""))
						.param("opensAt", request.opensAt())
						.param("dueAt", request.dueAt())
						.param("joinCode", joinCode)
						.update();
				return new CreateAssignmentResponse(assignmentId, joinCode, joinUrlFor(joinCode));
			} catch (DuplicateKeyException joinCodeCollision) {
				// Extremely rare: generate another code.
			}
		}
		throw new IllegalStateException("Could not generate a unique join code");
	}

	public void closeAssignment(long instructorId, UUID assignmentId) {
		int updatedRows = jdbcClient.sql("""
				update assignments set closed_at = coalesce(closed_at, :now)
				where assignment_id = :assignmentId
				  and course_id in (select course_id from courses where instructor_id = :instructorId)
				""")
				.param("now", OffsetDateTime.now())
				.param("assignmentId", assignmentId)
				.param("instructorId", instructorId)
				.update();
		if (updatedRows == 0) {
			throw ApiException.notFound("Assignment not found.");
		}
	}

	/** One row per active enrolled student, including students who have not started. */
	public List<StudentSummary> summarizeAssignment(long instructorId, UUID assignmentId) {
		Long courseId = jdbcClient.sql("""
				select assignments.course_id from assignments
				join courses on courses.course_id = assignments.course_id
				where assignments.assignment_id = :assignmentId and courses.instructor_id = :instructorId
				""")
				.param("assignmentId", assignmentId)
				.param("instructorId", instructorId)
				.query(Long.class)
				.optional()
				.orElseThrow(() -> ApiException.notFound("Assignment not found."));

		return jdbcClient.sql("""
				select enrollments.student_reference,
				       sessions.started_at, sessions.last_saved_at, sessions.submitted_at,
				       (select count(*) from code_runs where code_runs.student_work_session_id = sessions.student_work_session_id) as run_count,
				       sessions.student_work_session_id is not null as has_session
				from enrollments
				left join student_work_sessions sessions
				  on sessions.enrollment_id = enrollments.enrollment_id and sessions.assignment_id = :assignmentId
				where enrollments.course_id = :courseId and enrollments.active
				order by enrollments.student_reference
				""")
				.param("assignmentId", assignmentId)
				.param("courseId", courseId)
				.query((resultSet, rowNumber) -> {
					OffsetDateTime submittedAt = resultSet.getObject("submitted_at", OffsetDateTime.class);
					String status = submittedAt != null ? "submitted"
							: resultSet.getBoolean("has_session") ? "inProgress" : "notStarted";
					return new StudentSummary(
							resultSet.getString("student_reference"),
							status,
							resultSet.getObject("started_at", OffsetDateTime.class),
							resultSet.getObject("last_saved_at", OffsetDateTime.class),
							submittedAt,
							resultSet.getInt("run_count"));
				})
				.list();
	}

	private String joinUrlFor(String joinCode) {
		String publicBaseUrl = properties.publicBaseUrl().replaceAll("/+$", "");
		return publicBaseUrl + "/?joinCode=" + URLEncoder.encode(joinCode, StandardCharsets.UTF_8);
	}
}
