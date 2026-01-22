package org.note.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for author_view table.
 *
 * <p>This entity represents the CQRS read model for user data from Access BC.
 * It is synchronized via domain events and optimized for read operations.</p>
 *
 * <p>Note: This entity uses String for access_level and account_status to match
 * the database schema. The domain layer uses proper enums (AccessLevel, AccountStatus).</p>
 */
@Entity
@Table(name = "author_view")
public class AuthorViewEntity extends PanacheEntityBase {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "access_level", length = 20, nullable = false)
    private String accessLevel;

    @Column(name = "account_status", length = 20, nullable = false)
    private String accountStatus;

    @Column(name = "full_name", length = 200, nullable = false)
    private String fullName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * JPA lifecycle callback - sets timestamps on entity creation.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    /**
     * JPA lifecycle callback - updates the updated_at timestamp.
     *
     * <p>Note: The database also has a trigger for this, but having it here
     * ensures consistency even if the trigger is disabled.</p>
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
