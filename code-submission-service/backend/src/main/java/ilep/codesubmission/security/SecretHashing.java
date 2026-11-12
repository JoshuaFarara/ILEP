package ilep.codesubmission.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** SHA-256 hashing for API keys and session tokens, and random token generation. */
public final class SecretHashing {

	private static final SecureRandom secureRandom = new SecureRandom();

	private SecretHashing() {
	}

	public static byte[] sha256(String value) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}

	/** A URL-safe random token with the given prefix and 32 bytes of entropy. */
	public static String newRandomToken(String prefix) {
		byte[] randomBytes = new byte[32];
		secureRandom.nextBytes(randomBytes);
		return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
	}
}
