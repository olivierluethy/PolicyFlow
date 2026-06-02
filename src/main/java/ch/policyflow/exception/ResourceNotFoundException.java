package ch.policyflow.exception;

/**
 * Thrown when a requested entity does not exist. Mapped to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param message a human-readable description of what was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience constructor producing a message like {@code "Customer with id 5 not found"}.
     *
     * @param entity the entity type name
     * @param id     the missing id
     */
    public ResourceNotFoundException(String entity, Long id) {
        super(entity + " with id " + id + " not found");
    }
}
