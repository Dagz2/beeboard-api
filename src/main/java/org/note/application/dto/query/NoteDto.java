package org.note.application.dto.query;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for Note query results.
 */
public class NoteDto {
    private UUID id;
    private String title;
    private String content;
    private String contentType;
    private String category;
    private String visibility;
    private String status;

    @Deprecated(since = "v2.0", forRemoval = false)
    private UUID authorId;  // Kept for backward compatibility

    private AuthorDto author;  // NEW: Enriched author information

    private UUID projectId;
    private Set<UUID> tagIds;
    private Set<RecipientDto> recipients;
    private List<CommentDto> comments;
    private AuditInfoDto auditInfo;
    private Instant archivedAt;

    public NoteDto() {
    }

    public NoteDto(UUID id, String title, String content, String contentType, String category,
                   String visibility, String status, UUID authorId, UUID projectId,
                   Set<UUID> tagIds, Set<RecipientDto> recipients, List<CommentDto> comments,
                   AuditInfoDto auditInfo, Instant archivedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.contentType = contentType;
        this.category = category;
        this.visibility = visibility;
        this.status = status;
        this.authorId = authorId;
        this.projectId = projectId;
        this.tagIds = tagIds;
        this.recipients = recipients;
        this.comments = comments;
        this.auditInfo = auditInfo;
        this.archivedAt = archivedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Deprecated(since = "v2.0", forRemoval = false)
    public UUID getAuthorId() {
        return authorId;
    }

    @Deprecated(since = "v2.0", forRemoval = false)
    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public AuthorDto getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDto author) {
        this.author = author;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public Set<UUID> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<UUID> tagIds) {
        this.tagIds = tagIds;
    }

    public Set<RecipientDto> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<RecipientDto> recipients) {
        this.recipients = recipients;
    }

    public List<CommentDto> getComments() {
        return comments;
    }

    public void setComments(List<CommentDto> comments) {
        this.comments = comments;
    }

    public AuditInfoDto getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoDto auditInfo) {
        this.auditInfo = auditInfo;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }
}
