package ilep.codesubmission.error;

/** The single error shape used by every endpoint. */
public record ApiErrorResponse(String error, String message) {
}
