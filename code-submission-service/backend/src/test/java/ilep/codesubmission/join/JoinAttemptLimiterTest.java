package ilep.codesubmission.join;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import ilep.codesubmission.configuration.CodeSubmissionProperties;

class JoinAttemptLimiterTest {

	private static final class AdjustableClock extends Clock {
		private Instant now = Instant.parse("2026-11-12T10:00:00Z");

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(java.time.ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}

	private final AdjustableClock clock = new AdjustableClock();
	private final JoinAttemptLimiter joinAttemptLimiter = new JoinAttemptLimiter(new CodeSubmissionProperties(null, null,
			null, null, new CodeSubmissionProperties.Join(3, 5, Duration.ofMinutes(5)), null), clock);

	@Test
	void blocksAJoinCodeAfterTooManyFailures() {
		for (int failure = 0; failure < 3; failure++) {
			assertThat(joinAttemptLimiter.isBlocked("ABCD-EFGH", "address-" + failure)).isFalse();
			joinAttemptLimiter.recordFailure("ABCD-EFGH", "address-" + failure);
		}
		assertThat(joinAttemptLimiter.isBlocked("ABCD-EFGH", "a-new-address")).isTrue();
		assertThat(joinAttemptLimiter.isBlocked("WXYZ-2345", "a-new-address")).isFalse();
	}

	@Test
	void blocksAClientAddressAfterTooManyFailuresAcrossJoinCodes() {
		for (int failure = 0; failure < 5; failure++) {
			joinAttemptLimiter.recordFailure("CODE-000" + failure, "192.0.2.10");
		}
		assertThat(joinAttemptLimiter.isBlocked("ABCD-EFGH", "192.0.2.10")).isTrue();
	}

	@Test
	void failuresExpireAfterTheWindow() {
		for (int failure = 0; failure < 3; failure++) {
			joinAttemptLimiter.recordFailure("ABCD-EFGH", "192.0.2.10");
		}
		clock.now = clock.now.plus(Duration.ofMinutes(6));

		assertThat(joinAttemptLimiter.isBlocked("ABCD-EFGH", "192.0.2.10")).isFalse();
	}
}
