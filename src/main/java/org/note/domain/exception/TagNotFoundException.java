package org.note.domain.exception;

import org.note.domain.model.TagId;
import org.note.domain.model.TagName;

/**
 * Exception thrown when a Tag is not found.
 */
public class TagNotFoundException extends RuntimeException {

    private TagNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates an exception for a tag not found by ID.
     */
    public static TagNotFoundException withId(TagId id) {
        return new TagNotFoundException("Tag not found with id: " + id);
    }

    /**
     * Creates an exception for a tag not found by name.
     */
    public static TagNotFoundException withName(TagName name) {
        return new TagNotFoundException("Tag not found with name: " + name);
    }
}
