package org.note.domain.model;

/**
 * Enum representing the lifecycle status of a Note.
 *
 * <p>The note lifecycle follows this state machine:
 * <ul>
 *   <li>ACTIVE → ARCHIVED (soft delete)</li>
 *   <li>ARCHIVED → ACTIVE (restore)</li>
 *   <li>ARCHIVED → DELETED (hard delete)</li>
 * </ul>
 *
 * <p>Business rules:
 * <ul>
 *   <li>ACTIVE notes can be edited, commented, and viewed according to visibility rules</li>
 *   <li>ARCHIVED notes are read-only and only viewable by author and SUPERVISOR</li>
 *   <li>DELETED notes are soft-deleted and only viewable by SUPERVISOR for recovery</li>
 * </ul>
 */
public enum NoteStatus {

    /**
     * Note is active and can be edited, commented, and viewed.
     * Access is determined by the note's visibility rules (PRIVATE, SHARED, PROJECT).
     */
    ACTIVE,

    /**
     * Note is archived (soft deleted) and becomes read-only.
     * Only the author and users with SUPERVISOR access level can view archived notes.
     */
    ARCHIVED,

    /**
     * Note is permanently deleted (soft delete).
     * Only users with SUPERVISOR access level can view deleted notes for potential recovery.
     */
    DELETED;

    /**
     * Checks if notes with this status can be edited.
     *
     * @return true if the status allows editing (ACTIVE only), false otherwise
     */
    public boolean canBeEdited() {
        return this == ACTIVE;
    }

    /**
     * Checks if notes with this status can receive new comments.
     *
     * @return true if the status allows commenting (ACTIVE only), false otherwise
     */
    public boolean canReceiveComments() {
        return this == ACTIVE;
    }

    /**
     * Checks if notes with this status can be archived.
     *
     * @return true if the status allows archiving (ACTIVE only), false otherwise
     */
    public boolean canBeArchived() {
        return this == ACTIVE;
    }

    /**
     * Checks if notes with this status can be restored to ACTIVE.
     *
     * @return true if the status allows restoration (ARCHIVED only), false otherwise
     */
    public boolean canBeRestored() {
        return this == ARCHIVED;
    }

    /**
     * Checks if this status represents a deleted state (soft or hard).
     *
     * @return true if the note is archived or deleted, false otherwise
     */
    public boolean isDeleted() {
        return this == ARCHIVED || this == DELETED;
    }
}
