package org.note.application.usecase.command.note;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.note.UpdateNoteCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for updating a note's title and content.
 */
@ApplicationScoped
public class UpdateNote {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the update note command.
     *
     * @param command the command containing updated data
     * @param userContext the user context
     */
    @Transactional
    public void execute(UpdateNoteCommand command, NoteUserContext userContext) {
        // Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // Validate access
        accessValidator.validateCanEditNote(note, userContext);

        // Update note
        NoteTitle newTitle = new NoteTitle(command.title());
        NoteContent newContent = new NoteContent(
                command.content(),
                ContentType.valueOf(command.contentType())
        );

        note.updateTitle(newTitle);
        note.updateContent(newContent);

        // Persist
        noteRepository.update(note);
    }
}
