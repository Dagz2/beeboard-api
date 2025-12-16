package org.access.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object encapsulating a password reset request's ID.
 */
public record PasswordResetRequestId(UUID id) {

    // -------------------- Creation --------------------

    /**
     * Compact constructor that validates the request ID is not null.
     *
     * @param id the unique identifier
     * @throws NullPointerException if {@code id} is null
     */
    public PasswordResetRequestId {
        Objects.requireNonNull(id, "Id is required");
    }

    /**
     * Factory method to generate a new unique {@code PasswordResetRequestId}.
     *
     * @return a new {@code PasswordResetRequestId} with a randomly generated UUID
     */
    public static PasswordResetRequestId newId() {
        return new PasswordResetRequestId(UUID.randomUUID());
    }

    // -------------------- Overrides --------------------

    /**
     * Returns the string representation of the UUID.
     *
     * @return the UUID as a string
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
