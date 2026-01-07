package org.note.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Value Object carrying audit information for tracking entity lifecycle timestamps.
 *
 * <p><strong>DDD Pattern:</strong> This is a Value Object that encapsulates temporal
 * audit data for domain entities. Unlike typical immutable value objects, AuditInfo
 * is intentionally mutable to support efficient timestamp updates via the {@link #touch()}
 * method without requiring complete entity reconstruction.
 *
 * <p><strong>Mutability Justification:</strong> While Value Objects are typically immutable,
 * AuditInfo breaks this rule for pragmatic reasons:
 * <ul>
 *   <li>Timestamps change frequently with every entity modification</li>
 *   <li>Immutability would require creating new AuditInfo instances for every update</li>
 *   <li>This would cascade to entity reconstruction, hurting performance</li>
 *   <li>The mutability is controlled and limited to the {@code touch()} method</li>
 * </ul>
 *
 * <p><strong>Structure:</strong>
 * <ul>
 *   <li>{@code createdAt}: Immutable timestamp of entity creation</li>
 *   <li>{@code updatedAt}: Mutable timestamp of last modification</li>
 * </ul>
 *
 * <p><strong>Usage Pattern:</strong>
 * <pre>
 * // At creation
 * AuditInfo audit = AuditInfo.of(Instant.now());
 *
 * // After modification
 * entity.getAuditInfo().touch(); // Updates updatedAt
 * </pre>
 */
public final class AuditInfo {

    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Private constructor to enforce use of factory methods.
     *
     * @param createdAt the creation timestamp (must not be null, immutable)
     * @param updatedAt the last modification timestamp (must not be null, mutable via touch())
     * @throws NullPointerException if either timestamp is null
     */
    public AuditInfo(Instant createdAt, Instant updatedAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    /**
     * Factory method to create a fresh AuditInfo for a newly created entity.
     *
     * <p>Both {@code createdAt} and {@code updatedAt} are initialized to the same
     * timestamp, reflecting that the entity has just been created and not yet modified.
     *
     * <p>Typical usage:
     * <pre>
     * Note note = Note.create(...);
     * AuditInfo audit = AuditInfo.of(Instant.now()); // At entity creation
     * </pre>
     *
     * @param now the current timestamp to use for both creation and update times
     * @return a new AuditInfo instance with synchronized timestamps
     * @throws NullPointerException if now is null
     */
    public static AuditInfo of(Instant now) {
        return new AuditInfo(now, now);
    }

    /**
     * Updates the {@code updatedAt} timestamp to the current time.
     *
     * <p>This method is called whenever the owning entity is modified through any
     * domain operation. It provides a simple mechanism to track when an entity was
     * last changed without manually managing timestamps.
     *
     * <p><strong>Mutability Note:</strong> This is the only mutating operation on AuditInfo.
     * The {@code createdAt} timestamp remains immutable throughout the entity's lifecycle.
     *
     * <p>Typical usage:
     * <pre>
     * note.updateTitle(newTitle);
     * note.getAuditInfo().touch(); // Automatically updates updatedAt
     * </pre>
     */
    public void touch() {
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Two AuditInfo instances are equal if both timestamps match.
     *
     * <p>This follows the standard Value Object equality pattern where equality
     * is based on structural comparison of all attributes rather than identity.
     *
     * <p><strong>Note:</strong> Because {@code updatedAt} is mutable, equality can
     * change over the object's lifetime when {@link #touch()} is called. This is
     * acceptable because AuditInfo is not typically used in hash-based collections.
     *
     * @param o the object to compare with
     * @return true if both timestamps are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditInfo that)) return false;
        return createdAt.equals(that.createdAt) && updatedAt.equals(that.updatedAt);
    }

    /**
     * Returns a hash code based on both timestamps.
     *
     * <p><strong>Warning:</strong> Because {@code updatedAt} is mutable, the hash code
     * can change when {@link #touch()} is called. Therefore, AuditInfo instances should
     * NOT be used as keys in hash-based collections (HashMap, HashSet) as this would
     * violate the hash code contract.
     *
     * @return hash code based on both creation and update timestamps
     */
    @Override
    public int hashCode() {
        return Objects.hash(createdAt, updatedAt);
    }

    /**
     * Returns a string representation of this AuditInfo for debugging purposes.
     *
     * <p>Format: {@code AuditInfo{createdAt=..., updatedAt=...}}
     *
     * @return a string representation containing both timestamps
     */
    @Override
    public String toString() {
        return "AuditInfo{createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }
}
