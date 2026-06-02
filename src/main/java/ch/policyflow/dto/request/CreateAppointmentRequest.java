package ch.policyflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * Payload for creating a customer appointment.
 *
 * @param customerId      the customer the appointment is with (required)
 * @param title           short title (required)
 * @param description     longer description (optional)
 * @param appointmentDate the scheduled date and time (required)
 */
public record CreateAppointmentRequest(
        @NotNull(message = "customerId is required") @Positive Long customerId,
        @NotBlank(message = "title is required") String title,
        String description,
        @NotNull(message = "appointmentDate is required") LocalDateTime appointmentDate) {
}
