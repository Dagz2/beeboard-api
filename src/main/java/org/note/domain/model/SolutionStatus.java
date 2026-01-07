package org.note.domain.model;

/**
 * Enum representing the status of a Comment as a potential solution.
 *
 * <p>Solution workflow:
 * <ol>
 *   <li>User creates a Note (typically with Category: QUESTION or BUG)</li>
 *   <li>Contributors add Comments to the Discussion</li>
 *   <li>A contributor marks their Comment as PROPOSED solution</li>
 *   <li>Note Author or SUPERVISOR reviews and marks it as VALIDATED</li>
 *   <li>The validated Solution is highlighted in the Discussion</li>
 * </ol>
 *
 * <p>Business rules:
 * <ul>
 *   <li>Only REGULAR comments can be proposed as solutions</li>
 *   <li>Only the comment author can propose their own comment</li>
 *   <li>Only the note author or SUPERVISOR can validate a proposed solution</li>
 *   <li>A note can have at most one VALIDATED solution</li>
 * </ul>
 */
public enum SolutionStatus {

    /**
     * Normal comment, not proposed as a solution.
     * This is the default state for all new comments.
     */
    REGULAR,

    /**
     * Comment proposed as a solution by its author.
     * Awaiting validation by the Note Author or SUPERVISOR.
     */
    PROPOSED,

    /**
     * Solution validated by the Note Author or SUPERVISOR.
     * This comment is marked as the accepted answer/resolution.
     */
    VALIDATED;

    /**
     * Checks if a comment with this status can be proposed as a solution.
     *
     * @return true if the comment is REGULAR and can be proposed, false otherwise
     */
    public boolean canBeProposed() {
        return this == REGULAR;
    }

    /**
     * Checks if a comment with this status can be validated as a solution.
     *
     * @return true if the comment is PROPOSED and can be validated, false otherwise
     */
    public boolean canBeValidated() {
        return this == PROPOSED;
    }

    /**
     * Checks if a comment with this status can be rejected (returned to REGULAR).
     *
     * @return true if the comment is PROPOSED and can be rejected, false otherwise
     */
    public boolean canBeRejected() {
        return this == PROPOSED;
    }

    /**
     * Checks if this status represents a resolved state.
     *
     * @return true if the solution is VALIDATED, false otherwise
     */
    public boolean isResolved() {
        return this == VALIDATED;
    }
}
