package org.access.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;

import org.access.domain.model.RateLimit;
import org.access.domain.repository.RateLimitRepository;
import org.access.infrastructure.entity.RateLimitEntity;
import org.access.infrastructure.mapper.RateLimitMapper;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.time.Instant;
import java.util.Optional;

/**
 * JPA-based implementation of the {@link RateLimitRepository} interface using Panache.
 *
 * This class provides persistence logic for {@link RateLimit} entries,
 * delegating object-relational mapping to {@link RateLimitMapper}.
 */
@ApplicationScoped
public class RateLimitRepositoryImpl implements RateLimitRepository, PanacheRepositoryBase<RateLimitEntity, String> {

    /**
     * Finds a rate limit entry by email address.
     *
     * @param email the email address to look up
     * @return an {@link Optional} containing the rate limit if found
     */
    @Override
    public Optional<RateLimit> findByEmail(String email) {
        return findByIdOptional(email)
            .map(RateLimitMapper::toDomain);
    }

    /**
     * Saves or updates a rate limit entry.
     *
     * @param rateLimit the rate limit to persist
     */
    @Override
    public void save(RateLimit rateLimit) {
        RateLimitEntity entity = RateLimitMapper.toEntity(rateLimit);
        getEntityManager().merge(entity);
    }

    /**
     * Deletes all rate limit entries where the window started before the cutoff.
     *
     * @param cutoff entries with windowStart before this instant will be deleted
     * @return the number of entries deleted
     */
    @Override
    public long deleteExpired(Instant cutoff) {
        return delete("windowStart < ?1", cutoff);
    }
}
