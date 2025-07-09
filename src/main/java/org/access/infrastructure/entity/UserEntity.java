package org.access.infrastructure.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.*;

/**
 * JPA entity representing a user in the system.
 * 
 * Maps to the "user" table and holds user-related data including identity, name,
 * email, contract status, activity, and audit information.
 * 
 * This entity is part of the infrastructure layer and mirrors the domain aggregate `User`.
 */
@Entity
@Table(name = "user")
public class UserEntity extends PanacheEntityBase {

    /**
     * Unique identifier for the user.
     */
    @Id
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * First name of the user.
     */
    @NotNull
    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    /**
     * Last name of the user.
     */
    @NotNull
    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    /**
     * Email address of the user.
     */
    @NotNull
    @Column(name = "email", length = 255, nullable = false)
    private String email;

    /**
     * Date at which the user's contract ends.
     */
    @NotNull
    @Column(name = "contract_end_date", nullable = false)
    private LocalDate contractEndDate;

    /**
     * Whether the user account is currently locked.
     */
    @NotNull
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    /**
     * Timestamp of the user's last recorded activity.
     */
    @NotNull
    @Column(name = "last_activity_date", nullable = false)
    private Instant lastActivityDate;

    /**
     * Unique identifier for the role.
     */
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    /**
     * Audit information including creation and last modification timestamps.
     */
    @Embedded
    @NotNull
    private AuditInfoEmbeddable auditInfo;

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

    public AuditInfoEmbeddable getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoEmbeddable auditInfo) {
        this.auditInfo = auditInfo;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }
}