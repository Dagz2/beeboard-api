package org.note.application.usecase.command.note;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.note.ChangeNoteCategoryCommand;
import org.note.domain.exception.NoteNotFoundException;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.domain.service.NoteAccessValidator;

/**
 * Use case for changing a note's category after creation.
 *
 * <p>Authorization: Only the note author can change the category.
 *
 * <p>Business Rules:
 * <ul>
 *   <li>Only note author can change category</li>
 *   <li>Category must be one of the predefined NoteCategory enum values</li>
 *   <li>Categories: BUG, TIP, DOCUMENTATION, QUESTION, ANNOUNCEMENT, RECOMMENDATION, BUSINESS_RULE, CODE_SNIPPET</li>
 * </ul>
 *
 * <p>Use cases:
 * <ul>
 *   <li>Note was initially categorized incorrectly</li>
 *   <li>Note's purpose evolved (e.g., QUESTION became TIP after solution found)</li>
 *   <li>Better categorization discovered after creation</li>
 * </ul>
 */
@ApplicationScoped
public class ChangeNoteCategory {

    @Inject
    NoteRepository noteRepository;

    @Inject
    NoteAccessValidator accessValidator;

    /**
     * Executes the change note category use case.
     *
     * @param command the command containing noteId and new category
     * @param userContext the authenticated user context
     * @throws NoteNotFoundException if the note doesn't exist
     * @throws org.note.domain.exception.UnauthorizedNoteAccessException if the user is not the note author
     * @throws IllegalArgumentException if the category string is invalid
     */
    @Transactional
    public void execute(ChangeNoteCategoryCommand command, NoteUserContext userContext) {
        // 1. Find note
        NoteId noteId = new NoteId(command.noteId());
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> NoteNotFoundException.withId(noteId));

        // 2. Authorize - only note author can change category
        accessValidator.validateCanEditNote(note, userContext);

        // 3. Convert category string to enum
        NoteCategory newCategory = NoteCategory.valueOf(command.category());

        // 4. Execute domain logic
        note.changeCategory(newCategory);

        // 5. Persist
        noteRepository.update(note);
    }
}
