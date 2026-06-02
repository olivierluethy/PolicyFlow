package ch.policyflow.resource;

import ch.policyflow.dto.request.LoginRequest;
import ch.policyflow.dto.response.LoginResponse;
import ch.policyflow.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Authentication endpoints. This is the only resource that does not require a token.
 */
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    /**
     * Authenticates a user and returns a signed JWT.
     *
     * @param request the login credentials
     * @return the token and user details
     */
    @POST
    @Path("/login")
    @PermitAll
    public LoginResponse login(@Valid LoginRequest request) {
        return authService.login(request);
    }
}
