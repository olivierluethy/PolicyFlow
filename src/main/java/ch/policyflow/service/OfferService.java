package ch.policyflow.service;

import ch.policyflow.domain.entity.AuditLog;
import ch.policyflow.domain.entity.Customer;
import ch.policyflow.domain.entity.InsuranceProvider;
import ch.policyflow.domain.entity.Offer;
import ch.policyflow.domain.entity.Policy;
import ch.policyflow.domain.enums.OfferStatus;
import ch.policyflow.dto.request.CreateOfferRequest;
import ch.policyflow.dto.response.CalculationResult;
import ch.policyflow.exception.BusinessException;
import ch.policyflow.exception.ResourceNotFoundException;
import ch.policyflow.security.CurrentUser;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Business logic for insurance offers, including premium calculation on creation,
 * acceptance (which issues a policy), rejection and scheduled expiry.
 */
@ApplicationScoped
public class OfferService {

    private static final Logger LOG = Logger.getLogger(OfferService.class);

    @Inject
    PremiumCalculatorService calculator;

    @Inject
    PolicyService policyService;

    @Inject
    CurrentUser currentUser;

    /**
     * Lists offers, optionally filtered by status.
     *
     * @param status the status to filter by, or {@code null} for all offers
     * @return matching offers, newest first
     */
    public List<Offer> list(OfferStatus status) {
        return status == null ? Offer.findAllOrdered() : Offer.findByStatus(status);
    }

    /**
     * Loads a single offer by id.
     *
     * @param id the offer id
     * @return the offer
     * @throws ResourceNotFoundException if it does not exist
     */
    public Offer get(Long id) {
        Offer offer = Offer.findById(id);
        if (offer == null) {
            throw new ResourceNotFoundException("Offer", id);
        }
        return offer;
    }

    /**
     * Creates an offer, calculating its premium from the request inputs and the
     * selected provider's base premium.
     *
     * @param request the validated offer payload
     * @return the persisted offer
     * @throws ResourceNotFoundException if the customer or provider does not exist
     */
    @Transactional
    public Offer create(CreateOfferRequest request) {
        Customer customer = Customer.findById(request.customerId());
        if (customer == null || !customer.active) {
            throw new ResourceNotFoundException("Customer", request.customerId());
        }
        InsuranceProvider provider = InsuranceProvider.findById(request.providerId());
        if (provider == null) {
            throw new ResourceNotFoundException("InsuranceProvider", request.providerId());
        }

        // Premium is always computed server-side — never trusted from the client.
        CalculationResult result = calculator.calculate(
                request.canton(), request.age(), request.franchise(),
                request.unfalleinschluss(), provider.basePremium);

        Offer offer = new Offer();
        offer.customer = customer;
        offer.provider = provider;
        offer.canton = request.canton();
        offer.age = request.age();
        offer.franchise = request.franchise();
        offer.unfalleinschluss = request.unfalleinschluss();
        offer.monthlyPremium = result.monthlyPremium();
        offer.yearlyPremium = result.yearlyPremium();
        offer.status = OfferStatus.PENDING;
        offer.notes = request.notes();
        offer.persist();

        AuditLog.log("Offer", offer.id, "CREATE",
                "Created offer for " + customer.firstName + " " + customer.lastName
                        + " (" + provider.name + ", CHF " + result.monthlyPremium() + "/month)",
                currentUser.username());
        return offer;
    }

    /**
     * Accepts a pending offer and issues a policy from it.
     *
     * @param id the offer id
     * @return the resulting policy
     * @throws ResourceNotFoundException if the offer does not exist
     * @throws BusinessException         if the offer is not in {@code PENDING} state
     */
    @Transactional
    public Policy accept(Long id) {
        Offer offer = get(id);
        if (offer.status != OfferStatus.PENDING) {
            throw new BusinessException("Only pending offers can be accepted (current status: "
                    + offer.status + ")");
        }
        offer.status = OfferStatus.ACCEPTED;
        Policy policy = policyService.createFromOffer(offer);

        AuditLog.log("Offer", offer.id, "ACCEPT",
                "Offer accepted; issued policy " + policy.policyNumber, currentUser.username());
        return policy;
    }

    /**
     * Rejects a pending offer.
     *
     * @param id the offer id
     * @return the rejected offer
     * @throws ResourceNotFoundException if the offer does not exist
     * @throws BusinessException         if the offer is not in {@code PENDING} state
     */
    @Transactional
    public Offer reject(Long id) {
        Offer offer = get(id);
        if (offer.status != OfferStatus.PENDING) {
            throw new BusinessException("Only pending offers can be rejected (current status: "
                    + offer.status + ")");
        }
        offer.status = OfferStatus.REJECTED;
        AuditLog.log("Offer", offer.id, "REJECT", "Offer rejected", currentUser.username());
        return offer;
    }

    /**
     * Scheduled daily job that expires pending offers whose validity has lapsed.
     * Runs every day at 02:00.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    void expireOldOffers() {
        List<Offer> stale = Offer.list("status = ?1 and validUntil < ?2",
                OfferStatus.PENDING, LocalDate.now());
        for (Offer offer : stale) {
            offer.status = OfferStatus.EXPIRED;
            AuditLog.log("Offer", offer.id, "EXPIRE",
                    "Offer expired (valid until " + offer.validUntil + ")", "system");
        }
        if (!stale.isEmpty()) {
            LOG.infof("Expired %d stale offer(s)", stale.size());
        }
    }
}
