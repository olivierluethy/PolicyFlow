package ch.policyflow.service;

import ch.policyflow.domain.entity.Appointment;
import ch.policyflow.domain.entity.AuditLog;
import ch.policyflow.domain.entity.Customer;
import ch.policyflow.dto.request.CreateAppointmentRequest;
import ch.policyflow.dto.request.UpdateAppointmentRequest;
import ch.policyflow.exception.ResourceNotFoundException;
import ch.policyflow.security.CurrentUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Business logic for customer appointments, with full audit logging.
 */
@ApplicationScoped
public class AppointmentService {

    /** Look-ahead window, in days, used by {@link #getUpcoming()}. */
    private static final int UPCOMING_WINDOW_DAYS = 7;

    @Inject
    CurrentUser currentUser;

    /** @return all appointments, soonest first. */
    public List<Appointment> list() {
        return Appointment.findAllOrdered();
    }

    /** @return scheduled appointments within the next 7 days. */
    public List<Appointment> getUpcoming() {
        return Appointment.findUpcoming(UPCOMING_WINDOW_DAYS);
    }

    /**
     * Loads a single appointment by id.
     *
     * @param id the appointment id
     * @return the appointment
     * @throws ResourceNotFoundException if it does not exist
     */
    public Appointment get(Long id) {
        Appointment appointment = Appointment.findById(id);
        if (appointment == null) {
            throw new ResourceNotFoundException("Appointment", id);
        }
        return appointment;
    }

    /**
     * Lists appointments for a customer.
     *
     * @param customerId the customer id
     * @return the customer's appointments
     */
    public List<Appointment> getByCustomer(Long customerId) {
        return Appointment.findByCustomer(customerId);
    }

    /**
     * Creates a new appointment.
     *
     * @param request the validated creation payload
     * @return the persisted appointment
     * @throws ResourceNotFoundException if the customer does not exist
     */
    @Transactional
    public Appointment create(CreateAppointmentRequest request) {
        Customer customer = Customer.findById(request.customerId());
        if (customer == null || !customer.active) {
            throw new ResourceNotFoundException("Customer", request.customerId());
        }
        Appointment appointment = new Appointment();
        appointment.customer = customer;
        appointment.title = request.title();
        appointment.description = request.description();
        appointment.appointmentDate = request.appointmentDate();
        appointment.persist();

        AuditLog.log("Appointment", appointment.id, "CREATE",
                "Scheduled appointment '" + appointment.title + "' with "
                        + customer.firstName + " " + customer.lastName,
                currentUser.username());
        return appointment;
    }

    /**
     * Updates an existing appointment.
     *
     * @param id      the appointment id
     * @param request the validated update payload
     * @return the updated appointment
     * @throws ResourceNotFoundException if the appointment does not exist
     */
    @Transactional
    public Appointment update(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = get(id);
        appointment.title = request.title();
        appointment.description = request.description();
        appointment.appointmentDate = request.appointmentDate();
        appointment.status = request.status();

        AuditLog.log("Appointment", appointment.id, "UPDATE",
                "Updated appointment '" + appointment.title + "'", currentUser.username());
        return appointment;
    }

    /**
     * Deletes an appointment.
     *
     * @param id the appointment id
     * @throws ResourceNotFoundException if the appointment does not exist
     */
    @Transactional
    public void delete(Long id) {
        Appointment appointment = get(id);
        String title = appointment.title;
        appointment.delete();
        AuditLog.log("Appointment", id, "DELETE",
                "Deleted appointment '" + title + "'", currentUser.username());
    }
}
