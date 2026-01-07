package org.note.domain.model;

/**
 * Enum representing the type of content contained in a Note.
 *
 * <p>The content type indicates how the note body should be interpreted,
 * displayed, or processed by the presentation layer.
 *
 * <p>Content types supported:
 * <ul>
 *   <li>TEXT: Plain or rich text content (default)</li>
 *   <li>CODE: Source code with syntax highlighting</li>
 *   <li>IMAGE: Visual content (URLs or embedded)</li>
 *   <li>MIXED: Combination of multiple content types</li>
 * </ul>
 */
public enum ContentType {

    /**
     * Plain or rich text content.
     * Used for general-purpose textual notes, explanations, and documentation.
     */
    TEXT,

    /**
     * Embedded images or screenshots.
     * Body typically contains image URLs or base64-encoded image data.
     */
    IMAGE,

    /**
     * Code snippets or technical content.
     * Should be displayed with syntax highlighting and monospace font.
     */
    CODE,

    /**
     * Combination of multiple content types.
     * Contains a mix of text, code, images, or other formats.
     */
    MIXED;

    /**
     * Checks if this content type requires syntax highlighting.
     *
     * @return true if content type is CODE, false otherwise
     */
    public boolean requiresSyntaxHighlighting() {
        return this == CODE;
    }

    /**
     * Checks if this content type represents visual media.
     *
     * @return true if content type is IMAGE, false otherwise
     */
    public boolean isVisualMedia() {
        return this == IMAGE;
    }

    /**
     * Checks if this content type is plain text.
     *
     * @return true if content type is TEXT, false otherwise
     */
    public boolean isPlainText() {
        return this == TEXT;
    }
}
