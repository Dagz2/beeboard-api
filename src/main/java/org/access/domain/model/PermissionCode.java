package org.access.domain.model;

import java.util.Objects;

/**
 * Value Object representing the stable, functional code of a permission.
 *
 * This code uniquely identifies a permission in the system, such as
 * "CREATE_TASK", "DELETE_COMMENT", or "VIEW_DASHBOARD".
 *
 * It is intended to be consistent, immutable, and used in logic such as access checks
 * and role-to-permission mappings. The value must be non-null, non-blank,
 * and consist of uppercase letters, digits, or underscores only.
 *
 * Example usage:
 * <pre>
 *     PermissionCode code = new PermissionCode("MANAGE_PROJECTS");
 * </pre>
 *
 * @param value the internal code representing the permission
 * @throws NullPointerException     if {@code value} is null
 * @throws IllegalArgumentException if {@code value} is blank or contains invalid characters
 */
public record PermissionCode(String value) {

    /**
     * Compact constructor that validates the permission code.
     *
     * @param value the code identifying the permission
     */
    public PermissionCode {
        Objects.requireNonNull(value, "Permission code cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Permission code cannot be blank");
        }
        if (!value.matches("^[A-Z0-9_]+$")) {
            throw new IllegalArgumentException(
                "Permission code must be uppercase and contain only letters, digits, or underscores: " + value
            );
        }
    }
}
