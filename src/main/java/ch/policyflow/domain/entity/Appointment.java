package ch.policyflow.domain.entity;

import ch.policyflow.domain.enums.AppointmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A scheduled meeting between an advisor and a customer.
 */
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @Column(name = "title", nullable = false, length = 255)
    public String title;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "appointment_date", nullable = false)
    public LocalDateTime appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Lists all appointments for a customer, soonest first.
     *
     * @param customerId the customer id
     * @return the customer's appointments
     */
    public static List<Appointment> findByCustomer(Long customerId) {
        return list("customer.id = ?1 order by appointmentDate asc", customerId);
    }

    /**
     * Lists scheduled appointments occurring within the next {@code days} days.
     *
     * @param days the look-ahead window in days
     * @return upcoming appointments, soonest first
     */
    public static List<Appointment> findUpcoming(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime until = now.plusDays(days);
        return list("status = ?1 and appointmentDate between ?2 and ?3 order by appointmentDate asc",
                AppointmentStatus.SCHEDULED, now, until);
    }

    /**
     * Lists appointments with the given status, soonest first.
     *
     * @param status the status to filter by
     * @return matching appointments
     */
    public static List<Appointment> findByStatus(AppointmentStatus status) {
        return list("status = ?1 order by appointmentDate asc", status);
    }

    /** @return all appointments, soonest first. */
    public static List<Appointment> findAllOrdered() {
        return listAll(io.quarkus.panache.common.Sort.ascending("appointmentDate"));
    }
}
