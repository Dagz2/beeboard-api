package org.access.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing the unique identifier of an audit log entry.
 */
public class AuditLogId {

    private final UUID id;

    public AuditLogId(UUID id) {
        this.id = Objects.requireNonNull(id, "Audit log ID cannot be null");
    }

    public static AuditLogId newId() {
        return new AuditLogId(UUID.randomUUID());
    }

    public UUID id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLogId other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "AuditLogId[" + id + "]";
    }
}
