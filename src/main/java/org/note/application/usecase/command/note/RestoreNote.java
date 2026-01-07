package org.note.application.usecase.command.note;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.note.RestoreNoteCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.Note;
import org.note.domain.model.NoteId;
import org.note.domain.model.NoteUserContext;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for restoring an archived note.
 */
@ApplicationScoped
public class RestoreNote {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the restore note command.
     *
     * @param command the command
     * @param userContext the user context
     */
    @Transactional
    public void execute(RestoreNoteCommand command, NoteUserContext userContext) {
        // Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // Validate access
        accessValidator.validateCanArchiveNote(note, userContext);

        // Restore note
        note.restore();

        // Persist
        noteRepository.update(note);
    }
}
