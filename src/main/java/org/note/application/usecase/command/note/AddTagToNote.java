package org.note.application.usecase.command.note;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.note.AddTagToNoteCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.exception.TagNotFoundException;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.domain.repository.TagRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for adding a tag to a note.
 */
@ApplicationScoped
public class AddTagToNote {

    @Inject
    NoteRepository noteRepository;

    @Inject
    TagRepository tagRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the add tag to note command.
     *
     * @param command the command
     * @param userContext the user context
     */
    @Transactional
    public void execute(AddTagToNoteCommand command, NoteUserContext userContext) {
        // Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // Validate access
        accessValidator.validateCanEditNote(note, userContext);

        // Find tag and increment usage
        TagId tagId = new TagId(command.tagId());
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> TagNotFoundException.withId(tagId));

        // Add tag to note
        note.addTag(tagId);
        tag.incrementUsage();

        // Persist
        noteRepository.update(note);
        tagRepository.update(tag);
    }
}
