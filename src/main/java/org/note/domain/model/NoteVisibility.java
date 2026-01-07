package org.note.domain.model;

/**
 * Enum representing the visibility scope of a Note.
 *
 * <p>Visibility determines who can access and view a note, implementing
 * the access control model for the Note bounded context.
 *
 * <p>Visibility levels (from most to least restrictive):
 * <ol>
 *   <li>PRIVATE: Only the note author</li>
 *   <li>PROJECT: Only project members</li>
 *   <li>SHARED: All team members in the organization</li>
 * </ol>
 *
 * <p>Business rules:
 * <ul>
 *   <li>PRIVATE notes are only visible to their author</li>
 *   <li>SHARED notes are visible to all users with TEAM_MEMBER trust scope</li>
 *   <li>PROJECT notes require a projectId and are visible to project contributors</li>
 * </ul>
 */
public enum NoteVisibility {

    /**
     * Private visibility - only the author can view this note.
     * Used for personal workspace and confidential information.
     */
    PRIVATE,

    /**
     * Shared visibility - all team members can view this note.
     * Used for organization-wide shared knowledge and collaboration.
     */
    SHARED,

    /**
     * Project visibility - only project members can view this note.
     * Used for project-specific knowledge and discussions.
     * Requires a projectId to be set on the note.
     */
    PROJECT;

    /**
     * Checks if this visibility is public to all team members.
     *
     * @return true if visibility is SHARED, false otherwise
     */
    public boolean isPublic() {
        return this == SHARED;
    }

    /**
     * Checks if this visibility requires project membership to access.
     *
     * @return true if visibility is PROJECT, false otherwise
     */
    public boolean requiresProjectMembership() {
        return this == PROJECT;
    }

    /**
     * Checks if this visibility requires explicit sharing (recipients list).
     *
     * @return true if visibility is PRIVATE or SHARED and uses explicit sharing, false otherwise
     */
    public boolean requiresExplicitSharing() {
        // PRIVATE notes can be explicitly shared with specific users
        // SHARED notes can also have explicit recipient lists for notifications
        return this == PRIVATE;
    }

    /**
     * Checks if notes with this visibility are restricted to the author only.
     *
     * @return true if visibility is PRIVATE, false otherwise
     */
    public boolean isAuthorOnly() {
        return this == PRIVATE;
    }
}
