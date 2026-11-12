package ilep.codesubmission.join;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Computes {@code joinHash = HMAC-SHA256(courseSecret, normalizedEagleIdentifier)}.
 *
 * ILEP computes the same value locally when it synchronizes a roster, so the server can verify a
 * student's Eagle ID at join time without ever storing it. ILEP must use the same normalization.
 */
public final class JoinHashCalculator {

	private JoinHashCalculator() {
	}

	/** Removes spaces and hyphens and uppercases, so " 900-000-001 " becomes "900000001". */
	public static String normalizeEagleIdentifier(String typedEagleIdentifier) {
		return typedEagleIdentifier.replaceAll("[\\s-]", "").toUpperCase();
	}

	public static byte[] calculateJoinHash(byte[] courseSecret, String eagleIdentifier) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(courseSecret, "HmacSHA256"));
			return mac.doFinal(normalizeEagleIdentifier(eagleIdentifier).getBytes(StandardCharsets.UTF_8));
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("HMAC-SHA256 is not available", exception);
		}
	}
}
