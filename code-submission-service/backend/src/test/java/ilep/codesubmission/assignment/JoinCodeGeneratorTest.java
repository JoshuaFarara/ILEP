package ilep.codesubmission.assignment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class JoinCodeGeneratorTest {

	private final JoinCodeGenerator joinCodeGenerator = new JoinCodeGenerator();

	@Test
	void joinCodesHaveFourCharactersHyphenFourCharactersWithoutLookAlikes() {
		for (int attempt = 0; attempt < 1000; attempt++) {
			assertThat(joinCodeGenerator.newJoinCode()).matches("^[A-HJ-KMNP-Z2-9]{4}-[A-HJ-KMNP-Z2-9]{4}$");
		}
	}

	@Test
	void joinCodesAreVirtuallyUnique() {
		Set<String> joinCodes = new HashSet<>();
		for (int attempt = 0; attempt < 10_000; attempt++) {
			joinCodes.add(joinCodeGenerator.newJoinCode());
		}
		assertThat(joinCodes).hasSizeGreaterThan(9_990);
	}

	@Test
	void normalizationAcceptsLowercaseAndMissingHyphen() {
		assertThat(JoinCodeGenerator.normalizeJoinCode("k7qm4xtb")).isEqualTo("K7QM-4XTB");
		assertThat(JoinCodeGenerator.normalizeJoinCode(" K7QM-4XTB ")).isEqualTo("K7QM-4XTB");
	}
}
