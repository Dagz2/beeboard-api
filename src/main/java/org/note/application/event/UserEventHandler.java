package org.note.application.event;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import org.access.domain.event.*;
import org.jboss.logging.Logger;
import org.note.application.service.AuthorViewApplicationService;
import org.note.domain.model.AccessLevel;
import org.note.domain.model.AuthorView;
import org.note.domain.repository.AuthorViewRepository;
import org.shared.domain.ApplicationRole;
import org.shared.domain.AccountStatus;

import java.util.UUID;

/**
 * Handles user-related events from Access BC to synchronize the author_view CQRS read model.
 *
 * <p>This handler is the Anti-Corruption Layer boundary between Access BC and Note BC.
 * It translates Access BC concepts (ApplicationRole) into Note BC vocabulary (AccessLevel)
 * and synchronizes the author_view table.</p>
 *
 * <p>All handlers use {@code @ObservesAsync} for asynchronous processing to avoid
 * blocking the Access BC transaction. Each handler is idempotent and includes
 * error handling to prevent event processing failures from propagating.</p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li><b>Event Processing</b>: Listen to 7 user-related events from Access BC</li>
 *   <li><b>ACL Translation</b>: Convert ApplicationRole → AccessLevel at BC boundary</li>
 *   <li><b>Data Synchronization</b>: Keep author_view table in sync with Access BC</li>
 *   <li><b>Error Resilience</b>: Log and handle failures without propagating</li>
 * </ul>
 */
@ApplicationScoped
public class UserEventHandler {

    private static final Logger LOG = Logger.getLogger(UserEventHandler.class);

    @Inject
    AuthorViewRepository authorViewRepository;

    @Inject
    AuthorViewApplicationService authorViewService;

    /**
     * Anti-Corruption Layer: Translates ApplicationRole (Access BC) to AccessLevel (Note BC).
     *
     * <p>This is the boundary translation that keeps Note BC's vocabulary clean.
     * ApplicationRole is Access BC's Published Language - we translate it here
     * so that author_view stores only Note BC vocabulary.</p>
     *
     * <h3>Translation Rules:</h3>
     * <ul>
     *   <li><b>ADMIN, SUPERVISOR</b> → SUPERVISOR: Full administrative privileges</li>
     *   <li><b>MANAGER</b> → MODERATOR: Content moderation and management</li>
     *   <li><b>MEMBER</b> → COMMENTER: Standard participation (read, write, comment)</li>
     *   <li><b>GUEST</b> → VIEWER: Read-only access</li>
     * </ul>
     *
     * @param applicationRole the ApplicationRole from Access BC
     * @return the corresponding AccessLevel in Note BC vocabulary
     */
    private AccessLevel translateToAccessLevel(ApplicationRole applicationRole) {
        return switch (applicationRole) {
            case ADMIN, SUPERVISOR -> AccessLevel.SUPERVISOR;   // Full privileges
            case MANAGER -> AccessLevel.MODERATOR;              // Content management
            case MEMBER -> AccessLevel.COMMENTER;               // Standard participation
            case GUEST -> AccessLevel.VIEWER;                   // Read-only
        };
    }

    /**
     * Handles UserCreated events by creating a new entry in author_view.
     *
     * <p>This is the primary synchronization mechanism for new users.
     * When a user is created in Access BC, Note BC immediately gets a local copy
     * with the translated AccessLevel.</p>
     *
     * <p>Anti-Corruption Layer: Translates ApplicationRole → AccessLevel here.</p>
     *
     * @param event the UserCreatedEvent from Access BC
     */
    public void onUserCreated(@ObservesAsync UserCreatedEvent event) {
        LOG.infof("Handling UserCreated event for user %s (role: %s, status: %s)",
            event.userId(), event.applicationRole(), event.accountStatus());

        try {
            // ACL Translation: ApplicationRole → AccessLevel
            AccessLevel accessLevel = translateToAccessLevel(event.applicationRole());

            AuthorView authorView = new AuthorView(
                event.userId(),
                accessLevel,
                event.accountStatus(),
                event.fullName()
            );

            authorViewRepository.save(authorView);

            LOG.infof("Successfully created author_view for user %s (accessLevel: %s)",
                event.userId(), accessLevel);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to handle UserCreated event for user %s", event.userId());
            // Note: In production, consider republishing the event or triggering an alert
            // The sync endpoint can be used to recover from missing entries
        }
    }

    /**
     * Handles UserRoleChanged events by updating access level in author_view.
     *
     * <p>When a user's role changes in Access BC, their ApplicationRole may change,
     * which affects their authorization level in Note BC. We update the local
     * author_view to reflect the new AccessLevel.</p>
     *
     * <p>Anti-Corruption Layer: Translates new ApplicationRole → AccessLevel.</p>
     *
     * @param event the UserRoleChangedEvent containing userId and role change details
     */
    public void onUserRoleChanged(@ObservesAsync UserRoleChangedEvent event) {
        LOG.infof("Handling UserRoleChanged event for user %s (old: %s, new: %s)",
            event.userId(), event.oldApplicationRole(), event.newApplicationRole());

        try {
            // ACL Translation: ApplicationRole → AccessLevel
            AccessLevel newAccessLevel = translateToAccessLevel(event.newApplicationRole());

            authorViewRepository.updateAccessLevel(event.userId(), newAccessLevel);

            LOG.infof("Successfully updated access_level for user %s to %s",
                event.userId(), newAccessLevel);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to handle UserRoleChanged event for user %s", event.userId());
            // Note: In production, consider republishing the event or triggering an alert
            // The sync endpoint can be used to recover from inconsistent state
        }
    }

    /**
     * Handles UserFullNameChanged events by updating full name in author_view.
     *
     * <p>When a user's first or last name changes in Access BC, Note BC must update
     * the author_view to reflect the new full name. This ensures that note/comment
     * DTOs display the current author name without cross-BC REST calls.</p>
     *
     * <p>This handler is asynchronous and idempotent. If the update fails,
     * the author_view will remain stale but won't break Access BC operations.</p>
     *
     * @param event the UserFullNameChangedEvent containing userId and name change details
     */
    public void onUserFullNameChanged(@ObservesAsync UserFullNameChangedEvent event) {
        LOG.infof("Handling UserFullNameChanged event for user %s (old: '%s', new: '%s')",
            event.userId(), event.oldFullName(), event.newFullName());

        try {
            authorViewService.updateFullName(event.userId(), event.newFullName());

            LOG.infof("Successfully updated full_name for user %s to '%s'",
                event.userId(), event.newFullName());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to handle UserFullNameChanged event for user %s", event.userId());
            // Note: Do not propagate exception - author_view will remain stale but won't break Access BC
            // The sync endpoint can be used to recover from inconsistent state
        }
    }

    /**
     * Handles RoleApplicationRoleChanged events by bulk updating access level for all affected users.
     *
     * <p>When a role's ApplicationRole changes in Access BC, ALL users with that role
     * are affected. This is a high-impact event that requires bulk updates to author_view.</p>
     *
     * <p>Anti-Corruption Layer: Translates new ApplicationRole → AccessLevel.</p>
     *
     * @param event the RoleApplicationRoleChangedEvent containing roleId and affected user IDs
     */
    public void onRoleApplicationRoleChanged(@ObservesAsync RoleApplicationRoleChangedEvent event) {
        LOG.infof("Handling RoleApplicationRoleChanged event for role %s (code: %s), affecting %d users",
            event.roleId(), event.roleCode(), event.affectedUserIds().size());

        try {
            // ACL Translation: ApplicationRole → AccessLevel
            AccessLevel newAccessLevel = translateToAccessLevel(event.newApplicationRole());

            int updatedCount = 0;
            for (UUID userId : event.affectedUserIds()) {
                authorViewRepository.updateAccessLevel(userId, newAccessLevel);
                updatedCount++;
            }

            LOG.infof("Successfully updated access_level for %d users to %s (role: %s)",
                updatedCount, newAccessLevel, event.roleCode());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to handle RoleApplicationRoleChanged event for role %s", event.roleCode());
        }
    }

    /**
     * Handles UserLocked events by updating account status in author_view.
     *
     * <p>When a user is locked in Access BC, their AccountStatus changes to LOCKED,
     * which should immediately prevent them from accessing notes in Note BC.</p>
     *
     * @param event the UserLockedEvent containing userId and lock details
     */
    public void onUserLocked(@ObservesAsync UserLockedEvent event) {
        LOG.infof("Handling UserLocked event for user %s (email: %s, reason: %s)",
            event.userId(), event.email(), event.reason().orElse("not specified"));

        try {
            authorViewRepository.updateAccountStatus(event.userId(), AccountStatus.LOCKED);

            LOG.infof("Successfully updated account_status to LOCKED for user %s", event.userId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to handle UserLocked event for user %s", event.userId());
        }
    }

    /**
     * Handles UserUnlocked events by updating account status in author_view.
     *
     * <p>When a user is unlocked in Access BC, their AccountStatus changes to ACTIVE,
     * which should restore their access to notes in Note BC.</p>
     *
     * @param event the UserUnlockedEvent containing userId and unlock details
     */
    public void onUserUnlocked(@ObservesAsync UserUnlockedEvent event) {
        LOG.infof("Handling UserUnlocked event for user %s (email: %s)",
            event.userId(), event.email());

        try {
            authorViewRepository.updateAccountStatus(event.userId(), AccountStatus.ACTIVE);

            LOG.infof("Successfully updated account_status to ACTIVE for user %s", event.userId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to handle UserUnlocked event for user %s", event.userId());
        }
    }

    /**
     * Handles UserDeleted events by removing the user from author_view.
     *
     * <p>When a user is deleted in Access BC, their entry should be removed from author_view.
     * This is part of GDPR compliance (right to be forgotten).</p>
     *
     * <p>Future enhancement: This could also trigger anonymization or reassignment of the
     * user's notes and comments.</p>
     *
     * @param event the UserDeletedEvent containing userId and deletion details
     */
    public void onUserDeleted(@ObservesAsync UserDeletedEvent event) {
        LOG.infof("Handling UserDeleted event for user %s (email: %s, fullName: %s)",
            event.userId(), event.email(), event.fullName());

        try {
            authorViewRepository.deleteById(event.userId());

            LOG.infof("Successfully deleted author_view for user %s", event.userId());

            // TODO FUTURE: Implement note/comment anonymization or reassignment
            // This is critical for GDPR compliance (right to be forgotten)
            // Example: noteRepository.anonymizeAuthor(event.userId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to handle UserDeleted event for user %s", event.userId());
        }
    }

    /**
     * Handles UserContractTerminated events by updating account status in author_view.
     *
     * <p>When a user's contract is terminated in Access BC, their account status changes
     * to EXPIRED, and their access to notes should be immediately revoked.</p>
     *
     * <p>Future enhancement: This could trigger archival of the user's notes for
     * compliance and knowledge retention purposes.</p>
     *
     * @param event the UserContractTerminatedEvent containing userId and termination details
     */
    public void onUserContractTerminated(@ObservesAsync UserContractTerminatedEvent event) {
        LOG.infof("Handling UserContractTerminated event for user %s (email: %s, termination date: %s)",
            event.userId(), event.email(), event.terminationDate());

        try {
            authorViewRepository.updateAccountStatus(event.userId(), AccountStatus.EXPIRED);

            LOG.infof("Successfully updated account_status to EXPIRED for user %s", event.userId());

            // TODO FUTURE: Implement note archival for terminated employees
            // Consider compliance requirements for data retention
            // Example: noteRepository.archiveNotesByAuthor(event.userId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to handle UserContractTerminated event for user %s", event.userId());
        }
    }
}
