package org.acme.domain.model;

import java.util.Objects;

/**
 * Value Object representing the name of a permission.
 * 
 * A permission name is a human-readable label that describes what the permission allows,
 * such as "Create Task", "Delete Comment", or "View Dashboard". It must be non-null and non-blank.
 * 
 * This class enforces basic validation to ensure the permission name is valid and meaningful.
 *
 * Example usage:
 * <pre>
 *     PermissionName name = new PermissionName("Create Task");
 * </pre>
 *
 * @param value the display name of the permission
 * @throws NullPointerException     if {@code value} is null
 * @throws IllegalArgumentException if {@code value} is blank
 */
public record PermissionName(String value) {

    /**
     * Compact constructor that validates the permission name.
     *
     * @param value the permission name
     */
    public PermissionName {
        Objects.requireNonNull(value, "Permission name cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Permission name cannot be blank");
        }
    }
}
