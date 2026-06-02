package ch.policyflow.resource;

import ch.policyflow.dto.response.DashboardStats;
import ch.policyflow.service.DashboardService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Dashboard aggregation endpoint.
 */
@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "ADVISOR"})
public class DashboardResource {

    @Inject
    DashboardService dashboardService;

    /**
     * Returns the dashboard key figures.
     *
     * @return aggregated statistics
     */
    @GET
    @Path("/stats")
    public DashboardStats stats() {
        return dashboardService.getStats();
    }
}
