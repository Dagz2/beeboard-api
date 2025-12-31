package org.access.domain.exception;

/**
 * Exception thrown when an actor attempts to modify their own role or permissions.
 * Self-modification is forbidden to prevent privilege escalation attacks.
 */
public class SelfModificationForbiddenException extends SecurityException {

    public SelfModificationForbiddenException(String message) {
        super(message);
    }

    public static SelfModificationForbiddenException cannotModifyOwnRole() {
        return new SelfModificationForbiddenException(
            "Users cannot modify their own role. This operation must be performed by another authorized user."
        );
    }

    public static SelfModificationForbiddenException cannotModifyOwnPermissions() {
        return new SelfModificationForbiddenException(
            "Users cannot modify their own permissions. This operation must be performed by another authorized user."
        );
    }
}
