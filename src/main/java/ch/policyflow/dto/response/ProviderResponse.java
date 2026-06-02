package ch.policyflow.dto.response;

import ch.policyflow.domain.entity.InsuranceProvider;
import java.math.BigDecimal;

/**
 * API representation of an {@link InsuranceProvider}.
 */
public record ProviderResponse(
        Long id,
        String code,
        String name,
        String logoUrl,
        BigDecimal basePremium,
        boolean active) {

    /**
     * Maps an {@link InsuranceProvider} entity to its API representation.
     *
     * @param p the entity
     * @return the response DTO
     */
    public static ProviderResponse from(InsuranceProvider p) {
        return new ProviderResponse(p.id, p.code, p.name, p.logoUrl, p.basePremium, p.active);
    }
}
