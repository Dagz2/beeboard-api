package org.note.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique identifier for a Comment entity.
 *
 * <p>This is a strongly-typed ID that prevents confusion with other entity IDs
 * and ensures type safety at compile time. The ID is immutable and based on UUID.
 *
 * @param id the unique UUID identifier
 */
public record CommentId(UUID id) {

    /**
     * Canonical constructor with validation.
     *
     * @param id the UUID value
     * @throws NullPointerException if id is null
     */
    public CommentId {
        Objects.requireNonNull(id, "Comment ID cannot be null");
    }

    /**
     * Factory method to create a new Comment identifier with a random UUID.
     *
     * <p>Use this method when creating new Comment entities.
     *
     * @return a new CommentId instance with a randomly generated UUID
     */
    public static CommentId newId() {
        return new CommentId(UUID.randomUUID());
    }

    /**
     * Returns the string representation of the Comment ID.
     *
     * @return the UUID as a string
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
