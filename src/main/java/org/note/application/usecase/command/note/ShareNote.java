package org.note.application.usecase.command.note;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.note.ShareNoteCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for sharing a note with a user.
 */
@ApplicationScoped
public class ShareNote {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the share note command.
     *
     * @param command the command
     * @param userContext the user context
     */
    @Transactional
    public void execute(ShareNoteCommand command, NoteUserContext userContext) {
        // Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // Validate access
        accessValidator.validateCanEditNote(note, userContext);

        // Create recipient and share
        AuthorId recipientUserId = new AuthorId(command.userId());
        Recipient recipient = Recipient.now(recipientUserId);
        note.shareWith(recipient);

        // Persist
        noteRepository.update(note);
    }
}
