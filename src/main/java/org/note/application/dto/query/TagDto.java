package org.note.application.dto.query;

import java.util.UUID;

/**
 * DTO for Tag query results.
 */
public class TagDto {
    private UUID id;
    private String name;
    private int usageCount;
    private AuditInfoDto auditInfo;

    public TagDto() {
    }

    public TagDto(UUID id, String name, int usageCount, AuditInfoDto auditInfo) {
        this.id = id;
        this.name = name;
        this.usageCount = usageCount;
        this.auditInfo = auditInfo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public AuditInfoDto getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoDto auditInfo) {
        this.auditInfo = auditInfo;
    }
}
