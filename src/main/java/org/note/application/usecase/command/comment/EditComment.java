package org.note.application.usecase.command.comment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.comment.EditCommentCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.exception.UnauthorizedNoteAccessException;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;

/**
 * Use case for editing comment content.
 *
 * <p>Authorization: Only the comment author can edit their own comments.
 * There is no time limit for editing comments.
 *
 * <p>Business Rules:
 * <ul>
 *   <li>User must be the original comment author</li>
 *   <li>Content must meet validation requirements (non-empty, max 5000 characters)</li>
 *   <li>Note and comment must exist</li>
 * </ul>
 */
@ApplicationScoped
public class EditComment {

    @Inject
    NoteRepository noteRepository;

    /**
     * Executes the edit comment use case.
     *
     * @param command the edit command containing noteId, commentId, and new content
     * @param userContext the authenticated user context
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws IllegalArgumentException if the comment doesn't exist in the note
     * @throws UnauthorizedNoteAccessException if the user is not the comment author
     * @throws IllegalArgumentException if the new content is invalid
     */
    @Transactional
    public void execute(EditCommentCommand command, NoteUserContext userContext) {
        // 1. Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // 2. Find comment within note
        CommentId commentId = new CommentId(command.commentId());
        Comment comment = note.findComment(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found in note"));

        // 3. Authorize - only comment author can edit (no time limit per user preference)
        AuthorId editorId = userContext.toAuthorId();
        if (!comment.canBeEditedBy(editorId)) {
            throw UnauthorizedNoteAccessException.cannotEditNote();
        }

        // 4. Execute domain logic
        comment.edit(command.content());

        // 5. Persist
        noteRepository.update(note);
    }
}
