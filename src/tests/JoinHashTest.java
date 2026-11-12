package tests;

import java.util.HexFormat;

import models.JoinHash;
import models.Student;

/**
 * Checks that ILEP computes join hashes exactly like the Code Submission service.
 * Synthetic Eagle IDs only. Run as a Java application.
 */
public class JoinHashTest {

	public static void main(String[] args) {
		byte[] courseSecret = HexFormat.of().parseHex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");

		// Same reference value the service's JoinHashCalculatorTest uses, computed with:
		// printf %s "900000001" | openssl dgst -sha256 -mac HMAC -macopt hexkey:000102...1e1f
		check(JoinHash.calculateHex(courseSecret, "900000001")
				.equals("816f1c3fd41479eb2c467b4a1a0a9bddd527d384c3aa365e54ffeca7c789af3c"),
				"matches the openssl reference value (and the service)");

		check(JoinHash.calculateHex(courseSecret, " 900-000 001 ").equals(JoinHash.calculateHex(courseSecret, "900000001")),
				"ignores spaces and hyphens");
		check(JoinHash.normalizeEagleIdentifier("ab-12 c").equals("AB12C"), "uppercases letters");
		check(!JoinHash.calculateHex(new byte[32], "900000001").equals(JoinHash.calculateHex(courseSecret, "900000001")),
				"a different course secret gives a different hash");

		Student withoutId = new Student();
		withoutId.setEagleID("  ");
		Student withId = new Student();
		withId.setEagleID("900000002");
		check(!JoinHash.hasUsableEagleIdentifier(withoutId) && JoinHash.hasUsableEagleIdentifier(withId),
				"detects students with no usable Eagle ID");
	}

	private static void check(boolean ok, String message) {
		System.out.println((ok ? "PASS " : "FAIL ") + message);
	}
}
