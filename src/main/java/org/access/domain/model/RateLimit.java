package org.access.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Domain model representing a rate limit entry for password reset requests.
 *
 * This entity tracks the number of password reset requests made by an email
 * address within a sliding time window to prevent abuse.
 */
public class RateLimit {

    private final String email;
    private final int requestCount;
    private final Instant windowStart;

    /**
     * Creates a new rate limit entry.
     *
     * @param email        the email address being rate limited
     * @param requestCount the number of requests made in the current window
     * @param windowStart  when the current rate limit window started
     */
    public RateLimit(String email, int requestCount, Instant windowStart) {
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.requestCount = requestCount;
        this.windowStart = Objects.requireNonNull(windowStart, "Window start cannot be null");
    }

    /**
     * Creates a new rate limit entry for a first request.
     *
     * @param email the email address to track
     * @return a new rate limit with count of 1 and current time as window start
     */
    public static RateLimit firstRequest(String email) {
        return new RateLimit(email, 1, Instant.now());
    }

    /**
     * Gets the email address being rate limited.
     */
    public String email() {
        return email;
    }

    /**
     * Gets the number of requests made in the current window.
     */
    public int requestCount() {
        return requestCount;
    }

    /**
     * Gets when the current rate limit window started.
     */
    public Instant windowStart() {
        return windowStart;
    }

    /**
     * Checks if the rate limit has been exceeded.
     *
     * @param maxRequests the maximum number of requests allowed in the window
     * @return true if the limit has been exceeded
     */
    public boolean isLimitExceeded(int maxRequests) {
        return requestCount >= maxRequests;
    }

    /**
     * Checks if the current time window has expired.
     *
     * @param windowHours the duration of the window in hours
     * @return true if the window has expired
     */
    public boolean isWindowExpired(int windowHours) {
        Instant windowEnd = windowStart.plus(windowHours, ChronoUnit.HOURS);
        return Instant.now().isAfter(windowEnd);
    }

    /**
     * Creates a new rate limit with an incremented request count.
     *
     * @return a new rate limit with count + 1
     */
    public RateLimit increment() {
        return new RateLimit(email, requestCount + 1, windowStart);
    }

    /**
     * Creates a new rate limit with a reset window starting now.
     *
     * @return a new rate limit with count of 1 and current time as window start
     */
    public RateLimit resetWindow() {
        return new RateLimit(email, 1, Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateLimit rateLimit = (RateLimit) o;
        return email.equals(rateLimit.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "RateLimit[email=" + email +
               ", count=" + requestCount +
               ", windowStart=" + windowStart + "]";
    }
}
