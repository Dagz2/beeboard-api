package org.note.application.usecase.command.note;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.note.UnshareNoteCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.AuthorId;
import org.note.domain.model.Note;
import org.note.domain.model.NoteId;
import org.note.domain.model.NoteUserContext;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for unsharing a note with a user.
 */
@ApplicationScoped
public class UnshareNote {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the unshare note command.
     *
     * @param command the command
     * @param userContext the user context
     */
    @Transactional
    public void execute(UnshareNoteCommand command, NoteUserContext userContext) {
        // Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // Validate access
        accessValidator.validateCanEditNote(note, userContext);

        // Unshare with user
        AuthorId recipientUserId = new AuthorId(command.userId());
        note.unshareWith(recipientUserId);

        // Persist
        noteRepository.update(note);
    }
}
