package org.access.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Value Object carrying audit information for an entity:
 * - createdAt: timestamp when the entity was created
 * - updatedAt: timestamp when the entity was last modified
 * (Not a record because we need to modify it)
 */
public final class AuditInfo {
    private final Instant createdAt;
    private Instant updatedAt;

    // -------------------- Creation -------------------- 

    /**
     * Private constructor. Use {@link #of(Instant)} to create a new instance.
     *
     * @param createdAt initial creation timestamp
     * @param updatedAt initial update timestamp
     */
    private AuditInfo(Instant createdAt, Instant updatedAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null.");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null.");
    }

    /**
     * Factory method to create a fresh AuditInfo, setting both createdAt and updatedAt to the provided timestamp.
     *
     * @param now the timestamp to use for both creation and update
     * @return a new AuditInfo instance
     * @throws NullPointerException if now is null
     */
    public static AuditInfo of(Instant now) {
        return new AuditInfo(now, now);
    }

    // -------------------- Domain methods -------------------- 

    /**
     * Touches the audit info, updating the updatedAt timestamp to the current time.
     */
    public void touch() {
        this.updatedAt = Instant.now();
    }

    // -------------------- Getters -------------------- 

    /**
     * Retrieve the creation timestamp.
     *
     * @return the Instant when the entity was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Retrieve the last update timestamp.
     *
     * @return the Instant when the entity was last updated
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    // -------------------- Overrides -------------------- 

    /**
     * Two AuditInfo instances are considered equal if their createdAt and updatedAt timestamps are equal.
     *
     * @param o the object to compare with
     * @return true if both AuditInfo instances have equal timestamps; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditInfo)) return false;
        AuditInfo that = (AuditInfo) o;
        return createdAt.equals(that.createdAt) && updatedAt.equals(that.updatedAt);
    }

    /**
     * Computes the hash code based solely on createdAt and updatedAt to maintain consistency with equals().
     *
     * @return hash code derived from createdAt and updatedAt
     */
    @Override
    public int hashCode() {
        return Objects.hash(createdAt, updatedAt);
    }

    /**
     * Returns a string representation of the AuditInfo, including creation and update timestamps.
     *
     * @return string representation of this AuditInfo
     */
    @Override
    public String toString() {
        return "AuditInfo{createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }
}