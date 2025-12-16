package org.access.application.service;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.access.domain.repository.PasswordResetRequestRepository;

/**
 * Service responsible for scheduled cleanup of expired password reset tokens
 * and rate limit entries.
 *
 * Runs periodic jobs to maintain database hygiene by removing obsolete data.
 */
@ApplicationScoped
public class PasswordResetCleanupService {

    @Inject
    PasswordResetRequestRepository resetRepository;

    @Inject
    RateLimitService rateLimitService;

    /**
     * Cleans up expired and used password reset tokens.
     *
     * This job runs every hour to delete tokens that are either:
     * - Marked as used, OR
     * - Expired (past their 15-minute validity window)
     *
     * Maintains database hygiene and prevents accumulation of obsolete data.
     */
    @Scheduled(every = "1h")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            resetRepository.deleteExpiredTokens();
            Log.info("Expired password reset tokens cleanup completed");
        } catch (Exception e) {
            Log.error("Failed to cleanup expired password reset tokens", e);
        }
    }

    /**
     * Cleans up expired rate limit entries.
     *
     * This job runs every hour to delete rate limit entries where the
     * time window has expired (older than the configured window duration).
     *
     * Maintains database hygiene for the rate limiting system.
     */
    @Scheduled(every = "1h")
    @Transactional
    public void cleanupExpiredRateLimits() {
        try {
            rateLimitService.cleanupExpiredLimits();
            Log.info("Expired rate limit entries cleanup completed");
        } catch (Exception e) {
            Log.error("Failed to cleanup expired rate limit entries", e);
        }
    }
}
