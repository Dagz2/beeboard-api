package org.acme.domain.model;

import java.util.Objects;

/**
 * Value Object representing the name of a role.
 * 
 * A role name is a human-readable label that identifies a role within the system 
 * (e.g. "Administrator", "Project Manager", "User"). It must be non-null and non-blank.
 * 
 * This class enforces basic validation to ensure the role name is valid and meaningful.
 *
 * Example usage:
 * <pre>
 *     RoleName name = new RoleName("Administrator");
 * </pre>
 *
 * @param value the textual name of the role
 * @throws NullPointerException     if {@code value} is null
 * @throws IllegalArgumentException if {@code value} is blank
 */
public record RoleName(String value) {

    /**
     * Compact constructor that validates the role name.
     *
     * @param value the role name
     */
    public RoleName {
        Objects.requireNonNull(value, "Role name cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be blank");
        }
    }
}
