package org.note.domain.exception;

import org.note.domain.model.CommentId;
import org.note.domain.model.NoteId;

/**
 * Exception thrown when attempting to reply to a comment that doesn't exist.
 */
public class ParentCommentNotFoundException extends RuntimeException {

    private ParentCommentNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates an exception for a parent comment not found by ID.
     *
     * @param parentCommentId the ID of the parent comment that was not found
     * @param noteId the ID of the note where the parent comment was expected
     * @return a new exception instance with a descriptive message
     */
    public static ParentCommentNotFoundException withId(CommentId parentCommentId, NoteId noteId) {
        return new ParentCommentNotFoundException(
            String.format("Parent comment with ID '%s' not found in note '%s'",
                parentCommentId.id(), noteId.id())
        );
    }
}
