package ch.policyflow.dto.response;

import ch.policyflow.domain.entity.Offer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * API representation of an {@link Offer}, with denormalised customer and provider
 * names for convenient display in the UI.
 */
public record OfferResponse(
        Long id,
        Long customerId,
        String customerName,
        Long providerId,
        String providerName,
        String canton,
        int age,
        int franchise,
        boolean unfalleinschluss,
        BigDecimal monthlyPremium,
        BigDecimal yearlyPremium,
        String status,
        LocalDate validUntil,
        String notes,
        LocalDateTime createdAt) {

    /**
     * Maps an {@link Offer} entity to its API representation.
     *
     * @param o the entity
     * @return the response DTO
     */
    public static OfferResponse from(Offer o) {
        return new OfferResponse(
                o.id,
                o.customer.id,
                o.customer.firstName + " " + o.customer.lastName,
                o.provider.id,
                o.provider.name,
                o.canton.name(),
                o.age,
                o.franchise,
                o.unfalleinschluss,
                o.monthlyPremium,
                o.yearlyPremium,
                o.status.name(),
                o.validUntil,
                o.notes,
                o.createdAt);
    }
}
