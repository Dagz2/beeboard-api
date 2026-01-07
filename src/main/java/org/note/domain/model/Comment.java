package org.note.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a Comment within a Note's discussion.
 *
 * <p><strong>DDD Pattern:</strong> This class follows the Entity pattern from Domain-Driven Design.
 * Comments are entities (not value objects) because they have distinct identity and mutable state,
 * but they are NOT aggregate roots. Comments exist within the Note aggregate boundary and their
 * lifecycle is controlled by the Note aggregate root.
 *
 * <p><strong>Aggregate Relationship:</strong> Comments belong to the Note aggregate and are managed
 * through the Note's {@link Note#addComment(AuthorId, String, CommentId)} method. They cannot be
 * created, modified, or deleted independently outside the Note aggregate context.
 *
 * <p><strong>Business Capabilities:</strong>
 * <ul>
 *   <li>Regular discussion: Comments support threaded discussions via parent-child relationships</li>
 *   <li>Solution workflow: Comments can be proposed as solutions and validated by note authors</li>
 *   <li>Moderation: Comments can be censored, flagged for review, or restored</li>
 * </ul>
 *
 * <p><strong>Invariants:</strong>
 * <ul>
 *   <li>A Comment MUST belong to exactly one Note (enforced by aggregate boundary)</li>
 *   <li>A Comment MUST have exactly one Author (immutable, set at creation)</li>
 *   <li>Content MUST be non-empty and not exceed 5000 characters</li>
 *   <li>Only the comment author can edit their own comments or propose them as solutions</li>
 *   <li>Only the note author (or SUPERVISOR) can validate proposed solutions</li>
 *   <li>Solution status transitions: REGULAR → PROPOSED → VALIDATED (or back to REGULAR)</li>
 * </ul>
 *
 * <p><strong>State Machine - Solution Status:</strong>
 * <pre>
 * REGULAR ←→ PROPOSED ←→ VALIDATED
 *    ↑          ↓
 *    └──────────┘
 * </pre>
 *
 * <p><strong>State Machine - Moderation Status:</strong>
 * <pre>
 * VISIBLE ←→ UNDER_REVIEW ←→ CENSORED
 *    ↑           ↓
 *    └───────────┘
 * </pre>
 */
public class Comment {

    private static final int MAX_CONTENT_LENGTH = 5000;

    private final CommentId id;
    private final AuthorId authorId;
    private String content;
    private final CommentId parentCommentId;
    private SolutionStatus solutionStatus;
    private ModerationStatus moderationStatus;
    private final AuditInfo auditInfo;

    // -------------------- Creation --------------------

    private Comment(CommentId id, AuthorId authorId, String content, CommentId parentCommentId,
                    SolutionStatus solutionStatus, ModerationStatus moderationStatus, AuditInfo auditInfo) {
        this.id = Objects.requireNonNull(id, "Comment ID is required");
        this.authorId = Objects.requireNonNull(authorId, "Author ID is required");
        this.content = validateContent(content);
        this.parentCommentId = parentCommentId;
        this.solutionStatus = Objects.requireNonNull(solutionStatus, "Solution status is required");
        this.moderationStatus = Objects.requireNonNull(moderationStatus, "Moderation status is required");
        this.auditInfo = Objects.requireNonNull(auditInfo, "Audit info is required");
    }

    /**
     * Factory method to create a new root-level comment (not a reply).
     *
     * <p>This convenience method creates a comment at the top level of a note's discussion,
     * without a parent comment. Use this for starting new discussion threads.
     *
     * <p>The comment is created with:
     * <ul>
     *   <li>Newly generated CommentId</li>
     *   <li>SolutionStatus.REGULAR (not proposed as solution)</li>
     *   <li>ModerationStatus.VISIBLE (no moderation issues)</li>
     *   <li>No parent comment (top-level)</li>
     *   <li>Current timestamp for audit information</li>
     * </ul>
     *
     * @param authorId the identifier of the comment author (must not be null)
     * @param content the comment text content (must not be null, 1-5000 characters)
     * @return a new root-level Comment instance
     * @throws NullPointerException if authorId or content is null
     * @throws IllegalArgumentException if content is blank or exceeds 5000 characters
     */
    public static Comment create(AuthorId authorId, String content) {
        return create(authorId, content, null);
    }

    /**
     * Factory method to create a new comment, optionally as a reply to another comment.
     *
     * <p>This is the primary factory method for creating comments. It supports both
     * root-level comments (parentCommentId = null) and threaded replies (parentCommentId != null).
     *
     * <p>The comment is created with:
     * <ul>
     *   <li>Newly generated CommentId</li>
     *   <li>SolutionStatus.REGULAR (not proposed as solution)</li>
     *   <li>ModerationStatus.VISIBLE (no moderation issues)</li>
     *   <li>Optional parent comment for threading</li>
     *   <li>Current timestamp for audit information</li>
     * </ul>
     *
     * @param authorId the identifier of the comment author (must not be null)
     * @param content the comment text content (must not be null, 1-5000 characters)
     * @param parentCommentId the identifier of the parent comment if this is a reply (can be null)
     * @return a new Comment instance
     * @throws NullPointerException if authorId or content is null
     * @throws IllegalArgumentException if content is blank or exceeds 5000 characters
     */
    public static Comment create(AuthorId authorId, String content, CommentId parentCommentId) {
        return new Comment(
                CommentId.newId(),
                authorId,
                content,
                parentCommentId,
                SolutionStatus.REGULAR,
                ModerationStatus.VISIBLE,
                AuditInfo.of(Instant.now())
        );
    }

    /**
     * Factory method to reconstitute a Comment from persistence.
     *
     * <p>This method is used by the infrastructure layer to rebuild Comment entities
     * from database records as part of reconstituting the Note aggregate. Unlike the
     * {@link #create(AuthorId, String, CommentId)} methods, this preserves all existing
     * state including ID, solution status, moderation status, and audit information.
     *
     * @param id the existing comment identifier (must not be null)
     * @param authorId the identifier of the comment author (must not be null)
     * @param content the comment text content (must not be null, 1-5000 characters)
     * @param parentCommentId the identifier of the parent comment if this is a reply (can be null)
     * @param solutionStatus the current solution status (must not be null)
     * @param moderationStatus the current moderation status (must not be null)
     * @param auditInfo the audit information with creation/modification timestamps (must not be null)
     * @return a reconstituted Comment instance with all historical state preserved
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if content is invalid
     */
    public static Comment reconstitute(CommentId id, AuthorId authorId, String content,
                                        CommentId parentCommentId, SolutionStatus solutionStatus,
                                        ModerationStatus moderationStatus, AuditInfo auditInfo) {
        return new Comment(id, authorId, content, parentCommentId, solutionStatus, moderationStatus, auditInfo);
    }

    // -------------------- Domain methods - Editing --------------------

    /**
     * Edits the comment content with new text.
     *
     * <p><strong>Authorization:</strong> Only the comment author should be allowed to call this method.
     * Authorization checks are enforced at the application layer before calling this domain method.
     *
     * <p>The content is validated to ensure it meets the same requirements as during creation
     * (non-empty, maximum 5000 characters).
     *
     * @param newContent the new comment text (must not be null, 1-5000 characters)
     * @throws NullPointerException if newContent is null
     * @throws IllegalArgumentException if newContent is blank or exceeds 5000 characters
     */
    public void edit(String newContent) {
        this.content = validateContent(newContent);
        this.auditInfo.touch();
    }

    // -------------------- Domain methods - Moderation --------------------

    /**
     * Censors the comment, hiding it from public view.
     *
     * <p>Censored comments are hidden from regular users but remain visible to moderators
     * and administrators for audit purposes. This is a soft-delete operation that preserves
     * the comment content for potential restoration.
     *
     * <p><strong>Authorization:</strong> Requires MODERATOR role or higher.
     *
     * <p>Use cases:
     * <ul>
     *   <li>Removing inappropriate or offensive content</li>
     *   <li>Hiding spam or off-topic comments</li>
     *   <li>Enforcing community guidelines</li>
     * </ul>
     */
    public void censor() {
        this.moderationStatus = ModerationStatus.CENSORED;
        this.auditInfo.touch();
    }

    /**
     * Flags the comment for moderation review.
     *
     * <p>Comments under review are temporarily hidden pending moderator decision.
     * This status is typically set automatically when users report a comment or when
     * automated content filters detect potential policy violations.
     *
     * <p><strong>Authorization:</strong> Can be triggered by any user (reporting) or moderators.
     */
    public void flagForReview() {
        this.moderationStatus = ModerationStatus.UNDER_REVIEW;
        this.auditInfo.touch();
    }

    /**
     * Restores the comment to visible status after moderation review.
     *
     * <p>This method is used to make a comment visible again after it was either:
     * <ul>
     *   <li>Under review and determined to be acceptable</li>
     *   <li>Censored but later deemed appropriate to restore</li>
     * </ul>
     *
     * <p><strong>Authorization:</strong> Requires MODERATOR role or higher.
     */
    public void restoreModeration() {
        this.moderationStatus = ModerationStatus.VISIBLE;
        this.auditInfo.touch();
    }

    // -------------------- Domain methods - Solution --------------------

    /**
     * Proposes this comment as a solution to the note's question or problem.
     *
     * <p>This method initiates the solution workflow, moving the comment from REGULAR status
     * to PROPOSED status. Proposed solutions await validation from the note author or a supervisor.
     *
     * <p><strong>Authorization:</strong> Only the comment author should be allowed to propose
     * their own comment as a solution. Authorization checks are enforced at the application layer.
     *
     * <p><strong>State transition:</strong> REGULAR → PROPOSED
     *
     * <p>Use cases:
     * <ul>
     *   <li>Answering a QUESTION note with a helpful response</li>
     *   <li>Providing a fix or workaround for a BUG note</li>
     *   <li>Offering a solution that resolves the note's core issue</li>
     * </ul>
     *
     * @throws IllegalStateException if the comment is not in REGULAR status
     */
    public void proposeAsSolution() {
        if (solutionStatus != SolutionStatus.REGULAR) {
            throw new IllegalStateException("Can only propose REGULAR comments as solutions");
        }
        this.solutionStatus = SolutionStatus.PROPOSED;
        this.auditInfo.touch();
    }

    /**
     * Validates this comment as the accepted solution to the note.
     *
     * <p>This method completes the solution workflow, moving the comment from PROPOSED status
     * to VALIDATED status. A validated solution indicates that the note's question or problem
     * has been resolved.
     *
     * <p><strong>Authorization:</strong> Only the note author (who owns the problem) or a
     * SUPERVISOR should be allowed to validate solutions. Authorization checks are enforced
     * at the application layer.
     *
     * <p><strong>State transition:</strong> PROPOSED → VALIDATED
     *
     * <p><strong>Business rule:</strong> Only one comment per note should be in VALIDATED status
     * at a time (enforced by the Note aggregate when calling this method).
     *
     * @throws IllegalStateException if the comment is not in PROPOSED status
     */
    public void validateSolution() {
        if (solutionStatus != SolutionStatus.PROPOSED) {
            throw new IllegalStateException("Can only validate PROPOSED solutions");
        }
        this.solutionStatus = SolutionStatus.VALIDATED;
        this.auditInfo.touch();
    }

    /**
     * Rejects a proposed solution, returning it to regular comment status.
     *
     * <p>This method is used when:
     * <ul>
     *   <li>The note author determines the proposed solution doesn't resolve the issue</li>
     *   <li>The proposed solution is incorrect or incomplete</li>
     *   <li>A better solution has been found</li>
     * </ul>
     *
     * <p><strong>State transition:</strong> PROPOSED → REGULAR
     *
     * <p>This is a graceful operation that silently succeeds even if the comment is not
     * in PROPOSED status (idempotent behavior).
     *
     * <p><strong>Authorization:</strong> Note author or SUPERVISOR role.
     */
    public void rejectSolution() {
        if (solutionStatus == SolutionStatus.PROPOSED) {
            this.solutionStatus = SolutionStatus.REGULAR;
            this.auditInfo.touch();
        }
    }

    /**
     * Unvalidates a previously validated solution, returning it to proposed status.
     *
     * <p>This method is used when:
     * <ul>
     *   <li>The validated solution is discovered to be incorrect or incomplete</li>
     *   <li>The problem context has changed, making the solution no longer valid</li>
     *   <li>The note author wants to accept a different solution instead</li>
     * </ul>
     *
     * <p><strong>State transition:</strong> VALIDATED → PROPOSED
     *
     * <p>This is a graceful operation that silently succeeds even if the comment is not
     * in VALIDATED status (idempotent behavior).
     *
     * <p><strong>Authorization:</strong> Note author or SUPERVISOR role.
     */
    public void unvalidateSolution() {
        if (solutionStatus == SolutionStatus.VALIDATED) {
            this.solutionStatus = SolutionStatus.PROPOSED;
            this.auditInfo.touch();
        }
    }

    // -------------------- Query methods --------------------

    /**
     * Checks if this comment is currently visible to regular users.
     *
     * @return true if moderation status is VISIBLE, false if censored or under review
     */
    public boolean isVisible() {
        return moderationStatus == ModerationStatus.VISIBLE;
    }

    /**
     * Checks if this comment has been censored by a moderator.
     *
     * @return true if moderation status is CENSORED, false otherwise
     */
    public boolean isCensored() {
        return moderationStatus == ModerationStatus.CENSORED;
    }

    /**
     * Checks if this comment is currently under moderation review.
     *
     * @return true if moderation status is UNDER_REVIEW, false otherwise
     */
    public boolean isUnderReview() {
        return moderationStatus == ModerationStatus.UNDER_REVIEW;
    }

    /**
     * Checks if this comment is a reply to another comment (threaded discussion).
     *
     * @return true if this comment has a parent comment, false if it's a root-level comment
     */
    public boolean isReply() {
        return parentCommentId != null;
    }

    /**
     * Checks if this comment is a validated solution to the note.
     *
     * @return true if solution status is VALIDATED, false otherwise
     */
    public boolean isSolution() {
        return solutionStatus == SolutionStatus.VALIDATED;
    }

    /**
     * Checks if this comment is a proposed solution awaiting validation.
     *
     * @return true if solution status is PROPOSED, false otherwise
     */
    public boolean isProposedSolution() {
        return solutionStatus == SolutionStatus.PROPOSED;
    }

    /**
     * Checks if this comment is a regular comment (not part of solution workflow).
     *
     * @return true if solution status is REGULAR, false if proposed or validated
     */
    public boolean isRegularComment() {
        return solutionStatus == SolutionStatus.REGULAR;
    }

    /**
     * Checks if a specific user can edit this comment based on authorship.
     *
     * <p>Only the original comment author has permission to edit their own comments.
     * This method performs a simple identity check without considering elevated privileges
     * like SUPERVISOR (those are checked at the application layer).
     *
     * @param editorId the identifier of the user attempting to edit
     * @return true if the editor is the comment author, false otherwise
     */
    public boolean canBeEditedBy(AuthorId editorId) {
        return this.authorId.equals(editorId);
    }

    // -------------------- Private helpers --------------------

    /**
     * Validates comment content according to business rules.
     *
     * <p>Validation rules:
     * <ul>
     *   <li>Content must not be null</li>
     *   <li>Content must not be blank (only whitespace)</li>
     *   <li>Content must not exceed 5000 characters</li>
     * </ul>
     *
     * @param content the content to validate
     * @return the validated content (unchanged if valid)
     * @throws NullPointerException if content is null
     * @throws IllegalArgumentException if content is blank or exceeds maximum length
     */
    private static String validateContent(String content) {
        Objects.requireNonNull(content, "Content cannot be null");
        if (content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be blank");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Content cannot exceed %d characters, got: %d",
                    MAX_CONTENT_LENGTH, content.length())
            );
        }
        return content;
    }

    // -------------------- Getters --------------------

    public CommentId getId() {
        return id;
    }

    public AuthorId getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public CommentId getParentCommentId() {
        return parentCommentId;
    }

    public SolutionStatus getSolutionStatus() {
        return solutionStatus;
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    // -------------------- Overrides --------------------

    /**
     * Two Comment instances are considered equal if they share the same business identifier (CommentId).
     *
     * <p>This implements the DDD Entity identity pattern where entity equality is based solely
     * on the unique identifier, not on mutable attributes like content, status, or timestamps.
     * This ensures that a Comment remains the "same" comment even as its content is edited
     * or its statuses change through the moderation and solution workflows.
     *
     * @param o the object to compare with
     * @return true if both objects are the same instance or have equal CommentIds, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment comment)) return false;
        return id.equals(comment.id);
    }

    /**
     * Returns a hash code based solely on the CommentId.
     *
     * <p>This is consistent with {@link #equals(Object)} and ensures Comments can be used
     * in hash-based collections while maintaining correct equality semantics.
     *
     * @return hash code based on the comment identifier
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this Comment for debugging purposes.
     *
     * <p>Format: {@code Comment{id=..., authorId=..., solutionStatus=..., moderationStatus=...}}
     *
     * <p>Note: Content is intentionally excluded to keep the output concise and avoid
     * cluttering logs with potentially long text.
     *
     * @return a string representation containing id, author, solution status, and moderation status
     */
    @Override
    public String toString() {
        return "Comment{id=" + id + ", authorId=" + authorId + ", solutionStatus=" + solutionStatus +
                ", moderationStatus=" + moderationStatus + "}";
    }
}
