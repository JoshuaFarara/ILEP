package ilep.codesubmission.coderun;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ilep.codesubmission.configuration.CodeSubmissionProperties;

/**
 * Runs real programs in the sandbox. Needs Docker and the runner image:
 * {@code docker build -t ilep-java-runner:25 code-submission-service/runner-image}.
 * Skip with {@code ./gradlew test -PexcludeDockerTests}.
 */
@Tag("docker")
class DockerJavaCodeRunnerTest {

	private static final CodeSubmissionProperties.Runner runnerProperties = new CodeSubmissionProperties.Runner(
			"docker", "ilep-java-runner:25", Duration.ofSeconds(15), 5, "256m", "0.5", 64, 4096, 16384, 4);

	private final DockerJavaCodeRunner dockerJavaCodeRunner = new DockerJavaCodeRunner(
			new CodeSubmissionProperties(null, null, null, null, null, runnerProperties));

	@Test
	void compilesAndRunsWithStandardInput() {
		CodeRunResult codeRunResult = dockerJavaCodeRunner.compileAndRun("""
				import java.util.Scanner;
				public class Greeting {
				    public static void main(String[] arguments) {
				        Scanner scanner = new Scanner(System.in);
				        System.out.println("Hello, " + scanner.nextLine() + "!");
				    }
				}
				""", "Student One\n");

		assertThat(codeRunResult.compileSucceeded()).isTrue();
		assertThat(codeRunResult.standardOutput()).isEqualTo("Hello, Student One!\n");
		assertThat(codeRunResult.exitCode()).isZero();
		assertThat(codeRunResult.timedOut()).isFalse();
	}

	@Test
	void reportsCompileErrors() {
		CodeRunResult codeRunResult = dockerJavaCodeRunner.compileAndRun("""
				public class Main {
				    public static void main(String[] arguments) {
				        int count = 5
				    }
				}
				""", "");

		assertThat(codeRunResult.compileSucceeded()).isFalse();
		assertThat(codeRunResult.standardError()).contains("Main.java:3: error: ';' expected");
		assertThat(codeRunResult.exitCode()).isNull();
	}

	@Test
	void reportsRuntimeExceptionsAndExitCodes() {
		CodeRunResult codeRunResult = dockerJavaCodeRunner.compileAndRun("""
				public class Main {
				    public static void main(String[] arguments) {
				        int[] numbers = new int[2];
				        System.out.println(numbers[5]);
				    }
				}
				""", "");

		assertThat(codeRunResult.compileSucceeded()).isTrue();
		assertThat(codeRunResult.standardError()).contains("ArrayIndexOutOfBoundsException");
		assertThat(codeRunResult.exitCode()).isEqualTo(1);
	}

	@Test
	void stopsInfiniteLoopsAtTheTimeLimit() {
		CodeRunResult codeRunResult = dockerJavaCodeRunner.compileAndRun("""
				public class Main {
				    public static void main(String[] arguments) {
				        while (true) { }
				    }
				}
				""", "");

		assertThat(codeRunResult.timedOut()).isTrue();
		assertThat(codeRunResult.durationMilliseconds()).isLessThan(15_000);
	}

	@Test
	void blocksNetworkAccess() {
		CodeRunResult codeRunResult = dockerJavaCodeRunner.compileAndRun("""
				public class Main {
				    public static void main(String[] arguments) throws Exception {
				        try (var socket = new java.net.Socket("example.com", 80)) {
				            System.out.println("CONNECTED");
				        } catch (Exception exception) {
				            System.out.println("BLOCKED " + exception.getClass().getSimpleName());
				        }
				    }
				}
				""", "");

		assertThat(codeRunResult.standardOutput()).startsWith("BLOCKED").doesNotContain("CONNECTED");
	}

	@Test
	void blocksWritingOutsideTheWorkspace() {
		CodeRunResult codeRunResult = dockerJavaCodeRunner.compileAndRun("""
				import java.nio.file.*;
				public class Main {
				    public static void main(String[] arguments) {
				        for (String target : new String[] {"/etc/attack.txt", "/submission/attack.txt", "/home/attack.txt"}) {
				            try {
				                Files.writeString(Path.of(target), "x");
				                System.out.println("WROTE " + target);
				            } catch (Exception exception) {
				                System.out.println("DENIED " + target);
				            }
				        }
				    }
				}
				""", "");

		assertThat(codeRunResult.standardOutput()).doesNotContain("WROTE");
		assertThat(codeRunResult.standardOutput()).contains("DENIED /etc/attack.txt", "DENIED /submission/attack.txt");
	}

	@Test
	void containsAForkBombWithTheProcessLimit() {
		CodeRunResult codeRunResult = dockerJavaCodeRunner.compileAndRun("""
				import java.util.*;
				public class Main {
				    public static void main(String[] arguments) {
				        List<Thread> threads = new ArrayList<>();
				        try {
				            for (int index = 0; index < 10_000; index++) {
				                Thread thread = new Thread(() -> { try { Thread.sleep(60_000); } catch (InterruptedException e) {} });
				                thread.setDaemon(true);
				                thread.start();
				                threads.add(thread);
				            }
				            System.out.println("STARTED ALL");
				        } catch (Throwable limitReached) {
				            System.out.println("LIMITED AT " + threads.size());
				        }
				    }
				}
				""", "");

		assertThat(codeRunResult.standardOutput()).startsWith("LIMITED AT").doesNotContain("STARTED ALL");
		assertThat(codeRunResult.standardError()).contains("pthread_create failed");
	}

	@Test
	void capsHugeOutput() {
		CodeRunResult codeRunResult = dockerJavaCodeRunner.compileAndRun("""
				public class Main {
				    public static void main(String[] arguments) {
				        for (int line = 0; line < 100_000; line++) {
				            System.out.println("This line is repeated to produce a very large amount of output.");
				        }
				    }
				}
				""", "");

		assertThat(codeRunResult.outputTruncated()).isTrue();
		assertThat(codeRunResult.standardOutput().length()).isLessThanOrEqualTo(4096);
	}
}
