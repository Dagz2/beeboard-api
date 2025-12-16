package org.access.application.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.access.infrastructure.entity.RateLimitEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service responsible for rate limiting password reset requests.
 *
 * Implements a sliding window rate limiting mechanism to prevent abuse
 * by limiting the number of reset requests per email within a time window.
 */
@ApplicationScoped
public class RateLimitService {

    @ConfigProperty(name = "password.reset.rate.limit", defaultValue = "3")
    Integer rateLimit;

    @ConfigProperty(name = "password.reset.rate.window.hours", defaultValue = "1")
    Integer windowHours;

    /**
     * Checks if the given email has exceeded the rate limit.
     *
     * @param email the email address to check
     * @return true if rate limit is exceeded, false otherwise
     */
    @Transactional
    public boolean isRateLimitExceeded(String email) {
        RateLimitEntity entity = RateLimitEntity.findById(email);

        if (entity == null) {
            // No previous requests, not rate limited
            return false;
        }

        Instant windowStart = entity.getWindowStart();
        Instant now = Instant.now();
        Instant windowEnd = windowStart.plus(windowHours, ChronoUnit.HOURS);

        if (now.isAfter(windowEnd)) {
            // Window has expired, reset the counter
            entity.setRequestCount(0);
            entity.setWindowStart(now);
            entity.persist();
            return false;
        }

        // Within the window, check if limit is exceeded
        return entity.getRequestCount() >= rateLimit;
    }

    /**
     * Increments the request counter for the given email.
     *
     * If this is the first request or the window has expired, a new window is started.
     *
     * @param email the email address to increment
     */
    @Transactional
    public void incrementRateLimit(String email) {
        RateLimitEntity entity = RateLimitEntity.findById(email);
        Instant now = Instant.now();

        if (entity == null) {
            // First request for this email
            entity = new RateLimitEntity();
            entity.setEmail(email);
            entity.setRequestCount(1);
            entity.setWindowStart(now);
            entity.persist();
        } else {
            Instant windowEnd = entity.getWindowStart().plus(windowHours, ChronoUnit.HOURS);

            if (now.isAfter(windowEnd)) {
                // Window has expired, start a new one
                entity.setRequestCount(1);
                entity.setWindowStart(now);
            } else {
                // Within the window, increment
                entity.setRequestCount(entity.getRequestCount() + 1);
            }
            entity.persist();
        }

        Log.debug("Rate limit incremented for email: " + email +
                  ", count: " + entity.getRequestCount());
    }

    /**
     * Cleans up expired rate limit entries.
     *
     * This method deletes rate limit entries where the window has expired,
     * maintaining database hygiene. Typically called by a scheduled job.
     */
    @Transactional
    public void cleanupExpiredLimits() {
        Instant cutoff = Instant.now().minus(windowHours, ChronoUnit.HOURS);
        long deletedCount = RateLimitEntity.delete("windowStart < ?1", cutoff);

        if (deletedCount > 0) {
            Log.info("Cleaned up " + deletedCount + " expired rate limit entries");
        }
    }
}
