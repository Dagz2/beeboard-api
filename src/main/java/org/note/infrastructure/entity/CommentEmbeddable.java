package org.note.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Embeddable object representing a comment within a note.
 * Comments are part of the Note aggregate and stored within the note.
 */
@Embeddable
public class CommentEmbeddable {

    @NotNull
    @Column(name = "comment_id", nullable = false)
    private UUID commentId;

    @NotNull
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @NotNull
    @Column(name = "content", length = 5000, nullable = false)
    private String content;

    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @NotNull
    @Column(name = "solution_status", length = 20, nullable = false)
    private String solutionStatus;

    @NotNull
    @Column(name = "moderation_status", length = 20, nullable = false)
    private String moderationStatus;

    @Embedded
    @NotNull
    private AuditInfoEmbeddable auditInfo;

    public CommentEmbeddable() {
    }

    public CommentEmbeddable(UUID commentId, UUID authorId, String content, UUID parentCommentId,
                             String solutionStatus, String moderationStatus, AuditInfoEmbeddable auditInfo) {
        this.commentId = commentId;
        this.authorId = authorId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.solutionStatus = solutionStatus;
        this.moderationStatus = moderationStatus;
        this.auditInfo = auditInfo;
    }

    public UUID getCommentId() {
        return commentId;
    }

    public void setCommentId(UUID commentId) {
        this.commentId = commentId;
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

    public AuditInfoEmbeddable getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoEmbeddable auditInfo) {
        this.auditInfo = auditInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentEmbeddable that)) return false;
        return commentId.equals(that.commentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId);
    }
}
