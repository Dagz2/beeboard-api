package org.note.application.usecase.command.comment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.comment.DeleteCommentCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.port.ProjectAccessPort;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for deleting a comment (soft delete).
 *
 * <p><strong>Implementation Note:</strong> This use case implements soft delete by
 * marking the comment as CENSORED, which hides it from regular users but keeps it
 * visible to moderators and supervisors for audit purposes.
 *
 * <p>Authorization: Comment author, note author, moderators (if project owner),
 * or supervisors can delete comments.
 *
 * <p>Business Rules:
 * <ul>
 *   <li>Uses {@link Comment#censor()} to achieve soft delete semantics</li>
 *   <li>Deleted comments are hidden from regular users</li>
 *   <li>Deleted comments remain visible to moderators for audit trail</li>
 *   <li>Deleted comments can be restored via RestoreComment use case</li>
 * </ul>
 */
@ApplicationScoped
public class DeleteComment {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    @Inject
    ProjectAccessPort projectAccessPort;

    /**
     * Executes the delete comment use case (soft delete).
     *
     * @param command the delete command containing noteId and commentId
     * @param userContext the authenticated user context
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws IllegalArgumentException if the comment doesn't exist in the note
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if the user lacks permission
     */
    @Transactional
    public void execute(DeleteCommentCommand command, NoteUserContext userContext) {
        // 1. Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // 2. Find comment
        CommentId commentId = new CommentId(command.commentId());
        Comment comment = note.findComment(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found in note"));

        // 3. Authorize - comment author OR moderator/supervisor can delete
        // validateCanModerateComment allows: comment author, note author, SUPERVISOR, MODERATOR (if project owner)
        accessValidator.validateCanModerateComment(note, comment, userContext, projectAccessPort);

        // 4. Soft delete using censor() method
        // This marks comment as CENSORED (hidden from users, visible to moderators)
        comment.censor();

        // 5. Persist
        noteRepository.update(note);
    }
}
