package org.note.application.usecase.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.note.application.dto.query.CommentDto;
import org.note.application.mapper.CommentMapper;
import org.note.application.service.AuthorEnrichmentService;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.port.ProjectAccessPort;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Query service for retrieving comment information.
 *
 * <p>Provides read-only access to comments with proper authorization checks.
 * All query methods validate that the user has view access to the note before
 * returning comment data.
 *
 * <p><strong>Visibility Rules:</strong>
 * <ul>
 *   <li>Regular users see only VISIBLE comments</li>
 *   <li>Moderators and supervisors see all comments including CENSORED</li>
 *   <li>Proposed and validated solutions are filtered for solution-specific queries</li>
 * </ul>
 */
@ApplicationScoped
public class CommentQueryService {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    @Inject
    ProjectAccessPort projectAccessPort;

    @Inject
    AuthorEnrichmentService authorEnrichmentService;

    /**
     * Find a single comment by ID within a note.
     *
     * <p>User must have view access to the note. Returns empty Optional if
     * comment doesn't exist or is censored and user lacks moderation rights.
     *
     * @param noteId the note identifier
     * @param commentId the comment identifier
     * @param userContext the authenticated user context
     * @return Optional containing the comment DTO if found and accessible
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if user cannot view the note
     */
    public Optional<CommentDto> findById(UUID noteId, UUID commentId, NoteUserContext userContext) {
        NoteId noteIdObj = new NoteId(noteId);
        Note note = noteRepository.findById(noteIdObj)
            .orElseThrow(() -> NoteNotFoundException.withId(noteIdObj));

        // Validate user can view this note
        accessValidator.validateCanViewNote(note, userContext, projectAccessPort);

        CommentId commentIdObj = new CommentId(commentId);
        return note.findComment(commentIdObj)
            .map(CommentMapper::toDto)
            .map(authorEnrichmentService::enrichCommentDto);
    }

    /**
     * Get all comments for a note.
     *
     * <p>Regular users see only VISIBLE comments. Moderators and supervisors
     * see all comments including censored ones for audit purposes.
     *
     * @param noteId the note identifier
     * @param userContext the authenticated user context
     * @return list of comment DTOs (censored comments filtered for regular users)
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if user cannot view the note
     */
    public List<CommentDto> findByNoteId(UUID noteId, NoteUserContext userContext) {
        NoteId noteIdObj = new NoteId(noteId);
        Note note = noteRepository.findById(noteIdObj)
            .orElseThrow(() -> NoteNotFoundException.withId(noteIdObj));

        // Validate user can view this note
        accessValidator.validateCanViewNote(note, userContext, projectAccessPort);

        boolean isModerator = userContext.isSupervisor() || userContext.isModerator();

        List<CommentDto> commentDtos = note.getComments().stream()
            .filter(comment -> isModerator || comment.isVisible())  // Hide censored from regular users
            .map(CommentMapper::toDto)
            .collect(Collectors.toList());
        return authorEnrichmentService.enrichCommentDtos(commentDtos);
    }

    /**
     * Get comment thread (parent comment and all its replies).
     *
     * <p>Returns the parent comment and all comments that reference it as their
     * parent. Censored comments are filtered for regular users.
     *
     * @param noteId the note identifier
     * @param parentCommentId the parent comment identifier
     * @param userContext the authenticated user context
     * @return list of comment DTOs in the thread (parent + replies)
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if user cannot view the note
     */
    public List<CommentDto> findThreadByCommentId(UUID noteId, UUID parentCommentId, NoteUserContext userContext) {
        NoteId noteIdObj = new NoteId(noteId);
        Note note = noteRepository.findById(noteIdObj)
            .orElseThrow(() -> NoteNotFoundException.withId(noteIdObj));

        // Validate user can view this note
        accessValidator.validateCanViewNote(note, userContext, projectAccessPort);

        CommentId parentCommentIdObj = new CommentId(parentCommentId);
        boolean isModerator = userContext.isSupervisor() || userContext.isModerator();

        List<CommentDto> commentDtos = note.getComments().stream()
            .filter(comment -> isModerator || comment.isVisible())
            .filter(comment ->
                comment.getId().equals(parentCommentIdObj) ||  // Parent itself
                (comment.getParentCommentId() != null &&
                 comment.getParentCommentId().equals(parentCommentIdObj)))  // Replies
            .map(CommentMapper::toDto)
            .collect(Collectors.toList());
        return authorEnrichmentService.enrichCommentDtos(commentDtos);
    }

    /**
     * Get all proposed solutions for a note.
     *
     * <p>Returns all comments in PROPOSED status that are awaiting validation.
     * Only returns visible comments (censored proposed solutions are excluded).
     *
     * @param noteId the note identifier
     * @param userContext the authenticated user context
     * @return list of proposed solution comment DTOs
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if user cannot view the note
     */
    public List<CommentDto> findProposedSolutions(UUID noteId, NoteUserContext userContext) {
        NoteId noteIdObj = new NoteId(noteId);
        Note note = noteRepository.findById(noteIdObj)
            .orElseThrow(() -> NoteNotFoundException.withId(noteIdObj));

        // Validate user can view this note
        accessValidator.validateCanViewNote(note, userContext, projectAccessPort);

        List<CommentDto> commentDtos = note.getComments().stream()
            .filter(Comment::isProposedSolution)
            .filter(Comment::isVisible)  // Don't show censored proposed solutions
            .map(CommentMapper::toDto)
            .collect(Collectors.toList());
        return authorEnrichmentService.enrichCommentDtos(commentDtos);
    }

    /**
     * Get the validated solution for a note (if one exists).
     *
     * <p>Returns the comment that has been validated as the accepted solution
     * for this note. Only one comment can be in VALIDATED status at a time.
     *
     * @param noteId the note identifier
     * @param userContext the authenticated user context
     * @return Optional containing the validated solution comment DTO if one exists
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if user cannot view the note
     */
    public Optional<CommentDto> findValidatedSolution(UUID noteId, NoteUserContext userContext) {
        NoteId noteIdObj = new NoteId(noteId);
        Note note = noteRepository.findById(noteIdObj)
            .orElseThrow(() -> NoteNotFoundException.withId(noteIdObj));

        // Validate user can view this note
        accessValidator.validateCanViewNote(note, userContext, projectAccessPort);

        return note.getValidatedSolution()
            .map(CommentMapper::toDto)
            .map(authorEnrichmentService::enrichCommentDto);
    }
}
