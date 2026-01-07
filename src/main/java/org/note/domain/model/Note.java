package org.note.domain.model;

import org.note.domain.exception.InvalidNoteOperationException;

import java.time.Instant;
import java.util.*;

/**
 * Aggregate Root representing a Note in the knowledge management system.
 *
 * <p><strong>DDD Pattern:</strong> This class follows the Aggregate Root pattern from
 * Domain-Driven Design. It controls access to all entities and value objects within
 * its aggregate boundary, ensuring consistency and enforcing business rules.
 *
 * <p>A Note is a piece of content containing information to be shared on various topics.
 * Notes support discussions through comments, which may lead to validated solutions for
 * questions or bugs. The Note aggregate contains Comments as entities within its boundary.
 *
 * <p><strong>Aggregate Boundary:</strong>
 * <ul>
 *   <li>Note (Aggregate Root)</li>
 *   <li>Comment (Entity, lifecycle bound to Note)</li>
 *   <li>Various Value Objects (NoteTitle, NoteContent, etc.)</li>
 * </ul>
 *
 * <p><strong>Invariants:</strong>
 * <ul>
 *   <li>A Note MUST have exactly one Author (immutable)</li>
 *   <li>A Note MUST have exactly one Category (mandatory classification)</li>
 *   <li>A Note MUST have a Visibility type</li>
 *   <li>A Note CAN have zero or more Tags (flexible keywords)</li>
 *   <li>A Note CAN have zero or more Comments forming a Discussion</li>
 *   <li>A Note CAN be linked to zero or one Project (required for PROJECT visibility)</li>
 *   <li>If visibility = PROJECT, projectId MUST be non-null</li>
 *   <li>Only ACTIVE notes can receive new comments</li>
 *   <li>Comments are owned by the Note aggregate</li>
 * </ul>
 *
 * <p><strong>Lifecycle:</strong> ACTIVE → ARCHIVED (soft delete) → DELETED (hard delete available to SUPERVISOR)
 */
public class Note {

    private final NoteId id;
    private NoteTitle title;
    private NoteContent content;
    private NoteCategory category;
    private NoteVisibility visibility;
    private NoteStatus status;
    private final AuthorId authorId;
    private ProjectId projectId;
    private final Set<TagId> tagIds;
    private final Set<Recipient> recipients;
    private final List<Comment> comments;
    private final AuditInfo auditInfo;
    private Instant archivedAt;

    // -------------------- Creation --------------------

    private Note(NoteId id, NoteTitle title, NoteContent content, NoteCategory category,
                 NoteVisibility visibility, NoteStatus status, AuthorId authorId, ProjectId projectId,
                 Set<TagId> tagIds, Set<Recipient> recipients, List<Comment> comments,
                 AuditInfo auditInfo, Instant archivedAt) {
        this.id = Objects.requireNonNull(id, "Note ID is required");
        this.title = Objects.requireNonNull(title, "Title is required");
        this.content = Objects.requireNonNull(content, "Content is required");
        this.category = Objects.requireNonNull(category, "Category is required");
        this.visibility = Objects.requireNonNull(visibility, "Visibility is required");
        this.status = Objects.requireNonNull(status, "Status is required");
        this.authorId = Objects.requireNonNull(authorId, "Author ID is required");
        this.projectId = projectId;
        this.tagIds = Objects.requireNonNull(tagIds, "Tag IDs set cannot be null");
        this.recipients = Objects.requireNonNull(recipients, "Recipients set cannot be null");
        this.comments = Objects.requireNonNull(comments, "Comments list cannot be null");
        this.auditInfo = Objects.requireNonNull(auditInfo, "Audit info is required");
        this.archivedAt = archivedAt;

        validateProjectLinkForVisibility();
    }

    /**
     * Factory method to create a new Note aggregate.
     *
     * <p>This is the primary way to create new notes in the system. The note is
     * created in ACTIVE status with a newly generated ID, empty collections for
     * tags, recipients, and comments, and current timestamp for audit information.
     *
     * @param title the note title (must not be null)
     * @param content the note content body and type (must not be null)
     * @param category the mandatory note category classification (must not be null)
     * @param visibility the visibility scope determining access (must not be null)
     * @param authorId the identifier of the note author (must not be null, immutable)
     * @param projectId the project identifier if visibility is PROJECT (can be null for other visibilities)
     * @return a new Note instance in ACTIVE status
     * @throws InvalidNoteOperationException if visibility is PROJECT but projectId is null
     */
    public static Note create(NoteTitle title, NoteContent content, NoteCategory category,
                              NoteVisibility visibility, AuthorId authorId, ProjectId projectId) {
        Note note = new Note(
                NoteId.newId(),
                title,
                content,
                category,
                visibility,
                NoteStatus.ACTIVE,
                authorId,
                projectId,
                new HashSet<>(),
                new HashSet<>(),
                new ArrayList<>(),
                AuditInfo.of(Instant.now()),
                null
        );
        note.validateProjectLinkForVisibility();
        return note;
    }

    /**
     * Reconstitutes a Note aggregate from persistence.
     *
     * <p>This factory method is used by the infrastructure layer to rebuild the
     * Note aggregate from its persisted state. It preserves all state including
     * collections and audit information. Collections are defensively copied to
     * ensure encapsulation.
     *
     * @param id the note identifier
     * @param title the note title
     * @param content the note content
     * @param category the note category
     * @param visibility the visibility scope
     * @param status the current lifecycle status
     * @param authorId the author identifier
     * @param projectId the project identifier (can be null)
     * @param tagIds the set of tag identifiers
     * @param recipients the set of recipients for sharing
     * @param comments the list of comments in the discussion
     * @param auditInfo the audit timestamps
     * @param archivedAt the archival timestamp (null if not archived)
     * @return the reconstituted Note aggregate
     */
    public static Note reconstitute(NoteId id, NoteTitle title, NoteContent content, NoteCategory category,
                                     NoteVisibility visibility, NoteStatus status, AuthorId authorId,
                                     ProjectId projectId, Set<TagId> tagIds, Set<Recipient> recipients,
                                     List<Comment> comments, AuditInfo auditInfo, Instant archivedAt) {
        return new Note(id, title, content, category, visibility, status, authorId, projectId,
                new HashSet<>(tagIds), new HashSet<>(recipients), new ArrayList<>(comments),
                auditInfo, archivedAt);
    }

    // -------------------- Domain methods - Basic updates --------------------

    /**
     * Updates the note title.
     *
     * @param newTitle the new title for the note (must not be null)
     * @throws NullPointerException if newTitle is null
     */
    public void updateTitle(NoteTitle newTitle) {
        this.title = Objects.requireNonNull(newTitle, "Title cannot be null");
        this.auditInfo.touch();
    }

    /**
     * Updates the note content.
     *
     * @param newContent the new content for the note (must not be null)
     * @throws NullPointerException if newContent is null
     */
    public void updateContent(NoteContent newContent) {
        this.content = Objects.requireNonNull(newContent, "Content cannot be null");
        this.auditInfo.touch();
    }

    /**
     * Changes the note category.
     *
     * @param newCategory the new category classification (must not be null)
     * @throws NullPointerException if newCategory is null
     */
    public void changeCategory(NoteCategory newCategory) {
        this.category = Objects.requireNonNull(newCategory, "Category cannot be null");
        this.auditInfo.touch();
    }

    /**
     * Changes the note visibility scope.
     *
     * <p>Validates that if the new visibility is PROJECT, a projectId must be set.
     * This ensures the invariant that PROJECT-visible notes are always linked to a project.
     *
     * @param newVisibility the new visibility scope (must not be null)
     * @throws NullPointerException if newVisibility is null
     * @throws InvalidNoteOperationException if new visibility is PROJECT but projectId is null
     */
    public void changeVisibility(NoteVisibility newVisibility) {
        this.visibility = Objects.requireNonNull(newVisibility, "Visibility cannot be null");
        validateProjectLinkForVisibility();
        this.auditInfo.touch();
    }

    /**
     * Links the note to a project.
     *
     * <p>Use this method to associate a note with a specific project, typically
     * when changing visibility to PROJECT or when organizing project-related notes.
     *
     * @param newProjectId the project identifier to link (can be null to remove link if visibility allows)
     */
    public void linkToProject(ProjectId newProjectId) {
        this.projectId = newProjectId;
        this.auditInfo.touch();
    }

    /**
     * Unlinks the note from its current project.
     *
     * <p>This operation is only allowed if the note's visibility is not PROJECT.
     * PROJECT-visible notes must always be linked to a project to maintain consistency.
     *
     * @throws InvalidNoteOperationException if visibility is PROJECT (cannot unlink)
     */
    public void unlinkFromProject() {
        if (visibility == NoteVisibility.PROJECT) {
            throw InvalidNoteOperationException.projectRequiredForVisibility();
        }
        this.projectId = null;
        this.auditInfo.touch();
    }

    // -------------------- Domain methods - Status Lifecycle --------------------

    /**
     * Archives the note (soft delete).
     *
     * <p>Moves the note to ARCHIVED status, making it read-only and visible only
     * to the author and SUPERVISOR. Records the archival timestamp for audit purposes.
     * Archived notes can be restored.
     */
    public void archive() {
        this.status = NoteStatus.ARCHIVED;
        this.archivedAt = Instant.now();
        this.auditInfo.touch();
    }

    /**
     * Restores the note from archived status.
     *
     * <p>Moves the note back to ACTIVE status, making it editable and visible
     * according to its visibility rules. Clears the archival timestamp.
     */
    public void restore() {
        this.status = NoteStatus.ACTIVE;
        this.archivedAt = null;
        this.auditInfo.touch();
    }

    /**
     * Soft deletes the note.
     *
     * <p>Moves the note to DELETED status. Soft-deleted notes are only visible
     * to SUPERVISOR for potential recovery. Hard deletion (permanent removal) is
     * a repository operation that should only be performed by SUPERVISOR.
     */
    public void softDelete() {
        this.status = NoteStatus.DELETED;
        this.auditInfo.touch();
    }

    // -------------------- Domain methods - Tags --------------------

    /**
     * Adds a tag to the note.
     *
     * <p>Tags are flexible user-defined keywords used to organize and categorize notes.
     * A note can have multiple tags.
     *
     * @param tagId the identifier of the tag to add (must not be null)
     * @throws NullPointerException if tagId is null
     */
    public void addTag(TagId tagId) {
        Objects.requireNonNull(tagId, "Tag ID cannot be null");
        this.tagIds.add(tagId);
        this.auditInfo.touch();
    }

    /**
     * Removes a tag from the note.
     *
     * @param tagId the identifier of the tag to remove (must not be null)
     * @throws NullPointerException if tagId is null
     */
    public void removeTag(TagId tagId) {
        Objects.requireNonNull(tagId, "Tag ID cannot be null");
        this.tagIds.remove(tagId);
        this.auditInfo.touch();
    }

    /**
     * Checks if the note has any tags.
     *
     * @return true if the note has at least one tag, false otherwise
     */
    public boolean hasTags() {
        return !tagIds.isEmpty();
    }

    /**
     * Checks if the note has a specific tag.
     *
     * @param tagId the tag identifier to check
     * @return true if the note has this tag, false otherwise
     */
    public boolean hasTag(TagId tagId) {
        return tagIds.contains(tagId);
    }

    /**
     * Returns an unmodifiable view of the tag IDs.
     *
     * <p>Returns a defensive copy to preserve encapsulation and prevent
     * external modification of the internal collection.
     *
     * @return an unmodifiable set of tag identifiers
     */
    public Set<TagId> getTagIds() {
        return Collections.unmodifiableSet(tagIds);
    }

    // -------------------- Domain methods - Recipients --------------------

    /**
     * Shares the note with a recipient user.
     *
     * <p>Used for explicit sharing, particularly with PRIVATE notes or to
     * notify specific users about SHARED notes.
     *
     * @param recipient the recipient to share with (must not be null)
     * @throws NullPointerException if recipient is null
     */
    public void shareWith(Recipient recipient) {
        Objects.requireNonNull(recipient, "Recipient cannot be null");
        this.recipients.add(recipient);
        this.auditInfo.touch();
    }

    /**
     * Removes sharing with a specific user.
     *
     * @param userId the identifier of the user to unshare with (must not be null)
     * @throws NullPointerException if userId is null
     */
    public void unshareWith(AuthorId userId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        this.recipients.removeIf(r -> r.userId().equals(userId));
        this.auditInfo.touch();
    }

    /**
     * Checks if the note is explicitly shared with a specific user.
     *
     * @param userId the user identifier to check
     * @return true if the note is shared with this user, false otherwise
     */
    public boolean isSharedWith(AuthorId userId) {
        return recipients.stream().anyMatch(r -> r.userId().equals(userId));
    }

    /**
     * Returns an unmodifiable view of the recipients.
     *
     * <p>Returns a defensive copy to preserve encapsulation and prevent
     * external modification of the internal collection.
     *
     * @return an unmodifiable set of recipients
     */
    public Set<Recipient> getRecipients() {
        return Collections.unmodifiableSet(recipients);
    }

    // -------------------- Domain methods - Comments (Discussion) --------------------

    /**
     * Adds a comment to the note's discussion.
     *
     * <p>Comments are entities within the Note aggregate boundary. The note must be
     * in ACTIVE status to accept new comments. This ensures discussions only occur
     * on active notes, not archived or deleted ones.
     *
     * @param comment the comment to add to the discussion (must not be null)
     * @throws NullPointerException if comment is null
     * @throws InvalidNoteOperationException if the note is not in ACTIVE status
     */
    public void addComment(Comment comment) {
        Objects.requireNonNull(comment, "Comment cannot be null");
        if (!isActive()) {
            throw InvalidNoteOperationException.cannotAddCommentToInactiveNote();
        }
        this.comments.add(comment);
        this.auditInfo.touch();
    }

    /**
     * Removes a comment from the note.
     *
     * <p>Authorization should be checked externally - only the comment author
     * or authorized moderators should be able to remove comments.
     *
     * @param commentId the identifier of the comment to remove (must not be null)
     * @throws NullPointerException if commentId is null
     */
    public void removeComment(CommentId commentId) {
        Objects.requireNonNull(commentId, "Comment ID cannot be null");
        this.comments.removeIf(c -> c.getId().equals(commentId));
        this.auditInfo.touch();
    }

    /**
     * Finds a comment by its identifier within this note's discussion.
     *
     * @param commentId the comment identifier to find
     * @return an Optional containing the comment if found, empty otherwise
     */
    public Optional<Comment> findComment(CommentId commentId) {
        return comments.stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst();
    }

    /**
     * Checks if the note has a validated solution comment.
     *
     * <p>A validated solution indicates that one of the comments has been marked
     * as the accepted answer/resolution for the note (typically for QUESTION or BUG categories).
     *
     * @return true if a validated solution exists, false otherwise
     */
    public boolean hasValidatedSolution() {
        return comments.stream().anyMatch(Comment::isSolution);
    }

    /**
     * Gets the validated solution comment if one exists.
     *
     * @return an Optional containing the validated solution comment, empty if none exists
     */
    public Optional<Comment> getValidatedSolution() {
        return comments.stream()
                .filter(Comment::isSolution)
                .findFirst();
    }

    /**
     * Returns an unmodifiable view of the comments.
     *
     * <p>Returns a defensive copy to preserve encapsulation and prevent
     * external modification of the internal collection.
     *
     * @return an unmodifiable list of comments in the discussion
     */
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    // -------------------- Domain methods - Query methods --------------------

    /**
     * Checks if the note is owned by the specified author.
     *
     * @param checkAuthorId the author identifier to check
     * @return true if this author owns the note, false otherwise
     */
    public boolean isOwnedBy(AuthorId checkAuthorId) {
        return this.authorId.equals(checkAuthorId);
    }

    /**
     * Checks if the note is in ACTIVE status.
     *
     * @return true if the note is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return status == NoteStatus.ACTIVE;
    }

    /**
     * Checks if the note is in ARCHIVED status.
     *
     * @return true if the note is ARCHIVED, false otherwise
     */
    public boolean isArchived() {
        return status == NoteStatus.ARCHIVED;
    }

    /**
     * Checks if the note is in DELETED status.
     *
     * @return true if the note is DELETED, false otherwise
     */
    public boolean isDeleted() {
        return status == NoteStatus.DELETED;
    }

    /**
     * Checks if the note requires a project link (visibility is PROJECT).
     *
     * @return true if visibility is PROJECT, false otherwise
     */
    public boolean requiresProject() {
        return visibility == NoteVisibility.PROJECT;
    }

    /**
     * Checks if the note is linked to a specific project.
     *
     * @param checkProjectId the project identifier to check
     * @return true if the note is linked to this project, false otherwise
     */
    public boolean isLinkedToProject(ProjectId checkProjectId) {
        return projectId != null && projectId.equals(checkProjectId);
    }

    // -------------------- Private validation methods --------------------

    /**
     * Validates that if visibility is PROJECT, projectId must be set.
     */
    private void validateProjectLinkForVisibility() {
        if (visibility == NoteVisibility.PROJECT && projectId == null) {
            throw InvalidNoteOperationException.projectRequiredForVisibility();
        }
    }

    // -------------------- Getters --------------------

    public NoteId getId() {
        return id;
    }

    public NoteTitle getTitle() {
        return title;
    }

    public NoteContent getContent() {
        return content;
    }

    public NoteCategory getCategory() {
        return category;
    }

    public NoteVisibility getVisibility() {
        return visibility;
    }

    public NoteStatus getStatus() {
        return status;
    }

    public AuthorId getAuthorId() {
        return authorId;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    // -------------------- Overrides --------------------

    /**
     * Two Note instances are considered equal if they share the same business identifier (NoteId).
     *
     * <p>This implements the DDD Entity identity pattern where entity equality is based solely
     * on the unique identifier, not on mutable attributes. This ensures that a Note remains
     * the "same" note even as its content, status, or other properties change over time.
     *
     * @param o the object to compare with
     * @return true if both objects are the same instance or have equal NoteIds, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note note)) return false;
        return id.equals(note.id);
    }

    /**
     * Computes the hash code based solely on the NoteId to maintain consistency with equals().
     *
     * <p>Using only the ID ensures that the hash code remains stable throughout the entity's
     * lifecycle, even as mutable attributes change. This is essential for using Note instances
     * in hash-based collections.
     *
     * @return hash code derived from the NoteId
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of the Note with key identifying information.
     *
     * <p>Includes the note's ID, title, category, visibility, status, author, and comment count.
     * This provides a concise summary useful for debugging and logging.
     *
     * @return a string representation of the note
     */
    @Override
    public String toString() {
        return "Note{id=" + id + ", title=" + title + ", category=" + category +
                ", visibility=" + visibility + ", status=" + status +
                ", authorId=" + authorId + ", commentsCount=" + comments.size() + "}";
    }
}
