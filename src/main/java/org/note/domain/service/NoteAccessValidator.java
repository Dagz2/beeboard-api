package org.note.domain.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.note.domain.exception.UnauthorizedNoteAccessException;
import org.note.domain.model.*;
import org.note.domain.port.ProjectAccessPort;

import java.util.Objects;

/**
 * Domain service that validates access to notes and comments.
 * Implements all access policy rules defined in the Note BC ubiquitous language.
 *
 * This is a stateless service that encapsulates cross-aggregate business rules.
 */
@ApplicationScoped
public class NoteAccessValidator {

    /**
     * Validates if a user can view a note.
     *
     * Access Rules:
     * - IF user is Author → ALLOW
     * - IF user is Recipient → ALLOW
     * - IF accessLevel == SUPERVISOR → ALLOW
     * - IF visibility == PRIVATE → DENY
     * - IF visibility == SHARED → ALLOW (all authenticated users)
     * - IF visibility == PROJECT → ALLOW if user is ProjectMember
     *
     * @param note the note to access
     * @param userContext the user context
     * @param projectAccessPort port to check project membership
     * @throws UnauthorizedNoteAccessException if user cannot view the note
     */
    public void validateCanViewNote(Note note, NoteUserContext userContext, ProjectAccessPort projectAccessPort) {
        Objects.requireNonNull(note, "Note cannot be null");
        Objects.requireNonNull(userContext, "User context cannot be null");

        // Rule: Author can always view their own notes
        if (note.isOwnedBy(userContext.toAuthorId())) {
            return;
        }

        // Rule: Recipient can always view shared notes
        if (note.isSharedWith(userContext.toAuthorId())) {
            return;
        }

        // Rule: SUPERVISOR can view all notes
        if (userContext.isSupervisor()) {
            return;
        }

        // Check visibility rules
        switch (note.getVisibility()) {
            case PRIVATE:
                // Only author and recipients can see private notes (already checked above)
                throw UnauthorizedNoteAccessException.cannotViewNote();

            case SHARED:
                // SHARED notes visible to all authenticated users
                // No additional check needed - if user has a valid context, they can view
                break;

            case PROJECT:
                // PROJECT notes visible only to project members
                Objects.requireNonNull(projectAccessPort, "Project access port cannot be null for PROJECT visibility");
                if (note.getProjectId() == null ||
                        !projectAccessPort.isContributor(note.getProjectId().id(), userContext.userId())) {
                    throw UnauthorizedNoteAccessException.cannotViewNote();
                }
                break;
        }
    }

    /**
     * Validates if a user can comment on a note.
     *
     * Access Rules:
     * - IF cannot view note → DENY
     * - IF note.status != ACTIVE → DENY
     * - IF accessLevel == VIEWER → DENY
     * - ELSE → ALLOW
     *
     * @param note the note to comment on
     * @param userContext the user context
     * @param projectAccessPort port to check project membership
     * @throws UnauthorizedNoteAccessException if user cannot comment
     */
    public void validateCanComment(Note note, NoteUserContext userContext, ProjectAccessPort projectAccessPort) {
        Objects.requireNonNull(note, "Note cannot be null");
        Objects.requireNonNull(userContext, "User context cannot be null");

        // Rule: Must be able to view the note
        validateCanViewNote(note, userContext, projectAccessPort);

        // Rule: Note must be ACTIVE
        if (!note.isActive()) {
            throw UnauthorizedNoteAccessException.cannotCommentOnNote();
        }

        // Rule: VIEWER access level cannot comment
        if (!userContext.canComment()) {
            throw UnauthorizedNoteAccessException.cannotCommentOnNote();
        }
    }

    /**
     * Validates if a user can edit a note.
     *
     * Access Rules:
     * - IF user is Author → ALLOW
     * - ELSE → DENY
     *
     * @param note the note to edit
     * @param userContext the user context
     * @throws UnauthorizedNoteAccessException if user cannot edit
     */
    public void validateCanEditNote(Note note, NoteUserContext userContext) {
        Objects.requireNonNull(note, "Note cannot be null");
        Objects.requireNonNull(userContext, "User context cannot be null");

        // Rule: Only author can edit
        if (!note.isOwnedBy(userContext.toAuthorId())) {
            throw UnauthorizedNoteAccessException.cannotEditNote();
        }
    }

    /**
     * Validates if a user can archive a note.
     *
     * Access Rules:
     * - IF user is Author → ALLOW
     * - IF accessLevel == SUPERVISOR → ALLOW
     * - ELSE → DENY
     *
     * @param note the note to archive
     * @param userContext the user context
     * @throws UnauthorizedNoteAccessException if user cannot archive
     */
    public void validateCanArchiveNote(Note note, NoteUserContext userContext) {
        Objects.requireNonNull(note, "Note cannot be null");
        Objects.requireNonNull(userContext, "User context cannot be null");

        // Rule: Author or SUPERVISOR can archive
        if (!note.isOwnedBy(userContext.toAuthorId()) && !userContext.isSupervisor()) {
            throw UnauthorizedNoteAccessException.cannotArchiveNote();
        }
    }

    /**
     * Validates if a user can delete a note (soft delete).
     *
     * Access Rules:
     * - IF user is Author → ALLOW
     * - IF accessLevel == SUPERVISOR → ALLOW
     * - ELSE → DENY
     *
     * @param note the note to delete
     * @param userContext the user context
     * @throws UnauthorizedNoteAccessException if user cannot delete
     */
    public void validateCanDeleteNote(Note note, NoteUserContext userContext) {
        Objects.requireNonNull(note, "Note cannot be null");
        Objects.requireNonNull(userContext, "User context cannot be null");

        // Rule: Author or SUPERVISOR can delete
        if (!note.isOwnedBy(userContext.toAuthorId()) && !userContext.isSupervisor()) {
            throw UnauthorizedNoteAccessException.cannotDeleteNote();
        }
    }

    /**
     * Validates if a user can validate a solution.
     *
     * Access Rules:
     * - IF user is note.Author → ALLOW
     * - IF accessLevel == SUPERVISOR → ALLOW
     * - ELSE → DENY
     *
     * @param note the note containing the solution
     * @param userContext the user context
     * @throws UnauthorizedNoteAccessException if user cannot validate solution
     */
    public void validateCanValidateSolution(Note note, NoteUserContext userContext) {
        Objects.requireNonNull(note, "Note cannot be null");
        Objects.requireNonNull(userContext, "User context cannot be null");

        // Rule: Only note author or SUPERVISOR can validate solutions
        if (!note.isOwnedBy(userContext.toAuthorId()) && !userContext.isSupervisor()) {
            throw UnauthorizedNoteAccessException.cannotValidateSolution();
        }
    }

    /**
     * Validates if a user can moderate a comment.
     *
     * Access Rules:
     * - IF user is note.Author → ALLOW
     * - IF user is comment.Author → ALLOW (edit/delete only)
     * - IF accessLevel == SUPERVISOR → ALLOW
     * - IF accessLevel == MODERATOR AND user is ProjectOwner → ALLOW
     * - ELSE → DENY
     *
     * @param note the note containing the comment
     * @param comment the comment to moderate
     * @param userContext the user context
     * @param projectAccessPort port to check project ownership
     * @throws UnauthorizedNoteAccessException if user cannot moderate
     */
    public void validateCanModerateComment(Note note, Comment comment, NoteUserContext userContext,
                                            ProjectAccessPort projectAccessPort) {
        Objects.requireNonNull(note, "Note cannot be null");
        Objects.requireNonNull(comment, "Comment cannot be null");
        Objects.requireNonNull(userContext, "User context cannot be null");

        // Rule: Note author can moderate all comments on their note
        if (note.isOwnedBy(userContext.toAuthorId())) {
            return;
        }

        // Rule: Comment author can moderate their own comment
        if (comment.canBeEditedBy(userContext.toAuthorId())) {
            return;
        }

        // Rule: SUPERVISOR can moderate all comments
        if (userContext.isSupervisor()) {
            return;
        }

        // Rule: MODERATOR can moderate if they are the project owner
        if (userContext.isModerator() && note.getProjectId() != null &&
                projectAccessPort != null &&
                projectAccessPort.isOwner(note.getProjectId().id(), userContext.userId())) {
            return;
        }

        throw UnauthorizedNoteAccessException.cannotModerateComment();
    }

    /**
     * Validates if a user can propose a comment as a solution.
     *
     * Access Rules:
     * - IF user is NOT comment.Author → DENY
     * - IF comment.solutionStatus != REGULAR → DENY
     * - ELSE → ALLOW
     *
     * @param comment the comment to propose as solution
     * @param userContext the user context
     * @throws UnauthorizedNoteAccessException if user cannot propose solution
     */
    public void validateCanProposeSolution(Comment comment, NoteUserContext userContext) {
        Objects.requireNonNull(comment, "Comment cannot be null");
        Objects.requireNonNull(userContext, "User context cannot be null");

        // Rule: Only comment author can propose their comment as a solution
        if (!comment.canBeEditedBy(userContext.toAuthorId())) {
            throw UnauthorizedNoteAccessException.cannotProposeSolution();
        }

        // Rule: Comment must be REGULAR to be proposed
        // This is actually enforced by the Comment entity itself in proposeAsSolution()
        // but we validate it here as well for clarity
        if (!comment.isRegularComment()) {
            throw UnauthorizedNoteAccessException.cannotProposeSolution();
        }
    }
}
