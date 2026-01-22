package org.access.application.dto.query;

import org.access.domain.model.SecurityTier;
import org.shared.domain.ApplicationRole;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing a role returned from a query.
 *
 * This class is used in the application layer to expose role-related information
 * to clients, typically in response to a read/query use case.
 *
 * A role is composed of:
 * - a unique identifier (roleId)
 * - a functional code used internally
 * - a human-readable name
 * - audit metadata
 * - a set of permission identifiers (UUIDs) associated with this role
 * - a security tier indicating the role's privilege level
 * - a system flag indicating if this is a protected system role
 * - an application role for cross-BC communication
 */
public class RoleDto {

    /**
     * Unique identifier of the role.
     */
    public UUID roleId;

    /**
     * Functional code that identifies the role internally (e.g. "ADMIN", "MANAGER").
     */
    public String code;

    /**
     * Human-readable name of the role (e.g. "Administrator", "Project Manager").
     */
    public String name;

    /**
     * Audit metadata containing creation and update timestamps.
     */
    public AuditInfoDto auditInfo;

    /**
     * Set of permission identifiers (UUIDs) associated with this role.
     */
    public Set<UUID> permissions = new HashSet<>();

    /**
     * Security tier indicating the role's privilege level in the hierarchy.
     * Read-only field for visibility purposes.
     */
    public SecurityTier tier;

    /**
     * Indicates whether this is a system-protected role that cannot be deleted.
     * Read-only field for visibility purposes.
     */
    public boolean isSystem;

    /**
     * Application role used for cross-BC communication and coarse-grained authorization.
     * Read-only field for visibility purposes.
     */
    public ApplicationRole applicationRole;

    /**
     * Constructs a new RoleDto instance with all required fields.
     *
     * @param roleId          the unique role identifier
     * @param code            the functional code of the role
     * @param name            the display name of the role
     * @param auditInfo       the audit metadata
     * @param permissions     the set of permission UUIDs associated with this role
     * @param tier            the security tier of the role
     * @param isSystem        whether this is a system-protected role
     * @param applicationRole the application role for cross-BC communication
     */
    public RoleDto(UUID roleId, String code, String name, AuditInfoDto auditInfo, Set<UUID> permissions,
                   SecurityTier tier, boolean isSystem, ApplicationRole applicationRole) {
        this.roleId = roleId;
        this.code = code;
        this.name = name;
        this.auditInfo = auditInfo;
        this.permissions = permissions;
        this.tier = tier;
        this.isSystem = isSystem;
        this.applicationRole = applicationRole;
    }

    // -------------------- Getters & Setters --------------------

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuditInfoDto getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoDto auditInfo) {
        this.auditInfo = auditInfo;
    }

    public Set<UUID> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<UUID> permissions) {
        this.permissions = permissions;
    }

    public SecurityTier getTier() {
        return tier;
    }

    public void setTier(SecurityTier tier) {
        this.tier = tier;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public ApplicationRole getApplicationRole() {
        return applicationRole;
    }

    public void setApplicationRole(ApplicationRole applicationRole) {
        this.applicationRole = applicationRole;
    }
}
