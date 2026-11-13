package tests;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.JsonText;

/** Checks the small JSON helper used by the Code Submission client. Run as a Java application. */
public class JsonTextTest {

	public static void main(String[] args) {
		// Writing
		Map<String, Object> roster = new LinkedHashMap<>();
		roster.put("label", "CSCI1301A Fall 2026");
		roster.put("students", List.of(Map.of("studentReference", "student_abc")));
		roster.put("count", 3);
		roster.put("open", true);
		roster.put("dueAt", null);
		String json = JsonText.write(roster);
		check(json.equals("{\"label\":\"CSCI1301A Fall 2026\",\"students\":[{\"studentReference\":\"student_abc\"}],"
				+ "\"count\":3,\"open\":true,\"dueAt\":null}"), "writes objects, arrays, numbers, booleans and null: " + json);
		check(JsonText.write("Line 1\nTab\t\"quoted\" back\\slash").equals(
				"\"Line 1\\nTab\\t\\\"quoted\\\" back\\\\slash\""), "escapes newlines, tabs, quotes and backslashes");
		check(JsonText.write("\u0001").equals("\"\\u0001\""), "escapes control characters");

		// Reading
		Map<String, Object> parsed = JsonText.readObject(
				" { \"assignmentId\" : \"df4f\", \"runCount\": 2, \"ratio\": 0.5, \"submittedAt\": null,"
						+ " \"flags\": [true, false], \"note\": \"caf\\u00e9 \\\"ok\\\"\\n\" } ");
		check("df4f".equals(parsed.get("assignmentId")), "reads strings");
		check(Long.valueOf(2).equals(parsed.get("runCount")), "reads whole numbers as Long");
		check(Double.valueOf(0.5).equals(parsed.get("ratio")), "reads decimals as Double");
		check(parsed.containsKey("submittedAt") && parsed.get("submittedAt") == null, "reads null");
		check(List.of(true, false).equals(parsed.get("flags")), "reads arrays of booleans");
		check("café \"ok\"\n".equals(parsed.get("note")), "reads unicode and escape sequences");
		check(JsonText.readArray("[]").isEmpty() && JsonText.readObject("{}").isEmpty(), "reads empty array and object");

		// Round trip
		String starterCode = "public class Main {\n\tSystem.out.println(\"Hi\");\n}\n";
		Map<String, Object> roundTrip = JsonText.readObject(JsonText.write(Map.of("starterCode", starterCode)));
		check(starterCode.equals(roundTrip.get("starterCode")), "Java source survives a round trip unchanged");

		// Malformed input
		for (String malformed : new String[] { "", "{", "{\"a\" 1}", "[1,]", "{\"a\":tru}", "\"unterminated", "{} extra",
				"{\"a\":\"\\q\"}" }) {
			check(throwsFormatException(malformed), "rejects malformed JSON: " + malformed);
		}
		check(throwsFormatException2(), "readObject rejects an array");
	}

	private static boolean throwsFormatException(String json) {
		try {
			JsonText.read(json);
			return false;
		} catch (JsonText.JsonFormatException expected) {
			return true;
		}
	}

	private static boolean throwsFormatException2() {
		try {
			JsonText.readObject("[1, 2]");
			return false;
		} catch (JsonText.JsonFormatException expected) {
			return true;
		}
	}

	private static void check(boolean ok, String message) {
		System.out.println((ok ? "PASS " : "FAIL ") + message);
	}
}
