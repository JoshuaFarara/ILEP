package ilep.codesubmission.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.jayway.jsonpath.JsonPath;

import ilep.codesubmission.coderun.CodeRunResult;
import ilep.codesubmission.coderun.JavaCodeRunner;
import ilep.codesubmission.join.JoinHashCalculator;

/**
 * Starts the whole service against a throwaway PostgreSQL container.
 *
 * SYNTHETIC DATA ONLY: every name and ID in these tests is fake. The code runner is replaced by a
 * fake so these tests do not need the sandbox image; DockerJavaCodeRunnerTest covers the sandbox.
 */
@SpringBootTest(properties = {
		"code-submission.instructor.development-api-key=" + IntegrationTestBase.instructorApiKey,
		"code-submission.cryptography.course-secret-encryption-key=AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=",
		"code-submission.public-base-url=http://localhost:8080",
		"code-submission.join.maximum-failures-per-client-address=1000",
})
@AutoConfigureMockMvc
@Import(IntegrationTestBase.TestInfrastructure.class)
public abstract class IntegrationTestBase {

	public static final String instructorApiKey = "synthetic-instructor-api-key-for-tests";
	public static final String courseSecretHex = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f";

	@Autowired
	protected MockMvc mockMvc;

	@TestConfiguration(proxyBeanMethods = false)
	static class TestInfrastructure {

		@Bean
		@ServiceConnection
		PostgreSQLContainer postgreSQLContainer() {
			return new PostgreSQLContainer("postgres:16-alpine");
		}

		/** Echoes the standard input back, so tests can see what was "run" without Docker. */
		@Bean
		@Primary
		JavaCodeRunner fakeJavaCodeRunner() {
			return (sourceCode, standardInput) -> sourceCode.contains("COMPILE_ERROR")
					? new CodeRunResult(false, "", "Main.java:1: error: synthetic compile error", null, false, false, 5)
					: new CodeRunResult(true, "echo:" + standardInput, "", 0, false, false, 5);
		}
	}

	/** Synthetic roster: student reference -> Eagle ID. */
	protected static Map<String, String> syntheticRoster(String courseReference) {
		Map<String, String> roster = new LinkedHashMap<>();
		for (int studentNumber = 1; studentNumber <= 5; studentNumber++) {
			roster.put(courseReference + "-student-" + studentNumber, "90000000" + studentNumber);
		}
		return roster;
	}

	protected void synchronizeRoster(String courseReference, Map<String, String> eagleIdentifiersByStudentReference)
			throws Exception {
		mockMvc.perform(put("/api/v1/courses/{courseReference}/roster", courseReference)
				.header("Authorization", "Bearer " + instructorApiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.content(rosterJson("Synthetic Course " + courseReference, eagleIdentifiersByStudentReference)))
				.andExpect(status().isOk());
	}

	protected static String rosterJson(String label, Map<String, String> eagleIdentifiersByStudentReference) {
		byte[] courseSecret = HexFormat.of().parseHex(courseSecretHex);
		String studentsJson = eagleIdentifiersByStudentReference.entrySet().stream()
				.map(entry -> "{\"studentReference\":\"" + entry.getKey() + "\",\"joinHash\":\""
						+ HexFormat.of().formatHex(JoinHashCalculator.calculateJoinHash(courseSecret, entry.getValue()))
						+ "\"}")
				.collect(Collectors.joining(","));
		return "{\"label\":\"" + label + "\",\"courseSecret\":\"" + courseSecretHex + "\",\"students\":[" + studentsJson
				+ "]}";
	}

	protected record CreatedAssignment(String assignmentId, String joinCode) {
	}

	protected CreatedAssignment createAssignment(String courseReference) throws Exception {
		String responseBody = mockMvc.perform(post("/api/v1/courses/{courseReference}/assignments", courseReference)
				.header("Authorization", "Bearer " + instructorApiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title": "Lab 4: Loops", "instructions": "Print 1 to 10.",
						 "starterCode": "public class Main {\\n}\\n",
						 "settings": {"artificialIntelligenceUse": "restricted", "artificialIntelligenceNote": "Explain errors only."}}
						"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return new CreatedAssignment(JsonPath.read(responseBody, "$.assignmentId"), JsonPath.read(responseBody, "$.joinCode"));
	}

	protected String joinAndGetStudentToken(String joinCode, String eagleIdentifier) throws Exception {
		String responseBody = mockMvc.perform(post("/api/v1/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"joinCode\":\"" + joinCode + "\",\"eagleIdentifier\":\"" + eagleIdentifier + "\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(responseBody, "$.studentToken");
	}

	protected void acknowledgeNotice(String studentToken) throws Exception {
		mockMvc.perform(post("/api/v1/student/session/notice-acknowledgement")
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"noticeVersion\":1}"))
				.andExpect(status().isNoContent());
	}

	protected static String uniqueCourseReference() {
		return "course-" + java.util.UUID.randomUUID().toString().substring(0, 12);
	}
}
