package ch.policyflow.domain.entity;

import ch.policyflow.domain.enums.Canton;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A broker client (natural person) for whom offers and policies are managed.
 *
 * <p>Modelled with the Panache active-record pattern: persistence operations and
 * domain-specific finders live directly on the entity.</p>
 */
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    public String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    public String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @Column(name = "phone", length = 50)
    public String phone;

    @Column(name = "date_of_birth", nullable = false)
    public LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "canton", nullable = false, length = 2)
    public Canton canton;

    @Column(name = "address", length = 255)
    public String address;

    @Column(name = "city", length = 100)
    public String city;

    @Column(name = "zip", length = 10)
    public String zip;

    /** Soft-delete flag; inactive customers are hidden from default listings. */
    @Column(name = "active", nullable = false)
    public boolean active = true;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** @return the customer's current age in completed years. */
    public int getAge() {
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Finds a customer by their unique email address.
     *
     * @param email the email to look up
     * @return the matching customer, or {@code null} if none exists
     */
    public static Customer findByEmail(String email) {
        return find("email", email).firstResult();
    }

    /**
     * Lists all active customers domiciled in the given canton.
     *
     * @param canton the canton to filter by
     * @return matching active customers
     */
    public static List<Customer> findByCanton(Canton canton) {
        return list("canton = ?1 and active = true", canton);
    }

    /**
     * Case-insensitive search over first name, last name and email.
     *
     * @param query the search term
     * @return active customers matching the query, ordered by last name
     */
    public static List<Customer> searchByName(String query) {
        String pattern = "%" + query.toLowerCase() + "%";
        return list("active = true and (lower(firstName) like ?1 or lower(lastName) like ?1 or lower(email) like ?1)"
                + " order by lastName", pattern);
    }

    /** @return all active customers, ordered by last name. */
    public static List<Customer> findAllActive() {
        return list("active = true order by lastName");
    }
}
