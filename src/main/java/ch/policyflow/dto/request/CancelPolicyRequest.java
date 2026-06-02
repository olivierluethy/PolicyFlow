package ch.policyflow.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for cancelling a policy.
 *
 * @param reason the cancellation reason recorded in the audit trail (required)
 */
public record CancelPolicyRequest(
        @NotBlank(message = "reason is required") String reason) {
}
