package ch.policyflow.dto.response;

import ch.policyflow.domain.entity.AuditLog;
import java.time.LocalDateTime;

/**
 * API representation of an {@link AuditLog} entry.
 */
public record AuditLogResponse(
        Long id,
        String entityType,
        Long entityId,
        String action,
        String description,
        String performedBy,
        LocalDateTime performedAt) {

    /**
     * Maps an {@link AuditLog} entity to its API representation.
     *
     * @param a the entity
     * @return the response DTO
     */
    public static AuditLogResponse from(AuditLog a) {
        return new AuditLogResponse(
                a.id, a.entityType, a.entityId, a.action, a.description, a.performedBy, a.performedAt);
    }
}
