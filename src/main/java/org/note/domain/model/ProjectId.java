package org.note.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique identifier for a Project.
 *
 * <p>This ID represents a reference to a Project from the Project bounded context.
 * It is used when a Note has PROJECT visibility, linking the note to a specific
 * project without coupling to the Project aggregate from another bounded context.
 *
 * <p>This is a strongly-typed ID that prevents confusion with other entity IDs
 * and ensures type safety at compile time. The ID is immutable and based on UUID.
 *
 * @param id the unique UUID identifier referencing a Project from the Project BC
 */
public record ProjectId(UUID id) {

    /**
     * Canonical constructor with validation.
     *
     * @param id the UUID value
     * @throws NullPointerException if id is null
     */
    public ProjectId {
        Objects.requireNonNull(id, "Project ID cannot be null");
    }

    /**
     * Returns the string representation of the Project ID.
     *
     * @return the UUID as a string
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
