package ch.policyflow.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Credentials submitted to {@code POST /api/auth/login}.
 *
 * @param username the account username
 * @param password the plaintext password (verified against the stored BCrypt hash)
 */
public record LoginRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "password is required") String password) {
}
