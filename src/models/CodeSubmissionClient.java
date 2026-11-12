package models;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.CodeSubmissionCourseStore.CodeSubmissionAssignment;
import models.CodeSubmissionCourseStore.Course;

/**
 * Talks to the Code Submission service's instructor API.
 *
 * Every call blocks, so call it from a background thread (CodeSubmissionController does), never on
 * the JavaFX application thread.
 *
 * Data rule: only pseudonyms (courseReference, studentReference), join hashes, the course label and
 * the course secret are sent. Names, emails, Eagle IDs and CRNs never leave this computer.
 */
public class CodeSubmissionClient {

	private static final Duration requestTimeout = Duration.ofSeconds(10);

	private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(requestTimeout).build();
	private final String serviceUrl;
	private final String instructorApiKey;

	public CodeSubmissionClient(CodeSubmissionSettings settings) {
		this.serviceUrl = settings.getServiceUrl();
		this.instructorApiKey = settings.getInstructorApiKey();
	}

	/** The service refused a request or could not be reached. The message is safe to show the instructor. */
	public static class CodeSubmissionException extends Exception {
		private final int httpStatus;
		private final String errorCode;

		public CodeSubmissionException(int httpStatus, String errorCode, String message) {
			super(message);
			this.httpStatus = httpStatus;
			this.errorCode = errorCode;
		}

		/** 0 when the service could not be reached at all. */
		public int getHttpStatus() {
			return httpStatus;
		}

		public String getErrorCode() {
			return errorCode;
		}
	}

	/** Settings for a new assignment. artificialIntelligenceUse is forbidden, restricted or permitted. */
	public record NewAssignment(String title, String instructions, String starterCode, String artificialIntelligenceUse,
			String artificialIntelligenceNote, OffsetDateTime dueAt) {
	}

	/** One student's progress, as the service reports it (by pseudonym only). */
	public record StudentSubmissionStatus(String studentReference, String status, OffsetDateTime startedAt,
			OffsetDateTime lastSavedAt, OffsetDateTime submittedAt, int runCount) {
	}

	/** Result of a roster sync: how many students were sent, and how many were skipped for having no Eagle ID. */
	public record RosterSynchronizationResult(int studentCount, int skippedStudentCount) {
	}

	// ------------------------------------------------------------ instructor API
	/**
	 * Sends the course's pseudonymous roster. Students left out lose access on the service. Creates
	 * pseudonyms in {@code course} for new students; save the course store afterwards.
	 */
	public RosterSynchronizationResult synchronizeRoster(Course course, List<Student> students)
			throws CodeSubmissionException {
		List<Object> rosterStudents = new ArrayList<>();
		int skippedStudentCount = 0;
		Set<String> includedEagleIdentifiers = new HashSet<>();
		for (Student student : students) {
			if (!JoinHash.hasUsableEagleIdentifier(student)) {
				skippedStudentCount++;
				continue;
			}
			if (!includedEagleIdentifiers.add(JoinHash.normalizeEagleIdentifier(student.getEagleID()))) {
				continue; // the same student on two selected rosters
			}
			Map<String, Object> rosterStudent = new LinkedHashMap<>();
			rosterStudent.put("studentReference", course.studentReferenceFor(student));
			rosterStudent.put("joinHash", JoinHash.calculateHex(course.getCourseSecret(), student.getEagleID()));
			rosterStudents.add(rosterStudent);
		}
		Map<String, Object> requestBody = new LinkedHashMap<>();
		requestBody.put("label", course.getLabel());
		requestBody.put("courseSecret", course.getCourseSecretHex());
		requestBody.put("students", rosterStudents);

		send("PUT", "/api/v1/courses/" + encode(course.getCourseReference()) + "/roster", requestBody);
		return new RosterSynchronizationResult(rosterStudents.size(), skippedStudentCount);
	}

	public CodeSubmissionAssignment createAssignment(Course course, NewAssignment newAssignment)
			throws CodeSubmissionException {
		Map<String, Object> settings = new LinkedHashMap<>();
		settings.put("artificialIntelligenceUse", newAssignment.artificialIntelligenceUse());
		settings.put("artificialIntelligenceNote", newAssignment.artificialIntelligenceNote());
		Map<String, Object> requestBody = new LinkedHashMap<>();
		requestBody.put("title", newAssignment.title());
		requestBody.put("instructions", newAssignment.instructions());
		requestBody.put("starterCode", newAssignment.starterCode());
		if (newAssignment.dueAt() != null) {
			requestBody.put("dueAt", newAssignment.dueAt().toString());
		}
		requestBody.put("settings", settings);

		Map<String, Object> response = JsonText.readObject(
				send("POST", "/api/v1/courses/" + encode(course.getCourseReference()) + "/assignments", requestBody));
		return new CodeSubmissionAssignment(
				(String) response.get("assignmentId"),
				(String) response.get("joinCode"),
				(String) response.get("joinUrl"),
				newAssignment.title());
	}

	public void closeAssignment(String assignmentId) throws CodeSubmissionException {
		send("POST", "/api/v1/assignments/" + encode(assignmentId) + "/close", null);
	}

	public List<StudentSubmissionStatus> summarizeAssignment(String assignmentId) throws CodeSubmissionException {
		List<StudentSubmissionStatus> statuses = new ArrayList<>();
		for (Object row : JsonText.readArray(send("GET", "/api/v1/assignments/" + encode(assignmentId) + "/summary", null))) {
			@SuppressWarnings("unchecked")
			Map<String, Object> summaryRow = (Map<String, Object>) row;
			statuses.add(new StudentSubmissionStatus(
					(String) summaryRow.get("studentReference"),
					(String) summaryRow.get("status"),
					parseTime(summaryRow.get("startedAt")),
					parseTime(summaryRow.get("lastSavedAt")),
					parseTime(summaryRow.get("submittedAt")),
					summaryRow.get("runCount") instanceof Number runCount ? runCount.intValue() : 0));
		}
		return statuses;
	}

	// ------------------------------------------------------------ HTTP
	private String send(String method, String path, Object requestBody) throws CodeSubmissionException {
		if (serviceUrl.isBlank() || instructorApiKey.isBlank()) {
			throw new CodeSubmissionException(0, "not_configured", "Enter the service address and API key first.");
		}
		URI uri;
		try {
			uri = URI.create(serviceUrl + path);
		} catch (IllegalArgumentException exception) {
			throw new CodeSubmissionException(0, "invalid_service_url", "The service address is not a valid URL.");
		}
		HttpRequest.Builder request = HttpRequest.newBuilder(uri)
				.timeout(requestTimeout)
				.header("Authorization", "Bearer " + instructorApiKey)
				.header("Accept", "application/json");
		if (requestBody == null) {
			request.method(method, HttpRequest.BodyPublishers.noBody());
		} else {
			request.header("Content-Type", "application/json")
					.method(method, HttpRequest.BodyPublishers.ofString(JsonText.write(requestBody), StandardCharsets.UTF_8));
		}

		HttpResponse<String> response;
		try {
			response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new CodeSubmissionException(0, "unreachable",
					"Can't reach the Code Submission service. Check the address and your connection.");
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new CodeSubmissionException(0, "interrupted", "The request was cancelled.");
		}

		if (response.statusCode() >= 200 && response.statusCode() < 300) {
			return response.body();
		}
		throw errorFrom(response);
	}

	private static CodeSubmissionException errorFrom(HttpResponse<String> response) {
		String errorCode = "http_" + response.statusCode();
		String message = "The service returned an error (" + response.statusCode() + ").";
		try {
			Map<String, Object> errorBody = JsonText.readObject(response.body());
			if (errorBody.get("error") instanceof String error) {
				errorCode = error;
			}
			if (errorBody.get("message") instanceof String serverMessage && !serverMessage.isBlank()) {
				message = serverMessage;
			}
		} catch (RuntimeException notJson) {
			// Keep the generic message.
		}
		if (response.statusCode() == 401) {
			message = "The service did not accept the API key. Check it in Code Submission settings.";
		}
		return new CodeSubmissionException(response.statusCode(), errorCode, message);
	}

	private static OffsetDateTime parseTime(Object value) {
		return value instanceof String text && !text.isBlank() ? OffsetDateTime.parse(text) : null;
	}

	private static String encode(String pathSegment) {
		return URLEncoder.encode(pathSegment, StandardCharsets.UTF_8).replace("+", "%20");
	}
}
