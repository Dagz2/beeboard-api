package org.access.domain.exception;

/**
 * Exception thrown when an actor attempts to modify or delete a system-protected resource.
 * System resources (roles and permissions marked as isSystem=true) can only be modified
 * by SUPER_ADMIN (TIER_0).
 */
public class SystemResourceProtectedException extends SecurityException {

    public SystemResourceProtectedException(String message) {
        super(message);
    }

    public static SystemResourceProtectedException cannotModifySystemRole(String roleCode) {
        return new SystemResourceProtectedException(
            String.format("Cannot modify system role '%s'. Only SUPER_ADMIN can modify system roles.", roleCode)
        );
    }

    public static SystemResourceProtectedException cannotDeleteSystemRole(String roleCode) {
        return new SystemResourceProtectedException(
            String.format("Cannot delete system role '%s'. System roles are protected and cannot be deleted.", roleCode)
        );
    }

    public static SystemResourceProtectedException cannotModifySystemPermission(String permissionCode) {
        return new SystemResourceProtectedException(
            String.format("Cannot modify system permission '%s'. Only SUPER_ADMIN can modify system permissions.", permissionCode)
        );
    }

    public static SystemResourceProtectedException cannotDeleteSystemPermission(String permissionCode) {
        return new SystemResourceProtectedException(
            String.format("Cannot delete system permission '%s'. System permissions are protected and cannot be deleted.", permissionCode)
        );
    }
}
