package ch.policyflow.domain.entity;

import ch.policyflow.domain.enums.PolicyStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A binding insurance contract issued when a customer accepts an {@link Offer}.
 */
@Entity
@Table(name = "policies")
public class Policy extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "offer_id", nullable = false, unique = true)
    public Offer offer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    public InsuranceProvider provider;

    @Column(name = "policy_number", nullable = false, unique = true, length = 50)
    public String policyNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public PolicyStatus status = PolicyStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    @Column(name = "end_date")
    public LocalDate endDate;

    @Column(name = "monthly_premium", nullable = false, precision = 10, scale = 2)
    public BigDecimal monthlyPremium;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Generates a unique policy number in the format {@code HR-2024-XXXXXX},
     * where {@code XXXXXX} is a zero-padded random six-digit sequence.
     *
     * @return a freshly generated policy number
     */
    public static String generatePolicyNumber() {
        int year = LocalDate.now().getYear();
        String candidate;
        do {
            int seq = ThreadLocalRandom.current().nextInt(0, 1_000_000);
            candidate = String.format("HR-%d-%06d", year, seq);
        } while (findByPolicyNumber(candidate) != null);
        return candidate;
    }

    /**
     * Finds a policy by its unique policy number.
     *
     * @param policyNumber the policy number
     * @return the matching policy, or {@code null} if none exists
     */
    public static Policy findByPolicyNumber(String policyNumber) {
        return find("policyNumber", policyNumber).firstResult();
    }

    /**
     * Lists all policies for a customer, newest first.
     *
     * @param customerId the customer id
     * @return the customer's policies
     */
    public static List<Policy> findByCustomer(Long customerId) {
        return list("customer.id = ?1 order by createdAt desc", customerId);
    }

    /** @return all active policies, newest first. */
    public static List<Policy> findActive() {
        return list("status = ?1 order by createdAt desc", PolicyStatus.ACTIVE);
    }

    /**
     * Lists policies with the given status, newest first.
     *
     * @param status the status to filter by
     * @return matching policies
     */
    public static List<Policy> findByStatus(PolicyStatus status) {
        return list("status = ?1 order by createdAt desc", status);
    }

    /** @return all policies, newest first. */
    public static List<Policy> findAllOrdered() {
        return listAll(io.quarkus.panache.common.Sort.descending("createdAt"));
    }
}
