package org.note.domain.model;

/**
 * Enum representing access levels within the Note bounded context.
 *
 * <p>This enum is part of the Anti-Corruption Layer, translating external
 * {@code ApplicationRole} values from the Access BC into Note BC vocabulary.
 *
 * <p>Translation rules from ApplicationRole:
 * <ul>
 *   <li>ADMIN, SUPERVISOR → SUPERVISOR</li>
 *   <li>MANAGER → MODERATOR</li>
 *   <li>MEMBER → COMMENTER</li>
 *   <li>GUEST → VIEWER</li>
 * </ul>
 *
 * <p>Access level hierarchy (from highest to lowest privileges):
 * <ol>
 *   <li>SUPERVISOR: Global oversight, full access</li>
 *   <li>MODERATOR: Content management within projects</li>
 *   <li>COMMENTER: Standard participation with commenting</li>
 *   <li>VIEWER: Read-only access</li>
 * </ol>
 */
public enum AccessLevel {

    /**
     * Global oversight role with full privileges.
     * Can view all notes (including archived), moderate all comments,
     * access deleted notes for recovery, and validate solutions.
     */
    SUPERVISOR,

    /**
     * Content management role for project-level moderation.
     * Can moderate comments within owned or managed projects.
     */
    MODERATOR,

    /**
     * Standard participant role with interactive privileges.
     * Can view notes per visibility rules, add comments, and propose solutions.
     */
    COMMENTER,

    /**
     * Read-only role with view-only access.
     * Can view notes per visibility rules but cannot comment or interact.
     */
    VIEWER;

    /**
     * Checks if this access level allows commenting on notes.
     *
     * @return true if level is COMMENTER or higher, false for VIEWER
     */
    public boolean canComment() {
        return this != VIEWER;
    }

    /**
     * Checks if this access level allows content moderation.
     *
     * @return true if level is MODERATOR or SUPERVISOR, false otherwise
     */
    public boolean canModerate() {
        return this == MODERATOR || this == SUPERVISOR;
    }

    /**
     * Checks if this access level has supervisory privileges.
     *
     * @return true if level is SUPERVISOR, false otherwise
     */
    public boolean canSupervise() {
        return this == SUPERVISOR;
    }

    /**
     * Checks if this access level has at least the specified level of privileges.
     *
     * @param required the minimum required access level
     * @return true if this level has equal or higher privileges than required
     */
    public boolean isAtLeast(AccessLevel required) {
        // Lower ordinal = higher privilege (SUPERVISOR=0, VIEWER=3)
        return this.ordinal() <= required.ordinal();
    }
}
