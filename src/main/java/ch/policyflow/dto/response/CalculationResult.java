package ch.policyflow.dto.response;

import java.math.BigDecimal;

/**
 * The full, transparent breakdown of a premium calculation. Every factor that fed
 * into the final figure is exposed so advisors can explain the quote to customers.
 *
 * @param providerCode     the provider's short code
 * @param providerName     the provider's display name
 * @param canton           the canton code used for pricing
 * @param age              the insured age
 * @param ageGroup         the resolved age-group label
 * @param ageFactor        the age multiplier applied
 * @param regionalFactor   the canton multiplier applied
 * @param franchise        the deductible amount
 * @param franchiseFactor  the deductible discount multiplier applied
 * @param unfalleinschluss whether accident cover is included
 * @param accidentSurcharge the surcharge multiplier applied (1.08 if accident cover, else 1.00)
 * @param basePremium      the provider's base monthly premium
 * @param monthlyPremium   the final monthly premium
 * @param yearlyPremium    the final yearly premium (monthly × 12)
 */
public record CalculationResult(
        String providerCode,
        String providerName,
        String canton,
        int age,
        String ageGroup,
        double ageFactor,
        double regionalFactor,
        int franchise,
        double franchiseFactor,
        boolean unfalleinschluss,
        double accidentSurcharge,
        BigDecimal basePremium,
        BigDecimal monthlyPremium,
        BigDecimal yearlyPremium) {
}
