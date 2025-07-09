package org.access.application.dto.query;

import java.util.UUID;

/**
 * Data Transfer Object representing a system permission.
 *
 * This DTO is used in the application layer to expose permission-related data
 * in response to read/query operations. A permission typically defines a specific
 * access or action that a role can be granted within the system.
 */
public class PermissionDto {

    /**
     * Unique identifier of the permission.
     */
    public UUID id;

    /**
     * Functional code that identifies the permission (e.g. "VIEW_TASKS").
     */
    public String code;

    /**
     * Human-readable name for the permission (e.g. "View Tasks").
     */
    public String name;

    /**
     * Textual description of what the permission allows.
     */
    public String description;

    /**
     * Default no-args constructor for frameworks and serialization.
     */
    public PermissionDto() {
    }

    /**
     * Full constructor to create a fully populated PermissionDto.
     *
     * @param permissionId the unique identifier of the permission
     * @param code         the functional code of the permission
     * @param name         the display name of the permission
     * @param description  the detailed description of the permission
     */
    public PermissionDto(UUID permissionId, String code, String name, String description) {
        this.id = permissionId;
        this.code = code;
        this.name = name;
        this.description = description;
    }

    // -------------------- Getters & Setters --------------------

    public UUID getPermissionId() {
        return id;
    }

    public void setPermissionId(UUID permissionId) {
        this.id = permissionId;
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
}
