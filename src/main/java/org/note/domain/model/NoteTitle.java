package org.note.domain.model;

import java.util.Objects;

/**
 * Value Object representing the title of a Note.
 *
 * <p>The title is a short, descriptive summary of the note's content.
 * It is mandatory and must be concise to ensure readability in lists and searches.
 *
 * <p>Business rules:
 * <ul>
 *   <li>Maximum length: 200 characters</li>
 *   <li>Cannot be null or blank</li>
 * </ul>
 *
 * @param value the title text
 */
public record NoteTitle(String value) {

    private static final int MAX_LENGTH = 200;

    /**
     * Canonical constructor with validation.
     *
     * @param value the title text
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if value is blank or exceeds maximum length
     */
    public NoteTitle {
        Objects.requireNonNull(value, "Note title cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("Note title cannot be blank");
        }

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Note title cannot exceed %d characters, got: %d",
                    MAX_LENGTH, value.length())
            );
        }
    }

    /**
     * Returns the title as a string.
     *
     * @return the title value
     */
    @Override
    public String toString() {
        return value;
    }
}
