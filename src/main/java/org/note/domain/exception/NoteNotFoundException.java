package org.note.domain.exception;

import org.note.domain.model.NoteId;

/**
 * Exception thrown when a Note is not found.
 */
public class NoteNotFoundException extends RuntimeException {

    private NoteNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates an exception for a note not found by ID.
     */
    public static NoteNotFoundException withId(NoteId id) {
        return new NoteNotFoundException("Note not found with id: " + id);
    }
}
