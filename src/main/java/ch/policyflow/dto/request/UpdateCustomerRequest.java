package ch.policyflow.dto.request;

import ch.policyflow.domain.enums.Canton;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

/**
 * Payload for updating an existing customer. All mutable fields are supplied.
 *
 * @param firstName   given name (required)
 * @param lastName    family name (required)
 * @param email       unique email address (required, validated)
 * @param phone       phone number (optional)
 * @param dateOfBirth date of birth, must be in the past (required)
 * @param canton      canton of residence (required)
 * @param address     street address (optional)
 * @param city        city (optional)
 * @param zip         postal code (optional)
 */
public record UpdateCustomerRequest(
        @NotBlank(message = "firstName is required") String firstName,
        @NotBlank(message = "lastName is required") String lastName,
        @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
        String phone,
        @NotNull(message = "dateOfBirth is required") @Past(message = "dateOfBirth must be in the past") LocalDate dateOfBirth,
        @NotNull(message = "canton is required") Canton canton,
        String address,
        String city,
        String zip) {
}
