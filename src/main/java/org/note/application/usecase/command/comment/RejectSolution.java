package org.note.application.usecase.command.comment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.comment.RejectSolutionCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for rejecting a proposed solution.
 *
 * <p>Authorization: Only the note author or supervisor can reject proposed solutions.
 *
 * <p>Business Rules:
 * <ul>
 *   <li>Note author or SUPERVISOR can reject</li>
 *   <li>Rejected solutions return to REGULAR status (per user preference)</li>
 *   <li>State transition: PROPOSED → REGULAR</li>
 *   <li>Operation is idempotent - silently succeeds if comment not in PROPOSED status</li>
 * </ul>
 *
 * <p>Use cases for rejection:
 * <ul>
 *   <li>Proposed solution doesn't actually resolve the issue</li>
 *   <li>Proposed solution is incorrect or incomplete</li>
 *   <li>A better solution has been found</li>
 * </ul>
 */
@ApplicationScoped
public class RejectSolution {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the reject solution use case.
     *
     * @param command the command containing noteId and commentId
     * @param userContext the authenticated user context
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if the user is not authorized
     * @throws IllegalArgumentException if the comment doesn't exist in the note
     */
    @Transactional
    public void execute(RejectSolutionCommand command, NoteUserContext userContext) {
        // 1. Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // 2. Authorize - note author or SUPERVISOR can reject
        accessValidator.validateCanValidateSolution(note, userContext);

        // 3. Find comment
        CommentId commentId = new CommentId(command.commentId());
        Comment comment = note.findComment(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found in note"));

        // 4. Execute domain logic (idempotent - silently succeeds if not PROPOSED)
        comment.rejectSolution();

        // 5. Persist
        noteRepository.update(note);
    }
}
