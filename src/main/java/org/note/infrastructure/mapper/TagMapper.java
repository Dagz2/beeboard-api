package org.note.infrastructure.mapper;

import org.note.domain.model.AuditInfo;
import org.note.domain.model.Tag;
import org.note.domain.model.TagId;
import org.note.domain.model.TagName;
import org.note.infrastructure.entity.AuditInfoEmbeddable;
import org.note.infrastructure.entity.TagEntity;

import java.util.Objects;

/**
 * Mapper for converting between Tag domain objects and TagEntity.
 */
public class TagMapper {

    private TagMapper() {
        // Utility class
    }

    /**
     * Converts a TagEntity to a Tag domain object.
     */
    public static Tag toDomain(TagEntity entity) {
        Objects.requireNonNull(entity, "TagEntity cannot be null");

        return Tag.reconstitute(
                new TagId(entity.getTagId()),
                new TagName(entity.getName()),
                entity.getUsageCount(),
                new AuditInfo(
                        entity.getAuditInfo().getCreatedAt(),
                        entity.getAuditInfo().getUpdatedAt()
                )
        );
    }

    /**
     * Converts a Tag domain object to a TagEntity.
     */
    public static TagEntity toEntity(Tag tag) {
        Objects.requireNonNull(tag, "Tag cannot be null");

        TagEntity entity = new TagEntity();
        entity.setTagId(tag.getId().id());
        entity.setName(tag.getName().value());
        entity.setUsageCount(tag.getUsageCount());

        AuditInfoEmbeddable auditInfo = new AuditInfoEmbeddable();
        auditInfo.setCreatedAt(tag.getAuditInfo().getCreatedAt());
        auditInfo.setUpdatedAt(tag.getAuditInfo().getUpdatedAt());
        entity.setAuditInfo(auditInfo);

        return entity;
    }
}
