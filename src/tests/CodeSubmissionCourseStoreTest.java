package tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import models.CodeSubmissionCourseStore;
import models.CodeSubmissionCourseStore.CodeSubmissionAssignment;
import models.CodeSubmissionCourseStore.Course;
import models.Roster;
import models.Student;

/**
 * Checks ILEP's local record of Code Submission pseudonyms. Uses a temporary folder and synthetic
 * students only. Run as a Java application.
 */
public class CodeSubmissionCourseStoreTest {

	public static void main(String[] args) throws Exception {
		Path temporaryFolder = Files.createTempDirectory("code-submission-store-test");
		Path coursesFile = temporaryFolder.resolve("code-submission-courses.properties");

		Roster sectionA = new Roster("00001-Fall 2026-CSCI1301A.csv");
		Roster sectionB = new Roster("00002-Fall 2026-CSCI1301B.csv");
		Student studentOne = syntheticStudent("Student", "One", "900000001");
		Student studentTwo = syntheticStudent("Student", "Two", "900000002");

		CodeSubmissionCourseStore store = CodeSubmissionCourseStore.load(coursesFile);
		Course course = store.courseFor(List.of(sectionA, sectionB));
		String studentOneReference = course.studentReferenceFor(studentOne);
		String studentTwoReference = course.studentReferenceFor(studentTwo);

		check(course.getCourseReference().matches("^course_[0-9a-f]{24}$"), "course reference is random and opaque");
		check(course.getCourseSecretHex().matches("^[0-9a-f]{64}$"), "course secret is 32 random bytes");
		check(!course.getCourseReference().contains("00001"), "course reference does not contain the CRN");
		check(studentOneReference.matches("^student_[0-9a-f]{24}$"), "student reference is random and opaque");
		check(!studentOneReference.equals(studentTwoReference), "each student gets a different reference");
		check(course.studentReferenceFor(studentOne).equals(studentOneReference), "asking again returns the same reference");
		check(course.getLabel().equals("CSCI1301A, CSCI1301B Fall 2026"), "label names sections and term: " + course.getLabel());
		check(!course.getLabel().contains("00001"), "label does not contain the CRN");
		check(store.courseFor(List.of(sectionB, sectionA)) == course, "roster order does not matter");
		check(store.courseFor(List.of(sectionA)) != course, "a different set of rosters is a different course");

		course.setCurrentAssignment(new CodeSubmissionAssignment("assignment-1", "ABCD-2345",
				"http://localhost:8080/?joinCode=ABCD-2345", "Lab 4"));
		store.save();

		// Reload from disk: everything must come back the same.
		CodeSubmissionCourseStore reloadedStore = CodeSubmissionCourseStore.load(coursesFile);
		Course reloadedCourse = reloadedStore.courseFor(List.of(sectionA, sectionB));
		check(reloadedCourse.getCourseReference().equals(course.getCourseReference()), "course reference survives a reload");
		check(reloadedCourse.getCourseSecretHex().equals(course.getCourseSecretHex()), "course secret survives a reload");
		check(reloadedCourse.studentReferenceFor(studentOne).equals(studentOneReference), "student reference survives a reload");
		check(reloadedCourse.getCurrentAssignment().map(CodeSubmissionAssignment::joinCode).orElse("").equals("ABCD-2345"),
				"current assignment survives a reload");
		check(reloadedCourse.studentFor(studentTwoReference, List.of(studentOne, studentTwo)).orElse(null) == studentTwo,
				"a reference maps back to the right student");
		check(reloadedCourse.studentFor("student_unknown", List.of(studentOne, studentTwo)).isEmpty(),
				"an unknown reference maps to no student");

		String savedText = Files.readString(coursesFile);
		check(savedText.contains("CLASS I CONFIDENTIAL"), "file is labeled Class I Confidential");
		check(!savedText.contains("One") && !savedText.contains("Two"), "file stores no student names");

		reloadedCourse.clearCurrentAssignment();
		reloadedStore.save();
		check(CodeSubmissionCourseStore.load(coursesFile).courseFor(List.of(sectionA, sectionB)).getCurrentAssignment().isEmpty(),
				"clearing the assignment is saved");

		try (var files = Files.list(temporaryFolder)) {
			files.forEach(file -> file.toFile().delete());
		}
		Files.delete(temporaryFolder);
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
