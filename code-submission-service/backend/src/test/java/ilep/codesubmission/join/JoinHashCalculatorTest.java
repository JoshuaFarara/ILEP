package ilep.codesubmission.join;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HexFormat;

import org.junit.jupiter.api.Test;

class JoinHashCalculatorTest {

	private static final byte[] courseSecret = HexFormat.of()
			.parseHex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");

	@Test
	void matchesReferenceValueComputedWithOpenssl() {
		// printf %s "900000001" | openssl dgst -sha256 -mac HMAC -macopt hexkey:000102...1e1f
		String expectedJoinHash = "816f1c3fd41479eb2c467b4a1a0a9bddd527d384c3aa365e54ffeca7c789af3c";

		byte[] joinHash = JoinHashCalculator.calculateJoinHash(courseSecret, "900000001");

		assertThat(HexFormat.of().formatHex(joinHash)).isEqualTo(expectedJoinHash);
	}

	@Test
	void ignoresSpacesHyphensAndLetterCase() {
		byte[] plainJoinHash = JoinHashCalculator.calculateJoinHash(courseSecret, "900000001");

		assertThat(JoinHashCalculator.calculateJoinHash(courseSecret, " 900-000 001 ")).isEqualTo(plainJoinHash);
		assertThat(JoinHashCalculator.normalizeEagleIdentifier("ab-12 c")).isEqualTo("AB12C");
	}

	@Test
	void differentCourseSecretsGiveDifferentHashes() {
		byte[] otherCourseSecret = new byte[32];

		assertThat(JoinHashCalculator.calculateJoinHash(otherCourseSecret, "900000001"))
				.isNotEqualTo(JoinHashCalculator.calculateJoinHash(courseSecret, "900000001"));
	}
}
