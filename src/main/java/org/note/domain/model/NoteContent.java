package org.note.domain.model;

import java.util.Objects;

/**
 * Value Object representing the content of a Note.
 *
 * <p>Note content consists of a body (the actual text or data) and a content type
 * that indicates how the content should be interpreted or displayed.
 *
 * <p>Business rules:
 * <ul>
 *   <li>Body cannot be null (but can be empty)</li>
 *   <li>Maximum body length: 50,000 characters</li>
 *   <li>Content type must be specified</li>
 * </ul>
 *
 * <p>Content types supported:
 * <ul>
 *   <li>{@link ContentType#TEXT TEXT}: Plain text content</li>
 *   <li>{@link ContentType#CODE CODE}: Source code or technical content</li>
 *   <li>{@link ContentType#IMAGE IMAGE}: Image URLs or base64-encoded images</li>
 *   <li>{@link ContentType#MIXED MIXED}: Combination of different content types</li>
 * </ul>
 *
 * @param body the content body text
 * @param type the content type indicator
 */
public record NoteContent(String body, ContentType type) {

    private static final int MAX_LENGTH = 50_000;

    /**
     * Canonical constructor with validation.
     *
     * @param body the content body text
     * @param type the content type
     * @throws NullPointerException if body or type is null
     * @throws IllegalArgumentException if body exceeds maximum length
     */
    public NoteContent {
        Objects.requireNonNull(body, "Content body cannot be null");
        Objects.requireNonNull(type, "Content type cannot be null");

        if (body.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Content body cannot exceed %d characters, got: %d",
                    MAX_LENGTH, body.length())
            );
        }
    }

    /**
     * Factory method to create text content.
     *
     * @param body the plain text content
     * @return a new NoteContent with TEXT type
     */
    public static NoteContent text(String body) {
        return new NoteContent(body, ContentType.TEXT);
    }

    /**
     * Factory method to create code content.
     *
     * @param body the source code or technical content
     * @return a new NoteContent with CODE type
     */
    public static NoteContent code(String body) {
        return new NoteContent(body, ContentType.CODE);
    }

    /**
     * Factory method to create image content.
     *
     * @param body the image URL or base64-encoded image data
     * @return a new NoteContent with IMAGE type
     */
    public static NoteContent image(String body) {
        return new NoteContent(body, ContentType.IMAGE);
    }

    /**
     * Factory method to create mixed content.
     *
     * @param body the content containing multiple types
     * @return a new NoteContent with MIXED type
     */
    public static NoteContent mixed(String body) {
        return new NoteContent(body, ContentType.MIXED);
    }

    /**
     * Returns a string representation showing the content type and body length.
     *
     * @return formatted string with type and length information
     */
    @Override
    public String toString() {
        return "NoteContent{type=" + type + ", length=" + body.length() + "}";
    }
}
