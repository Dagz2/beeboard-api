package org.access.application.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.access.domain.model.RateLimit;
import org.access.domain.repository.RateLimitRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Service responsible for rate limiting password reset requests.
 *
 * Implements a sliding window rate limiting mechanism to prevent abuse
 * by limiting the number of reset requests per email within a time window.
 */
@ApplicationScoped
public class RateLimitService {

    private final RateLimitRepository rateLimitRepository;

    @ConfigProperty(name = "password.reset.rate.limit", defaultValue = "3")
    Integer rateLimit;

    @ConfigProperty(name = "password.reset.rate.window.hours", defaultValue = "1")
    Integer windowHours;

    @Inject
    public RateLimitService(RateLimitRepository rateLimitRepository) {
        this.rateLimitRepository = rateLimitRepository;
    }

    /**
     * Checks if the given email has exceeded the rate limit.
     *
     * @param email the email address to check
     * @return true if rate limit is exceeded, false otherwise
     */
    @Transactional
    public boolean isRateLimitExceeded(String email) {
        Optional<RateLimit> rateLimitOpt = rateLimitRepository.findByEmail(email);

        if (rateLimitOpt.isEmpty()) {
            // No previous requests, not rate limited
            return false;
        }

        RateLimit entry = rateLimitOpt.get();

        if (entry.isWindowExpired(windowHours)) {
            // Window has expired, reset the counter
            rateLimitRepository.save(entry.resetWindow());
            return false;
        }

        // Within the window, check if limit is exceeded
        return entry.isLimitExceeded(rateLimit);
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
        Optional<RateLimit> rateLimitOpt = rateLimitRepository.findByEmail(email);

        RateLimit entry;
        if (rateLimitOpt.isEmpty()) {
            // First request for this email
            entry = RateLimit.firstRequest(email);
        } else {
            RateLimit existing = rateLimitOpt.get();
            if (existing.isWindowExpired(windowHours)) {
                // Window has expired, start a new one
                entry = existing.resetWindow();
            } else {
                // Within the window, increment
                entry = existing.increment();
            }
        }

        rateLimitRepository.save(entry);

        Log.debug("Rate limit incremented for email: " + email +
                  ", count: " + entry.requestCount());
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
        long deletedCount = rateLimitRepository.deleteExpired(cutoff);

        if (deletedCount > 0) {
            Log.info("Cleaned up " + deletedCount + " expired rate limit entries");
        }
    }
}
