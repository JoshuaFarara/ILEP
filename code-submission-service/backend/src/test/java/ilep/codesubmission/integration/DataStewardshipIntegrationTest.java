package ilep.codesubmission.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Data stewardship check (docs/code-submission/data-classification.md): after a full synthetic class
 * session, no Eagle ID may appear anywhere in the database or in the logs, in text or raw bytes.
 */
@ExtendWith(OutputCaptureExtension.class)
class DataStewardshipIntegrationTest extends IntegrationTestBase {

	private static final List<String> forbiddenValues = List.of("900000001", "900000002", "900000003", "900000004",
			"900000005", "999999999", "Student One", "student.one@example.edu");

	@Autowired
	private JdbcClient jdbcClient;

	@Test
	void noStudentIdentifierIsStoredOrLogged(CapturedOutput capturedOutput) throws Exception {
		String courseReference = uniqueCourseReference();
		synchronizeRoster(courseReference, syntheticRoster(courseReference));
		CreatedAssignment assignment = createAssignment(courseReference);

		for (Map.Entry<String, String> student : syntheticRoster(courseReference).entrySet()) {
			String studentToken = joinAndGetStudentToken(assignment.joinCode(), student.getValue());
			acknowledgeNotice(studentToken);
			mockMvc.perform(put("/api/v1/student/session/code").header("Authorization", "Bearer " + studentToken)
					.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"class Main {}\"}"))
					.andExpect(status().isOk());
			mockMvc.perform(post("/api/v1/student/session/runs").header("Authorization", "Bearer " + studentToken)
					.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"class Main {}\",\"standardInput\":\"hi\"}"))
					.andExpect(status().isOk());
			mockMvc.perform(post("/api/v1/student/session/submit").header("Authorization", "Bearer " + studentToken)
					.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"class Main {}\"}"))
					.andExpect(status().isOk());
		}
		// A failed join with an unenrolled ID must not leave a trace either.
		mockMvc.perform(post("/api/v1/join").contentType(MediaType.APPLICATION_JSON)
				.content("{\"joinCode\":\"" + assignment.joinCode() + "\",\"eagleIdentifier\":\"999999999\"}"))
				.andExpect(status().isUnauthorized());

		String everyStoredValue = dumpEveryTable();
		for (String forbiddenValue : forbiddenValues) {
			assertThat(everyStoredValue).as("database contents").doesNotContain(forbiddenValue);
			assertThat(capturedOutput.getAll()).as("log output").doesNotContain(forbiddenValue);
		}
		assertThat(capturedOutput.getAll()).contains("POST /api/v1/join ->");
	}

	/** Every column of every table, with binary columns decoded both as hex and as UTF-8 text. */
	private String dumpEveryTable() {
		List<String> tableNames = jdbcClient.sql("""
				select table_name from information_schema.tables
				where table_schema = 'public' and table_type = 'BASE TABLE'
				""")
				.query(String.class).list();
		assertThat(tableNames).contains("enrollments", "student_work_sessions", "code_runs");

		StringBuilder everyStoredValue = new StringBuilder();
		for (String tableName : tableNames) {
			List<String> renderedRows = new ArrayList<>();
			jdbcClient.sql("select * from \"" + tableName + "\"").query(resultSet -> {
				int columnCount = resultSet.getMetaData().getColumnCount();
				StringBuilder renderedRow = new StringBuilder(tableName).append(':');
				for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
					Object value = resultSet.getObject(columnIndex);
					if (value instanceof byte[] bytes) {
						renderedRow.append(java.util.HexFormat.of().formatHex(bytes)).append('|')
								.append(new String(bytes, StandardCharsets.UTF_8)).append('|');
					} else {
						renderedRow.append(value).append('|');
					}
				}
				renderedRows.add(renderedRow.toString());
			});
			renderedRows.forEach(renderedRow -> everyStoredValue.append(renderedRow).append('\n'));
		}
		return everyStoredValue.toString();
	}
}
