package ch.policyflow.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Base class for all Panache entities.
 *
 * <p>It mirrors {@code PanacheEntity} but pins the primary-key strategy to
 * {@link GenerationType#IDENTITY}, which matches the {@code BIGSERIAL} identity
 * columns created by the Flyway migrations (the schema is owned by Flyway, not by
 * Hibernate). All active-record helpers (persist, findById, list, count, …) are
 * inherited from {@link PanacheEntityBase}.</p>
 */
@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
}
