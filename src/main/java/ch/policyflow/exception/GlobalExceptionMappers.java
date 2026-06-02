package ch.policyflow.exception;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Central collection of JAX-RS {@link ExceptionMapper}s. Together they guarantee
 * that every error leaves the API as a JSON {@link ErrorResponse} with an
 * appropriate HTTP status — never an HTML page or a stack trace.
 */
public final class GlobalExceptionMappers {

    private GlobalExceptionMappers() {
    }

    /** Maps {@link ResourceNotFoundException} to HTTP 404. */
    @Provider
    public static class NotFoundMapper implements ExceptionMapper<ResourceNotFoundException> {
        @Override
        public Response toResponse(ResourceNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(ex.getMessage(), 404))
                    .build();
        }
    }

    /** Maps {@link BusinessException} to HTTP 400. */
    @Provider
    public static class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {
        @Override
        public Response toResponse(BusinessException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.of(ex.getMessage(), 400))
                    .build();
        }
    }

    /** Maps {@link IllegalArgumentException} (bad inputs) to HTTP 400. */
    @Provider
    public static class IllegalArgumentMapper implements ExceptionMapper<IllegalArgumentException> {
        @Override
        public Response toResponse(IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.of(ex.getMessage(), 400))
                    .build();
        }
    }

    /** Maps bean-validation failures to HTTP 400 with a per-field violation list. */
    @Provider
    public static class ValidationMapper implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException ex) {
            List<String> violations = ex.getConstraintViolations().stream()
                    .map(ValidationMapper::format)
                    .toList();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Validation failed", violations, 400))
                    .build();
        }

        private static String format(ConstraintViolation<?> v) {
            String path = v.getPropertyPath().toString();
            int dot = path.lastIndexOf('.');
            String field = dot >= 0 ? path.substring(dot + 1) : path;
            return field + ": " + v.getMessage();
        }
    }

    /** Maps authentication failures (missing/invalid token) to HTTP 401. */
    @Provider
    public static class AuthFailedMapper implements ExceptionMapper<AuthenticationFailedException> {
        @Override
        public Response toResponse(AuthenticationFailedException ex) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ErrorResponse.of("Authentication required", 401))
                    .build();
        }
    }

    /** Maps unauthenticated access to HTTP 401. */
    @Provider
    public static class UnauthorizedMapper implements ExceptionMapper<UnauthorizedException> {
        @Override
        public Response toResponse(UnauthorizedException ex) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ErrorResponse.of("Authentication required", 401))
                    .build();
        }
    }

    /** Maps insufficient role/permission to HTTP 403. */
    @Provider
    public static class ForbiddenMapper implements ExceptionMapper<ForbiddenException> {
        @Override
        public Response toResponse(ForbiddenException ex) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ErrorResponse.of("Access denied", 403))
                    .build();
        }
    }

    /** Last-resort mapper: logs the cause and returns a generic HTTP 500 JSON body. */
    @Provider
    public static class GenericMapper implements ExceptionMapper<Exception> {

        private static final Logger LOG = Logger.getLogger(GenericMapper.class);

        @Override
        public Response toResponse(Exception ex) {
            if (ex instanceof jakarta.ws.rs.WebApplicationException wae) {
                Response orig = wae.getResponse();
                return Response.status(orig.getStatus())
                        .entity(ErrorResponse.of(ex.getMessage() == null ? "Request failed" : ex.getMessage(),
                                orig.getStatus()))
                        .build();
            }
            LOG.error("Unhandled exception", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of("Internal server error", 500))
                    .build();
        }
    }
}
