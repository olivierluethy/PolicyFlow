package ch.policyflow.dto.request;

import ch.policyflow.domain.enums.Canton;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Inputs for a single premium calculation.
 *
 * @param canton           the canton used for pricing (required)
 * @param age              the insured age, 0–120 (required)
 * @param franchise        the deductible amount (required)
 * @param unfalleinschluss whether accident cover is included
 * @param providerId       the provider whose base premium is used (required)
 */
public record PremiumCalculationRequest(
        @NotNull(message = "canton is required") Canton canton,
        @NotNull(message = "age is required") @Min(value = 0, message = "age must be >= 0") @Max(value = 120, message = "age must be <= 120") Integer age,
        @NotNull(message = "franchise is required") Integer franchise,
        boolean unfalleinschluss,
        @NotNull(message = "providerId is required") @Positive Long providerId) {
}
