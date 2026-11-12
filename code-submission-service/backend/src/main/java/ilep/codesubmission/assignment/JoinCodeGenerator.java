package ilep.codesubmission.assignment;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/**
 * Makes join codes like {@code K7QM-4XTB}: 8 characters from an alphabet without look-alikes
 * (no 0/O, 1/I/L), so students can read them off a projector. About 39 bits of randomness.
 */
@Component
public class JoinCodeGenerator {

	static final String joinCodeAlphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
	private static final int joinCodeLength = 8;

	private final SecureRandom secureRandom = new SecureRandom();

	public String newJoinCode() {
		StringBuilder joinCode = new StringBuilder(joinCodeLength + 1);
		for (int characterIndex = 0; characterIndex < joinCodeLength; characterIndex++) {
			if (characterIndex == joinCodeLength / 2) {
				joinCode.append('-');
			}
			joinCode.append(joinCodeAlphabet.charAt(secureRandom.nextInt(joinCodeAlphabet.length())));
		}
		return joinCode.toString();
	}

	/** Uppercases and restores the hyphen, so "k7qm4xtb" and "K7QM-4XTB" match. */
	public static String normalizeJoinCode(String typedJoinCode) {
		String compactJoinCode = typedJoinCode.replaceAll("[\\s-]", "").toUpperCase();
		if (compactJoinCode.length() != joinCodeLength) {
			return compactJoinCode;
		}
		return compactJoinCode.substring(0, joinCodeLength / 2) + "-" + compactJoinCode.substring(joinCodeLength / 2);
	}
}
