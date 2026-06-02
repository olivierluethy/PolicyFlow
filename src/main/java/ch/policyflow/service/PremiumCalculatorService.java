package ch.policyflow.service;

import ch.policyflow.domain.entity.InsuranceProvider;
import ch.policyflow.domain.enums.AgeGroup;
import ch.policyflow.domain.enums.Canton;
import ch.policyflow.domain.enums.Franchise;
import ch.policyflow.dto.request.PremiumCalculationRequest;
import ch.policyflow.dto.request.PremiumCompareRequest;
import ch.policyflow.dto.response.CalculationResult;
import ch.policyflow.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates Swiss mandatory health-insurance premiums.
 *
 * <p>The model is a multiplicative one — the provider's base monthly premium is
 * successively scaled by four independent factors:</p>
 *
 * <ol>
 *   <li><b>Age factor</b> – children (≤18) pay 0.55×, young adults (19–25) pay 0.85×,
 *       adults (26+) pay the full 1.00×.</li>
 *   <li><b>Regional factor</b> – each canton has a cost multiplier (e.g. Geneva 1.25×,
 *       Uri 0.95×) reflecting local healthcare costs.</li>
 *   <li><b>Franchise (deductible) factor</b> – a higher deductible lowers the premium;
 *       CHF 300 keeps 100% of the premium while CHF 2500 reduces it to 68%.</li>
 *   <li><b>Accident surcharge</b> – including accident cover (Unfalleinschluss) adds 8%.</li>
 * </ol>
 *
 * <p>The core {@link #calculate(Canton, int, int, boolean, BigDecimal)} method is
 * pure (no I/O), which keeps it trivially unit-testable.</p>
 */
@ApplicationScoped
public class PremiumCalculatorService {

    /** Multiplier applied when accident cover is included. */
    private static final BigDecimal ACCIDENT_SURCHARGE = new BigDecimal("1.08");
    private static final BigDecimal NO_SURCHARGE = BigDecimal.ONE;

    /**
     * Calculates a single premium from explicit inputs. This method performs no
     * database access and is the unit under test for {@code PremiumCalculatorServiceTest}.
     *
     * @param canton             the canton of residence
     * @param age                the insured age in years (must be ≥ 0)
     * @param franchise          the deductible amount (300/500/1000/1500/2000/2500)
     * @param unfalleinschluss   whether accident cover is included
     * @param providerBasePremium the provider's base monthly premium
     * @return a full {@link CalculationResult} breakdown
     * @throws IllegalArgumentException if the age is negative or the franchise is invalid
     */
    public CalculationResult calculate(Canton canton, int age, int franchise,
                                       boolean unfalleinschluss, BigDecimal providerBasePremium) {
        // 1. Determine the statutory age group (throws on negative age).
        AgeGroup ageGroup = AgeGroup.fromAge(age);

        // 2. Resolve the chosen deductible level (throws on invalid amount).
        Franchise franchiseLevel = Franchise.fromAmount(franchise);

        // 3. Gather the four pricing factors.
        BigDecimal ageFactor = BigDecimal.valueOf(ageGroup.getAgeFactor());
        BigDecimal regionalFactor = BigDecimal.valueOf(canton.getRegionalFactor());
        BigDecimal franchiseFactor = BigDecimal.valueOf(franchiseLevel.getDiscountFactor());
        BigDecimal accidentSurcharge = unfalleinschluss ? ACCIDENT_SURCHARGE : NO_SURCHARGE;

        // 4. Apply them multiplicatively and round the monthly premium to 2 decimals.
        BigDecimal monthly = providerBasePremium
                .multiply(ageFactor)
                .multiply(regionalFactor)
                .multiply(franchiseFactor)
                .multiply(accidentSurcharge)
                .setScale(2, RoundingMode.HALF_UP);

        // 5. Derive the yearly premium.
        BigDecimal yearly = monthly.multiply(BigDecimal.valueOf(12)).setScale(2, RoundingMode.HALF_UP);

        return new CalculationResult(
                null, null,
                canton.name(),
                age,
                ageGroup.getLabel(),
                ageGroup.getAgeFactor(),
                canton.getRegionalFactor(),
                franchise,
                franchiseLevel.getDiscountFactor(),
                unfalleinschluss,
                accidentSurcharge.doubleValue(),
                providerBasePremium,
                monthly,
                yearly);
    }

    /**
     * Calculates the premium at every available deductible level for the same
     * canton/age/accident inputs.
     *
     * @param canton             the canton of residence
     * @param age                the insured age in years
     * @param unfalleinschluss   whether accident cover is included
     * @param providerBasePremium the provider's base monthly premium
     * @return one {@link CalculationResult} per franchise level, cheapest deductible first
     */
    public List<CalculationResult> compareAllFranchises(Canton canton, int age,
                                                        boolean unfalleinschluss, BigDecimal providerBasePremium) {
        List<CalculationResult> results = new ArrayList<>();
        for (Franchise f : Franchise.values()) {
            results.add(calculate(canton, age, f.getAmount(), unfalleinschluss, providerBasePremium));
        }
        return results;
    }

    /**
     * Calculates a single premium for an API request, resolving the provider's base
     * premium from the database and enriching the result with provider details.
     *
     * @param request the calculation inputs
     * @return the calculation result, including provider code and name
     * @throws ResourceNotFoundException if the provider does not exist
     */
    public CalculationResult calculate(PremiumCalculationRequest request) {
        InsuranceProvider provider = loadProvider(request.providerId());
        CalculationResult core = calculate(request.canton(), request.age(),
                request.franchise(), request.unfalleinschluss(), provider.basePremium);
        return withProvider(core, provider);
    }

    /**
     * Compares all deductible levels for an API request, resolving the provider.
     *
     * @param request the comparison inputs
     * @return one result per franchise level, enriched with provider details
     * @throws ResourceNotFoundException if the provider does not exist
     */
    public List<CalculationResult> compare(PremiumCompareRequest request) {
        InsuranceProvider provider = loadProvider(request.providerId());
        List<CalculationResult> core = compareAllFranchises(request.canton(), request.age(),
                request.unfalleinschluss(), provider.basePremium);
        return core.stream().map(r -> withProvider(r, provider)).toList();
    }

    private InsuranceProvider loadProvider(Long providerId) {
        InsuranceProvider provider = InsuranceProvider.findById(providerId);
        if (provider == null) {
            throw new ResourceNotFoundException("InsuranceProvider", providerId);
        }
        return provider;
    }

    private CalculationResult withProvider(CalculationResult r, InsuranceProvider p) {
        return new CalculationResult(
                p.code, p.name, r.canton(), r.age(), r.ageGroup(), r.ageFactor(),
                r.regionalFactor(), r.franchise(), r.franchiseFactor(), r.unfalleinschluss(),
                r.accidentSurcharge(), r.basePremium(), r.monthlyPremium(), r.yearlyPremium());
    }
}
