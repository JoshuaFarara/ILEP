package ilep.codesubmission.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;

/**
 * Turns every failure into {@link ApiErrorResponse}. Never includes request content in the
 * response or the log, because request bodies can carry Class I data such as an Eagle ID.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
		return ResponseEntity.status(exception.getHttpStatus())
				.body(new ApiErrorResponse(exception.getErrorCode(), exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleValidationFailure(MethodArgumentNotValidException exception) {
		String fieldNames = exception.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField())
				.distinct()
				.sorted()
				.reduce((first, second) -> first + ", " + second)
				.orElse("request");
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
				.body(new ApiErrorResponse("validation_failed", "Invalid or missing field(s): " + fieldNames));
	}

	@ExceptionHandler({ HandlerMethodValidationException.class, ConstraintViolationException.class })
	ResponseEntity<ApiErrorResponse> handleParameterValidationFailure(Exception exception) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
				.body(new ApiErrorResponse("validation_failed", "Invalid path or query parameter."));
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	ResponseEntity<ApiErrorResponse> handleUnreadableRequest(Exception exception) {
		return ResponseEntity.badRequest()
				.body(new ApiErrorResponse("invalid_request", "The request could not be read."));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<ApiErrorResponse> handleUnsupportedMethod(HttpRequestMethodNotSupportedException exception) {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
				.body(new ApiErrorResponse("method_not_allowed", "This method is not supported here."));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiErrorResponse("not_found", "Not found."));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
		// Log the exception type and stack only; exception messages from lower layers are not trusted
		// to be free of submitted content.
		logger.error("Unexpected error of type {}", exception.getClass().getName(), exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiErrorResponse("internal_error", "Something went wrong. Please try again."));
	}
}
