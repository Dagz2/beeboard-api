package org.note.application.usecase.command.comment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.comment.CensorCommentCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.port.ProjectAccessPort;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for censoring inappropriate comments (moderation action).
 *
 * <p>Authorization: Moderators or supervisors can censor comments.
 *
 * <p>Business Rules:
 * <ul>
 *   <li>MODERATOR or SUPERVISOR role required</li>
 *   <li>Censored comments are hidden from regular users</li>
 *   <li>Censored comments remain visible to moderators for audit</li>
 *   <li>Censored comments can be restored via RestoreComment use case</li>
 * </ul>
 *
 * <p>Use cases for censoring:
 * <ul>
 *   <li>Removing inappropriate or offensive content</li>
 *   <li>Hiding spam or off-topic comments</li>
 *   <li>Enforcing community guidelines</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This differs from DeleteComment (user-initiated) in that
 * this is a moderator-initiated action, though both use the same underlying
 * {@link Comment#censor()} domain method.
 */
@ApplicationScoped
public class CensorComment {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    @Inject
    ProjectAccessPort projectAccessPort;

    /**
     * Executes the censor comment use case.
     *
     * @param command the command containing noteId and commentId
     * @param userContext the authenticated user context
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws IllegalArgumentException if the comment doesn't exist in the note
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if the user lacks moderation permission
     */
    @Transactional
    public void execute(CensorCommentCommand command, NoteUserContext userContext) {
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
        comment.censor();

        // 5. Persist
        noteRepository.update(note);
    }
}
