package org.note.application.usecase.command.note;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.note.ChangeNoteVisibilityCommand;
import org.note.domain.exception.InvalidNoteOperationException;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.port.ProjectAccessPort;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for changing a note's visibility after creation.
 *
 * <p>Authorization: Only the note author can change the visibility.
 *
 * <p>Business Rules:
 * <ul>
 *   <li>Only note author can change visibility</li>
 *   <li>Visibility must be one of: PRIVATE, SHARED, PROJECT</li>
 *   <li>PROJECT visibility requires a valid projectId</li>
 *   <li>Project must exist and be accessible</li>
 * </ul>
 *
 * <p>Use cases:
 * <ul>
 *   <li>Make private note shareable (PRIVATE → SHARED)</li>
 *   <li>Associate note with project (PRIVATE → PROJECT)</li>
 *   <li>Make project note private (PROJECT → PRIVATE)</li>
 *   <li>Change project association (PROJECT → different PROJECT)</li>
 * </ul>
 */
@ApplicationScoped
public class ChangeNoteVisibility {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    @Inject
    ProjectAccessPort projectAccessPort;

    /**
     * Executes the change note visibility use case.
     *
     * @param command the command containing noteId, new visibility, and optional projectId
     * @param userContext the authenticated user context
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if the user is not the note author
     * @throws IllegalArgumentException if the visibility string is invalid or project doesn't exist
     * @throws InvalidNoteOperationException if PROJECT visibility requested without projectId
     */
    @Transactional
    public void execute(ChangeNoteVisibilityCommand command, NoteUserContext userContext) {
        // 1. Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // 2. Authorize - only note author can change visibility
        accessValidator.validateCanEditNote(note, userContext);

        // 3. Convert visibility string to enum
        NoteVisibility newVisibility = NoteVisibility.valueOf(command.visibility());

        // 4. Validate and link project if visibility is PROJECT
        if (newVisibility == NoteVisibility.PROJECT) {
            if (command.projectId() == null) {
                throw InvalidNoteOperationException.projectRequiredForVisibility();
            }
            ProjectId projectId = new ProjectId(command.projectId());

            // Verify project exists
            if (!projectAccessPort.exists(projectId.id())) {
                throw new IllegalArgumentException("Project does not exist: " + projectId.id());
            }

            // Link to project first, then change visibility
            note.linkToProject(projectId);
        }

        // 5. Execute domain logic
        note.changeVisibility(newVisibility);

        // 6. Persist
        noteRepository.update(note);
    }
}
