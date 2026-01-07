package org.note.domain.exception;

/**
 * Exception thrown when an invalid operation is attempted on a note.
 */
public class InvalidNoteOperationException extends RuntimeException {

    private InvalidNoteOperationException(String message) {
        super(message);
    }

    /**
     * Note is not in ACTIVE status.
     */
    public static InvalidNoteOperationException noteIsNotActive() {
        return new InvalidNoteOperationException("Note is not in ACTIVE status");
    }

    /**
     * Note is archived.
     */
    public static InvalidNoteOperationException noteIsArchived() {
        return new InvalidNoteOperationException("Note is archived");
    }

    /**
     * Note is deleted.
     */
    public static InvalidNoteOperationException noteIsDeleted() {
        return new InvalidNoteOperationException("Note is deleted");
    }

    /**
     * Project is required for PROJECT visibility.
     */
    public static InvalidNoteOperationException projectRequiredForVisibility() {
        return new InvalidNoteOperationException("Project ID is required when visibility is PROJECT");
    }

    /**
     * Cannot add comment to inactive note.
     */
    public static InvalidNoteOperationException cannotAddCommentToInactiveNote() {
        return new InvalidNoteOperationException("Cannot add comment to a note that is not ACTIVE");
    }

    /**
     * Cannot validate a solution that is not proposed.
     */
    public static InvalidNoteOperationException cannotValidateNonProposedSolution() {
        return new InvalidNoteOperationException("Can only validate comments with PROPOSED solution status");
    }

    /**
     * Cannot propose a comment that is not regular.
     */
    public static InvalidNoteOperationException cannotProposeNonRegularComment() {
        return new InvalidNoteOperationException("Can only propose comments with REGULAR solution status");
    }
}
