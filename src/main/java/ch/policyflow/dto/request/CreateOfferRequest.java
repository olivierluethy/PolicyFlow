package ch.policyflow.dto.request;

import ch.policyflow.domain.enums.Canton;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Payload for creating an offer. The monthly and yearly premiums are not supplied
 * by the client — they are computed server-side by the premium calculator.
 *
 * @param customerId       the target customer (required)
 * @param providerId       the insurance provider (required)
 * @param canton           the canton used for pricing (required)
 * @param age              the insured age, 0–120 (required)
 * @param franchise        the deductible amount: 300/500/1000/1500/2000/2500 (required)
 * @param unfalleinschluss whether accident cover is included
 * @param notes            free-text notes (optional)
 */
public record CreateOfferRequest(
        @NotNull(message = "customerId is required") @Positive Long customerId,
        @NotNull(message = "providerId is required") @Positive Long providerId,
        @NotNull(message = "canton is required") Canton canton,
        @NotNull(message = "age is required") @Min(value = 0, message = "age must be >= 0") @Max(value = 120, message = "age must be <= 120") Integer age,
        @NotNull(message = "franchise is required") Integer franchise,
        boolean unfalleinschluss,
        String notes) {
}
