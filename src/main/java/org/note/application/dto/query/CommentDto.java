package org.note.application.dto.query;

import java.util.UUID;

/**
 * DTO for Comment query results.
 */
public class CommentDto {
    private UUID id;
    private UUID authorId;
    private String content;
    private UUID parentCommentId;
    private String solutionStatus;
    private String moderationStatus;
    private AuditInfoDto auditInfo;

    public CommentDto() {
    }

    public CommentDto(UUID id, UUID authorId, String content, UUID parentCommentId,
                      String solutionStatus, String moderationStatus, AuditInfoDto auditInfo) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.solutionStatus = solutionStatus;
        this.moderationStatus = moderationStatus;
        this.auditInfo = auditInfo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(UUID parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getSolutionStatus() {
        return solutionStatus;
    }

    public void setSolutionStatus(String solutionStatus) {
        this.solutionStatus = solutionStatus;
    }

    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public AuditInfoDto getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoDto auditInfo) {
        this.auditInfo = auditInfo;
    }
}
