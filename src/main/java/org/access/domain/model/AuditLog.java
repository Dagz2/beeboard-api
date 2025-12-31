package org.access.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable entity representing an audit log entry.
 *
 * Audit logs track all security-sensitive operations in the system.
 * Once created, an audit log entry cannot be modified or deleted.
 */
public class AuditLog {

    private final AuditLogId id;
    private final Instant occurredAt;
    private final UserId actorId;
    private final String actorEmail;
    private final SecurityTier actorTier;
    private final String action;
    private final String targetType;
    private final String targetId;
    private final String oldValue;
    private final String newValue;
    private final String ipAddress;
    private final String userAgent;

    /**
     * Creates a new audit log entry.
     */
    public AuditLog(AuditLogId id, Instant occurredAt, UserId actorId, String actorEmail,
                    SecurityTier actorTier, String action, String targetType, String targetId,
                    String oldValue, String newValue, String ipAddress, String userAgent) {
        this.id = Objects.requireNonNull(id, "Audit log ID cannot be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
        this.actorId = Objects.requireNonNull(actorId, "Actor ID cannot be null");
        this.actorEmail = Objects.requireNonNull(actorEmail, "Actor email cannot be null");
        this.actorTier = Objects.requireNonNull(actorTier, "Actor tier cannot be null");
        this.action = Objects.requireNonNull(action, "Action cannot be null");
        this.targetType = Objects.requireNonNull(targetType, "Target type cannot be null");
        this.targetId = targetId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    /**
     * Factory method to create a new audit log entry.
     */
    public static AuditLog create(UserId actorId, String actorEmail, SecurityTier actorTier,
                                   String action, String targetType, String targetId,
                                   String oldValue, String newValue, String ipAddress, String userAgent) {
        return new AuditLog(
            AuditLogId.newId(),
            Instant.now(),
            actorId,
            actorEmail,
            actorTier,
            action,
            targetType,
            targetId,
            oldValue,
            newValue,
            ipAddress,
            userAgent
        );
    }

    // -------------------- Getters --------------------

    public AuditLogId id() {
        return id;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    public UserId actorId() {
        return actorId;
    }

    public String actorEmail() {
        return actorEmail;
    }

    public SecurityTier actorTier() {
        return actorTier;
    }

    public String action() {
        return action;
    }

    public String targetType() {
        return targetType;
    }

    public String targetId() {
        return targetId;
    }

    public String oldValue() {
        return oldValue;
    }

    public String newValue() {
        return newValue;
    }

    public String ipAddress() {
        return ipAddress;
    }

    public String userAgent() {
        return userAgent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLog other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "AuditLog[" + action + " by " + actorEmail + " at " + occurredAt + "]";
    }
}
