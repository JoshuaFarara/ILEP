package ilep.codesubmission.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class InstructorApiIntegrationTest extends IntegrationTestBase {

	@Test
	void rejectsMissingOrWrongApiKey() throws Exception {
		String courseReference = uniqueCourseReference();
		String rosterJson = rosterJson("Synthetic", syntheticRoster(courseReference));

		mockMvc.perform(put("/api/v1/courses/{courseReference}/roster", courseReference)
				.contentType(MediaType.APPLICATION_JSON).content(rosterJson))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("unauthorized"));

		mockMvc.perform(put("/api/v1/courses/{courseReference}/roster", courseReference)
				.header("Authorization", "Bearer not-the-right-key")
				.contentType(MediaType.APPLICATION_JSON).content(rosterJson))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void studentTokenCannotUseInstructorApi() throws Exception {
		String courseReference = uniqueCourseReference();
		synchronizeRoster(courseReference, syntheticRoster(courseReference));
		CreatedAssignment assignment = createAssignment(courseReference);
		String studentToken = joinAndGetStudentToken(assignment.joinCode(), "900000001");

		mockMvc.perform(get("/api/v1/assignments/{assignmentId}/summary", assignment.assignmentId())
				.header("Authorization", "Bearer " + studentToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void rosterSynchronizationIsIdempotentAndDeactivatesRemovedStudents() throws Exception {
		String courseReference = uniqueCourseReference();
		Map<String, String> roster = syntheticRoster(courseReference);

		synchronizeRoster(courseReference, roster);
		synchronizeRoster(courseReference, roster);
		CreatedAssignment assignment = createAssignment(courseReference);

		mockMvc.perform(get("/api/v1/assignments/{assignmentId}/summary", assignment.assignmentId())
				.header("Authorization", "Bearer " + instructorApiKey))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(5));

		// Student 5 leaves the course.
		roster.remove(courseReference + "-student-5");
		synchronizeRoster(courseReference, roster);

		mockMvc.perform(get("/api/v1/assignments/{assignmentId}/summary", assignment.assignmentId())
				.header("Authorization", "Bearer " + instructorApiKey))
				.andExpect(jsonPath("$.length()").value(4));
		mockMvc.perform(post("/api/v1/join").contentType(MediaType.APPLICATION_JSON)
				.content("{\"joinCode\":\"" + assignment.joinCode() + "\",\"eagleIdentifier\":\"900000005\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsInvalidRoster() throws Exception {
		String courseReference = uniqueCourseReference();
		mockMvc.perform(put("/api/v1/courses/{courseReference}/roster", courseReference)
				.header("Authorization", "Bearer " + instructorApiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"label\":\"Synthetic\",\"courseSecret\":\"too-short\",\"students\":[]}"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.error").value("validation_failed"))
				.andExpect(jsonPath("$.message").value(Matchers.containsString("courseSecret")));
	}

	@Test
	void createAssignmentReturnsJoinCodeAndJoinUrl() throws Exception {
		String courseReference = uniqueCourseReference();
		synchronizeRoster(courseReference, syntheticRoster(courseReference));

		mockMvc.perform(post("/api/v1/courses/{courseReference}/assignments", courseReference)
				.header("Authorization", "Bearer " + instructorApiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"Lab 1\",\"settings\":{\"artificialIntelligenceUse\":\"forbidden\"}}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.joinCode").value(Matchers.matchesPattern("^[A-Z2-9]{4}-[A-Z2-9]{4}$")))
				.andExpect(jsonPath("$.joinUrl").value(Matchers.startsWith("http://localhost:8080/?joinCode=")));
	}

	@Test
	void createAssignmentRejectsUnknownCourseAndInvalidAiLevel() throws Exception {
		mockMvc.perform(post("/api/v1/courses/{courseReference}/assignments", uniqueCourseReference())
				.header("Authorization", "Bearer " + instructorApiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"Lab 1\",\"settings\":{\"artificialIntelligenceUse\":\"forbidden\"}}"))
				.andExpect(status().isNotFound());

		String courseReference = uniqueCourseReference();
		synchronizeRoster(courseReference, syntheticRoster(courseReference));
		mockMvc.perform(post("/api/v1/courses/{courseReference}/assignments", courseReference)
				.header("Authorization", "Bearer " + instructorApiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"Lab 1\",\"settings\":{\"artificialIntelligenceUse\":\"sometimes\"}}"))
				.andExpect(status().isUnprocessableContent());
	}

	@Test
	void summaryReflectsStudentProgress() throws Exception {
		String courseReference = uniqueCourseReference();
		synchronizeRoster(courseReference, syntheticRoster(courseReference));
		CreatedAssignment assignment = createAssignment(courseReference);

		String studentOneToken = joinAndGetStudentToken(assignment.joinCode(), "900000001");
		acknowledgeNotice(studentOneToken);
		mockMvc.perform(post("/api/v1/student/session/runs").header("Authorization", "Bearer " + studentOneToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"class Main {}\",\"standardInput\":\"\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/student/session/submit").header("Authorization", "Bearer " + studentOneToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"class Main {}\"}"))
				.andExpect(status().isOk());
		joinAndGetStudentToken(assignment.joinCode(), "900000002");

		mockMvc.perform(get("/api/v1/assignments/{assignmentId}/summary", assignment.assignmentId())
				.header("Authorization", "Bearer " + instructorApiKey))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].studentReference").value(courseReference + "-student-1"))
				.andExpect(jsonPath("$[0].status").value("submitted"))
				.andExpect(jsonPath("$[0].runCount").value(1))
				.andExpect(jsonPath("$[1].status").value("inProgress"))
				.andExpect(jsonPath("$[2].status").value("notStarted"));
	}
}
