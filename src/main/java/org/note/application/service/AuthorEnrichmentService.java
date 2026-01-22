package org.note.application.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.note.application.dto.query.AuthorDto;
import org.note.application.dto.query.CommentDto;
import org.note.application.dto.query.NoteDto;
import org.note.domain.model.AuthorView;
import org.note.domain.repository.AuthorViewRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for enriching Note and Comment DTOs with author information.
 *
 * <p>This service fetches author data from the author_view CQRS read model
 * and enriches DTOs with AuthorDto objects containing userId and fullName.</p>
 *
 * <h3>Performance Optimization:</h3>
 * <p>Bulk enrichment methods collect all unique author IDs upfront and query
 * author_view once using an IN clause, avoiding N+1 query problems.</p>
 *
 * <h3>Error Handling:</h3>
 * <ul>
 *   <li><b>Author not found</b>: Sets author to null with a warning log</li>
 *   <li><b>Null DTO</b>: Returns null (defensive programming)</li>
 *   <li><b>Empty lists</b>: Returns empty list (no-op)</li>
 * </ul>
 *
 * <h3>Usage Pattern:</h3>
 * <pre>
 * // In Query Services:
 * List&lt;NoteDto&gt; notes = noteRepository.findAll()
 *     .stream()
 *     .map(NoteMapper::toDto)
 *     .collect(Collectors.toList());
 *
 * // Enrich in bulk (one query for all authors)
 * return authorEnrichmentService.enrichNoteDtos(notes);
 * </pre>
 */
@ApplicationScoped
public class AuthorEnrichmentService {

    @Inject
    AuthorViewRepository authorViewRepository;

    /**
     * Enriches a single NoteDto with author information.
     *
     * <p>For single-note queries, this is acceptable. For bulk queries,
     * use {@link #enrichNoteDtos(List)} to avoid N+1 queries.</p>
     *
     * @param noteDto the note DTO to enrich
     * @return the enriched note DTO (same instance, mutated)
     */
    public NoteDto enrichNoteDto(NoteDto noteDto) {
        if (noteDto == null) {
            return null;
        }

        // Enrich note author
        enrichSingleAuthor(noteDto.getAuthorId(), noteDto::setAuthor);

        // Enrich comment authors
        if (noteDto.getComments() != null && !noteDto.getComments().isEmpty()) {
            enrichCommentDtos(noteDto.getComments());
        }

        return noteDto;
    }

    /**
     * Enriches multiple NoteDtos with author information in bulk.
     *
     * <p>This method collects all unique author IDs (from notes and comments)
     * and fetches them in a single query, then enriches all DTOs.</p>
     *
     * <p><b>Performance</b>: For N notes with M comments each, this executes
     * only 1 query instead of N + (N * M) queries.</p>
     *
     * @param noteDtos the list of note DTOs to enrich
     * @return the enriched note DTOs (same instances, mutated)
     */
    public List<NoteDto> enrichNoteDtos(List<NoteDto> noteDtos) {
        if (noteDtos == null || noteDtos.isEmpty()) {
            return noteDtos;
        }

        // Collect all unique author IDs (notes + comments)
        Set<UUID> authorIds = new HashSet<>();

        for (NoteDto noteDto : noteDtos) {
            authorIds.add(noteDto.getAuthorId());

            if (noteDto.getComments() != null) {
                for (CommentDto comment : noteDto.getComments()) {
                    authorIds.add(comment.getAuthorId());
                }
            }
        }

        // Bulk fetch all authors (ONE query)
        Map<UUID, AuthorDto> authorCache = fetchAuthorsInBulk(authorIds);

        // Enrich all notes and their comments
        for (NoteDto noteDto : noteDtos) {
            noteDto.setAuthor(authorCache.get(noteDto.getAuthorId()));

            if (noteDto.getComments() != null) {
                for (CommentDto comment : noteDto.getComments()) {
                    comment.setAuthor(authorCache.get(comment.getAuthorId()));
                }
            }
        }

        return noteDtos;
    }

    /**
     * Enriches a single CommentDto with author information.
     *
     * @param commentDto the comment DTO to enrich
     * @return the enriched comment DTO (same instance, mutated)
     */
    public CommentDto enrichCommentDto(CommentDto commentDto) {
        if (commentDto == null) {
            return null;
        }

        enrichSingleAuthor(commentDto.getAuthorId(), commentDto::setAuthor);
        return commentDto;
    }

    /**
     * Enriches multiple CommentDtos with author information in bulk.
     *
     * @param commentDtos the list of comment DTOs to enrich
     * @return the enriched comment DTOs (same instances, mutated)
     */
    public List<CommentDto> enrichCommentDtos(List<CommentDto> commentDtos) {
        if (commentDtos == null || commentDtos.isEmpty()) {
            return commentDtos;
        }

        // Collect unique author IDs
        Set<UUID> authorIds = commentDtos.stream()
            .map(CommentDto::getAuthorId)
            .collect(Collectors.toSet());

        // Bulk fetch (ONE query)
        Map<UUID, AuthorDto> authorCache = fetchAuthorsInBulk(authorIds);

        // Enrich all comments
        for (CommentDto commentDto : commentDtos) {
            commentDto.setAuthor(authorCache.get(commentDto.getAuthorId()));
        }

        return commentDtos;
    }

    // ========== Private Helper Methods ==========

    /**
     * Fetches a single author and sets it using the provided setter.
     */
    private void enrichSingleAuthor(UUID authorId, java.util.function.Consumer<AuthorDto> setter) {
        authorViewRepository.findById(authorId)
            .map(this::toAuthorDto)
            .ifPresentOrElse(
                setter,
                () -> {
                    Log.warn("Author not found in author_view: " + authorId +
                             ". Setting author to null. Consider running author_view sync.");
                    setter.accept(null);
                }
            );
    }

    /**
     * Fetches multiple authors in bulk using IN clause query.
     *
     * <p>This method uses Panache's list() method with a WHERE IN clause
     * to fetch all authors in a single query.</p>
     *
     * @param authorIds set of author IDs to fetch
     * @return map of authorId -> AuthorDto
     */
    private Map<UUID, AuthorDto> fetchAuthorsInBulk(Set<UUID> authorIds) {
        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Use Panache to query: SELECT * FROM author_view WHERE user_id IN (...)
        List<AuthorView> authors = authorViewRepository.findAllByIds(authorIds);

        // Convert to map for O(1) lookup
        Map<UUID, AuthorDto> authorMap = authors.stream()
            .collect(Collectors.toMap(
                AuthorView::userId,
                this::toAuthorDto
            ));

        // Log warning for missing authors
        Set<UUID> missingIds = new HashSet<>(authorIds);
        missingIds.removeAll(authorMap.keySet());

        if (!missingIds.isEmpty()) {
            Log.warn("Authors not found in author_view: " + missingIds +
                     ". Consider running author_view sync. Affected DTOs will have null author.");
        }

        return authorMap;
    }

    /**
     * Converts AuthorView domain model to AuthorDto.
     */
    private AuthorDto toAuthorDto(AuthorView authorView) {
        return new AuthorDto(
            authorView.userId(),
            authorView.fullName()
        );
    }
}
