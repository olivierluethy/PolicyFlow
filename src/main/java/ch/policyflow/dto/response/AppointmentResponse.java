package ch.policyflow.dto.response;

import ch.policyflow.domain.entity.Appointment;
import java.time.LocalDateTime;

/**
 * API representation of an {@link Appointment}.
 */
public record AppointmentResponse(
        Long id,
        Long customerId,
        String customerName,
        String title,
        String description,
        LocalDateTime appointmentDate,
        String status,
        LocalDateTime createdAt) {

    /**
     * Maps an {@link Appointment} entity to its API representation.
     *
     * @param a the entity
     * @return the response DTO
     */
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.id,
                a.customer.id,
                a.customer.firstName + " " + a.customer.lastName,
                a.title,
                a.description,
                a.appointmentDate,
                a.status.name(),
                a.createdAt);
    }
}
