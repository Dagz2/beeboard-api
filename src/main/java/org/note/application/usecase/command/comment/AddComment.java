package org.note.application.usecase.command.comment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.comment.AddCommentCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.port.ProjectAccessPort;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for adding a comment to a note.
 */
@ApplicationScoped
public class AddComment {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    @Inject
    ProjectAccessPort projectAccessPort;

    /**
     * Executes the add comment command.
     *
     * @param command the command
     * @param userContext the user context
     */
    @Transactional
    public void execute(AddCommentCommand command, NoteUserContext userContext) {
        // Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // Validate access
        accessValidator.validateCanComment(note, userContext, projectAccessPort);

        // Create comment
        AuthorId authorId = userContext.toAuthorId();
        CommentId parentCommentId = command.parentCommentId() != null
                ? new CommentId(command.parentCommentId())
                : null;

        Comment comment = Comment.create(authorId, command.content(), parentCommentId);

        // Add comment to note
        note.addComment(comment);

        // Persist
        noteRepository.update(note);
    }
}
