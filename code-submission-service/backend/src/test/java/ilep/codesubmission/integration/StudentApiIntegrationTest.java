package ilep.codesubmission.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class StudentApiIntegrationTest extends IntegrationTestBase {

	private String courseReference;
	private CreatedAssignment assignment;

	@BeforeEach
	void createCourseAndAssignment() throws Exception {
		courseReference = uniqueCourseReference();
		synchronizeRoster(courseReference, syntheticRoster(courseReference));
		assignment = createAssignment(courseReference);
	}

	@Test
	void joinSucceedsWithEnrolledIdInAnyFormat() throws Exception {
		String lowercaseCodeWithoutHyphen = assignment.joinCode().replace("-", "").toLowerCase();
		joinAndGetStudentToken(lowercaseCodeWithoutHyphen, " 900-000-003 ");
	}

	@Test
	void joinFailsTheSameWayForWrongIdAndWrongCode() throws Exception {
		mockMvc.perform(post("/api/v1/join").contentType(MediaType.APPLICATION_JSON)
				.content("{\"joinCode\":\"" + assignment.joinCode() + "\",\"eagleIdentifier\":\"999999999\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("unauthorized"));
		mockMvc.perform(post("/api/v1/join").contentType(MediaType.APPLICATION_JSON)
				.content("{\"joinCode\":\"ZZZZ-ZZZZ\",\"eagleIdentifier\":\"900000001\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("unauthorized"));
	}

	@Test
	void sessionShowsAssignmentAndStarterCode() throws Exception {
		String studentToken = joinAndGetStudentToken(assignment.joinCode(), "900000001");

		mockMvc.perform(get("/api/v1/student/session").header("Authorization", "Bearer " + studentToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Lab 4: Loops"))
				.andExpect(jsonPath("$.artificialIntelligenceUse").value("restricted"))
				.andExpect(jsonPath("$.currentCode").value("public class Main {\n}\n"))
				.andExpect(jsonPath("$.status").value("inProgress"))
				.andExpect(jsonPath("$.noticeAcknowledged").value(false))
				.andExpect(jsonPath("$.currentNoticeVersion").value(1));
	}

	@Test
	void savingAndRunningRequireTheNoticeAcknowledgement() throws Exception {
		String studentToken = joinAndGetStudentToken(assignment.joinCode(), "900000001");

		mockMvc.perform(put("/api/v1/student/session/code").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"class Main {}\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("notice_not_acknowledged"));
		mockMvc.perform(post("/api/v1/student/session/runs").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"class Main {}\"}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/v1/student/session/notice-acknowledgement")
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"noticeVersion\":0}"))
				.andExpect(status().isConflict());
		acknowledgeNotice(studentToken);

		mockMvc.perform(put("/api/v1/student/session/code").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"class Main {}\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.savedAt").exists());
	}

	@Test
	void autosaveIsKeptAcrossRejoining() throws Exception {
		String studentToken = joinAndGetStudentToken(assignment.joinCode(), "900000001");
		acknowledgeNotice(studentToken);
		mockMvc.perform(put("/api/v1/student/session/code").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"// my work in progress\"}"))
				.andExpect(status().isOk());

		String secondDeviceToken = joinAndGetStudentToken(assignment.joinCode(), "900000001");

		mockMvc.perform(get("/api/v1/student/session").header("Authorization", "Bearer " + secondDeviceToken))
				.andExpect(jsonPath("$.currentCode").value("// my work in progress"))
				.andExpect(jsonPath("$.noticeAcknowledged").value(true));
	}

	@Test
	void runReturnsCompilerAndProgramResults() throws Exception {
		String studentToken = joinAndGetStudentToken(assignment.joinCode(), "900000001");
		acknowledgeNotice(studentToken);

		mockMvc.perform(post("/api/v1/student/session/runs").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"class Main {}\",\"standardInput\":\"5\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.compileSucceeded").value(true))
				.andExpect(jsonPath("$.standardOutput").value("echo:5"))
				.andExpect(jsonPath("$.exitCode").value(0));
		mockMvc.perform(post("/api/v1/student/session/runs").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"COMPILE_ERROR\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.compileSucceeded").value(false))
				.andExpect(jsonPath("$.standardError").value(org.hamcrest.Matchers.containsString("error")));
	}

	@Test
	void submitLocksTheSession() throws Exception {
		String studentToken = joinAndGetStudentToken(assignment.joinCode(), "900000001");
		acknowledgeNotice(studentToken);

		mockMvc.perform(post("/api/v1/student/session/submit").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"public class Main { /* final */ }\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.submittedAt").exists());

		mockMvc.perform(put("/api/v1/student/session/code").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"changed after submit\"}"))
				.andExpect(status().isConflict());
		mockMvc.perform(post("/api/v1/student/session/submit").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"again\"}"))
				.andExpect(status().isConflict());
		mockMvc.perform(get("/api/v1/student/session").header("Authorization", "Bearer " + studentToken))
				.andExpect(jsonPath("$.status").value("submitted"))
				.andExpect(jsonPath("$.currentCode").value("public class Main { /* final */ }"));
	}

	@Test
	void closedAssignmentRejectsJoiningAndSaving() throws Exception {
		String studentToken = joinAndGetStudentToken(assignment.joinCode(), "900000001");
		acknowledgeNotice(studentToken);

		mockMvc.perform(post("/api/v1/assignments/{assignmentId}/close", assignment.assignmentId())
				.header("Authorization", "Bearer " + instructorApiKey))
				.andExpect(status().isNoContent());

		mockMvc.perform(put("/api/v1/student/session/code").header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"late edit\"}"))
				.andExpect(status().isConflict());
		mockMvc.perform(post("/api/v1/join").contentType(MediaType.APPLICATION_JSON)
				.content("{\"joinCode\":\"" + assignment.joinCode() + "\",\"eagleIdentifier\":\"900000002\"}"))
				.andExpect(status().isConflict());
	}

	@Test
	void studentEndpointsRequireAToken() throws Exception {
		mockMvc.perform(get("/api/v1/student/session")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/v1/student/session").header("Authorization", "Bearer student_madeUpToken"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void unknownApiPathsAreDenied() throws Exception {
		mockMvc.perform(get("/api/v1/something-else")).andExpect(status().isUnauthorized());
	}
}
