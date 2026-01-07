package org.note.domain.repository;

import org.note.domain.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Note aggregate persistence.
 * Defines the contract for Note storage and retrieval.
 */
public interface NoteRepository {

    // -------------------- Query methods --------------------

    /**
     * Finds a note by its ID.
     *
     * @param id the note ID
     * @return the note if found, empty otherwise
     */
    Optional<Note> findById(NoteId id);

    /**
     * Finds all notes created by a specific author.
     *
     * @param authorId the author ID
     * @return list of notes by the author
     */
    List<Note> findByAuthorId(AuthorId authorId);

    /**
     * Finds all notes in a specific category.
     *
     * @param category the note category
     * @return list of notes in the category
     */
    List<Note> findByCategory(NoteCategory category);

    /**
     * Finds all notes with a specific visibility.
     *
     * @param visibility the note visibility
     * @return list of notes with the visibility
     */
    List<Note> findByVisibility(NoteVisibility visibility);

    /**
     * Finds all notes linked to a specific project.
     *
     * @param projectId the project ID
     * @return list of notes linked to the project
     */
    List<Note> findByProjectId(ProjectId projectId);

    /**
     * Finds all notes that have a specific tag.
     *
     * @param tagId the tag ID
     * @return list of notes with the tag
     */
    List<Note> findByTagId(TagId tagId);

    /**
     * Finds all notes with a specific status.
     *
     * @param status the note status
     * @return list of notes with the status
     */
    List<Note> findByStatus(NoteStatus status);

    /**
     * Finds all active notes (status = ACTIVE).
     *
     * @return list of active notes
     */
    List<Note> findAllActive();

    // -------------------- Persistence methods --------------------

    /**
     * Saves a new note.
     *
     * @param note the note to save
     */
    void save(Note note);

    /**
     * Updates an existing note.
     *
     * @param note the note to update
     */
    void update(Note note);

    // -------------------- Existence methods --------------------

    /**
     * Checks if a note exists with the given ID.
     *
     * @param id the note ID
     * @return true if exists, false otherwise
     */
    boolean existsById(NoteId id);

    // -------------------- Deletion methods --------------------

    /**
     * Hard deletes a note by its ID.
     * This is a permanent operation and should only be performed by SUPERVISOR.
     *
     * @param id the note ID
     */
    void deleteById(NoteId id);
}
