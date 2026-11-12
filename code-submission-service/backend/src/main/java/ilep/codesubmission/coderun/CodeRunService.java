package ilep.codesubmission.coderun;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import ilep.codesubmission.configuration.CodeSubmissionProperties;
import ilep.codesubmission.error.ApiException;
import ilep.codesubmission.security.SecretHashing;
import ilep.codesubmission.studentsession.StudentWorkSessionService;

/**
 * Runs a student's code: saves it, runs it in the sandbox, and records the result.
 *
 * Limits: one run at a time per student, and at most {@code maximum-concurrent-runs} across the
 * whole service, so one class can't overload the machine. The database transaction is kept short
 * and is never held open while a program runs.
 */
@Service
public class CodeRunService {

	private static final long waitForFreeRunnerSeconds = 20;

	private final JavaCodeRunner javaCodeRunner;
	private final StudentWorkSessionService studentWorkSessionService;
	private final JdbcClient jdbcClient;
	private final TransactionTemplate transactionTemplate;
	private final CodeSubmissionProperties.Runner runnerProperties;
	private final Semaphore serviceWideRunPermits;
	private final Set<UUID> sessionsWithRunInProgress = ConcurrentHashMap.newKeySet();

	public CodeRunService(JavaCodeRunner javaCodeRunner, StudentWorkSessionService studentWorkSessionService,
			JdbcClient jdbcClient, TransactionTemplate transactionTemplate, CodeSubmissionProperties properties) {
		this.javaCodeRunner = javaCodeRunner;
		this.studentWorkSessionService = studentWorkSessionService;
		this.jdbcClient = jdbcClient;
		this.transactionTemplate = transactionTemplate;
		this.runnerProperties = properties.runner();
		this.serviceWideRunPermits = new Semaphore(runnerProperties.maximumConcurrentRuns(), true);
	}

	public CodeRunResult runCode(UUID studentWorkSessionId, String code, String standardInput) {
		String safeStandardInput = standardInput == null ? "" : standardInput;
		if (safeStandardInput.getBytes(StandardCharsets.UTF_8).length > runnerProperties.maximumStandardInputBytes()) {
			throw ApiException.validationFailed("Standard input is too long.");
		}
		// Saving first also checks that the session is editable (notice acknowledged, not submitted, not closed).
		transactionTemplate.executeWithoutResult(status -> studentWorkSessionService.saveCode(studentWorkSessionId, code));

		if (!sessionsWithRunInProgress.add(studentWorkSessionId)) {
			throw ApiException.conflict("Your previous run is still going. Wait for it to finish.");
		}
		try {
			if (!serviceWideRunPermits.tryAcquire(waitForFreeRunnerSeconds, TimeUnit.SECONDS)) {
				throw ApiException.rateLimited("The code runner is busy. Try again in a few seconds.");
			}
			try {
				CodeRunResult codeRunResult = javaCodeRunner.compileAndRun(code, safeStandardInput);
				recordRun(studentWorkSessionId, code, codeRunResult);
				return codeRunResult;
			} finally {
				serviceWideRunPermits.release();
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new CodeRunnerUnavailableException("The run was interrupted.");
		} finally {
			sessionsWithRunInProgress.remove(studentWorkSessionId);
		}
	}

	private void recordRun(UUID studentWorkSessionId, String code, CodeRunResult codeRunResult) {
		jdbcClient.sql("""
				insert into code_runs (student_work_session_id, code_sha256, compile_succeeded, exit_code, timed_out,
				  output_truncated, standard_output, standard_error, duration_milliseconds)
				values (:studentWorkSessionId, :codeSha256, :compileSucceeded, :exitCode, :timedOut,
				  :outputTruncated, :standardOutput, :standardError, :durationMilliseconds)
				""")
				.param("studentWorkSessionId", studentWorkSessionId)
				.param("codeSha256", SecretHashing.sha256(code))
				.param("compileSucceeded", codeRunResult.compileSucceeded())
				.param("exitCode", codeRunResult.exitCode())
				.param("timedOut", codeRunResult.timedOut())
				.param("outputTruncated", codeRunResult.outputTruncated())
				.param("standardOutput", removeNullCharacters(codeRunResult.standardOutput()))
				.param("standardError", removeNullCharacters(codeRunResult.standardError()))
				.param("durationMilliseconds", (int) Math.min(Integer.MAX_VALUE, codeRunResult.durationMilliseconds()))
				.update();
	}

	/** PostgreSQL text columns cannot store the NUL character, which a program can print. */
	private static String removeNullCharacters(String text) {
		return text.replace("\u0000", "");
	}
}
