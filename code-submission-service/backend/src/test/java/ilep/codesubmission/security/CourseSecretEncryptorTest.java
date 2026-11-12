package ilep.codesubmission.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import ilep.codesubmission.configuration.CodeSubmissionProperties;

class CourseSecretEncryptorTest {

	private static CodeSubmissionProperties propertiesWithKey(String base64Key) {
		return new CodeSubmissionProperties(null, null, new CodeSubmissionProperties.Cryptography(base64Key), null,
				null, null);
	}

	@Test
	void encryptsAndDecryptsRoundTrip() {
		var encryptor = new CourseSecretEncryptor(propertiesWithKey(Base64.getEncoder().encodeToString(new byte[32])));
		byte[] courseSecret = "synthetic course secret 32 bytes".getBytes();

		byte[] encrypted = encryptor.encrypt(courseSecret);

		assertThat(encrypted).isNotEqualTo(courseSecret);
		assertThat(encryptor.decrypt(encrypted)).isEqualTo(courseSecret);
	}

	@Test
	void sameSecretEncryptsDifferentlyEachTime() {
		var encryptor = new CourseSecretEncryptor(propertiesWithKey(Base64.getEncoder().encodeToString(new byte[32])));
		byte[] courseSecret = new byte[32];

		assertThat(encryptor.encrypt(courseSecret)).isNotEqualTo(encryptor.encrypt(courseSecret));
	}

	@Test
	void refusesToStartWithoutAValidKey() {
		assertThatThrownBy(() -> new CourseSecretEncryptor(propertiesWithKey("")))
				.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> new CourseSecretEncryptor(propertiesWithKey(Base64.getEncoder().encodeToString(new byte[16]))))
				.isInstanceOf(IllegalStateException.class);
	}
}
