package ch.policyflow.resource;

import ch.policyflow.domain.enums.PolicyStatus;
import ch.policyflow.dto.request.CancelPolicyRequest;
import ch.policyflow.dto.response.PolicyResponse;
import ch.policyflow.pdf.PdfService;
import ch.policyflow.service.PolicyService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Endpoints for issued policies, including cancellation and PDF export.
 */
@Path("/api/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "ADVISOR"})
public class PolicyResource {

    @Inject
    PolicyService policyService;

    @Inject
    PdfService pdfService;

    /**
     * Lists policies, optionally filtered by status.
     *
     * @param status optional status filter (e.g. {@code ACTIVE})
     * @return matching policies
     */
    @GET
    public List<PolicyResponse> list(@QueryParam("status") PolicyStatus status) {
        return policyService.list(status).stream().map(PolicyResponse::from).toList();
    }

    /**
     * Returns a single policy.
     *
     * @param id the policy id
     * @return the policy (404 if not found)
     */
    @GET
    @Path("/{id}")
    public PolicyResponse get(@PathParam("id") Long id) {
        return PolicyResponse.from(policyService.get(id));
    }

    /**
     * Lists all policies for a customer.
     *
     * @param customerId the customer id
     * @return the customer's policies
     */
    @GET
    @Path("/customer/{customerId}")
    public List<PolicyResponse> byCustomer(@PathParam("customerId") Long customerId) {
        return policyService.getByCustomer(customerId).stream().map(PolicyResponse::from).toList();
    }

    /**
     * Cancels a policy.
     *
     * @param id      the policy id
     * @param request the cancellation reason
     * @return the cancelled policy
     */
    @PUT
    @Path("/{id}/cancel")
    public PolicyResponse cancel(@PathParam("id") Long id, @Valid CancelPolicyRequest request) {
        return PolicyResponse.from(policyService.cancel(id, request.reason()));
    }

    /**
     * Downloads the policy as a PDF.
     *
     * @param id the policy id
     * @return a PDF attachment
     */
    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    public Response pdf(@PathParam("id") Long id) {
        byte[] pdf = pdfService.generatePolicyPdf(id);
        return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=\"policy-" + id + ".pdf\"")
                .build();
    }
}
