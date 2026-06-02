package ch.policyflow.resource;

import ch.policyflow.domain.enums.OfferStatus;
import ch.policyflow.dto.request.CreateOfferRequest;
import ch.policyflow.dto.response.OfferResponse;
import ch.policyflow.dto.response.PolicyResponse;
import ch.policyflow.pdf.PdfService;
import ch.policyflow.service.OfferService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Endpoints for insurance offers, including acceptance, rejection and PDF export.
 */
@Path("/api/offers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "ADVISOR"})
public class OfferResource {

    @Inject
    OfferService offerService;

    @Inject
    PdfService pdfService;

    /**
     * Lists offers, optionally filtered by status.
     *
     * @param status optional status filter (e.g. {@code PENDING})
     * @return matching offers
     */
    @GET
    public List<OfferResponse> list(@QueryParam("status") OfferStatus status) {
        return offerService.list(status).stream().map(OfferResponse::from).toList();
    }

    /**
     * Returns a single offer.
     *
     * @param id the offer id
     * @return the offer (404 if not found)
     */
    @GET
    @Path("/{id}")
    public OfferResponse get(@PathParam("id") Long id) {
        return OfferResponse.from(offerService.get(id));
    }

    /**
     * Creates an offer (premium calculated server-side).
     *
     * @param request the offer payload
     * @return HTTP 201 with the created offer
     */
    @POST
    public Response create(@Valid CreateOfferRequest request) {
        OfferResponse created = OfferResponse.from(offerService.create(request));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Accepts an offer, issuing a policy.
     *
     * @param id the offer id
     * @return the newly issued policy
     */
    @PUT
    @Path("/{id}/accept")
    public PolicyResponse accept(@PathParam("id") Long id) {
        return PolicyResponse.from(offerService.accept(id));
    }

    /**
     * Rejects an offer.
     *
     * @param id the offer id
     * @return the rejected offer
     */
    @PUT
    @Path("/{id}/reject")
    public OfferResponse reject(@PathParam("id") Long id) {
        return OfferResponse.from(offerService.reject(id));
    }

    /**
     * Downloads the offer as a PDF.
     *
     * @param id the offer id
     * @return a PDF attachment
     */
    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    public Response pdf(@PathParam("id") Long id) {
        byte[] pdf = pdfService.generateOfferPdf(id);
        return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=\"offer-" + id + ".pdf\"")
                .build();
    }
}
