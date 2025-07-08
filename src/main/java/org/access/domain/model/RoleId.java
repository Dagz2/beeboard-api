package org.acme.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object encapsulating a Role's ID.
 */
public record RoleId (UUID id){

    // -------------------- Creation -------------------- 

    /**
     * Compact constructor that validates the role ID is not null.
     *
     * @param id the unique identifier
     * @throws NullPointerException if {@code id} is null
     */
    public RoleId {
        Objects.requireNonNull(id, "Id is required");
    }

    /**
     * Factory method to generate a new unique {@code RoleId}.
     *
     * @return a new {@code RoleId} with a randomly generated UUID
     */
    public static RoleId newId() {
        return new RoleId(UUID.randomUUID());
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