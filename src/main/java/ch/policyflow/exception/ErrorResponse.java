package ch.policyflow.exception;

import java.util.List;

/**
 * Standard JSON error envelope returned for every API error, so clients never
 * receive plain-text or HTML error pages.
 *
 * @param error      a short error category/message
 * @param violations field-level validation messages (empty unless a validation error)
 * @param status     the HTTP status code
 */
public record ErrorResponse(String error, List<String> violations, int status) {

    /**
     * Builds an error envelope without field violations.
     *
     * @param error  the error message
     * @param status the HTTP status code
     * @return the error response
     */
    public static ErrorResponse of(String error, int status) {
        return new ErrorResponse(error, List.of(), status);
    }
}
