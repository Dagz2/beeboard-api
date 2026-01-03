package org.access.application.service;

import org.access.domain.model.RateLimit;
import org.access.domain.repository.RateLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService")
class RateLimitServiceTest {

    @Mock
    RateLimitRepository rateLimitRepository;

    RateLimitService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new RateLimitService(rateLimitRepository);

        // Set configuration values via reflection
        Field rateLimitField = RateLimitService.class.getDeclaredField("rateLimit");
        rateLimitField.setAccessible(true);
        rateLimitField.set(service, 3);

        Field windowHoursField = RateLimitService.class.getDeclaredField("windowHours");
        windowHoursField.setAccessible(true);
        windowHoursField.set(service, 1);
    }

    @Nested
    @DisplayName("isRateLimitExceeded")
    class IsRateLimitExceeded {

        @Test
        @DisplayName("should return false when no previous requests")
        void shouldReturnFalseWhenNoRequests() {
            // Given
            when(rateLimitRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

            // When
            boolean result = service.isRateLimitExceeded("test@example.com");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when under limit")
        void shouldReturnFalseWhenUnderLimit() {
            // Given
            RateLimit entry = new RateLimit("test@example.com", 2, Instant.now());
            when(rateLimitRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(entry));

            // When
            boolean result = service.isRateLimitExceeded("test@example.com");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return true when limit exceeded")
        void shouldReturnTrueWhenLimitExceeded() {
            // Given
            RateLimit entry = new RateLimit("test@example.com", 3, Instant.now());
            when(rateLimitRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(entry));

            // When
            boolean result = service.isRateLimitExceeded("test@example.com");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when window expired")
        void shouldReturnFalseWhenWindowExpired() {
            // Given
            Instant expiredWindowStart = Instant.now().minus(2, ChronoUnit.HOURS);
            RateLimit expiredEntry = new RateLimit("test@example.com", 5, expiredWindowStart);
            when(rateLimitRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(expiredEntry));

            // When
            boolean result = service.isRateLimitExceeded("test@example.com");

            // Then
            assertThat(result).isFalse();
            verify(rateLimitRepository).save(any(RateLimit.class));
        }
    }

    @Nested
    @DisplayName("incrementRateLimit")
    class IncrementRateLimit {

        @Test
        @DisplayName("should create first request entry")
        void shouldCreateFirstRequestEntry() {
            // Given
            when(rateLimitRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

            // When
            service.incrementRateLimit("test@example.com");

            // Then
            verify(rateLimitRepository).save(argThat(entry ->
                entry.email().equals("test@example.com") &&
                entry.requestCount() == 1
            ));
        }

        @Test
        @DisplayName("should increment existing entry")
        void shouldIncrementExistingEntry() {
            // Given
            RateLimit existingEntry = new RateLimit("test@example.com", 2, Instant.now());
            when(rateLimitRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingEntry));

            // When
            service.incrementRateLimit("test@example.com");

            // Then
            verify(rateLimitRepository).save(argThat(entry ->
                entry.email().equals("test@example.com") &&
                entry.requestCount() == 3
            ));
        }

        @Test
        @DisplayName("should reset window when expired")
        void shouldResetWindowWhenExpired() {
            // Given
            Instant expiredWindowStart = Instant.now().minus(2, ChronoUnit.HOURS);
            RateLimit expiredEntry = new RateLimit("test@example.com", 5, expiredWindowStart);
            when(rateLimitRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(expiredEntry));

            // When
            service.incrementRateLimit("test@example.com");

            // Then
            verify(rateLimitRepository).save(argThat(entry ->
                entry.email().equals("test@example.com") &&
                entry.requestCount() == 1
            ));
        }
    }

    @Nested
    @DisplayName("cleanupExpiredLimits")
    class CleanupExpiredLimits {

        @Test
        @DisplayName("should call repository to delete expired entries")
        void shouldDeleteExpiredEntries() {
            // Given
            when(rateLimitRepository.deleteExpired(any(Instant.class))).thenReturn(5L);

            // When
            service.cleanupExpiredLimits();

            // Then
            verify(rateLimitRepository).deleteExpired(any(Instant.class));
        }
    }
}
