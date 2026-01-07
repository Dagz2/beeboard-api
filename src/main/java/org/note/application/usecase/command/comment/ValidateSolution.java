package org.note.application.usecase.command.comment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.comment.ValidateSolutionCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for validating a proposed solution.
 */
@ApplicationScoped
public class ValidateSolution {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the validate solution command.
     *
     * @param command the command
     * @param userContext the user context
     */
    @Transactional
    public void execute(ValidateSolutionCommand command, NoteUserContext userContext) {
        // Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // Validate access
        accessValidator.validateCanValidateSolution(note, userContext);

        // Find comment
        CommentId commentId = new CommentId(command.commentId());
        Comment comment = note.findComment(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found in note"));

        // Validate solution
        comment.validateSolution();

        // Persist
        noteRepository.update(note);
    }
}
