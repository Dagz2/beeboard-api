package org.acme.domain.model;
import java.util.Objects;

/**
 * Value Object representing a hashed password.
 * Ensures the hash value is non-null and non-blank.
 * Overrides {@link #toString()} to prevent accidental exposure of the hash.
 */
public record HashedPassword(String value) {

    /**
     * Canonical constructor enforcing non-null and non-blank hash.
     *
     * @param value the hashed password string; must not be null or blank
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if value is blank
     */
    public HashedPassword {
        Objects.requireNonNull(value, "Hashed password cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Hashed password cannot be blank");
        }
    }

    /**
     * Returns a placeholder to avoid revealing the actual hash.
     *
     * @return a constant placeholder string
     */
    @Override
    public String toString() {
        return "<protected>";
    }
}
