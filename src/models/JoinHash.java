package models;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Computes the join hash that lets the Code Submission service check a student's Eagle ID
 * without ever receiving it from ILEP:
 *
 * <pre>
 * joinHash = hex( HMAC-SHA256(key = courseSecret, message = normalized Eagle ID) )
 * </pre>
 *
 * This must match the service's JoinHashCalculator exactly, including normalization
 * (code-submission-service/README.md, "Join hash").
 */
public final class JoinHash {

	private JoinHash() {
	}

	/** Removes spaces and hyphens and uppercases, so " 900-000-001 " becomes "900000001". */
	public static String normalizeEagleIdentifier(String eagleIdentifier) {
		return eagleIdentifier == null ? "" : eagleIdentifier.replaceAll("[\\s-]", "").toUpperCase();
	}

	/** False for students whose roster row has no usable Eagle ID; they cannot join. */
	public static boolean hasUsableEagleIdentifier(Student student) {
		return !normalizeEagleIdentifier(student.getEagleID()).isEmpty();
	}

	public static String calculateHex(byte[] courseSecret, String eagleIdentifier) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(courseSecret, "HmacSHA256"));
			byte[] hash = mac.doFinal(normalizeEagleIdentifier(eagleIdentifier).getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("HMAC-SHA256 is not available", exception);
		}
	}
}
