package ilep.codesubmission.coderun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ilep.codesubmission.configuration.CodeSubmissionProperties;

/**
 * Runs each program in a new, locked-down Docker container that is removed afterwards.
 *
 * Isolation: no network; read-only root file system with a small writable /workspace; memory,
 * swap, CPU and process-count limits; all Linux capabilities dropped; no privilege escalation;
 * unprivileged user; a time limit inside the container and a wall-clock limit enforced here.
 * Student code never runs inside this service's own process.
 */
@Component
public class DockerJavaCodeRunner implements JavaCodeRunner {

	private static final Logger logger = LoggerFactory.getLogger(DockerJavaCodeRunner.class);

	static final int compileErrorExitCode = 101;
	static final int setupErrorExitCode = 102;
	static final int timeLimitExitCode = 124;
	private static final int dockerFailureExitCode = 125;

	private final CodeSubmissionProperties.Runner runnerProperties;
	private final ExecutorService outputReaders = Executors.newVirtualThreadPerTaskExecutor();

	public DockerJavaCodeRunner(CodeSubmissionProperties properties) {
		this.runnerProperties = properties.runner();
	}

	@Override
	public CodeRunResult compileAndRun(String sourceCode, String standardInput) {
		String className = PublicClassNameDetector.detectClassName(sourceCode);
		String containerName = "code-run-" + UUID.randomUUID();
		Path submissionDirectory = null;
		long startNanoseconds = System.nanoTime();
		try {
			submissionDirectory = writeSubmissionFiles(className, sourceCode, standardInput);
			Process dockerProcess = new ProcessBuilder(dockerRunCommand(containerName, className, submissionDirectory))
					.redirectInput(ProcessBuilder.Redirect.from(new java.io.File("/dev/null")))
					.start();
			Future<CappedOutput> standardOutputFuture = outputReaders.submit(() -> readCapped(dockerProcess.getInputStream()));
			Future<CappedOutput> standardErrorFuture = outputReaders.submit(() -> readCapped(dockerProcess.getErrorStream()));

			boolean finishedInTime = dockerProcess.waitFor(runnerProperties.wallClockTimeout().toMillis(), TimeUnit.MILLISECONDS);
			if (!finishedInTime) {
				killContainer(containerName);
				dockerProcess.destroyForcibly();
				dockerProcess.waitFor(5, TimeUnit.SECONDS);
			}
			CappedOutput standardOutput = standardOutputFuture.get(5, TimeUnit.SECONDS);
			CappedOutput standardError = standardErrorFuture.get(5, TimeUnit.SECONDS);
			long durationMilliseconds = (System.nanoTime() - startNanoseconds) / 1_000_000;
			boolean outputTruncated = standardOutput.truncated() || standardError.truncated();

			if (!finishedInTime) {
				return new CodeRunResult(true, standardOutput.text(), standardError.text(), null, true, outputTruncated,
						durationMilliseconds);
			}
			int exitCode = dockerProcess.exitValue();
			if (exitCode == dockerFailureExitCode && standardError.text().startsWith("docker:")) {
				throw new CodeRunnerUnavailableException("The sandbox could not start a container.");
			}
			if (exitCode == setupErrorExitCode) {
				throw new CodeRunnerUnavailableException("The sandbox could not prepare the submission.");
			}
			if (exitCode == compileErrorExitCode) {
				return new CodeRunResult(false, "", standardError.text(), null, false, outputTruncated, durationMilliseconds);
			}
			boolean timedOut = exitCode == timeLimitExitCode;
			return new CodeRunResult(true, standardOutput.text(), standardError.text(), timedOut ? null : exitCode,
					timedOut, outputTruncated, durationMilliseconds);
		} catch (IOException exception) {
			throw new CodeRunnerUnavailableException("The sandbox is unavailable.", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			killContainer(containerName);
			throw new CodeRunnerUnavailableException("The run was interrupted.", exception);
		} catch (ExecutionException | TimeoutException exception) {
			killContainer(containerName);
			throw new CodeRunnerUnavailableException("Could not read the program output.", exception);
		} finally {
			deleteQuietly(submissionDirectory);
		}
	}

	List<String> dockerRunCommand(String containerName, String className, Path submissionDirectory) {
		List<String> command = new ArrayList<>(List.of(
				runnerProperties.dockerCommand(), "run", "--rm",
				"--name", containerName,
				"--network", "none",
				"--read-only",
				"--tmpfs", "/workspace:rw,exec,nosuid,nodev,size=32m,mode=1777",
				"--tmpfs", "/tmp:rw,noexec,nosuid,nodev,size=8m,mode=1777",
				"--memory", runnerProperties.memoryLimit(),
				"--memory-swap", runnerProperties.memoryLimit(),
				"--cpus", runnerProperties.cpuLimit(),
				"--pids-limit", Integer.toString(runnerProperties.processLimit()),
				"--ulimit", "nofile=64:64",
				"--ulimit", "fsize=16777216:16777216",
				"--cap-drop", "ALL",
				"--security-opt", "no-new-privileges",
				"--user", "65534:65534",
				"--env", "CLASS_NAME=" + className,
				"--env", "PROGRAM_TIMEOUT_SECONDS=" + runnerProperties.programTimeoutSeconds(),
				// --mount instead of --volume: a Windows path's drive-letter colon ("C:\...") would
				// otherwise be confused with --volume's own ":" separators
				"--mount", bindMountReadOnly(submissionDirectory.toAbsolutePath().toString(), "/submission"),
				runnerProperties.image()));
		return command;
	}

	/**
	 * A read-only bind mount in Docker's --mount format. Docker reads that value as CSV, so a field
	 * containing a comma or quote is quoted (quotes doubled).
	 */
	static String bindMountReadOnly(String hostPath, String containerPath) {
		String sourceField = "source=" + hostPath;
		if (sourceField.contains(",") || sourceField.contains("\"")) {
			sourceField = "\"" + sourceField.replace("\"", "\"\"") + "\"";
		}
		return "type=bind," + sourceField + ",target=" + containerPath + ",readonly";
	}

	private Path writeSubmissionFiles(String className, String sourceCode, String standardInput) throws IOException {
		Path submissionDirectory = Files.createTempDirectory("code-run-");
		setPermissionsWhereSupported(submissionDirectory, "rwxr-xr-x");
		Path sourceFile = submissionDirectory.resolve(className + ".java");
		Files.writeString(sourceFile, sourceCode, StandardCharsets.UTF_8);
		setPermissionsWhereSupported(sourceFile, "rw-r--r--");
		Path inputFile = submissionDirectory.resolve("input.txt");
		Files.writeString(inputFile, standardInput, StandardCharsets.UTF_8);
		setPermissionsWhereSupported(inputFile, "rw-r--r--");
		return submissionDirectory;
	}

	/**
	 * On Linux and macOS the container's unprivileged user needs read access to the submission
	 * files. Windows has no POSIX permissions; there Docker Desktop makes the mounted files readable.
	 */
	private static void setPermissionsWhereSupported(Path path, String permissions) throws IOException {
		try {
			Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(permissions));
		} catch (UnsupportedOperationException windowsFileSystem) {
			// Nothing to set on Windows.
		}
	}

	private record CappedOutput(String text, boolean truncated) {
	}

	/** Keeps the first maximumOutputBytes bytes and keeps draining the rest so the process never blocks. */
	private CappedOutput readCapped(InputStream inputStream) {
		int maximumOutputBytes = runnerProperties.maximumOutputBytes();
		ByteArrayOutputStream keptBytes = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		boolean truncated = false;
		try (inputStream) {
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				int spaceLeft = maximumOutputBytes - keptBytes.size();
				if (bytesRead > spaceLeft) {
					truncated = true;
				}
				if (spaceLeft > 0) {
					keptBytes.write(buffer, 0, Math.min(bytesRead, spaceLeft));
				}
			}
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
		return new CappedOutput(keptBytes.toString(StandardCharsets.UTF_8), truncated);
	}

	private void killContainer(String containerName) {
		try {
			Process killProcess = new ProcessBuilder(runnerProperties.dockerCommand(), "rm", "--force", containerName)
					.redirectErrorStream(true)
					.redirectOutput(ProcessBuilder.Redirect.DISCARD)
					.start();
			if (!killProcess.waitFor(10, TimeUnit.SECONDS)) {
				killProcess.destroyForcibly();
			}
		} catch (IOException exception) {
			logger.warn("Could not remove sandbox container after a timeout: {}", exception.getClass().getSimpleName());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}

	private static void deleteQuietly(Path directory) {
		if (directory == null) {
			return;
		}
		try (var paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
		} catch (IOException exception) {
			logger.warn("Could not delete a temporary submission directory");
		}
	}
}
