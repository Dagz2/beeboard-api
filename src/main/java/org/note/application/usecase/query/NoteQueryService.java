package org.note.application.usecase.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.query.NoteDto;
import org.note.application.mapper.NoteMapper;
import org.note.application.service.AuthorEnrichmentService;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Query service for retrieving notes.
 * All methods are transactional with read-only semantics.
 */
@ApplicationScoped
public class NoteQueryService {

    @Inject
    NoteRepository noteRepository;

    @Inject
    AuthorEnrichmentService authorEnrichmentService;

    /**
     * Finds a note by its ID.
     *
     * @param noteId the note ID
     * @return the note DTO if found, empty otherwise
     */
    @Transactional
    public Optional<NoteDto> findById(UUID noteId) {
        return noteRepository.findById(new NoteId(noteId))
                .map(NoteMapper::toDto)
                .map(authorEnrichmentService::enrichNoteDto);
    }

    /**
     * Finds all notes by a specific author.
     *
     * @param authorId the author ID
     * @return list of note DTOs
     */
    @Transactional
    public List<NoteDto> findByAuthorId(UUID authorId) {
        List<NoteDto> noteDtos = noteRepository.findByAuthorId(new AuthorId(authorId))
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
        return authorEnrichmentService.enrichNoteDtos(noteDtos);
    }

    /**
     * Finds all notes in a specific category.
     *
     * @param category the category name
     * @return list of note DTOs
     */
    @Transactional
    public List<NoteDto> findByCategory(String category) {
        NoteCategory noteCategory = NoteCategory.valueOf(category);
        List<NoteDto> noteDtos = noteRepository.findByCategory(noteCategory)
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
        return authorEnrichmentService.enrichNoteDtos(noteDtos);
    }

    /**
     * Finds all notes with a specific visibility.
     *
     * @param visibility the visibility name
     * @return list of note DTOs
     */
    @Transactional
    public List<NoteDto> findByVisibility(String visibility) {
        NoteVisibility noteVisibility = NoteVisibility.valueOf(visibility);
        List<NoteDto> noteDtos = noteRepository.findByVisibility(noteVisibility)
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
        return authorEnrichmentService.enrichNoteDtos(noteDtos);
    }

    /**
     * Finds all notes linked to a specific project.
     *
     * @param projectId the project ID
     * @return list of note DTOs
     */
    @Transactional
    public List<NoteDto> findByProjectId(UUID projectId) {
        List<NoteDto> noteDtos = noteRepository.findByProjectId(new ProjectId(projectId))
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
        return authorEnrichmentService.enrichNoteDtos(noteDtos);
    }

    /**
     * Finds all notes with a specific tag.
     *
     * @param tagId the tag ID
     * @return list of note DTOs
     */
    @Transactional
    public List<NoteDto> findByTagId(UUID tagId) {
        List<NoteDto> noteDtos = noteRepository.findByTagId(new TagId(tagId))
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
        return authorEnrichmentService.enrichNoteDtos(noteDtos);
    }

    /**
     * Finds all notes with a specific status.
     *
     * @param status the status name
     * @return list of note DTOs
     */
    @Transactional
    public List<NoteDto> findByStatus(String status) {
        NoteStatus noteStatus = NoteStatus.valueOf(status);
        List<NoteDto> noteDtos = noteRepository.findByStatus(noteStatus)
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
        return authorEnrichmentService.enrichNoteDtos(noteDtos);
    }

    /**
     * Finds all active notes (status = ACTIVE).
     *
     * @return list of active note DTOs
     */
    @Transactional
    public List<NoteDto> findAllActive() {
        List<NoteDto> noteDtos = noteRepository.findAllActive()
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
        return authorEnrichmentService.enrichNoteDtos(noteDtos);
    }

    /**
     * Finds archived notes accessible to the current user.
     *
     * Access rules:
     * - SUPERVISOR: Can see all archived notes (from any author)
     * - Regular user: Can see only their own archived notes
     *
     * @param currentUserId the ID of the current authenticated user
     * @param isSupervisor whether the user has SUPERVISOR role
     * @return list of archived note DTOs accessible to the user
     */
    @Transactional
    public List<NoteDto> findArchivedNotes(UUID currentUserId, boolean isSupervisor) {
        // Get all archived notes from repository
        List<Note> archivedNotes = noteRepository.findByStatus(NoteStatus.ARCHIVED);

        // Filter based on user role
        if (!isSupervisor) {
            // Regular users: only see their own archived notes
            archivedNotes = archivedNotes.stream()
                    .filter(note -> note.getAuthorId().id().equals(currentUserId))
                    .collect(Collectors.toList());
        }
        // SUPERVISOR: sees all archived notes (no filtering)

        // Map to DTOs and enrich with author info
        List<NoteDto> noteDtos = archivedNotes.stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());

        return authorEnrichmentService.enrichNoteDtos(noteDtos);
    }
}
