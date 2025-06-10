package org.acme.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Aggregate root representing a system user.
 */

public class User {
    private final UserId id;
    private FullName fullName;
    private EmailAddress email;
    private RoleId roleId;
    private ContractEndDate contractEndDate;
    private boolean isLocked;
    private final AuditInfo auditInfo;
    private Instant lastActivityDate;

    /**
     * Private constructor for User. Use {@link #create(UserId, FullName, EmailAddress, RoleId, ContractEndDate)}.
     *
     * @param id unique identifier of the user
     * @param fullName full name value object
     * @param email email address value object
     * @param roleId role identifier value object
     * @param contractEndDate contract end date value object
     * @param auditInfo audit information value object
     * @param lastActivityDate timestamp of last user activity
     */
    private User(UserId id, FullName fullName, EmailAddress email, RoleId roleId, ContractEndDate contractEndDate, AuditInfo auditInfo, Instant lastActivityDate) {
        this.id = Objects.requireNonNull(id, "UserId cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "FullName cannot be null");
        this.email = Objects.requireNonNull(email, "EmailAddress cannot be null");
        this.roleId = Objects.requireNonNull(roleId, "RoleId cannot be null");
        this.contractEndDate = Objects.requireNonNull(contractEndDate, "ContractEndDate cannot be null");
        this.isLocked = false;
        this.auditInfo = Objects.requireNonNull(auditInfo, "AuditInfo cannot be null");
        this.lastActivityDate = Objects.requireNonNull(lastActivityDate, "LastActivityDate cannot be null");
    }

    /**
     * Factory method to create a new User with initial audit and activity timestamps.
     *
     * @param id unique identifier of the user
     * @param fullName full name value object
     * @param email email address value object
     * @param roleId role identifier value object
     * @param contractEndDate contract end date value object
     * @return a new User instance
     */
    public static User create(UserId id, FullName fullName, EmailAddress email, RoleId roleId, ContractEndDate contractEndDate) {
        Instant now = Instant.now();
        AuditInfo auditInfo = AuditInfo.of(now);
        return new User(id, fullName, email, roleId, contractEndDate, auditInfo, now);
    }

    /**
     * Gets the unique identifier of this user.
     *
     * @return the user ID
     */
    public UserId getId() {
        return id;
    }

    /**
     * Gets the full name of this user.
     *
     * @return the user's full name
     */
    public FullName getFullName() {
        return fullName;
    }

    /**
     * Gets the email address of this user.
     *
     * @return the user's email address
     */ 
    public EmailAddress getEmail() {
        return email;
    }

    /**
     * Gets the role identifier of this user.
     *
     * @return the role ID
     */
    public RoleId getRoleId() {
        return roleId;
    }

    /**
     * Gets the contract end date of this user.
     *
     * @return the contract end date
     */
    public ContractEndDate getContractEndDate() {
        return contractEndDate;
    }

    /**
     * Checks if the user's account is locked.
     *
     * @return true if locked; false otherwise
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Gets audit information for this user.
     *
     * @return the audit information
     */
    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    /**
     * Gets the timestamp of the last recorded activity for this user.
     *
     * @return the last activity timestamp
     */
    public Instant getLastActivityDate() {
        return lastActivityDate;
    }

    /**
     * Updates the user's email address.
     *
     * @param newEmail the new email address value object (must not be null)
     */
    public void changeEmail(EmailAddress newEmail) {
        this.email = Objects.requireNonNull(newEmail, "New email cannot be null");
        auditInfo.touch();
    }

    /**
     * Updates the user's full name.
     *
     * @param newFullName the new full name value object (must not be null)
     */
    public void changeFullName(FullName newFullName) {
        this.fullName = Objects.requireNonNull(newFullName, "New full name cannot be null");
        auditInfo.touch();
    }

    /**
     * Assigns a new role to the user.
     *
     * @param newRoleId the new role identifier value object (can't be null)
     */
    public void assignRole(RoleId newRoleId) {
        this.roleId = Objects.requireNonNull(newRoleId, "New role ID cannot be null");
        auditInfo.touch();
    }

    /**
     * Extends the user's contract end date.
     *
     * @param newEndDate the new contract end date value object (can't not be null)
     */
    public void extendContract(ContractEndDate newEndDate) {
        this.contractEndDate = Objects.requireNonNull(newEndDate, "New contract end date cannot be null");
        auditInfo.touch();
    }

    /**
     * Locks the user's account to prevent authentication.
     */
    public void lock() {
        this.isLocked = true;
        auditInfo.touch();
    }

    /**
     * Unlocks the user's account to allow authentication.
     */
    public void unlock() {
        this.isLocked = false;
        auditInfo.touch();
    }

    /**
     * Records an activity by updating the lastActivityDate.
     * Also updates the audit timestamp.
     */
    public void recordActivity() {
        this.lastActivityDate = Instant.now();
        auditInfo.touch();
    }

    /**
     * Checks whether the user's contract has expired.
     *
     * @return true if the current time is after the contract end date; false otherwise
     */
    public boolean isContractExpired() {
        return LocalDate.now().isAfter(contractEndDate.date());
    }

    /**
     * Two User instances are considered equal if they share the same business identifier (UserId).
     * This ensures entity identity is based solely on the unique id, not on mutable attributes.
     *
     * @param o the object to compare with
     * @return true if both objects are the same instance or have equal UserIds
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    /**
     * Computes the hash code based solely on the UserId to maintain consistency with equals().
     *
     * @return hash code derived from the UserId
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}