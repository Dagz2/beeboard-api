package org.note.application.usecase.command.comment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.comment.ProposeCommentAsSolutionCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for proposing a comment as a solution to a note.
 *
 * <p>Authorization: Only the comment author can propose their own comment as a solution.
 *
 * <p>Business Rules:
 * <ul>
 *   <li>Only comment author can propose (per user preference)</li>
 *   <li>Comment must be in REGULAR status (not already PROPOSED or VALIDATED)</li>
 *   <li>State transition: REGULAR → PROPOSED</li>
 *   <li>Proposed solutions await validation from note author or supervisor</li>
 * </ul>
 *
 * <p>Typical workflow:
 * <pre>
 * 1. User posts helpful comment (REGULAR status)
 * 2. User proposes their comment as solution (PROPOSED status)
 * 3. Note author validates solution (VALIDATED status)
 * </pre>
 */
@ApplicationScoped
public class ProposeCommentAsSolution {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the propose comment as solution use case.
     *
     * @param command the command containing noteId and commentId
     * @param userContext the authenticated user context
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws IllegalArgumentException if the comment doesn't exist in the note
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if the user is not the comment author
     * @throws IllegalStateException if the comment is not in REGULAR status
     */
    @Transactional
    public void execute(ProposeCommentAsSolutionCommand command, NoteUserContext userContext) {
        // 1. Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // 2. Find comment
        CommentId commentId = new CommentId(command.commentId());
        Comment comment = note.findComment(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found in note"));

        // 3. Authorize - only comment author can propose (per user preference)
        accessValidator.validateCanProposeSolution(comment, userContext);

        // 4. Execute domain logic (throws IllegalStateException if not REGULAR)
        comment.proposeAsSolution();

        // 5. Persist
        noteRepository.update(note);
    }
}
