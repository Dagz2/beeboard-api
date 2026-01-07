package org.note.application.usecase.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.query.NoteDto;
import org.note.application.mapper.NoteMapper;
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

    /**
     * Finds a note by its ID.
     *
     * @param noteId the note ID
     * @return the note DTO if found, empty otherwise
     */
    @Transactional
    public Optional<NoteDto> findById(UUID noteId) {
        return noteRepository.findById(new NoteId(noteId))
                .map(NoteMapper::toDto);
    }

    /**
     * Finds all notes by a specific author.
     *
     * @param authorId the author ID
     * @return list of note DTOs
     */
    @Transactional
    public List<NoteDto> findByAuthorId(UUID authorId) {
        return noteRepository.findByAuthorId(new AuthorId(authorId))
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
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
        return noteRepository.findByCategory(noteCategory)
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
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
        return noteRepository.findByVisibility(noteVisibility)
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds all notes linked to a specific project.
     *
     * @param projectId the project ID
     * @return list of note DTOs
     */
    @Transactional
    public List<NoteDto> findByProjectId(UUID projectId) {
        return noteRepository.findByProjectId(new ProjectId(projectId))
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds all notes with a specific tag.
     *
     * @param tagId the tag ID
     * @return list of note DTOs
     */
    @Transactional
    public List<NoteDto> findByTagId(UUID tagId) {
        return noteRepository.findByTagId(new TagId(tagId))
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
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
        return noteRepository.findByStatus(noteStatus)
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds all active notes (status = ACTIVE).
     *
     * @return list of active note DTOs
     */
    @Transactional
    public List<NoteDto> findAllActive() {
        return noteRepository.findAllActive()
                .stream()
                .map(NoteMapper::toDto)
                .collect(Collectors.toList());
    }
}
