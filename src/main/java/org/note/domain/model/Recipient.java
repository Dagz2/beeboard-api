package org.note.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Value Object representing a user with whom a Note has been explicitly shared.
 *
 * <p><strong>DDD Pattern:</strong> This is a Value Object that encapsulates the concept
 * of sharing a note with a specific user. It combines the recipient's identity with the
 * temporal aspect of when the sharing occurred.
 *
 * <p><strong>Business Context:</strong> Recipients are relevant only for notes with
 * {@link NoteVisibility#SHARED} visibility. They represent users who have been granted
 * explicit access to a note that would otherwise be private.
 *
 * <p><strong>Structure:</strong>
 * <ul>
 *   <li>{@code userId}: The identifier of the user who received access (references User from Access BC)</li>
 *   <li>{@code sharedAt}: The timestamp when the note was shared with this user</li>
 * </ul>
 *
 * <p><strong>Custom Equality:</strong> Unlike typical value objects that compare all fields,
 * Recipients are considered equal if they have the same {@code userId}, regardless of when
 * the note was shared. This prevents duplicate recipients and allows Set-based storage while
 * supporting resharing scenarios (see {@link #equals(Object)} for details).
 *
 * <p><strong>Usage Pattern:</strong>
 * <pre>
 * // Share a note with a user
 * Recipient recipient = Recipient.now(userId);
 * note.shareWith(recipient);
 *
 * // Check if note is shared with a user
 * boolean isShared = note.isSharedWith(userId);
 * </pre>
 */
public record Recipient(AuthorId userId, Instant sharedAt) {

    /**
     * Canonical record constructor with validation.
     *
     * @param userId the identifier of the user receiving access (must not be null)
     * @param sharedAt the timestamp when the note was shared (must not be null)
     * @throws NullPointerException if either parameter is null
     */
    public Recipient {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(sharedAt, "Shared timestamp cannot be null");
    }

    /**
     * Factory method to create a new Recipient with the current timestamp.
     *
     * <p>This is the typical way to create recipients when sharing a note, using
     * the current moment as the sharing timestamp.
     *
     * @param userId the identifier of the user to share with (must not be null)
     * @return a new Recipient instance with {@code sharedAt} set to {@code Instant.now()}
     * @throws NullPointerException if userId is null
     */
    public static Recipient now(AuthorId userId) {
        return new Recipient(userId, Instant.now());
    }

    /**
     * Two Recipients are considered equal if they have the same userId.
     *
     * <p><strong>Custom Equality Rationale:</strong> This deviates from typical Value Object
     * equality (which compares all fields) for important business reasons:
     * <ul>
     *   <li>Prevents duplicate recipients in the Set&lt;Recipient&gt; collection</li>
     *   <li>Allows checking "is user X a recipient?" without knowing the exact sharing timestamp</li>
     *   <li>Supports resharing: if you share with the same user again, it updates the timestamp
     *       rather than creating a duplicate entry</li>
     * </ul>
     *
     * <p>The {@code sharedAt} timestamp is maintained for audit purposes but does not
     * affect equality or identity from the business perspective.
     *
     * @param o the object to compare with
     * @return true if both recipients have the same userId, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipient recipient)) return false;
        return userId.equals(recipient.userId);
    }

    /**
     * Returns a hash code based solely on the userId.
     *
     * <p>This is consistent with the custom {@link #equals(Object)} implementation,
     * allowing Recipients to be used in hash-based collections like HashSet while
     * ensuring that recipients with the same userId but different timestamps are
     * treated as duplicates.
     *
     * @return hash code based on the user identifier
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    /**
     * Returns a string representation of this Recipient for debugging purposes.
     *
     * <p>Format: {@code Recipient{userId=..., sharedAt=...}}
     *
     * @return a string representation containing both userId and sharing timestamp
     */
    @Override
    public String toString() {
        return "Recipient{userId=" + userId + ", sharedAt=" + sharedAt + "}";
    }
}
