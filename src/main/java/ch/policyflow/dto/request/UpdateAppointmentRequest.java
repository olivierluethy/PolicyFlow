package ch.policyflow.dto.request;

import ch.policyflow.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Payload for updating an existing appointment, including its status.
 *
 * @param title           short title (required)
 * @param description     longer description (optional)
 * @param appointmentDate the scheduled date and time (required)
 * @param status          the appointment status (required)
 */
public record UpdateAppointmentRequest(
        @NotBlank(message = "title is required") String title,
        String description,
        @NotNull(message = "appointmentDate is required") LocalDateTime appointmentDate,
        @NotNull(message = "status is required") AppointmentStatus status) {
}
