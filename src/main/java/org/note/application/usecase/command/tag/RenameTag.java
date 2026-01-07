package org.note.application.usecase.command.tag;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.tag.RenameTagCommand;
import org.note.domain.exception.TagNotFoundException;
import org.note.domain.model.NoteUserContext;
import org.note.domain.model.Tag;
import org.note.domain.model.TagId;
import org.note.domain.model.TagName;
import org.note.domain.repository.TagRepository;

/**
 * Use case for renaming a tag.
 */
@ApplicationScoped
public class RenameTag {

    @Inject
    TagRepository tagRepository;

    /**
     * Executes the rename tag command.
     *
     * @param command the command
     * @param userContext the user context for audit trail
     */
    @Transactional
    public void execute(RenameTagCommand command, NoteUserContext userContext) {
        // Find tag
        TagId tagId = new TagId(command.tagId());
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> TagNotFoundException.withId(tagId));

        // Check if new name already exists
        TagName newName = new TagName(command.newName());
        if (tagRepository.existsByName(newName)) {
            throw new IllegalArgumentException("Tag with name '" + command.newName() + "' already exists");
        }

        // Rename tag
        tag.rename(newName);

        // Persist
        tagRepository.update(tag);
    }
}
