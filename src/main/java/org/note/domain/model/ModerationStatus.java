package org.note.domain.model;

/**
 * Enum representing the moderation status of a Comment.
 *
 * <p>Moderation workflow:
 * <ul>
 *   <li>VISIBLE → UNDER_REVIEW (when flagged by users)</li>
 *   <li>UNDER_REVIEW → VISIBLE (when approved by moderator)</li>
 *   <li>UNDER_REVIEW → CENSORED (when rejected by moderator)</li>
 *   <li>CENSORED → VISIBLE (when restored by moderator)</li>
 * </ul>
 *
 * <p>Business rules:
 * <ul>
 *   <li>VISIBLE comments are displayed with full content</li>
 *   <li>CENSORED comments show placeholder text "[Content removed by moderator]"</li>
 *   <li>UNDER_REVIEW comments remain visible but flagged for moderation</li>
 *   <li>Only MODERATOR and SUPERVISOR can change moderation status</li>
 * </ul>
 */
public enum ModerationStatus {

    /**
     * Comment is visible to all users who can view the note.
     * Content is displayed normally.
     */
    VISIBLE,

    /**
     * Comment is hidden by moderator due to policy violations.
     * Content is replaced with "[Content removed by moderator]" placeholder.
     */
    CENSORED,

    /**
     * Comment has been flagged by users and awaits moderation review.
     * Content remains visible but marked for moderator attention.
     */
    UNDER_REVIEW;

    /**
     * Checks if comments with this status are visible to users.
     *
     * @return true if the comment content is visible (VISIBLE or UNDER_REVIEW), false if censored
     */
    public boolean isVisible() {
        return this == VISIBLE || this == UNDER_REVIEW;
    }

    /**
     * Checks if this status requires moderator action.
     *
     * @return true if the comment is UNDER_REVIEW, false otherwise
     */
    public boolean requiresModeration() {
        return this == UNDER_REVIEW;
    }

    /**
     * Checks if a comment with this status can be restored to VISIBLE.
     *
     * @return true if the comment is CENSORED and can be restored, false otherwise
     */
    public boolean canBeRestored() {
        return this == CENSORED;
    }

    /**
     * Checks if a comment with this status can be censored.
     *
     * @return true if the comment is VISIBLE or UNDER_REVIEW, false otherwise
     */
    public boolean canBeCensored() {
        return this == VISIBLE || this == UNDER_REVIEW;
    }
}
