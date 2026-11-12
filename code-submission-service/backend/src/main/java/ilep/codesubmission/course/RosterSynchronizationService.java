package ilep.codesubmission.course;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ilep.codesubmission.error.ApiException;
import ilep.codesubmission.security.CourseSecretEncryptor;

/**
 * Replaces a course's roster with the one ILEP sends. Idempotent: sending the same roster twice
 * changes nothing. Students left out are deactivated (not deleted), so their past work is kept
 * for the retention period but they can no longer join.
 */
@Service
public class RosterSynchronizationService {

	private final JdbcClient jdbcClient;
	private final CourseSecretEncryptor courseSecretEncryptor;

	public RosterSynchronizationService(JdbcClient jdbcClient, CourseSecretEncryptor courseSecretEncryptor) {
		this.jdbcClient = jdbcClient;
		this.courseSecretEncryptor = courseSecretEncryptor;
	}

	public record RosterSynchronizationResult(String courseReference, int studentCount) {
	}

	@Transactional
	public RosterSynchronizationResult synchronizeRoster(long instructorId, String courseReference,
			RosterSynchronizationRequest request) {
		ensureUniqueStudentReferences(request.students());
		byte[] encryptedCourseSecret = courseSecretEncryptor.encrypt(HexFormat.of().parseHex(request.courseSecret()));
		OffsetDateTime now = OffsetDateTime.now();

		Long courseId = jdbcClient.sql("""
				insert into courses (instructor_id, course_reference, label, encrypted_course_secret)
				values (:instructorId, :courseReference, :label, :encryptedCourseSecret)
				on conflict (course_reference) do update
				  set label = excluded.label,
				      encrypted_course_secret = excluded.encrypted_course_secret,
				      updated_at = :now
				  where courses.instructor_id = excluded.instructor_id
				returning course_id
				""")
				.param("instructorId", instructorId)
				.param("courseReference", courseReference)
				.param("label", request.label())
				.param("encryptedCourseSecret", encryptedCourseSecret)
				.param("now", now)
				.query(Long.class)
				.optional()
				// The conflict update is skipped when another instructor owns this reference.
				.orElseThrow(() -> ApiException.notFound("Course not found."));

		jdbcClient.sql("update enrollments set active = false, updated_at = :now where course_id = :courseId")
				.param("now", now)
				.param("courseId", courseId)
				.update();

		for (RosterSynchronizationRequest.RosterStudent student : request.students()) {
			jdbcClient.sql("""
					insert into enrollments (course_id, student_reference, join_hash, active)
					values (:courseId, :studentReference, :joinHash, true)
					on conflict (course_id, student_reference) do update
					  set join_hash = excluded.join_hash, active = true, updated_at = :now
					""")
					.param("courseId", courseId)
					.param("studentReference", student.studentReference())
					.param("joinHash", HexFormat.of().parseHex(student.joinHash()))
					.param("now", now)
					.update();
		}
		return new RosterSynchronizationResult(courseReference, request.students().size());
	}

	private static void ensureUniqueStudentReferences(List<RosterSynchronizationRequest.RosterStudent> students) {
		Set<String> seenStudentReferences = new HashSet<>();
		Set<String> seenJoinHashes = new HashSet<>();
		for (RosterSynchronizationRequest.RosterStudent student : students) {
			if (!seenStudentReferences.add(student.studentReference())) {
				throw ApiException.validationFailed("Each studentReference must appear only once.");
			}
			if (!seenJoinHashes.add(student.joinHash().toLowerCase())) {
				throw ApiException.validationFailed("Each joinHash must appear only once.");
			}
		}
	}
}
