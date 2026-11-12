package ilep.codesubmission.join;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ilep.codesubmission.assignment.JoinCodeGenerator;
import ilep.codesubmission.configuration.CodeSubmissionProperties;
import ilep.codesubmission.error.ApiException;
import ilep.codesubmission.security.BearerTokenAuthenticationFilter;
import ilep.codesubmission.security.CourseSecretEncryptor;
import ilep.codesubmission.security.SecretHashing;

/**
 * Lets a student join an assignment with its join code and their Eagle ID.
 *
 * The Eagle ID is used only to compute a join hash inside this method. It is never stored, logged
 * or returned (DSCS Class I; data-classification.md, "Eagle ID").
 */
@Service
public class StudentJoinService {

	private static final String invalidJoinMessage = "That join code and ID don't match an open assignment you're enrolled in.";

	private final JdbcClient jdbcClient;
	private final CourseSecretEncryptor courseSecretEncryptor;
	private final JoinAttemptLimiter joinAttemptLimiter;
	private final CodeSubmissionProperties properties;

	public StudentJoinService(JdbcClient jdbcClient, CourseSecretEncryptor courseSecretEncryptor,
			JoinAttemptLimiter joinAttemptLimiter, CodeSubmissionProperties properties) {
		this.jdbcClient = jdbcClient;
		this.courseSecretEncryptor = courseSecretEncryptor;
		this.joinAttemptLimiter = joinAttemptLimiter;
		this.properties = properties;
	}

	public record JoinResult(String studentToken, OffsetDateTime expiresAt) {
	}

	private record OpenAssignment(UUID assignmentId, long courseId, byte[] encryptedCourseSecret, String starterCode,
			OffsetDateTime opensAt, OffsetDateTime closedAt) {
	}

	@Transactional
	public JoinResult join(String typedJoinCode, String eagleIdentifier, String clientAddress) {
		String joinCode = JoinCodeGenerator.normalizeJoinCode(typedJoinCode);
		if (joinAttemptLimiter.isBlocked(joinCode, clientAddress)) {
			throw ApiException.rateLimited("Too many attempts. Wait a few minutes and try again.");
		}

		Optional<OpenAssignment> assignment = jdbcClient.sql("""
				select assignments.assignment_id, assignments.course_id, courses.encrypted_course_secret,
				       assignments.starter_code, assignments.opens_at, assignments.closed_at
				from assignments join courses on courses.course_id = assignments.course_id
				where assignments.join_code = :joinCode
				""")
				.param("joinCode", joinCode)
				.query(OpenAssignment.class)
				.optional();
		OffsetDateTime now = OffsetDateTime.now();
		if (assignment.isEmpty()) {
			joinAttemptLimiter.recordFailure(joinCode, clientAddress);
			throw ApiException.unauthorized(invalidJoinMessage);
		}
		OpenAssignment openAssignment = assignment.get();
		if (openAssignment.closedAt() != null) {
			throw ApiException.conflict("This assignment is closed.");
		}
		if (openAssignment.opensAt() != null && now.isBefore(openAssignment.opensAt())) {
			throw ApiException.conflict("This assignment is not open yet.");
		}

		byte[] courseSecret = courseSecretEncryptor.decrypt(openAssignment.encryptedCourseSecret());
		byte[] joinHash = JoinHashCalculator.calculateJoinHash(courseSecret, eagleIdentifier);
		Optional<Long> enrollmentId = jdbcClient.sql("""
				select enrollment_id from enrollments
				where course_id = :courseId and join_hash = :joinHash and active
				""")
				.param("courseId", openAssignment.courseId())
				.param("joinHash", joinHash)
				.query(Long.class)
				.optional();
		if (enrollmentId.isEmpty()) {
			joinAttemptLimiter.recordFailure(joinCode, clientAddress);
			throw ApiException.unauthorized(invalidJoinMessage);
		}

		UUID studentWorkSessionId = findOrCreateWorkSession(openAssignment, enrollmentId.get());
		return issueStudentToken(studentWorkSessionId, now);
	}

	private UUID findOrCreateWorkSession(OpenAssignment openAssignment, long enrollmentId) {
		jdbcClient.sql("""
				insert into student_work_sessions (student_work_session_id, assignment_id, enrollment_id, current_code)
				values (:studentWorkSessionId, :assignmentId, :enrollmentId, :starterCode)
				on conflict (assignment_id, enrollment_id) do nothing
				""")
				.param("studentWorkSessionId", UUID.randomUUID())
				.param("assignmentId", openAssignment.assignmentId())
				.param("enrollmentId", enrollmentId)
				.param("starterCode", openAssignment.starterCode())
				.update();
		return jdbcClient.sql("""
				select student_work_session_id from student_work_sessions
				where assignment_id = :assignmentId and enrollment_id = :enrollmentId
				""")
				.param("assignmentId", openAssignment.assignmentId())
				.param("enrollmentId", enrollmentId)
				.query(UUID.class)
				.single();
	}

	private JoinResult issueStudentToken(UUID studentWorkSessionId, OffsetDateTime now) {
		String studentToken = SecretHashing.newRandomToken(BearerTokenAuthenticationFilter.studentTokenPrefix);
		OffsetDateTime expiresAt = now.plus(properties.studentSession().tokenLifetime());
		jdbcClient.sql("delete from student_session_tokens where expires_at < :now")
				.param("now", now)
				.update();
		jdbcClient.sql("""
				insert into student_session_tokens (token_hash, student_work_session_id, expires_at)
				values (:tokenHash, :studentWorkSessionId, :expiresAt)
				""")
				.param("tokenHash", SecretHashing.sha256(studentToken))
				.param("studentWorkSessionId", studentWorkSessionId)
				.param("expiresAt", expiresAt)
				.update();
		return new JoinResult(studentToken, expiresAt);
	}
}
