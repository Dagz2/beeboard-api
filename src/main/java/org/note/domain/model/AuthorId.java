package org.note.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique identifier for an Author (User).
 *
 * <p>This ID represents a reference to a User from the Access bounded context.
 * It is used to link Notes and Comments to their creators without coupling to
 * the User aggregate from another bounded context.
 *
 * <p>This is a strongly-typed ID that prevents confusion with other entity IDs
 * and ensures type safety at compile time. The ID is immutable and based on UUID.
 *
 * @param id the unique UUID identifier referencing a User from the Access BC
 */
public record AuthorId(UUID id) {

    /**
     * Canonical constructor with validation.
     *
     * @param id the UUID value
     * @throws NullPointerException if id is null
     */
    public AuthorId {
        Objects.requireNonNull(id, "Author ID cannot be null");
    }

    /**
     * Returns the string representation of the Author ID.
     *
     * @return the UUID as a string
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
