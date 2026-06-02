package ch.policyflow.domain.entity;

import ch.policyflow.domain.enums.Canton;
import ch.policyflow.domain.enums.OfferStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A non-binding insurance quote calculated for a customer and provider.
 *
 * <p>An offer captures the calculation inputs (canton, age, deductible, accident
 * cover) and the resulting monthly / yearly premium. When accepted it is converted
 * into a {@link Policy}.</p>
 */
@Entity
@Table(name = "offers")
public class Offer extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    public InsuranceProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "canton", nullable = false, length = 2)
    public Canton canton;

    @Column(name = "age", nullable = false)
    public int age;

    @Column(name = "franchise", nullable = false)
    public int franchise;

    @Column(name = "unfalleinschluss", nullable = false)
    public boolean unfalleinschluss = false;

    @Column(name = "monthly_premium", nullable = false, precision = 10, scale = 2)
    public BigDecimal monthlyPremium;

    @Column(name = "yearly_premium", nullable = false, precision = 10, scale = 2)
    public BigDecimal yearlyPremium;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public OfferStatus status = OfferStatus.PENDING;

    @Column(name = "valid_until")
    public LocalDate validUntil;

    @Column(name = "notes", columnDefinition = "TEXT")
    public String notes;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.validUntil == null) {
            this.validUntil = LocalDate.now().plusDays(30);
        }
    }

    /**
     * Lists all offers for a customer, newest first.
     *
     * @param customerId the customer id
     * @return the customer's offers
     */
    public static List<Offer> findByCustomer(Long customerId) {
        return list("customer.id = ?1 order by createdAt desc", customerId);
    }

    /**
     * Lists offers with the given status, newest first.
     *
     * @param status the status to filter by
     * @return matching offers
     */
    public static List<Offer> findByStatus(OfferStatus status) {
        return list("status = ?1 order by createdAt desc", status);
    }

    /** @return all pending offers, newest first. */
    public static List<Offer> findPending() {
        return findByStatus(OfferStatus.PENDING);
    }

    /** @return all offers, newest first. */
    public static List<Offer> findAllOrdered() {
        return listAll(io.quarkus.panache.common.Sort.descending("createdAt"));
    }
}
