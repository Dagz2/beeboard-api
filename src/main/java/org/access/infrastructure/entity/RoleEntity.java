package org.access.infrastructure.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;
import org.shared.domain.ApplicationRole;

/**
 * JPA entity representing a role in the system.
 *
 * A role aggregates a set of permissions and is used to control access to various features.
 * It is uniquely identified by its ID and has a code and a human-readable name.
 *
 * This entity maps to the "role" table in the database.
 */
@Entity
@Table(name = "role")
public class RoleEntity extends PanacheEntityBase {

    /**
     * Unique identifier for the role.
     */
    @Id
    @NotNull
    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    /**
     * Functional code of the role (e.g. "ADMIN", "MANAGER").
     */
    @NotNull
    @Column(name = "code", length = 50, nullable = false)
    private String code;

    /**
     * Human-readable name of the role.
     */
    @NotNull
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    /**
     * Audit information including creation and last modification timestamps.
     */
    @Embedded
    @NotNull
    private AuditInfoEmbeddable auditInfo;

    /**
     * Set of permissions associated with this role.
     */
    @ManyToMany
    @JoinTable(
        name = "role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionEntity> permissions = new HashSet<>();

    /**
     * Security tier classification of this role.
     * Determines the privilege level of users with this role.
     */
    @NotNull
    @Column(name = "tier", length = 10, nullable = false)
    private String tier;

    /**
     * Indicates if this is a system-protected role.
     * System roles cannot be deleted and their core properties
     * cannot be modified except by SUPER_ADMIN.
     */
    @NotNull
    @Column(name = "is_system", nullable = false)
    private boolean isSystem;

    /**
     * Application-level role for cross-BC communication.
     * Maps organizational roles to capability levels.
     */
    @NotNull
    @Column(name = "application_role", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationRole applicationRole;

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


    public AuditInfoEmbeddable getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoEmbeddable auditInfo) {
        this.auditInfo = auditInfo;
    }

    public Set<PermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionEntity> permissions) {
        this.permissions = permissions;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
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