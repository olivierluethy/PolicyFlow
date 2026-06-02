package ch.policyflow.resource;

import ch.policyflow.domain.entity.AuditLog;
import ch.policyflow.dto.response.AuditLogResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Read-only access to the audit trail.
 */
@Path("/api/audit")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "ADVISOR"})
public class AuditResource {

    private static final int DEFAULT_LIMIT = 50;

    /**
     * Returns the most recent audit entries.
     *
     * @return the last 50 audit entries, newest first
     */
    @GET
    public List<AuditLogResponse> recent() {
        return AuditLog.findRecent(DEFAULT_LIMIT).stream().map(AuditLogResponse::from).toList();
    }

    /**
     * Returns the audit trail for a specific entity.
     *
     * @param type the entity type (e.g. {@code Customer})
     * @param id   the entity id
     * @return matching audit entries, newest first
     */
    @GET
    @Path("/entity/{type}/{id}")
    public List<AuditLogResponse> forEntity(@PathParam("type") String type, @PathParam("id") Long id) {
        return AuditLog.findForEntity(type, id).stream().map(AuditLogResponse::from).toList();
    }
}
