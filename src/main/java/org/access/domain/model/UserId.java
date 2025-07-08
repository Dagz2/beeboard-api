package org.acme.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object encapsulating a user's ID.
 */
public record UserId (UUID id){

    // -------------------- Creation -------------------- 

    /**
     * Compact constructor that validates the user ID is not null.
     *
     * @param id the unique identifier
     * @throws NullPointerException if {@code id} is null
     */
    public UserId {
        Objects.requireNonNull(id, "Id is required");
    }

    /**
     * Factory method to generate a new unique {@code UserId}.
     *
     * @return a new {@code UserId} with a randomly generated UUID
     */
    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    // -------------------- Overrides -------------------- 

    /**
     * Returns a placeholder to avoid revealing the actual hash.
     *
     * @return a constant placeholder string
     */
    @Override
    public String toString() {
        return id.toString(); 
    }
}