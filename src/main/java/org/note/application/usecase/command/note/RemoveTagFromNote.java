package org.note.application.usecase.command.note;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.note.RemoveTagFromNoteCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.exception.TagNotFoundException;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.domain.repository.TagRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for removing a tag from a note.
 */
@ApplicationScoped
public class RemoveTagFromNote {

    @Inject
    NoteRepository noteRepository;

    @Inject
    TagRepository tagRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the remove tag from note command.
     *
     * @param command the command
     * @param userContext the user context
     */
    @Transactional
    public void execute(RemoveTagFromNoteCommand command, NoteUserContext userContext) {
        // Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // Validate access
        accessValidator.validateCanEditNote(note, userContext);

        // Find tag and decrement usage
        TagId tagId = new TagId(command.tagId());
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> TagNotFoundException.withId(tagId));

        // Remove tag from note
        note.removeTag(tagId);
        tag.decrementUsage();

        // Persist
        noteRepository.update(note);
        tagRepository.update(tag);
    }
}
