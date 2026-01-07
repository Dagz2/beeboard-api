package org.note.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a tag name.
 *
 * <p>Tags are flexible keywords used to categorize and organize notes.
 * Tag names follow strict naming conventions to ensure consistency and
 * enable reliable filtering and search.
 *
 * <p>Business rules:
 * <ul>
 *   <li>Must contain only lowercase letters, numbers, and hyphens</li>
 *   <li>Maximum length: 50 characters</li>
 *   <li>Cannot be blank</li>
 *   <li>Automatically normalized to lowercase and trimmed</li>
 * </ul>
 *
 * @param value the tag name string
 */
public record TagName(String value) {

    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9-]+$");
    private static final int MAX_LENGTH = 50;

    /**
     * Canonical constructor with validation and normalization.
     *
     * <p>The tag name is automatically normalized by converting to lowercase
     * and trimming whitespace.
     *
     * @param value the raw tag name string
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if value is blank, exceeds max length,
     *                                  or contains invalid characters
     */
    public TagName {
        Objects.requireNonNull(value, "Tag name cannot be null");

        // Normalize: trim and convert to lowercase
        String normalized = value.trim().toLowerCase();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be blank");
        }

        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Tag name cannot exceed %d characters, got: %d",
                    MAX_LENGTH, normalized.length())
            );
        }

        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                String.format("Tag name must contain only lowercase letters, numbers, and hyphens. Got: '%s'",
                    normalized)
            );
        }

        // Assign normalized value
        value = normalized;
    }

    /**
     * Returns the tag name as a string.
     *
     * @return the tag name value
     */
    @Override
    public String toString() {
        return value;
    }
}
