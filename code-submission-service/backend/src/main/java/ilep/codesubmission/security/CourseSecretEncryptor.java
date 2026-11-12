package ilep.codesubmission.security;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import ilep.codesubmission.configuration.CodeSubmissionProperties;

/**
 * Encrypts course secrets at rest with AES-256-GCM (DSCS: Class I protection).
 * Stored format: 12-byte nonce followed by ciphertext and 16-byte authentication tag.
 */
@Component
public class CourseSecretEncryptor {

	private static final int nonceLengthBytes = 12;
	private static final int authenticationTagLengthBits = 128;

	private final SecretKeySpec encryptionKey;
	private final SecureRandom secureRandom = new SecureRandom();

	public CourseSecretEncryptor(CodeSubmissionProperties properties) {
		String base64Key = properties.cryptography().courseSecretEncryptionKey();
		if (base64Key == null || base64Key.isBlank()) {
			throw new IllegalStateException(
					"COURSE_SECRET_ENCRYPTION_KEY is required: 32 random bytes, base64 encoded.");
		}
		byte[] keyBytes = Base64.getDecoder().decode(base64Key.trim());
		if (keyBytes.length != 32) {
			throw new IllegalStateException("COURSE_SECRET_ENCRYPTION_KEY must decode to exactly 32 bytes.");
		}
		this.encryptionKey = new SecretKeySpec(keyBytes, "AES");
	}

	public byte[] encrypt(byte[] courseSecret) {
		try {
			byte[] nonce = new byte[nonceLengthBytes];
			secureRandom.nextBytes(nonce);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(authenticationTagLengthBits, nonce));
			byte[] ciphertext = cipher.doFinal(courseSecret);
			return ByteBuffer.allocate(nonce.length + ciphertext.length).put(nonce).put(ciphertext).array();
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Course secret encryption failed", exception);
		}
	}

	public byte[] decrypt(byte[] storedValue) {
		try {
			ByteBuffer buffer = ByteBuffer.wrap(storedValue);
			byte[] nonce = new byte[nonceLengthBytes];
			buffer.get(nonce);
			byte[] ciphertext = new byte[buffer.remaining()];
			buffer.get(ciphertext);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(authenticationTagLengthBits, nonce));
			return cipher.doFinal(ciphertext);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Course secret decryption failed", exception);
		}
	}
}
