package ch.policyflow.domain.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;

/**
 * A Swiss health-insurance provider (e.g. CSS, Helsana, SWICA).
 *
 * <p>The {@code basePremium} is the provider's reference monthly premium for a
 * standard adult in a neutral canton; the {@code PremiumCalculatorService} adjusts
 * it by age, canton and deductible to produce a concrete quote.</p>
 */
@Entity
@Table(name = "insurance_providers")
public class InsuranceProvider extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 20)
    public String code;

    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "logo_url", length = 255)
    public String logoUrl;

    @Column(name = "base_premium", nullable = false, precision = 10, scale = 2)
    public BigDecimal basePremium;

    @Column(name = "active", nullable = false)
    public boolean active = true;

    /**
     * Finds a provider by its short code (e.g. {@code "CSS"}).
     *
     * @param code the provider code
     * @return the matching provider, or {@code null} if none exists
     */
    public static InsuranceProvider findByCode(String code) {
        return find("code", code).firstResult();
    }

    /** @return all active providers, ordered by name. */
    public static List<InsuranceProvider> findAllActive() {
        return list("active = true order by name");
    }
}
