package org.access.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * JPA entity representing a rate limit entry for password reset requests.
 *
 * Maps to the "rate_limit_password_reset" table and tracks the number
 * of password reset requests per email address within a time window.
 *
 * Used to prevent abuse by limiting the number of reset requests
 * a user can make within a specific time period.
 */
@Entity
@Table(name = "rate_limit_password_reset")
public class RateLimitEntity extends PanacheEntityBase {

    /**
     * Email address for which rate limiting is being tracked.
     * Serves as the primary key.
     */
    @Id
    @NotNull
    @Column(name = "email", length = 255, nullable = false)
    private String email;

    /**
     * Number of password reset requests made within the current time window.
     */
    @NotNull
    @Column(name = "request_count", nullable = false)
    private Integer requestCount;

    /**
     * Timestamp when the current rate limit window started.
     * Requests are counted from this point forward.
     */
    @NotNull
    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    // -------------------- Getters & Setters --------------------

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Integer requestCount) {
        this.requestCount = requestCount;
    }

    public Instant getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(Instant windowStart) {
        this.windowStart = windowStart;
    }
}
