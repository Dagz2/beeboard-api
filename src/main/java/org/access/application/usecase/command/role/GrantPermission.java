package org.access.application.usecase.command.role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.service.AuditService;
import org.access.domain.model.Permission;
import org.access.domain.model.PermissionId;
import org.access.domain.model.Role;
import org.access.domain.model.RoleId;
import org.access.domain.model.SecurityContext;
import org.access.domain.repository.PermissionRepository;
import org.access.domain.repository.RoleRepository;
import org.access.domain.service.PrivilegeEscalationGuard;
import org.access.exposition.security.SecurityContextCache;

/**
 * Use case for granting a permission to a role.
 *
 * This use case adds a {@link PermissionId} to a {@link Role}'s permission set
 * and persists the updated role using the {@link RoleRepository}.
 *
 * Security validations:
 * <ul>
 *   <li>Actor can only grant permissions with lower privilege than their own tier</li>
 *   <li>System roles can only be modified by TIER_0 actors</li>
 * </ul>
 */
@ApplicationScoped
public class GrantPermission {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PrivilegeEscalationGuard privilegeGuard;
    private final AuditService auditService;
    private final SecurityContextCache securityContextCache;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param roleRepository       the repository used to retrieve and persist roles
     * @param permissionRepository the repository used to retrieve permissions
     * @param privilegeGuard       the guard for privilege escalation protection
     * @param auditService         the service for audit logging
     * @param securityContextCache the cache for security context invalidation
     */
    @Inject
    public GrantPermission(RoleRepository roleRepository, PermissionRepository permissionRepository,
                           PrivilegeEscalationGuard privilegeGuard, AuditService auditService,
                           SecurityContextCache securityContextCache) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.privilegeGuard = privilegeGuard;
        this.auditService = auditService;
        this.securityContextCache = securityContextCache;
    }

    /**
     * Executes the addition of a permission to a role with security validation.
     *
     * @param roleId          the ID of the role to which the permission is granted
     * @param permissionId    the ID of the permission to grant
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the role or permission does not exist
     * @throws org.access.domain.exception.PrivilegeEscalationException if actor tries to grant higher tier permission
     * @throws org.access.domain.exception.SystemResourceProtectedException if non-TIER_0 actor tries to modify system role
     */
    @Transactional
    public void execute(RoleId roleId, PermissionId permissionId, SecurityContext securityContext) {
        // Load the role
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId.id()));

        // Load the permission to check tier
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permissionId.id()));

        // Validate system role modification (only TIER_0 can modify system roles)
        privilegeGuard.validateSystemRoleModification(securityContext.actorTier(), role);

        // Validate permission tier (actor can only grant lower privilege permissions)
        privilegeGuard.validatePermissionGrant(securityContext.actorTier(), permission);

        role.grantPermission(permissionId);
        roleRepository.save(role);

        // Audit the operation
        auditService.record(
            securityContext,
            "GRANT_PERMISSION",
            "ROLE",
            roleId.id().toString(),
            null,
            "{\"permissionId\": \"" + permissionId.id().toString() + "\"}"
        );

        // Invalidate security context cache for all users with this role
        securityContextCache.invalidateByRole(roleId);
    }
}
