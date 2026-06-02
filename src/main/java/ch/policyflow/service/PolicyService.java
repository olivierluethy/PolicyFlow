package ch.policyflow.service;

import ch.policyflow.domain.entity.AuditLog;
import ch.policyflow.domain.entity.Offer;
import ch.policyflow.domain.entity.Policy;
import ch.policyflow.domain.enums.PolicyStatus;
import ch.policyflow.exception.BusinessException;
import ch.policyflow.exception.ResourceNotFoundException;
import ch.policyflow.security.CurrentUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for issued insurance policies.
 */
@ApplicationScoped
public class PolicyService {

    @Inject
    CurrentUser currentUser;

    /**
     * Lists policies, optionally filtered by status.
     *
     * @param status the status to filter by, or {@code null} for all policies
     * @return matching policies, newest first
     */
    public List<Policy> list(PolicyStatus status) {
        return status == null ? Policy.findAllOrdered() : Policy.findByStatus(status);
    }

    /**
     * Loads a single policy by id.
     *
     * @param id the policy id
     * @return the policy
     * @throws ResourceNotFoundException if it does not exist
     */
    public Policy get(Long id) {
        Policy policy = Policy.findById(id);
        if (policy == null) {
            throw new ResourceNotFoundException("Policy", id);
        }
        return policy;
    }

    /**
     * Lists all policies belonging to a customer.
     *
     * @param customerId the customer id
     * @return the customer's policies
     */
    public List<Policy> getByCustomer(Long customerId) {
        return Policy.findByCustomer(customerId);
    }

    /**
     * Lists the active policies belonging to a customer.
     *
     * @param customerId the customer id
     * @return the customer's active policies
     */
    public List<Policy> getActiveByCustomer(Long customerId) {
        return Policy.list("customer.id = ?1 and status = ?2", customerId, PolicyStatus.ACTIVE);
    }

    /**
     * Issues a new policy from an accepted offer. Generates a unique policy number and
     * copies the agreed premium onto the contract.
     *
     * @param offer the accepted offer
     * @return the newly issued policy
     * @throws BusinessException if a policy already exists for this offer
     */
    @Transactional
    public Policy createFromOffer(Offer offer) {
        if (Policy.count("offer.id = ?1", offer.id) > 0) {
            throw new BusinessException("A policy already exists for offer " + offer.id);
        }
        Policy policy = new Policy();
        policy.offer = offer;
        policy.customer = offer.customer;
        policy.provider = offer.provider;
        policy.policyNumber = Policy.generatePolicyNumber();
        policy.status = PolicyStatus.ACTIVE;
        policy.startDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
        policy.monthlyPremium = offer.monthlyPremium;
        policy.persist();

        AuditLog.log("Policy", policy.id, "CREATE",
                "Issued policy " + policy.policyNumber + " for "
                        + offer.customer.firstName + " " + offer.customer.lastName,
                currentUser.username());
        return policy;
    }

    /**
     * Cancels an active policy.
     *
     * @param id     the policy id
     * @param reason the cancellation reason (recorded in the audit trail)
     * @return the cancelled policy
     * @throws ResourceNotFoundException if the policy does not exist
     * @throws BusinessException         if the policy is not active
     */
    @Transactional
    public Policy cancel(Long id, String reason) {
        Policy policy = get(id);
        if (policy.status != PolicyStatus.ACTIVE) {
            throw new BusinessException("Only active policies can be cancelled (current status: "
                    + policy.status + ")");
        }
        policy.status = PolicyStatus.CANCELLED;
        policy.endDate = LocalDate.now();

        AuditLog.log("Policy", policy.id, "CANCEL",
                "Cancelled policy " + policy.policyNumber + ": " + reason, currentUser.username());
        return policy;
    }
}
