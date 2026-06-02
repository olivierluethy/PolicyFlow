package ch.policyflow.service;

import ch.policyflow.domain.entity.AuditLog;
import ch.policyflow.domain.entity.User;
import ch.policyflow.dto.request.LoginRequest;
import ch.policyflow.dto.response.LoginResponse;
import ch.policyflow.security.TokenService;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;

/**
 * Handles credential verification and JWT issuance.
 */
@ApplicationScoped
public class AuthService {

    @Inject
    TokenService tokenService;

    /**
     * Verifies the supplied credentials against the stored BCrypt hash and, on success,
     * issues a signed JWT.
     *
     * @param request the login credentials
     * @return a {@link LoginResponse} containing the token and user details
     * @throws NotAuthorizedException if the username is unknown or the password is wrong
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = User.findByUsername(request.username());
        if (user == null || !BcryptUtil.matches(request.password(), user.passwordHash)) {
            // Same response for unknown user and wrong password — avoids user enumeration.
            throw new NotAuthorizedException("Invalid username or password");
        }

        String token = tokenService.generateToken(user);
        AuditLog.log("User", user.id, "LOGIN", "User '" + user.username + "' logged in", user.username);
        return new LoginResponse(token, user.username, user.role.name(),
                user.fullName == null ? user.username : user.fullName);
    }
}
