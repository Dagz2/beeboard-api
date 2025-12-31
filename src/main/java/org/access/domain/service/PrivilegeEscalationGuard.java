package org.access.domain.service;

import jakarta.enterprise.context.ApplicationScoped;

import org.access.domain.exception.InsufficientTierException;
import org.access.domain.exception.PrivilegeEscalationException;
import org.access.domain.exception.SelfModificationForbiddenException;
import org.access.domain.exception.SystemResourceProtectedException;
import org.access.domain.model.Permission;
import org.access.domain.model.Role;
import org.access.domain.model.SecurityTier;
import org.access.domain.model.UserId;

/**
 * Domain service responsible for enforcing privilege escalation protection rules.
 *
 * This guard implements the following security rules:
 * <ul>
 *   <li>No self-modification: Users cannot modify their own role</li>
 *   <li>No privilege escalation: Users can only assign roles/permissions of lower privilege (higher tier number)</li>
 *   <li>System resource protection: Only TIER_0 can modify system roles/permissions</li>
 * </ul>
 */
@ApplicationScoped
public class PrivilegeEscalationGuard {

    /**
     * Validates that the actor is not trying to modify their own role.
     *
     * @param actorId the ID of the user performing the action
     * @param targetUserId the ID of the user being modified
     * @throws SelfModificationForbiddenException if actor tries to modify their own role
     */
    public void validateNotSelfModification(UserId actorId, UserId targetUserId) {
        if (actorId.equals(targetUserId)) {
            throw SelfModificationForbiddenException.cannotModifyOwnRole();
        }
    }

    /**
     * Validates that the actor can assign the target role based on tier hierarchy.
     * An actor can only assign roles with a tier strictly lower in privilege (higher tier number).
     *
     * @param actorTier the security tier of the actor
     * @param targetRole the role being assigned
     * @throws PrivilegeEscalationException if the assignment would be a privilege escalation
     */
    public void validateRoleAssignment(SecurityTier actorTier, Role targetRole) {
        if (!actorTier.canManage(targetRole.tier())) {
            throw PrivilegeEscalationException.cannotAssignHigherTierRole(
                actorTier.name(), targetRole.tier().name()
            );
        }
    }

    /**
     * Validates that the actor can grant the target permission based on tier hierarchy.
     *
     * @param actorTier the security tier of the actor
     * @param targetPermission the permission being granted
     * @throws PrivilegeEscalationException if granting would be a privilege escalation
     */
    public void validatePermissionGrant(SecurityTier actorTier, Permission targetPermission) {
        if (!actorTier.canManage(targetPermission.tier())) {
            throw PrivilegeEscalationException.cannotGrantHigherTierPermission(
                actorTier.name(), targetPermission.tier().name()
            );
        }
    }

    /**
     * Validates that the actor can modify a system role.
     * Only TIER_0 actors can modify system roles.
     *
     * @param actorTier the security tier of the actor
     * @param role the role being modified
     * @throws SystemResourceProtectedException if a non-TIER_0 actor tries to modify a system role
     */
    public void validateSystemRoleModification(SecurityTier actorTier, Role role) {
        if (role.isSystem() && actorTier != SecurityTier.TIER_0) {
            throw SystemResourceProtectedException.cannotModifySystemRole(role.code().value());
        }
    }

    /**
     * Validates that the actor can delete a role.
     * System roles cannot be deleted by anyone.
     *
     * @param role the role being deleted
     * @throws SystemResourceProtectedException if trying to delete a system role
     */
    public void validateRoleDeletion(Role role) {
        if (role.isSystem()) {
            throw SystemResourceProtectedException.cannotDeleteSystemRole(role.code().value());
        }
    }

    /**
     * Validates that the actor can modify a system permission.
     * Only TIER_0 actors can modify system permissions.
     *
     * @param actorTier the security tier of the actor
     * @param permission the permission being modified
     * @throws SystemResourceProtectedException if a non-TIER_0 actor tries to modify a system permission
     */
    public void validateSystemPermissionModification(SecurityTier actorTier, Permission permission) {
        if (permission.isSystem() && actorTier != SecurityTier.TIER_0) {
            throw SystemResourceProtectedException.cannotModifySystemPermission(permission.code().value());
        }
    }

    /**
     * Validates that the actor can delete a permission.
     * System permissions cannot be deleted by anyone.
     *
     * @param permission the permission being deleted
     * @throws SystemResourceProtectedException if trying to delete a system permission
     */
    public void validatePermissionDeletion(Permission permission) {
        if (permission.isSystem()) {
            throw SystemResourceProtectedException.cannotDeleteSystemPermission(permission.code().value());
        }
    }

    /**
     * Validates that the actor has at least the required tier level.
     *
     * @param actorTier the security tier of the actor
     * @param requiredTier the minimum required tier
     * @throws InsufficientTierException if actor tier is insufficient
     */
    public void validateMinimumTier(SecurityTier actorTier, SecurityTier requiredTier) {
        if (!actorTier.isAtLeast(requiredTier)) {
            throw InsufficientTierException.requiresTier(requiredTier, actorTier);
        }
    }

    /**
     * Validates that the actor is TIER_0 (SUPER_ADMIN).
     *
     * @param actorTier the security tier of the actor
     * @param operation description of the operation being performed
     * @throws InsufficientTierException if actor is not TIER_0
     */
    public void validateTierZeroRequired(SecurityTier actorTier, String operation) {
        if (actorTier != SecurityTier.TIER_0) {
            throw InsufficientTierException.onlyTierZeroCanPerform(operation);
        }
    }

    /**
     * Validates that the actor can manage a user based on tier hierarchy.
     * An actor can only manage users with a tier strictly lower in privilege (higher tier number).
     *
     * @param actorTier the security tier of the actor
     * @param targetUserTier the security tier of the user being managed
     * @throws PrivilegeEscalationException if the actor cannot manage the target user
     */
    public void validateUserManagement(SecurityTier actorTier, SecurityTier targetUserTier) {
        if (!actorTier.canManage(targetUserTier)) {
            throw PrivilegeEscalationException.cannotManageHigherTierUser(
                actorTier.name(), targetUserTier.name()
            );
        }
    }
}
