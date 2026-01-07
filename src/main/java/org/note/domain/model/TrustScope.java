package org.note.domain.model;

/**
 * Enum representing trust scopes within the Note bounded context.
 *
 * <p>This enum is part of the Anti-Corruption Layer, translating external
 * {@code AuthType} values from the Access BC into Note BC vocabulary.
 *
 * <p>Translation rules from AuthType:
 * <ul>
 *   <li>INTERNAL → TEAM_MEMBER</li>
 *   <li>EXTERNAL → RESTRICTED_USER</li>
 * </ul>
 *
 * <p>Trust scope determines default access rights:
 * <ul>
 *   <li>TEAM_MEMBER: Can see SHARED notes by default</li>
 *   <li>RESTRICTED_USER: Only sees explicitly shared content</li>
 * </ul>
 */
public enum TrustScope {

    /**
     * Trusted member of the organization.
     * Has default access to SHARED notes and can participate fully in collaboration.
     */
    TEAM_MEMBER,

    /**
     * Limited trust user with temporary or restricted access.
     * Only sees content that has been explicitly shared with them.
     * Typically used for external collaborators or guest accounts.
     */
    RESTRICTED_USER;

    /**
     * Checks if this trust scope allows default access to SHARED notes.
     *
     * @return true if scope is TEAM_MEMBER, false for RESTRICTED_USER
     */
    public boolean canAccessSharedNotes() {
        return this == TEAM_MEMBER;
    }

    /**
     * Checks if this trust scope requires explicit sharing for access.
     *
     * @return true if scope is RESTRICTED_USER, false for TEAM_MEMBER
     */
    public boolean requiresExplicitAccess() {
        return this == RESTRICTED_USER;
    }

    /**
     * Checks if this trust scope represents a full team member.
     *
     * @return true if scope is TEAM_MEMBER, false otherwise
     */
    public boolean isFullMember() {
        return this == TEAM_MEMBER;
    }
}
