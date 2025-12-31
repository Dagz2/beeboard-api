package org.access.domain.exception;

/**
 * Exception thrown when an actor attempts to perform a privilege escalation.
 * This occurs when trying to assign a role or grant a permission of a tier
 * equal to or higher than the actor's own tier.
 */
public class PrivilegeEscalationException extends SecurityException {

    public PrivilegeEscalationException(String message) {
        super(message);
    }

    public static PrivilegeEscalationException cannotAssignHigherTierRole(String actorTier, String targetTier) {
        return new PrivilegeEscalationException(
            String.format("Cannot assign role of tier %s. Actor tier %s can only assign roles of lower privilege.",
                targetTier, actorTier)
        );
    }

    public static PrivilegeEscalationException cannotGrantHigherTierPermission(String actorTier, String permissionTier) {
        return new PrivilegeEscalationException(
            String.format("Cannot grant permission of tier %s. Actor tier %s can only grant permissions of lower privilege.",
                permissionTier, actorTier)
        );
    }

    public static PrivilegeEscalationException cannotManageHigherTierUser(String actorTier, String targetUserTier) {
        return new PrivilegeEscalationException(
            String.format("Cannot manage user of tier %s. Actor tier %s can only manage users of lower privilege.",
                targetUserTier, actorTier)
        );
    }
}
