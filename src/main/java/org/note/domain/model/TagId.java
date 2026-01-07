package org.note.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique identifier for a Tag aggregate.
 *
 * <p>This is a strongly-typed ID that prevents confusion with other entity IDs
 * and ensures type safety at compile time. The ID is immutable and based on UUID.
 *
 * @param id the unique UUID identifier
 */
public record TagId(UUID id) {

    /**
     * Canonical constructor with validation.
     *
     * @param id the UUID value
     * @throws NullPointerException if id is null
     */
    public TagId {
        Objects.requireNonNull(id, "Tag ID cannot be null");
    }

    /**
     * Factory method to create a new Tag identifier with a random UUID.
     *
     * <p>Use this method when creating new Tag aggregates.
     *
     * @return a new TagId instance with a randomly generated UUID
     */
    public static TagId newId() {
        return new TagId(UUID.randomUUID());
    }

    /**
     * Returns the string representation of the Tag ID.
     *
     * @return the UUID as a string
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
