package ch.policyflow.domain.entity;


import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

/**
 * An immutable record of a business action performed in the system, used for
 * traceability and compliance. Every service mutation appends an entry here.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @Column(name = "entity_type", nullable = false, length = 50)
    public String entityType;

    @Column(name = "entity_id", nullable = false)
    public Long entityId;

    @Column(name = "action", nullable = false, length = 50)
    public String action;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    public String description;

    @Column(name = "performed_by", length = 100)
    public String performedBy;

    @Column(name = "performed_at", nullable = false)
    public LocalDateTime performedAt;

    @PrePersist
    void onCreate() {
        this.performedAt = LocalDateTime.now();
    }

    /**
     * Creates and persists an audit entry.
     *
     * @param entityType  the affected entity type (e.g. {@code "Customer"})
     * @param entityId    the affected entity id
     * @param action      the action performed ({@code CREATE}, {@code UPDATE}, {@code DELETE}, ...)
     * @param description a human-readable description of what happened
     * @param performedBy the username of the actor, or {@code null} if system-initiated
     * @return the persisted audit entry
     */
    public static AuditLog log(String entityType, Long entityId, String action,
                               String description, String performedBy) {
        AuditLog entry = new AuditLog();
        entry.entityType = entityType;
        entry.entityId = entityId;
        entry.action = action;
        entry.description = description;
        entry.performedBy = performedBy;
        entry.persist();
        return entry;
    }

    /**
     * Returns the most recent audit entries.
     *
     * @param limit the maximum number of entries to return
     * @return audit entries, newest first
     */
    public static List<AuditLog> findRecent(int limit) {
        return findAll(Sort.descending("performedAt")).page(0, limit).list();
    }

    /**
     * Returns the full audit trail for a specific entity.
     *
     * @param entityType the entity type
     * @param entityId   the entity id
     * @return matching audit entries, newest first
     */
    public static List<AuditLog> findForEntity(String entityType, Long entityId) {
        return list("entityType = ?1 and entityId = ?2 order by performedAt desc", entityType, entityId);
    }
}
