package org.access.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing an email address.
 * Ensures the email has a valid format and is immutable.
 */
public record EmailAddress(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );
    
    // -------------------- Creation -------------------- 

    /**
     * Canonical constructor with validation.
     *
     * @param value the raw email string
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if value is blank or invalid format
     */
    public EmailAddress {
        Objects.requireNonNull(value, "Email cannot be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                String.format("Invalid email format: '%s'", trimmed)
            );
        }
        // assign trimmed value
        value = trimmed;
    }

    // -------------------- Overrides -------------------- 

    @Override
    public String toString() {
        return value;
    }
}