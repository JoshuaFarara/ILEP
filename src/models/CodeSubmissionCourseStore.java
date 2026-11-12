package models;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;

/**
 * ILEP's private record of each Code Submission course: the pseudonyms and secret that let the
 * service work without ever seeing a student's name, email, Eagle ID or CRN.
 *
 * For each course it keeps:
 * <ul>
 * <li>a random courseReference (sent to the service instead of the CRN)</li>
 * <li>a random 32-byte courseSecret (the key for each student's join hash)</li>
 * <li>Eagle ID → random studentReference, stable across roster syncs</li>
 * <li>the course's current assignment (id, join code, join link, title)</li>
 * </ul>
 *
 * Saved in {@code src/appData/code-submission-courses.properties}. This file links pseudonyms back
 * to real students, so it is Class I data (docs/code-submission/data-classification.md): it is
 * git-ignored, owner-only, and must never be committed, shared or given to an AI tool.
 *
 * A "course" is the set of rosters selected for a class session; selecting the same rosters again
 * (in any order) returns the same course.
 */
public class CodeSubmissionCourseStore {

	public static final Path DEFAULT_COURSES_FILE = Path.of("src", "appData", "code-submission-courses.properties");

	private static final SecureRandom secureRandom = new SecureRandom();

	private final Path coursesFile;
	private final Map<String, Course> coursesByKey = new HashMap<>();

	private CodeSubmissionCourseStore(Path coursesFile) {
		this.coursesFile = coursesFile;
	}

	/** An assignment created on the service. */
	public record CodeSubmissionAssignment(String assignmentId, String joinCode, String joinUrl, String title) {
	}

	/** One Code Submission course and its pseudonyms. */
	public static final class Course {
		private final String courseKey;
		private final String courseReference;
		private final String courseSecretHex;
		private String label;
		private final Map<String, String> studentReferencesByEagleIdentifier = new HashMap<>();
		private CodeSubmissionAssignment currentAssignment;

		private Course(String courseKey, String courseReference, String courseSecretHex, String label) {
			this.courseKey = courseKey;
			this.courseReference = courseReference;
			this.courseSecretHex = courseSecretHex;
			this.label = label;
		}

		public String getCourseReference() {
			return courseReference;
		}

		public String getCourseSecretHex() {
			return courseSecretHex;
		}

		public byte[] getCourseSecret() {
			return HexFormat.of().parseHex(courseSecretHex);
		}

		public String getLabel() {
			return label;
		}

		/** The student's pseudonym, created the first time it is asked for. */
		public String studentReferenceFor(Student student) {
			String eagleIdentifier = JoinHash.normalizeEagleIdentifier(student.getEagleID());
			return studentReferencesByEagleIdentifier.computeIfAbsent(eagleIdentifier,
					ignored -> "student_" + randomHex(12));
		}

		/** Finds which of these students a pseudonym belongs to, if any. */
		public Optional<Student> studentFor(String studentReference, List<Student> students) {
			for (Student student : students) {
				String eagleIdentifier = JoinHash.normalizeEagleIdentifier(student.getEagleID());
				if (studentReference.equals(studentReferencesByEagleIdentifier.get(eagleIdentifier))) {
					return Optional.of(student);
				}
			}
			return Optional.empty();
		}

		public Optional<CodeSubmissionAssignment> getCurrentAssignment() {
			return Optional.ofNullable(currentAssignment);
		}

		public void setCurrentAssignment(CodeSubmissionAssignment assignment) {
			this.currentAssignment = assignment;
		}

		public void clearCurrentAssignment() {
			this.currentAssignment = null;
		}
	}

	// ------------------------------------------------------------ loading and saving
	public static CodeSubmissionCourseStore load(Path coursesFile) throws IOException {
		CodeSubmissionCourseStore store = new CodeSubmissionCourseStore(coursesFile);
		if (!Files.exists(coursesFile)) {
			return store;
		}
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(coursesFile, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		int courseCount = Integer.parseInt(properties.getProperty("courseCount", "0"));
		for (int courseIndex = 0; courseIndex < courseCount; courseIndex++) {
			String prefix = "course." + courseIndex + ".";
			Course course = new Course(
					properties.getProperty(prefix + "key"),
					properties.getProperty(prefix + "reference"),
					properties.getProperty(prefix + "secret"),
					properties.getProperty(prefix + "label", ""));
			int studentCount = Integer.parseInt(properties.getProperty(prefix + "studentCount", "0"));
			for (int studentIndex = 0; studentIndex < studentCount; studentIndex++) {
				String studentPrefix = prefix + "student." + studentIndex + ".";
				course.studentReferencesByEagleIdentifier.put(
						properties.getProperty(studentPrefix + "eagleIdentifier"),
						properties.getProperty(studentPrefix + "reference"));
			}
			String assignmentId = properties.getProperty(prefix + "assignment.id");
			if (assignmentId != null) {
				course.currentAssignment = new CodeSubmissionAssignment(assignmentId,
						properties.getProperty(prefix + "assignment.joinCode", ""),
						properties.getProperty(prefix + "assignment.joinUrl", ""),
						properties.getProperty(prefix + "assignment.title", ""));
			}
			store.coursesByKey.put(course.courseKey, course);
		}
		return store;
	}

	public void save() throws IOException {
		Properties properties = new Properties();
		List<Course> courses = new ArrayList<>(coursesByKey.values());
		properties.setProperty("courseCount", Integer.toString(courses.size()));
		for (int courseIndex = 0; courseIndex < courses.size(); courseIndex++) {
			Course course = courses.get(courseIndex);
			String prefix = "course." + courseIndex + ".";
			properties.setProperty(prefix + "key", course.courseKey);
			properties.setProperty(prefix + "reference", course.courseReference);
			properties.setProperty(prefix + "secret", course.courseSecretHex);
			properties.setProperty(prefix + "label", course.label);
			List<Map.Entry<String, String>> students = new ArrayList<>(course.studentReferencesByEagleIdentifier.entrySet());
			properties.setProperty(prefix + "studentCount", Integer.toString(students.size()));
			for (int studentIndex = 0; studentIndex < students.size(); studentIndex++) {
				String studentPrefix = prefix + "student." + studentIndex + ".";
				properties.setProperty(studentPrefix + "eagleIdentifier", students.get(studentIndex).getKey());
				properties.setProperty(studentPrefix + "reference", students.get(studentIndex).getValue());
			}
			if (course.currentAssignment != null) {
				properties.setProperty(prefix + "assignment.id", course.currentAssignment.assignmentId());
				properties.setProperty(prefix + "assignment.joinCode", course.currentAssignment.joinCode());
				properties.setProperty(prefix + "assignment.joinUrl", course.currentAssignment.joinUrl());
				properties.setProperty(prefix + "assignment.title", course.currentAssignment.title());
			}
		}
		LocalDataFiles.createParentFolder(coursesFile);
		try (Writer writer = Files.newBufferedWriter(coursesFile, StandardCharsets.UTF_8)) {
			properties.store(writer,
					"ILEP Code Submission courses. CLASS I CONFIDENTIAL: links pseudonyms to students. Never commit or share.");
		}
		LocalDataFiles.restrictToOwner(coursesFile);
	}

	// ------------------------------------------------------------ courses
	/** The course for these rosters, created with a new reference and secret the first time. */
	public Course courseFor(List<Roster> rosters) {
		String courseKey = courseKeyFor(rosters);
		Course course = coursesByKey.computeIfAbsent(courseKey,
				ignored -> new Course(courseKey, "course_" + randomHex(12), randomHex(32), courseLabelFor(rosters)));
		course.label = courseLabelFor(rosters);
		return course;
	}

	/** Identifies a set of rosters regardless of order, e.g. "83129-Fall 2025-CSCI1302A". */
	public static String courseKeyFor(List<Roster> rosters) {
		TreeSet<String> rosterKeys = new TreeSet<>();
		for (Roster roster : rosters) {
			rosterKeys.add(nullToEmpty(roster.getCrn()) + "-" + nullToEmpty(roster.getSemesterTerm()) + " "
					+ nullToEmpty(roster.getYear()) + "-" + sectionName(roster));
		}
		return String.join("|", rosterKeys);
	}

	/**
	 * The label the service shows, e.g. "CSCI1302A, CSCI1302B Fall 2025". It names the course and
	 * term but never the CRN.
	 */
	public static String courseLabelFor(List<Roster> rosters) {
		TreeSet<String> sections = new TreeSet<>();
		TreeSet<String> terms = new TreeSet<>();
		for (Roster roster : rosters) {
			sections.add(sectionName(roster));
			String term = (nullToEmpty(roster.getSemesterTerm()) + " " + nullToEmpty(roster.getYear())).trim();
			if (!term.isEmpty()) {
				terms.add(term);
			}
		}
		return (String.join(", ", sections) + " " + String.join(", ", terms)).trim();
	}

	private static String sectionName(Roster roster) {
		return nullToEmpty(roster.getCourseName()) + nullToEmpty(roster.getCourseCode())
				+ nullToEmpty(roster.getCourseSection());
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value.trim();
	}

	private static String randomHex(int byteCount) {
		byte[] randomBytes = new byte[byteCount];
		secureRandom.nextBytes(randomBytes);
		return HexFormat.of().formatHex(randomBytes);
	}
}
