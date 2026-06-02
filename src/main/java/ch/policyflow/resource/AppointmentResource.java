package ch.policyflow.resource;

import ch.policyflow.dto.request.CreateAppointmentRequest;
import ch.policyflow.dto.request.UpdateAppointmentRequest;
import ch.policyflow.dto.response.AppointmentResponse;
import ch.policyflow.service.AppointmentService;
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
 * CRUD endpoints for customer appointments.
 */
@Path("/api/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "ADVISOR"})
public class AppointmentResource {

    @Inject
    AppointmentService appointmentService;

    /**
     * Lists appointments.
     *
     * @param upcoming if {@code true}, returns only appointments within the next 7 days
     * @return matching appointments
     */
    @GET
    public List<AppointmentResponse> list(@QueryParam("upcoming") boolean upcoming) {
        var appointments = upcoming ? appointmentService.getUpcoming() : appointmentService.list();
        return appointments.stream().map(AppointmentResponse::from).toList();
    }

    /**
     * Returns a single appointment.
     *
     * @param id the appointment id
     * @return the appointment (404 if not found)
     */
    @GET
    @Path("/{id}")
    public AppointmentResponse get(@PathParam("id") Long id) {
        return AppointmentResponse.from(appointmentService.get(id));
    }

    /**
     * Lists appointments for a customer.
     *
     * @param customerId the customer id
     * @return the customer's appointments
     */
    @GET
    @Path("/customer/{customerId}")
    public List<AppointmentResponse> byCustomer(@PathParam("customerId") Long customerId) {
        return appointmentService.getByCustomer(customerId).stream().map(AppointmentResponse::from).toList();
    }

    /**
     * Creates an appointment.
     *
     * @param request the creation payload
     * @return HTTP 201 with the created appointment
     */
    @POST
    public Response create(@Valid CreateAppointmentRequest request) {
        AppointmentResponse created = AppointmentResponse.from(appointmentService.create(request));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Updates an appointment.
     *
     * @param id      the appointment id
     * @param request the update payload
     * @return the updated appointment
     */
    @PUT
    @Path("/{id}")
    public AppointmentResponse update(@PathParam("id") Long id, @Valid UpdateAppointmentRequest request) {
        return AppointmentResponse.from(appointmentService.update(id, request));
    }

    /**
     * Deletes an appointment.
     *
     * @param id the appointment id
     * @return HTTP 204 on success
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        appointmentService.delete(id);
        return Response.noContent().build();
    }
}
