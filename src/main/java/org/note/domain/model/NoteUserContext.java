package org.note.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Anti-Corruption Layer (ACL) Value Object that translates external user context
 * from the Access bounded context into the Note BC's domain vocabulary.
 *
 * <p><strong>DDD Pattern:</strong> This is an Anti-Corruption Layer pattern implementation.
 * It acts as a protective barrier that prevents external domain concepts from leaking into
 * the Note domain model, maintaining the bounded context's integrity and independence.
 *
 * <p><strong>Responsibility:</strong> This record is the single entry point for user authentication
 * and authorization context into the Note domain. All external concepts from the Access BC
 * ({@code ApplicationRole}, {@code AuthType}) are translated here into Note BC vocabulary
 * ({@link AccessLevel}, {@link TrustScope}).
 *
 * <p><strong>Translation Rules:</strong>
 * <ul>
 *   <li>ApplicationRole → AccessLevel:
 *     <ul>
 *       <li>ADMIN, SUPERVISOR → SUPERVISOR (full privileges)</li>
 *       <li>MANAGER → MODERATOR (content management)</li>
 *       <li>MEMBER → COMMENTER (standard participation)</li>
 *       <li>GUEST → VIEWER (read-only)</li>
 *     </ul>
 *   </li>
 *   <li>AuthType → TrustScope:
 *     <ul>
 *       <li>INTERNAL → TEAM_MEMBER (trusted organization member)</li>
 *       <li>EXTERNAL → RESTRICTED_USER (limited trust guest)</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><strong>Usage Pattern:</strong>
 * <pre>
 * // At the exposition layer (REST controller)
 * NoteUserContext context = NoteUserContext.from(
 *     securityContext.getUserId(),
 *     securityContext.getRole(),      // "MEMBER"
 *     securityContext.getAuthType()   // "INTERNAL"
 * );
 *
 * // Pass to domain layer
 * noteAccessValidator.validateCanViewNote(note, context);
 * </pre>
 *
 * <p><strong>Benefits:</strong>
 * <ul>
 *   <li>Note BC remains independent from Access BC changes</li>
 *   <li>Clear vocabulary aligned with Note domain concepts</li>
 *   <li>Single point of translation for easier maintenance</li>
 *   <li>Testable without coupling to Access BC</li>
 * </ul>
 */
public record NoteUserContext(
        UUID userId,
        AccessLevel accessLevel,
        TrustScope trustScope
) {

    // -------------------- Creation --------------------

    /**
     * Canonical record constructor with validation.
     *
     * <p>This constructor is typically not called directly. Use the {@link #from(UUID, String, String)}
     * factory method instead, which handles translation from external vocabulary.
     *
     * @param userId the user's unique identifier (must not be null)
     * @param accessLevel the user's access level in Note BC vocabulary (must not be null)
     * @param trustScope the user's trust scope in Note BC vocabulary (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public NoteUserContext {
        Objects.requireNonNull(userId, "User ID is required");
        Objects.requireNonNull(accessLevel, "Access level is required");
        Objects.requireNonNull(trustScope, "Trust scope is required");
    }

    /**
     * Factory method to translate external user context into Note BC vocabulary.
     *
     * <p>This is the primary way to create NoteUserContext instances. It acts as the
     * Anti-Corruption Layer translation point, converting Access BC concepts into
     * Note BC concepts.
     *
     * <p><strong>Translation performed:</strong>
     * <ul>
     *   <li>{@code externalRole} (ApplicationRole) → {@link AccessLevel} via {@link #translateRole(String)}</li>
     *   <li>{@code externalAuthType} (AuthType) → {@link TrustScope} via {@link #translateAuthType(String)}</li>
     * </ul>
     *
     * <p>Unknown or null external values are defaulted to the most restrictive permissions:
     * <ul>
     *   <li>Unknown role → VIEWER (read-only access)</li>
     *   <li>Unknown auth type → RESTRICTED_USER (limited trust)</li>
     * </ul>
     *
     * @param userId the user's unique identifier (must not be null)
     * @param externalRole the external ApplicationRole name from Access BC (e.g., "ADMIN", "MEMBER", "GUEST")
     * @param externalAuthType the external AuthType name from Access BC (e.g., "INTERNAL", "EXTERNAL")
     * @return a new NoteUserContext with translated values
     * @throws NullPointerException if userId is null
     */
    public static NoteUserContext from(UUID userId, String externalRole, String externalAuthType) {
        Objects.requireNonNull(userId, "User ID is required");
        AccessLevel level = translateRole(externalRole);
        TrustScope scope = translateAuthType(externalAuthType);
        return new NoteUserContext(userId, level, scope);
    }

    // -------------------- Translation methods --------------------

    /**
     * Translates external ApplicationRole to Note BC's AccessLevel vocabulary.
     *
     * <p><strong>Translation mapping:</strong>
     * <ul>
     *   <li>"ADMIN", "SUPERVISOR" → SUPERVISOR (global oversight, full privileges)</li>
     *   <li>"MANAGER" → MODERATOR (content management, moderation)</li>
     *   <li>"MEMBER" → COMMENTER (standard participation with commenting)</li>
     *   <li>"GUEST" → VIEWER (read-only access)</li>
     *   <li>null or unknown → VIEWER (fail-safe to most restrictive)</li>
     * </ul>
     *
     * <p>Case-insensitive comparison is used for robustness.
     *
     * @param role the external ApplicationRole name (can be null)
     * @return the translated AccessLevel (never null, defaults to VIEWER)
     */
    private static AccessLevel translateRole(String role) {
        if (role == null) {
            return AccessLevel.VIEWER;
        }
        return switch (role.toUpperCase()) {
            case "ADMIN", "SUPERVISOR" -> AccessLevel.SUPERVISOR;
            case "MANAGER" -> AccessLevel.MODERATOR;
            case "MEMBER" -> AccessLevel.COMMENTER;
            case "GUEST" -> AccessLevel.VIEWER;
            default -> AccessLevel.VIEWER;
        };
    }

    /**
     * Translates external AuthType to Note BC's TrustScope vocabulary.
     *
     * <p><strong>Translation mapping:</strong>
     * <ul>
     *   <li>"INTERNAL" → TEAM_MEMBER (trusted organization member, can access SHARED notes)</li>
     *   <li>"EXTERNAL" → RESTRICTED_USER (guest user, needs explicit sharing)</li>
     *   <li>null or unknown → RESTRICTED_USER (fail-safe to most restrictive)</li>
     * </ul>
     *
     * <p>Trust scope determines default access rights:
     * <ul>
     *   <li>TEAM_MEMBER: Can see SHARED visibility notes by default</li>
     *   <li>RESTRICTED_USER: Only sees content explicitly shared with them</li>
     * </ul>
     *
     * @param authType the external AuthType name (can be null)
     * @return the translated TrustScope (never null, defaults to RESTRICTED_USER)
     */
    private static TrustScope translateAuthType(String authType) {
        if (authType == null) {
            return TrustScope.RESTRICTED_USER;
        }
        return "INTERNAL".equalsIgnoreCase(authType)
                ? TrustScope.TEAM_MEMBER
                : TrustScope.RESTRICTED_USER;
    }

    // -------------------- Query methods --------------------

    /**
     * Checks if the user has supervisor-level privileges.
     *
     * <p>Supervisors have global oversight with full privileges including:
     * <ul>
     *   <li>View all notes (including archived and deleted)</li>
     *   <li>Moderate all comments</li>
     *   <li>Validate solutions on any note</li>
     *   <li>Access deleted notes for recovery</li>
     * </ul>
     *
     * @return true if access level is SUPERVISOR, false otherwise
     */
    public boolean isSupervisor() {
        return accessLevel == AccessLevel.SUPERVISOR;
    }

    /**
     * Checks if the user has moderator privileges or higher.
     *
     * <p>Moderators can perform content management operations including:
     * <ul>
     *   <li>Censor inappropriate comments</li>
     *   <li>Flag comments for review</li>
     *   <li>Restore censored comments</li>
     * </ul>
     *
     * <p>Supervisors also have moderator privileges (hierarchical).
     *
     * @return true if access level is MODERATOR or SUPERVISOR, false otherwise
     */
    public boolean isModerator() {
        return accessLevel == AccessLevel.SUPERVISOR || accessLevel == AccessLevel.MODERATOR;
    }

    /**
     * Checks if the user is a trusted team member (internal user).
     *
     * <p>Team members have default access to SHARED visibility notes without
     * requiring explicit sharing. This represents trusted employees or organization
     * members.
     *
     * @return true if trust scope is TEAM_MEMBER, false if RESTRICTED_USER
     */
    public boolean isTeamMember() {
        return trustScope == TrustScope.TEAM_MEMBER;
    }

    /**
     * Checks if the user has permission to add comments to notes.
     *
     * <p>Commenting requires at least COMMENTER access level. VIEWERs have read-only
     * access and cannot participate in discussions.
     *
     * @return true if access level is COMMENTER, MODERATOR, or SUPERVISOR, false for VIEWER
     */
    public boolean canComment() {
        return accessLevel != AccessLevel.VIEWER;
    }

    /**
     * Converts this user context's userId into an AuthorId for domain operations.
     *
     * <p>This convenience method is used when creating notes or comments where an
     * AuthorId is required.
     *
     * @return a new AuthorId wrapping this context's userId
     */
    public AuthorId toAuthorId() {
        return new AuthorId(userId);
    }

    // -------------------- Overrides --------------------

    /**
     * Returns a string representation of this NoteUserContext for debugging purposes.
     *
     * <p>Format: {@code NoteUserContext{userId=..., accessLevel=..., trustScope=...}}
     *
     * @return a string representation containing userId, access level, and trust scope
     */
    @Override
    public String toString() {
        return "NoteUserContext{userId=" + userId + ", accessLevel=" + accessLevel + ", trustScope=" + trustScope + "}";
    }
}
