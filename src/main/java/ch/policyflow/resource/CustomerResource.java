package ch.policyflow.resource;

import ch.policyflow.dto.request.CreateCustomerRequest;
import ch.policyflow.dto.request.UpdateCustomerRequest;
import ch.policyflow.dto.response.CustomerResponse;
import ch.policyflow.service.CustomerService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
 * CRUD endpoints for customers. All endpoints require an ADMIN or ADVISOR role.
 */
@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "ADVISOR"})
public class CustomerResource {

    @Inject
    CustomerService customerService;

    /**
     * Lists customers, optionally filtered by a search term.
     *
     * @param search optional name/email search term
     * @return matching customers
     */
    @GET
    public List<CustomerResponse> list(@QueryParam("search") String search) {
        return customerService.list(search).stream().map(CustomerResponse::from).toList();
    }

    /**
     * Returns a single customer.
     *
     * @param id the customer id
     * @return the customer (404 if not found)
     */
    @GET
    @Path("/{id}")
    public CustomerResponse get(@PathParam("id") Long id) {
        return CustomerResponse.from(customerService.get(id));
    }

    /**
     * Creates a customer.
     *
     * @param request the creation payload
     * @return HTTP 201 with the created customer
     */
    @POST
    public Response create(@Valid CreateCustomerRequest request) {
        CustomerResponse created = CustomerResponse.from(customerService.create(request));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Updates a customer.
     *
     * @param id      the customer id
     * @param request the update payload
     * @return the updated customer
     */
    @PUT
    @Path("/{id}")
    public CustomerResponse update(@PathParam("id") Long id, @Valid UpdateCustomerRequest request) {
        return CustomerResponse.from(customerService.update(id, request));
    }

    /**
     * Soft-deletes a customer.
     *
     * @param id the customer id
     * @return HTTP 204 on success (400 if the customer has active policies)
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        customerService.delete(id);
        return Response.noContent().build();
    }
}
