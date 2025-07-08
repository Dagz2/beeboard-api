package org.access.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object encapsulating a Permission's ID.
 */
public record PermissionId (UUID id){

    // -------------------- Creation -------------------- 

    /**
     * Compact constructor that validates the permission ID is not null.
     *
     * @param id the unique identifier
     * @throws NullPointerException if {@code id} is null
     */
    public PermissionId {
        Objects.requireNonNull(id, "Id is required");
    }

    /**
     * Factory method to generate a new unique {@code PermissionId}.
     *
     * @return a new {@code PermissionId} with a randomly generated UUID
     */
    public static PermissionId newId() {
        return new PermissionId(UUID.randomUUID());
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