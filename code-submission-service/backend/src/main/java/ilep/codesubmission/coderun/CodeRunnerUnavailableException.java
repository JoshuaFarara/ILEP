package ilep.codesubmission.coderun;

import org.springframework.http.HttpStatus;

import ilep.codesubmission.error.ApiException;

/** The sandbox itself failed (not the student's program). Returned as 503. */
public class CodeRunnerUnavailableException extends ApiException {

	public CodeRunnerUnavailableException(String message) {
		super(HttpStatus.SERVICE_UNAVAILABLE, "runner_unavailable", message);
	}

	public CodeRunnerUnavailableException(String message, Throwable cause) {
		this(message);
		initCause(cause);
	}
}
