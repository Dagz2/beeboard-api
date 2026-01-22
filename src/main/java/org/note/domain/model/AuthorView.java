package org.note.domain.model;

import org.shared.domain.AccountStatus;

import java.util.Objects;
import java.util.UUID;

/**
 * CQRS Read Model representing user data from Access BC.
 *
 * <p>This is a materialized view synchronized via domain events from Access BC.
 * It uses Note BC vocabulary (AccessLevel) instead of Published Language (ApplicationRole).</p>
 *
 * <p>The Anti-Corruption Layer translation (ApplicationRole → AccessLevel) happens
 * in {@link org.note.application.event.UserEventHandler} when receiving events from Access BC.</p>
 *
 * <p>This model is optimized for read operations and contains only the data Note BC needs
 * for authorization decisions and author information display.</p>
 *
 * <h3>Design Principles:</h3>
 * <ul>
 *   <li><b>Bounded Context Independence</b>: No references to Access BC domain models</li>
 *   <li><b>Note BC Vocabulary</b>: Uses AccessLevel, not ApplicationRole</li>
 *   <li><b>Event-Driven Sync</b>: Updated via UserEventHandler, not REST calls</li>
 *   <li><b>Read Optimization</b>: Minimal data for fast queries</li>
 * </ul>
 */
public record AuthorView(
    UUID userId,
    AccessLevel accessLevel,      // Note BC vocabulary (translated from ApplicationRole)
    AccountStatus accountStatus,   // Published Language (simple enum shared between BCs)
    String fullName                // For display in DTOs
) {
    /**
     * Compact constructor with validation.
     */
    public AuthorView {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(accessLevel, "accessLevel is required");
        Objects.requireNonNull(accountStatus, "accountStatus is required");
        Objects.requireNonNull(fullName, "fullName is required");

        if (fullName.isBlank()) {
            throw new IllegalArgumentException("fullName cannot be blank");
        }
    }

    /**
     * Converts this AuthorView to NoteUserContext for authorization.
     *
     * <p>This is the primary use of AuthorView - transforming stored data
     * into the context needed for access control decisions.</p>
     *
     * @return NoteUserContext with userId and accessLevel
     */
    public NoteUserContext toUserContext() {
        return new NoteUserContext(userId, accessLevel);
    }

    /**
     * Checks if the author account is active.
     *
     * @return true if account status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return accountStatus == AccountStatus.ACTIVE;
    }

    /**
     * Checks if the author account is locked.
     *
     * @return true if account status is LOCKED, false otherwise
     */
    public boolean isLocked() {
        return accountStatus == AccountStatus.LOCKED;
    }

    /**
     * Checks if the author account is expired.
     *
     * @return true if account status is EXPIRED, false otherwise
     */
    public boolean isExpired() {
        return accountStatus == AccountStatus.EXPIRED;
    }
}
