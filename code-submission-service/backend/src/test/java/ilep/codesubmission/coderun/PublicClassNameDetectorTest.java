package ilep.codesubmission.coderun;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PublicClassNameDetectorTest {

	@Test
	void findsThePublicClass() {
		String sourceCode = """
				import java.util.Scanner;
				class Helper {}
				public final class GradeCalculator {
				    public static void main(String[] arguments) {}
				}
				""";
		assertThat(PublicClassNameDetector.detectClassName(sourceCode)).isEqualTo("GradeCalculator");
	}

	@Test
	void usesTheClassWithMainWhenNothingIsPublic() {
		String sourceCode = """
				class Helper { int value; }
				class Program {
				    static void main(String[] arguments) {}
				}
				""";
		assertThat(PublicClassNameDetector.detectClassName(sourceCode)).isEqualTo("Program");
	}

	@Test
	void ignoresClassNamesInCommentsAndStrings() {
		String sourceCode = """
				// public class Fake {}
				/* public class AlsoFake {} */
				public class RealOne {
				    String text = "public class InString";
				}
				""";
		assertThat(PublicClassNameDetector.detectClassName(sourceCode)).isEqualTo("RealOne");
	}

	@Test
	void defaultsToMain() {
		assertThat(PublicClassNameDetector.detectClassName("System.out.println(1);")).isEqualTo("Main");
		assertThat(PublicClassNameDetector.detectClassName("")).isEqualTo("Main");
	}
}
