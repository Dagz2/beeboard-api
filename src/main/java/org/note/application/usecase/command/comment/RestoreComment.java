package org.note.application.usecase.command.comment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.comment.RestoreCommentCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.port.ProjectAccessPort;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for restoring censored or deleted comments to visible status.
 *
 * <p>Authorization: Moderators or supervisors can restore comments.
 *
 * <p>Business Rules:
 * <ul>
 *   <li>MODERATOR or SUPERVISOR role required</li>
 *   <li>Restores comment from CENSORED or UNDER_REVIEW to VISIBLE status</li>
 *   <li>Can restore user-deleted comments (soft deleted via DeleteComment)</li>
 *   <li>Can restore moderator-censored comments</li>
 * </ul>
 *
 * <p>Use cases for restoring:
 * <ul>
 *   <li>Comment was censored by mistake</li>
 *   <li>User deleted comment but moderator wants to restore it</li>
 *   <li>Comment under review determined to be acceptable</li>
 * </ul>
 */
@ApplicationScoped
public class RestoreComment {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    @Inject
    ProjectAccessPort projectAccessPort;

    /**
     * Executes the restore comment use case.
     *
     * @param command the command containing noteId and commentId
     * @param userContext the authenticated user context
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws IllegalArgumentException if the comment doesn't exist in the note
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if the user lacks moderation permission
     */
    @Transactional
    public void execute(RestoreCommentCommand command, NoteUserContext userContext) {
        // 1. Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // 2. Find comment
        CommentId commentId = new CommentId(command.commentId());
        Comment comment = note.findComment(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found in note"));

        // 3. Authorize - moderator/supervisor only
        accessValidator.validateCanModerateComment(note, comment, userContext, projectAccessPort);

        // 4. Execute domain logic
        comment.restoreModeration();

        // 5. Persist
        noteRepository.update(note);
    }
}
