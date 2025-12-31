package org.access.infrastructure.mapper;

import org.access.domain.model.*;
import org.access.infrastructure.entity.AuditLogEntity;

import java.util.Objects;
import java.util.UUID;

/**
 * Mapper responsible for converting between {@link AuditLogEntity} (infrastructure)
 * and {@link AuditLog} (domain model).
 *
 * Note: This mapper only supports toDomain and toEntity for append operations.
 * Audit logs are immutable and should never be updated.
 */
public class AuditLogMapper {

    /**
     * Converts a JPA {@link AuditLogEntity} to a domain {@link AuditLog}.
     *
     * @param entity the AuditLogEntity from the persistence layer
     * @return the domain AuditLog object
     * @throws NullPointerException if the input entity is null
     */
    public static AuditLog toDomain(AuditLogEntity entity) {
        Objects.requireNonNull(entity, "AuditLogEntity cannot be null");

        return new AuditLog(
            new AuditLogId(entity.getId()),
            entity.getOccurredAt(),
            new UserId(entity.getActorId()),
            entity.getActorEmail(),
            SecurityTier.valueOf(entity.getActorTier()),
            entity.getAction(),
            entity.getTargetType(),
            entity.getTargetId() != null ? entity.getTargetId().toString() : null,
            entity.getOldValue(),
            entity.getNewValue(),
            entity.getIpAddress(),
            entity.getUserAgent()
        );
    }

    /**
     * Converts a domain {@link AuditLog} to a JPA {@link AuditLogEntity}.
     *
     * @param auditLog the domain audit log
     * @return the infrastructure-level AuditLogEntity
     * @throws NullPointerException if the input auditLog is null
     */
    public static AuditLogEntity toEntity(AuditLog auditLog) {
        Objects.requireNonNull(auditLog, "AuditLog cannot be null");

        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(auditLog.id().id());
        entity.setOccurredAt(auditLog.occurredAt());
        entity.setActorId(auditLog.actorId().id());
        entity.setActorEmail(auditLog.actorEmail());
        entity.setActorTier(auditLog.actorTier().name());
        entity.setAction(auditLog.action());
        entity.setTargetType(auditLog.targetType());
        entity.setTargetId(auditLog.targetId() != null ? UUID.fromString(auditLog.targetId()) : null);
        entity.setOldValue(auditLog.oldValue());
        entity.setNewValue(auditLog.newValue());
        entity.setIpAddress(auditLog.ipAddress());
        entity.setUserAgent(auditLog.userAgent());

        return entity;
    }
}
