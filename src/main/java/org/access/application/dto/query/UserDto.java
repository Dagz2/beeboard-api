package org.access.application.dto.query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing a user returned from a query.
 *
 * This class is used in the application layer to expose user-related data,
 * typically in response to a read/query use case.
 *
 * A user is represented by their identity, personal information, contract status,
 * activity data, audit metadata, and assigned role.
 */
public class UserDto {

    /**
     * Unique identifier of the user.
     */
    public UUID userId;

    /**
     * First name of the user.
     */
    public String firstName;

    /**
     * Last name of the user.
     */
    public String lastName;

    /**
     * Email address of the user.
     */
    public String email;

    /**
     * Date at which the user's contract ends.
     */
    public LocalDate contractEndDate;

    /**
     * Whether the user account is currently locked.
     */
    public Boolean isLocked;

    /**
     * Timestamp of the user's last recorded activity.
     */
    public Instant lastActivityDate;

    /**
     * Audit metadata containing creation and update timestamps.
     */
    public AuditInfoDto auditInfo;

    /**
     * Identifier of the role assigned to the user.
     */
    public UUID roleId;

    /**
     * Default no-args constructor for frameworks and serialization.
     * Useful when populating fields via setters or reflection.
     */
        public UserDto() {
    }

    /**
     * Full constructor to create a fully populated UserDto instance.
     *
     * @param userId           the unique identifier of the user
     * @param firstName        the user's first name
     * @param lastName         the user's last name
     * @param email            the user's email address
     * @param contractEndDate  the date at which the user's contract ends
     * @param isLocked         whether the user's account is currently locked
     * @param lastActivityDate the timestamp of the user's last recorded activity
     * @param auditInfo        audit metadata (createdAt / updatedAt)
     * @param roleId           the unique identifier of the assigned role
     */
    public UserDto(
        UUID userId,
        String firstName,
        String lastName,
        String email,
        LocalDate contractEndDate,
        Boolean isLocked,
        Instant lastActivityDate,
        AuditInfoDto auditInfo,
        UUID roleId
    ) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contractEndDate = contractEndDate;
        this.isLocked = isLocked;
        this.lastActivityDate = lastActivityDate;
        this.auditInfo = auditInfo;
        this.roleId = roleId;
    }

    // -------------------- Getters & Setters --------------------

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public Instant getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(Instant lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public AuditInfoDto getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoDto auditInfo) {
        this.auditInfo = auditInfo;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRole(UUID roleId) {
        this.roleId = roleId;
    }
}
