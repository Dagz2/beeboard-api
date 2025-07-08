package org.acme.domain.model;

import java.util.Objects;

/**
 * Value Object representing the stable, functional code of a role.
 * 
 * This code is used internally to identify a role in a consistent and language-independent way,
 * such as "ADMIN", "PROJECT_MANAGER", or "TEAM_MEMBER".
 *
 * Unlike display names, this code is intended to be unique, immutable, and stable across the system.
 * It must be uppercase, non-null, and non-blank.
 *
 * Example usage:
 * <pre>
 *     RoleCode code = new RoleCode("PROJECT_MANAGER");
 * </pre>
 *
 * @param value the string-based code of the role
 * @throws NullPointerException     if {@code value} is null
 * @throws IllegalArgumentException if {@code value} is blank or does not match the expected format
 */
public record RoleCode(String value) {

    /**
     * Compact constructor with validation rules.
     *
     * @param value the code identifying the role
     */
    public RoleCode {
        Objects.requireNonNull(value, "Role code cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Role code cannot be blank");
        }
        if (!value.matches("^[A-Z0-9_]+$")) {
            throw new IllegalArgumentException(
                "Role code must be uppercase and contain only letters, digits, or underscores: " + value
            );
        }
    }
}
