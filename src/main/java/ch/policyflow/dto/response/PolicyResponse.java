package ch.policyflow.dto.response;

import ch.policyflow.domain.entity.Policy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * API representation of a {@link Policy}.
 */
public record PolicyResponse(
        Long id,
        Long offerId,
        Long customerId,
        String customerName,
        Long providerId,
        String providerName,
        String policyNumber,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlyPremium,
        BigDecimal yearlyPremium,
        LocalDateTime createdAt) {

    /**
     * Maps a {@link Policy} entity to its API representation.
     *
     * @param p the entity
     * @return the response DTO
     */
    public static PolicyResponse from(Policy p) {
        return new PolicyResponse(
                p.id,
                p.offer.id,
                p.customer.id,
                p.customer.firstName + " " + p.customer.lastName,
                p.provider.id,
                p.provider.name,
                p.policyNumber,
                p.status.name(),
                p.startDate,
                p.endDate,
                p.monthlyPremium,
                p.monthlyPremium.multiply(BigDecimal.valueOf(12)),
                p.createdAt);
    }
}
