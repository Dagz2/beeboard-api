package org.note.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * JPA entity for Tag aggregate.
 */
@Entity
@Table(name = "tag")
public class TagEntity extends PanacheEntityBase {

    @Id
    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @NotNull
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @NotNull
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;

    @Embedded
    @NotNull
    private AuditInfoEmbeddable auditInfo;

    public TagEntity() {
    }

    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(UUID tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public AuditInfoEmbeddable getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoEmbeddable auditInfo) {
        this.auditInfo = auditInfo;
    }
}
