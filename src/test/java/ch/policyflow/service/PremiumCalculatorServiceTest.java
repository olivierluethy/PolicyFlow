package ch.policyflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.policyflow.domain.enums.AgeGroup;
import ch.policyflow.domain.enums.Canton;
import ch.policyflow.dto.response.CalculationResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for {@link PremiumCalculatorService}. No CDI or database is
 * involved — the calculation core is exercised directly with explicit inputs.
 */
class PremiumCalculatorServiceTest {

    private final PremiumCalculatorService service = new PremiumCalculatorService();
    private static final BigDecimal BASE = new BigDecimal("400.00");

    private BigDecimal expected(double base, double ageF, double regF, double franF, double accF) {
        return BigDecimal.valueOf(base * ageF * regF * franF * accF).setScale(2, RoundingMode.HALF_UP);
    }

    // --- Age-group boundaries --------------------------------------------------

    @Test
    @DisplayName("Age 18 resolves to KIND")
    void age18IsChild() {
        assertEquals(AgeGroup.KIND, AgeGroup.fromAge(18));
    }

    @Test
    @DisplayName("Age 19 resolves to JUNGER_ERWACHSENER")
    void age19IsYoungAdult() {
        assertEquals(AgeGroup.JUNGER_ERWACHSENER, AgeGroup.fromAge(19));
    }

    @Test
    @DisplayName("Age 25 resolves to JUNGER_ERWACHSENER")
    void age25IsYoungAdult() {
        assertEquals(AgeGroup.JUNGER_ERWACHSENER, AgeGroup.fromAge(25));
    }

    @Test
    @DisplayName("Age 26 resolves to ERWACHSENER")
    void age26IsAdult() {
        assertEquals(AgeGroup.ERWACHSENER, AgeGroup.fromAge(26));
    }

    @Test
    @DisplayName("Age 0 (newborn) resolves to KIND")
    void age0IsChild() {
        assertEquals(AgeGroup.KIND, AgeGroup.fromAge(0));
    }

    @Test
    @DisplayName("Negative age throws IllegalArgumentException")
    void negativeAgeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(Canton.LU, -1, 300, false, BASE));
    }

    // --- Franchise discount factors -------------------------------------------

    @Test
    @DisplayName("All six franchise levels apply their discount factor")
    void allFranchiseLevels() {
        int[] amounts = {300, 500, 1000, 1500, 2000, 2500};
        double[] factors = {1.00, 0.95, 0.86, 0.79, 0.73, 0.68};
        for (int i = 0; i < amounts.length; i++) {
            CalculationResult r = service.calculate(Canton.SZ, 40, amounts[i], false, BASE);
            // SZ regional factor is 1.00, adult age factor 1.00, no accident -> only franchise applies.
            BigDecimal exp = expected(400.00, 1.00, 1.00, factors[i], 1.00);
            assertEquals(exp, r.monthlyPremium(),
                    "Franchise " + amounts[i] + " should yield " + exp);
        }
    }

    @Test
    @DisplayName("Invalid franchise amount throws IllegalArgumentException")
    void invalidFranchiseThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(Canton.LU, 40, 999, false, BASE));
    }

    // --- Accident surcharge ----------------------------------------------------

    @Test
    @DisplayName("Accident cover adds exactly 8%")
    void accidentSurchargeIs8Percent() {
        CalculationResult without = service.calculate(Canton.SZ, 40, 300, false, BASE);
        CalculationResult with = service.calculate(Canton.SZ, 40, 300, true, BASE);
        BigDecimal ratio = with.monthlyPremium()
                .divide(without.monthlyPremium(), 4, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("1.0800"), ratio);
    }

    // --- Regional factor -------------------------------------------------------

    @Test
    @DisplayName("Canton LU regional factor 1.02 is applied")
    void cantonLuFactor() {
        CalculationResult r = service.calculate(Canton.LU, 40, 300, false, BASE);
        assertEquals(expected(400.00, 1.00, 1.02, 1.00, 1.00), r.monthlyPremium());
        assertEquals(1.02, r.regionalFactor());
    }

    @Test
    @DisplayName("Canton GE regional factor 1.25 is applied")
    void cantonGeFactor() {
        CalculationResult r = service.calculate(Canton.GE, 40, 300, false, BASE);
        assertEquals(expected(400.00, 1.00, 1.25, 1.00, 1.00), r.monthlyPremium());
    }

    // --- Full end-to-end calculations -----------------------------------------

    @Test
    @DisplayName("Full calc: LU, age 32, franchise 1000, no accident")
    void fullCalcLu() {
        CalculationResult r = service.calculate(Canton.LU, 32, 1000, false, BASE);
        // 400 * 1.00 (adult) * 1.02 (LU) * 0.86 (1000) * 1.00 = 350.88
        assertEquals(new BigDecimal("350.88"), r.monthlyPremium());
        assertEquals(new BigDecimal("4210.56"), r.yearlyPremium());
    }

    @Test
    @DisplayName("Full calc: ZH, age 19, franchise 300, with accident")
    void fullCalcZh() {
        CalculationResult r = service.calculate(Canton.ZH, 19, 300, true, BASE);
        // 400 * 0.85 (young) * 1.18 (ZH) * 1.00 (300) * 1.08 (accident) = 433.30
        assertEquals(new BigDecimal("433.30"), r.monthlyPremium());
        assertEquals("Junger Erwachsener", r.ageGroup());
    }

    @Test
    @DisplayName("Child tariff applies 0.55 age factor")
    void childTariff() {
        CalculationResult r = service.calculate(Canton.SZ, 10, 300, false, BASE);
        assertEquals(expected(400.00, 0.55, 1.00, 1.00, 1.00), r.monthlyPremium());
        assertEquals("Kind", r.ageGroup());
    }

    @Test
    @DisplayName("Yearly premium is always monthly times twelve")
    void yearlyIsTwelveMonthly() {
        CalculationResult r = service.calculate(Canton.BE, 45, 500, true, BASE);
        assertEquals(r.monthlyPremium().multiply(BigDecimal.valueOf(12)), r.yearlyPremium());
    }

    @Test
    @DisplayName("Monthly premium is rounded to two decimal places")
    void roundedToTwoDecimals() {
        CalculationResult r = service.calculate(Canton.ZH, 19, 1500, true, BASE);
        assertEquals(2, r.monthlyPremium().scale());
    }

    // --- Comparison ------------------------------------------------------------

    @Test
    @DisplayName("compareAllFranchises returns six descending-price results")
    void compareReturnsSixOrdered() {
        List<CalculationResult> results =
                service.compareAllFranchises(Canton.LU, 40, false, BASE);
        assertEquals(6, results.size());
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i).monthlyPremium()
                            .compareTo(results.get(i - 1).monthlyPremium()) < 0,
                    "Higher franchise should be cheaper");
        }
    }
}
