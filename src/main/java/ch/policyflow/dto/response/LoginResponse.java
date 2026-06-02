package ch.policyflow.dto.response;

/**
 * Response returned after a successful login.
 *
 * @param token    the signed JWT bearer token
 * @param username the authenticated username
 * @param role     the user's role
 * @param fullName the user's display name
 */
public record LoginResponse(String token, String username, String role, String fullName) {
}
