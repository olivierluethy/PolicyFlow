package ch.policyflow.resource;

import ch.policyflow.domain.entity.InsuranceProvider;
import ch.policyflow.dto.response.ProviderResponse;
import ch.policyflow.exception.ResourceNotFoundException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Read-only endpoints for insurance providers.
 */
@Path("/api/providers")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "ADVISOR"})
public class ProviderResource {

    /**
     * Lists all active providers.
     *
     * @return active providers
     */
    @GET
    public List<ProviderResponse> list() {
        return InsuranceProvider.findAllActive().stream().map(ProviderResponse::from).toList();
    }

    /**
     * Returns a single provider.
     *
     * @param id the provider id
     * @return the provider (404 if not found)
     */
    @GET
    @Path("/{id}")
    public ProviderResponse get(@PathParam("id") Long id) {
        InsuranceProvider provider = InsuranceProvider.findById(id);
        if (provider == null) {
            throw new ResourceNotFoundException("InsuranceProvider", id);
        }
        return ProviderResponse.from(provider);
    }
}
