package org.access.application.dto.query;

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
     * Constructs a new RoleDto instance with all required fields.
     *
     * @param roleId      the unique role identifier
     * @param code        the functional code of the role
     * @param name        the display name of the role
     * @param auditInfo   the audit metadata
     * @param permissions the set of permission UUIDs associated with this role
     */
    public RoleDto(UUID roleId, String code, String name, AuditInfoDto auditInfo, Set<UUID> permissions) {
        this.roleId = roleId;
        this.code = code;
        this.name = name;
        this.auditInfo = auditInfo;
        this.permissions = permissions;
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
}
