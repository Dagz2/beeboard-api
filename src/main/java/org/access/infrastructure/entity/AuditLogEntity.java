package org.access.infrastructure.entity;

import java.time.Instant;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * JPA entity representing an immutable audit log entry.
 *
 * This entity maps to the audit_log table which has database triggers
 * preventing any UPDATE or DELETE operations.
 */
@Entity
@Table(name = "audit_log")
public class AuditLogEntity extends PanacheEntityBase {

    @Id
    @NotNull
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @NotNull
    @Column(name = "actor_id", nullable = false, updatable = false)
    private UUID actorId;

    @NotNull
    @Column(name = "actor_email", length = 255, nullable = false, updatable = false)
    private String actorEmail;

    @NotNull
    @Column(name = "actor_tier", length = 10, nullable = false, updatable = false)
    private String actorTier;

    @NotNull
    @Column(name = "action", length = 100, nullable = false, updatable = false)
    private String action;

    @NotNull
    @Column(name = "target_type", length = 50, nullable = false, updatable = false)
    private String targetType;

    @Column(name = "target_id", updatable = false)
    private UUID targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb", updatable = false)
    private String oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb", updatable = false)
    private String newValue;

    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT", updatable = false)
    private String userAgent;

    // -------------------- Getters & Setters --------------------

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getActorTier() {
        return actorTier;
    }

    public void setActorTier(String actorTier) {
        this.actorTier = actorTier;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
