package tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import models.CodeSubmissionClient;
import models.CodeSubmissionClient.CodeSubmissionException;
import models.CodeSubmissionClient.NewAssignment;
import models.CodeSubmissionClient.StudentSubmissionStatus;
import models.CodeSubmissionCourseStore;
import models.CodeSubmissionCourseStore.CodeSubmissionAssignment;
import models.CodeSubmissionCourseStore.Course;
import models.CodeSubmissionSettings;
import models.Roster;
import models.Student;

/**
 * Talks to a running Code Submission service with a SYNTHETIC roster.
 *
 * Start the service first (code-submission-service/README.md), then run with:
 *   CODE_SUBMISSION_URL=http://localhost:8080 INSTRUCTOR_API_KEY=... (Run As > Java Application)
 * If the service isn't reachable, the test says so and stops without failing.
 */
public class CodeSubmissionClientTest {

	public static void main(String[] args) throws Exception {
		String serviceUrl = System.getenv().getOrDefault("CODE_SUBMISSION_URL", "http://localhost:8080");
		String instructorApiKey = System.getenv("INSTRUCTOR_API_KEY");
		if (instructorApiKey == null || instructorApiKey.isBlank()) {
			System.out.println("SKIP Set INSTRUCTOR_API_KEY (and CODE_SUBMISSION_URL) to run against a service.");
			return;
		}
		Path temporaryFolder = Files.createTempDirectory("code-submission-client-test");
		CodeSubmissionSettings settings = new CodeSubmissionSettings(temporaryFolder.resolve("settings.properties"));
		settings.setServiceUrl(serviceUrl + "/");
		settings.setInstructorApiKey(instructorApiKey);
		check(settings.getServiceUrl().equals(serviceUrl), "settings trim a trailing slash from the service address");
		CodeSubmissionClient client = new CodeSubmissionClient(settings);

		Roster roster = new Roster("00001-Fall 2026-CSCI1301Z.csv");
		List<Student> students = List.of(
				syntheticStudent("Student", "One", "900000001"),
				syntheticStudent("Student", "Two", "900000002"),
				syntheticStudent("Student", "Three", "900000003"),
				syntheticStudent("No", "Identifier", " "));
		CodeSubmissionCourseStore store = CodeSubmissionCourseStore.load(temporaryFolder.resolve("courses.properties"));
		Course course = store.courseFor(List.of(roster));

		CodeSubmissionClient.RosterSynchronizationResult syncResult;
		try {
			syncResult = client.synchronizeRoster(course, students);
		} catch (CodeSubmissionException exception) {
			if (exception.getHttpStatus() == 0) {
				System.out.println("SKIP " + exception.getMessage() + " (" + serviceUrl + ")");
				return;
			}
			throw exception;
		}
		check(syncResult.studentCount() == 3 && syncResult.skippedStudentCount() == 1,
				"roster sync sends 3 students and skips the one without an Eagle ID");

		CodeSubmissionAssignment assignment = client.createAssignment(course, new NewAssignment("Lab 4: Loops",
				"Print 1 to n.", "public class Main {\n}\n", "restricted", "Explain errors only.", null));
		check(assignment.joinCode().matches("^[A-Z2-9]{4}-[A-Z2-9]{4}$"), "create assignment returns a join code: " + assignment.joinCode());
		check(assignment.joinUrl().endsWith(assignment.joinCode()), "join link contains the join code");

		List<StudentSubmissionStatus> summary = client.summarizeAssignment(assignment.assignmentId());
		check(summary.size() == 3, "summary lists the 3 synced students");
		check(summary.stream().allMatch(row -> row.status().equals("notStarted") && row.runCount() == 0),
				"everyone starts as not started");
		check(summary.stream().allMatch(row -> course.studentFor(row.studentReference(), students).isPresent()),
				"every summary row maps back to a local student");

		client.closeAssignment(assignment.assignmentId());
		check(true, "close assignment succeeds");

		check(failsWith(() -> client.createAssignment(course, new NewAssignment("Bad", "", "", "sometimes", "", null)),
				"validation_failed"), "an invalid AI rule is reported as validation_failed");
		check(failsWith(() -> client.summarizeAssignment("00000000-0000-0000-0000-000000000000"), "not_found"),
				"an unknown assignment is reported as not_found");

		CodeSubmissionSettings wrongKeySettings = new CodeSubmissionSettings(temporaryFolder.resolve("other.properties"));
		wrongKeySettings.setServiceUrl(serviceUrl);
		wrongKeySettings.setInstructorApiKey("not-the-right-key");
		check(failsWith(() -> new CodeSubmissionClient(wrongKeySettings).summarizeAssignment(assignment.assignmentId()),
				"unauthorized"), "a wrong API key is reported as unauthorized");

		CodeSubmissionSettings unreachableSettings = new CodeSubmissionSettings(temporaryFolder.resolve("unreachable.properties"));
		unreachableSettings.setServiceUrl("http://127.0.0.1:9");
		unreachableSettings.setInstructorApiKey("any");
		check(failsWith(() -> new CodeSubmissionClient(unreachableSettings).closeAssignment(assignment.assignmentId()),
				"unreachable"), "an unreachable service is reported clearly");
	}

	private interface ServiceCall {
		void call() throws CodeSubmissionException;
	}

	private static boolean failsWith(ServiceCall serviceCall, String expectedErrorCode) {
		try {
			serviceCall.call();
			return false;
		} catch (CodeSubmissionException exception) {
			return exception.getErrorCode().equals(expectedErrorCode);
		}
	}

	private static Student syntheticStudent(String firstName, String lastName, String eagleIdentifier) {
		Student student = new Student();
		student.setFirstName(firstName);
		student.setLastName(lastName);
		student.setEagleID(eagleIdentifier);
		return student;
	}

	private static void check(boolean ok, String message) {
		System.out.println((ok ? "PASS " : "FAIL ") + message);
	}
}
