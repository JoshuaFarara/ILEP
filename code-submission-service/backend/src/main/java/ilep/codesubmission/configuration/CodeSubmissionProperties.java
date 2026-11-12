package ilep.codesubmission.configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** All settings under {@code code-submission.*} in application.yaml. */
@ConfigurationProperties(prefix = "code-submission")
public record CodeSubmissionProperties(
		String publicBaseUrl,
		Instructor instructor,
		Cryptography cryptography,
		StudentSession studentSession,
		Join join,
		Runner runner) {

	public record Instructor(String developmentApiKey) {
	}

	public record Cryptography(String courseSecretEncryptionKey) {
	}

	public record StudentSession(Duration tokenLifetime, int maximumCodeLength) {
	}

	public record Join(int maximumFailuresPerJoinCode, int maximumFailuresPerClientAddress, Duration failureWindow) {
	}

	public record Runner(
			String dockerCommand,
			String image,
			Duration wallClockTimeout,
			int programTimeoutSeconds,
			String memoryLimit,
			String cpuLimit,
			int processLimit,
			int maximumOutputBytes,
			int maximumStandardInputBytes,
			int maximumConcurrentRuns) {
	}
}
