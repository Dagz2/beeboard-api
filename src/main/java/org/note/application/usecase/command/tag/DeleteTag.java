package org.note.application.usecase.command.tag;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.tag.DeleteTagCommand;
import org.note.domain.exception.TagNotFoundException;
import org.note.domain.model.Tag;
import org.note.domain.model.TagId;
import org.note.domain.repository.TagRepository;

/**
 * Use case for deleting a tag.
 * Tag must be unused (usageCount = 0) to be deleted.
 */
@ApplicationScoped
public class DeleteTag {

    @Inject
    TagRepository tagRepository;

    /**
     * Executes the delete tag command.
     *
     * @param command the command
     */
    @Transactional
    public void execute(DeleteTagCommand command) {
        // Find tag
        TagId tagId = new TagId(command.tagId());
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> TagNotFoundException.withId(tagId));

        // Check if tag is unused
        if (!tag.isUnused()) {
            throw new IllegalStateException("Cannot delete tag that is in use. Usage count: " + tag.getUsageCount());
        }

        // Delete tag
        tagRepository.deleteById(tagId);
    }
}
