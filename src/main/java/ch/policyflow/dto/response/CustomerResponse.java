package ch.policyflow.dto.response;

import ch.policyflow.domain.entity.Customer;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * API representation of a {@link Customer}.
 */
public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        int age,
        String canton,
        String cantonName,
        String address,
        String city,
        String zip,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /**
     * Maps a {@link Customer} entity to its API representation.
     *
     * @param c the entity
     * @return the response DTO
     */
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(
                c.id,
                c.firstName,
                c.lastName,
                c.email,
                c.phone,
                c.dateOfBirth,
                c.getAge(),
                c.canton.name(),
                c.canton.getFullName(),
                c.address,
                c.city,
                c.zip,
                c.createdAt,
                c.updatedAt);
    }
}
