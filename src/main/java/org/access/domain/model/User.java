package org.access.domain.model;

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
    private Password password;
    private RoleId roleId;
    private ContractEndDate contractEndDate;
    private boolean isLocked;
    private final AuditInfo auditInfo;
    private Instant lastActivityDate;

    // -------------------- Creation -------------------- 

    /**
     * Private constructor for User.
     *
     * @param id unique identifier of the user
     * @param fullName full name value object
     * @param email email address value object
     * @param password password value object
     * @param roleId role identifier value object
     * @param contractEndDate contract end date value object
     * @param auditInfo audit information value object
     * @param lastActivityDate timestamp of last user activity
     */
    public User(UserId id, FullName fullName, EmailAddress email, Password password, RoleId roleId, ContractEndDate contractEndDate, Boolean isLocked, AuditInfo auditInfo, Instant lastActivityDate) {
        this.id = Objects.requireNonNull(id, "Id cannot be null.");
        this.fullName = Objects.requireNonNull(fullName, "FullName cannot be null.");
        this.email = Objects.requireNonNull(email, "EmailAddress cannot be null.");
        this.password = Objects.requireNonNull(password, "Password cannot be null.");
        this.roleId = Objects.requireNonNull(roleId, "RoleId cannot be null.");
        this.contractEndDate = Objects.requireNonNull(contractEndDate, "ContractEndDate cannot be null.");
        this.isLocked = Objects.requireNonNull(isLocked, "IsLocked cannot be null.");
        this.auditInfo = Objects.requireNonNull(auditInfo, "AuditInfo cannot be null.");
        this.lastActivityDate = lastActivityDate;
    }

    /**
     * Factory method to create a new User
     *
     * @param fullName full name value object
     * @param email email address value object
     * @param password password value object
     * @param roleId role identifier value object
     * @param contractEndDate contract end date value object
     * @return a new User instance
     */
    public static User create(FullName fullName, EmailAddress email, Password password, RoleId roleId, ContractEndDate contractEndDate) {
        Instant now = Instant.now();
        AuditInfo auditInfo = AuditInfo.of(now);
        Boolean isLocked = false; // User is active by default
        return new User(UserId.newId(), fullName, email, password, roleId, contractEndDate, isLocked, auditInfo, now);
    }

    // -------------------- Domain methods -------------------- 

    /**
     * Updates the user's email address.
     *
     * @param newEmail the new email address value object (must not be null)
     */
    public void changeEmail(EmailAddress newEmail) {
        this.email = Objects.requireNonNull(newEmail, "New email cannot be null.");
        auditInfo.touch();
    }

    /**
     * Updates the user's full name.
     *
     * @param newFullName the new full name value object (must not be null)
     */
    public void changeFullName(FullName newFullName) {
        this.fullName = Objects.requireNonNull(newFullName, "New full name cannot be null.");
        auditInfo.touch();
    }

    /**
     * Assigns a new role to the user.
     *
     * @param newRoleId the new role identifier value object (can't be null)
     */
    public void assignRole(RoleId newRoleId) {
        if(newRoleId == this.roleId) {
            throw new IllegalStateException("This user already have this role.");
        }
        this.roleId = Objects.requireNonNull(newRoleId, "New role ID cannot be null.");
        auditInfo.touch();
    }

    /**
     * Extends the user's contract end date.
     *
     * @param newEndDate the new contract end date value object (can't not be null)
     */
    public void extendContract(ContractEndDate newEndDate) {
        this.contractEndDate = Objects.requireNonNull(newEndDate, "New contract end date cannot be null.");
        //The new date must be after the old one
        if (!newEndDate.isAfter(this.contractEndDate)) {
        throw new IllegalArgumentException(String.format(
            "New contract end date (%s) must be after the current one (%s).", newEndDate.date(), this.contractEndDate.date()));
        }
        auditInfo.touch();
    }

    /**
     * Terminates the user's contract at the given date.
     * This method can be used for voluntary resignations or dismissals.
     *
     * @param terminationDate the effective termination date (must not be null and not in the future)
     * @throws NullPointerException if {@code terminationDate} is null
     * @throws IllegalArgumentException if {@code terminationDate} is in the future
     */
    public void terminateContract() {
        this.contractEndDate = ContractEndDate.newContractEndDate(LocalDate.now());
        lock();
        auditInfo.touch();
    }

    /**
     * Locks the user's account to prevent authentication.
     */
    public void lock() {
        if(this.isLocked == true) {
            throw new IllegalStateException("This user is already locked.");
        }
        this.isLocked = true;
        auditInfo.touch();
    }

    /**
     * Unlocks the user's account to allow authentication.
     */
    public void unlock() {
        if(this.isLocked == false) {
            throw new IllegalStateException("This user is already active.");
        }
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
     * Changes the user's password.
     *
     * @param newPassword the new password value object (must not be null)
     */
    public void changePassword(Password newPassword) {
        this.password = Objects.requireNonNull(newPassword, "New password cannot be null.");
        auditInfo.touch();
    }

    /**
     * Verifies if the provided plain text password matches the user's password.
     *
     * @param plainPassword the plain text password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean verifyPassword(String plainPassword) {
        return password.matches(plainPassword);
    }

    /**
     * Checks whether the user's contract has expired.
     *
     * @return true if the current time is after the contract end date; false otherwise
     */
    public boolean isContractExpired() {
        return LocalDate.now().isAfter(contractEndDate.date());
    }

    // -------------------- Getters -------------------- 

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
     * Gets the user's password.
     *
     * @return the password value object
     */
    public Password getPassword() {
        return password;
    }

    // -------------------- Overrides -------------------- 

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

    /**
     * Returns a string representation of the user, including their ID and current role.
     *
     * @return a string in the format {@code User[USER_ID]}
     */
    @Override
    public String toString() {
        return "User[" + this.id.id() + "]";
    }
}