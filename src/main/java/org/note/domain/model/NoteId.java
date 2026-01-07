package org.note.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique identifier for a Note aggregate.
 *
 * <p>This is a strongly-typed ID that prevents confusion with other entity IDs
 * and ensures type safety at compile time. The ID is immutable and based on UUID.
 *
 * @param id the unique UUID identifier
 */
public record NoteId(UUID id) {

    /**
     * Canonical constructor with validation.
     *
     * @param id the UUID value
     * @throws NullPointerException if id is null
     */
    public NoteId {
        Objects.requireNonNull(id, "Note ID cannot be null");
    }

    /**
     * Factory method to create a new Note identifier with a random UUID.
     *
     * <p>Use this method when creating new Note aggregates.
     *
     * @return a new NoteId instance with a randomly generated UUID
     */
    public static NoteId newId() {
        return new NoteId(UUID.randomUUID());
    }

    /**
     * Returns the string representation of the Note ID.
     *
     * @return the UUID as a string
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
