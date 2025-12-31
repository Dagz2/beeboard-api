package org.access.infrastructure.entity;

import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a permission within the system.
 *
 * A permission defines a specific action or access right (e.g. "VIEW_TASKS", "EDIT_PROJECT").
 * It is uniquely identified by its ID and has a code, name, and description.
 * 
 * This entity is part of the infrastructure layer and maps to the "permission" table.
 */
@Entity
@Table(name = "permission")
public class PermissionEntity extends PanacheEntityBase {

    /**
     * Unique identifier for the permission.
     */
    @Id
    @NotNull
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    /**
     * Functional code of the permission (e.g. "CREATE_USER").
     * Serves as a technical reference.
     */
    @NotNull
    @Column(name = "code", length = 100, nullable = false)
    private String code;

    /**
     * Human-readable name of the permission (e.g. "Create User").
     */
    @NotNull
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * Description of what the permission allows.
     */
    @NotNull
    @Column(name = "description", length = 100, nullable = false)
    private String description;

    /**
     * Security tier classification of this permission.
     * Determines which roles can grant or revoke this permission.
     */
    @NotNull
    @Column(name = "tier", length = 10, nullable = false)
    private String tier;

    /**
     * Indicates if this is a system-protected permission.
     * System permissions can only be modified by SUPER_ADMIN.
     */
    @NotNull
    @Column(name = "is_system", nullable = false)
    private boolean isSystem;

    // -------------------- Getters & Setters --------------------

    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
