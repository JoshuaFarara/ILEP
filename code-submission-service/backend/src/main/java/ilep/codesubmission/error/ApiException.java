package ilep.codesubmission.error;

import org.springframework.http.HttpStatus;

/**
 * An error returned to the client as {@code {"error": errorCode, "message": message}}.
 * Messages must never contain student identifiers or submitted content.
 */
public class ApiException extends RuntimeException {

	private final HttpStatus httpStatus;
	private final String errorCode;

	public ApiException(HttpStatus httpStatus, String errorCode, String message) {
		super(message);
		this.httpStatus = httpStatus;
		this.errorCode = errorCode;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public static ApiException notFound(String message) {
		return new ApiException(HttpStatus.NOT_FOUND, "not_found", message);
	}

	public static ApiException conflict(String message) {
		return new ApiException(HttpStatus.CONFLICT, "conflict", message);
	}

	public static ApiException validationFailed(String message) {
		return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "validation_failed", message);
	}

	public static ApiException unauthorized(String message) {
		return new ApiException(HttpStatus.UNAUTHORIZED, "unauthorized", message);
	}

	public static ApiException rateLimited(String message) {
		return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "rate_limited", message);
	}
}
