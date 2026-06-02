package ch.policyflow.security;

import ch.policyflow.domain.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Issues signed JSON Web Tokens for authenticated users.
 *
 * <p>The token's {@code groups} claim carries the user's role, which Quarkus maps to
 * roles for {@link jakarta.annotation.security.RolesAllowed} checks. Tokens are valid
 * for 24 hours.</p>
 */
@ApplicationScoped
public class TokenService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(24);

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    /**
     * Generates a signed JWT for the given user.
     *
     * @param user the authenticated user
     * @return a compact, signed JWT string
     */
    public String generateToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.username)
                .subject(user.username)
                .groups(Set.of(user.role.name()))
                .claim("fullName", user.fullName == null ? user.username : user.fullName)
                .claim("role", user.role.name())
                .expiresIn(TOKEN_VALIDITY)
                .sign();
    }
}
