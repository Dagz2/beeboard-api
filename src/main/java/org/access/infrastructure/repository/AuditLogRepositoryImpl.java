package org.access.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;

import org.access.domain.model.AuditLog;
import org.access.domain.model.AuditLogId;
import org.access.domain.model.UserId;
import org.access.domain.repository.AuditLogRepository;
import org.access.infrastructure.entity.AuditLogEntity;
import org.access.infrastructure.mapper.AuditLogMapper;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA-based implementation of the {@link AuditLogRepository} interface using Panache.
 *
 * This repository only supports append operations. Update and delete operations
 * are intentionally not implemented to maintain audit integrity. The database
 * also has triggers preventing modification of audit logs.
 */
@ApplicationScoped
public class AuditLogRepositoryImpl implements AuditLogRepository, PanacheRepository<AuditLogEntity> {

    @Override
    public void append(AuditLog auditLog) {
        persist(AuditLogMapper.toEntity(auditLog));
    }

    @Override
    public Optional<AuditLog> findById(AuditLogId id) {
        return find("id", id.id())
            .firstResultOptional()
            .map(AuditLogMapper::toDomain);
    }

    @Override
    public List<AuditLog> findByActorId(UserId actorId) {
        return find("actorId", actorId.id()).stream()
            .map(AuditLogMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByAction(String action) {
        return find("action", action).stream()
            .map(AuditLogMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByTimeRange(Instant from, Instant to) {
        return find("occurredAt >= ?1 and occurredAt <= ?2", from, to).stream()
            .map(AuditLogMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByTarget(String targetType, String targetId) {
        UUID targetUuid = targetId != null ? UUID.fromString(targetId) : null;
        return find("targetType = ?1 and targetId = ?2", targetType, targetUuid).stream()
            .map(AuditLogMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findAll(int offset, int limit) {
        return find("ORDER BY occurredAt DESC")
            .page(offset / limit, limit)
            .stream()
            .map(AuditLogMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return PanacheRepository.super.count();
    }
}
