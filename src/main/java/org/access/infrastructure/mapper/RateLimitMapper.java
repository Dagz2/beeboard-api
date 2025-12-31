package org.access.infrastructure.mapper;

import org.access.domain.model.RateLimit;
import org.access.infrastructure.entity.RateLimitEntity;

/**
 * Mapper for converting between {@link RateLimit} domain model
 * and {@link RateLimitEntity} JPA entity.
 */
public final class RateLimitMapper {

    private RateLimitMapper() {
        // Utility class, prevent instantiation
    }

    /**
     * Converts a JPA entity to a domain model.
     *
     * @param entity the JPA entity
     * @return the domain model
     */
    public static RateLimit toDomain(RateLimitEntity entity) {
        return new RateLimit(
            entity.getEmail(),
            entity.getRequestCount(),
            entity.getWindowStart()
        );
    }

    /**
     * Converts a domain model to a JPA entity.
     *
     * @param domain the domain model
     * @return the JPA entity
     */
    public static RateLimitEntity toEntity(RateLimit domain) {
        RateLimitEntity entity = new RateLimitEntity();
        entity.setEmail(domain.email());
        entity.setRequestCount(domain.requestCount());
        entity.setWindowStart(domain.windowStart());
        return entity;
    }
}
