package ch.policyflow.exception;

/**
 * Thrown when a business rule is violated (e.g. deleting a customer who still has
 * active policies, or accepting an offer that is not pending). Mapped to HTTP 400.
 */
public class BusinessException extends RuntimeException {

    /**
     * @param message a human-readable explanation of the violated rule
     */
    public BusinessException(String message) {
        super(message);
    }
}
