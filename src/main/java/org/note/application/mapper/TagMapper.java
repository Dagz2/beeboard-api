package org.note.application.mapper;

import org.note.application.dto.query.AuditInfoDto;
import org.note.application.dto.query.TagDto;
import org.note.domain.model.Tag;

/**
 * Mapper for converting Tag domain objects to DTOs.
 */
public class TagMapper {

    private TagMapper() {
        // Utility class
    }

    /**
     * Converts a Tag domain object to a TagDto.
     */
    public static TagDto toDto(Tag tag) {
        if (tag == null) {
            return null;
        }

        return new TagDto(
                tag.getId().id(),
                tag.getName().value(),
                tag.getUsageCount(),
                new AuditInfoDto(
                        tag.getAuditInfo().getCreatedAt(),
                        tag.getAuditInfo().getUpdatedAt()
                )
        );
    }
}
