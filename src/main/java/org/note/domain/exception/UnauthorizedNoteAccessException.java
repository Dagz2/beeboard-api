package org.note.domain.exception;

/**
 * Exception thrown when a user attempts an unauthorized operation on a note or comment.
 */
public class UnauthorizedNoteAccessException extends RuntimeException {

    private UnauthorizedNoteAccessException(String message) {
        super(message);
    }

    /**
     * User cannot view the note.
     */
    public static UnauthorizedNoteAccessException cannotViewNote() {
        return new UnauthorizedNoteAccessException("User is not authorized to view this note");
    }

    /**
     * User cannot edit the note.
     */
    public static UnauthorizedNoteAccessException cannotEditNote() {
        return new UnauthorizedNoteAccessException("User is not authorized to edit this note");
    }

    /**
     * User cannot comment on the note.
     */
    public static UnauthorizedNoteAccessException cannotCommentOnNote() {
        return new UnauthorizedNoteAccessException("User is not authorized to comment on this note");
    }

    /**
     * User cannot archive the note.
     */
    public static UnauthorizedNoteAccessException cannotArchiveNote() {
        return new UnauthorizedNoteAccessException("User is not authorized to archive this note");
    }

    /**
     * User cannot delete the note.
     */
    public static UnauthorizedNoteAccessException cannotDeleteNote() {
        return new UnauthorizedNoteAccessException("User is not authorized to delete this note");
    }

    /**
     * User cannot validate a solution.
     */
    public static UnauthorizedNoteAccessException cannotValidateSolution() {
        return new UnauthorizedNoteAccessException("User is not authorized to validate this solution");
    }

    /**
     * User cannot moderate a comment.
     */
    public static UnauthorizedNoteAccessException cannotModerateComment() {
        return new UnauthorizedNoteAccessException("User is not authorized to moderate this comment");
    }

    /**
     * User cannot propose a comment as a solution.
     */
    public static UnauthorizedNoteAccessException cannotProposeSolution() {
        return new UnauthorizedNoteAccessException("User is not authorized to propose this as a solution");
    }
}
