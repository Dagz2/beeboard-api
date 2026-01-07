package org.note.application.usecase.command.note;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.note.CreateNoteCommand;
import org.note.domain.exception.InvalidNoteOperationException;
import org.note.domain.model.*;
import org.note.domain.port.ProjectAccessPort;
import org.note.domain.repository.NoteRepository;

/**
 * Use case for creating a new note.
 */
@ApplicationScoped
public class CreateNote {

    @Inject
    NoteRepository noteRepository;

    @Inject
    ProjectAccessPort projectAccessPort;

    /**
     * Executes the create note command.
     *
     * @param command the command containing note data
     * @param userContext the user context
     */
    @Transactional
    public void execute(CreateNoteCommand command, NoteUserContext userContext) {
        // Convert command to domain value objects
        NoteTitle title = new NoteTitle(command.title());
        NoteContent content = createNoteContent(command.content(), command.contentType());
        NoteCategory category = NoteCategory.valueOf(command.category());
        NoteVisibility visibility = NoteVisibility.valueOf(command.visibility());
        AuthorId authorId = userContext.toAuthorId();
        ProjectId projectId = command.projectId() != null ? new ProjectId(command.projectId()) : null;

        // Validate project link if visibility is PROJECT
        if (visibility == NoteVisibility.PROJECT) {
            if (projectId == null) {
                throw InvalidNoteOperationException.projectRequiredForVisibility();
            }
            if (!projectAccessPort.exists(projectId.id())) {
                throw new IllegalArgumentException("Project does not exist");
            }
        }

        // Create note using domain factory
        Note note = Note.create(title, content, category, visibility, authorId, projectId);

        // Persist
        noteRepository.save(note);
    }

    private NoteContent createNoteContent(String body, String contentType) {
        ContentType type = ContentType.valueOf(contentType);
        return new NoteContent(body, type);
    }
}
