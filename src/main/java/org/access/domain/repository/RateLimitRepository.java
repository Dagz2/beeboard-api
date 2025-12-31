package org.access.domain.repository;

import org.access.domain.model.RateLimit;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository interface for accessing and persisting {@link RateLimit} entries.
 *
 * This is a Domain-Driven Design (DDD) style repository, representing
 * the contract for retrieving and storing rate limit data without
 * exposing infrastructure details.
 */
public interface RateLimitRepository {

    /**
     * Finds a rate limit entry by email address.
     *
     * @param email the email address to look up
     * @return an {@link Optional} containing the rate limit if found
     */
    Optional<RateLimit> findByEmail(String email);

    /**
     * Saves or updates a rate limit entry.
     *
     * @param rateLimit the rate limit to persist
     */
    void save(RateLimit rateLimit);

    /**
     * Deletes all rate limit entries where the window started before the cutoff.
     *
     * @param cutoff entries with windowStart before this instant will be deleted
     * @return the number of entries deleted
     */
    long deleteExpired(Instant cutoff);
}
