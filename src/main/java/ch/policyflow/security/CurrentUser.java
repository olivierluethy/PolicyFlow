package ch.policyflow.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Resolves the username of the currently authenticated principal for audit logging.
 * Falls back to {@code "system"} when no user is associated with the call (for example
 * a scheduled background job).
 */
@ApplicationScoped
public class CurrentUser {

    @Inject
    SecurityIdentity identity;

    /**
     * @return the authenticated username, or {@code "system"} if the call is anonymous
     */
    public String username() {
        if (identity == null || identity.isAnonymous() || identity.getPrincipal() == null) {
            return "system";
        }
        String name = identity.getPrincipal().getName();
        return (name == null || name.isBlank()) ? "system" : name;
    }
}
