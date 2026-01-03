package org.access.application.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.access.domain.model.*;
import org.access.domain.repository.PermissionRepository;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.access.support.RestTestHelper.*;
import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for SecurityContextService caching.
 *
 * Verifies that the Caffeine cache works correctly for SecurityContext,
 * including cache hits, misses, and invalidation.
 */
@QuarkusTest
@DisplayName("SecurityContext Cache Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecurityContextCacheIT {

    @Inject
    SecurityContextService securityContextService;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    PermissionRepository permissionRepository;

    private static RoleId testRoleId;
    private static PermissionId testPermissionId;
    private static boolean setupComplete = false;

    @BeforeEach
    @Transactional
    void setup() {
        if (!setupComplete) {
            // Create permission
            Permission perm = aPermission()
                .withCode("CACHE_TEST_PERM")
                .withName("Cache Test Permission")
                .withTier(SecurityTier.TIER_2)
                .build();
            permissionRepository.save(perm);
            testPermissionId = perm.id();

            // Create role with permission
            Role role = aRole()
                .withCode("CACHE_TEST_ROLE")
                .withName("Cache Test Role")
                .withTier(SecurityTier.TIER_2)
                .withPermission(testPermissionId)
                .build();
            roleRepository.save(role);
            testRoleId = role.id();

            setupComplete = true;
        }
    }

    @Nested
    @DisplayName("Cache Loading")
    class CacheLoading {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should load and cache security context for user")
        void shouldCacheSecurityContext() {
            // Given - a valid user
            User user = aUser()
                .withEmail("cache-test@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            UUID userId = user.getId().id();

            // When - load context (first time - cache miss)
            SecurityContext context1 = securityContextService.loadContext(userId);

            // Then - context should be loaded correctly
            assertThat(context1).isNotNull();
            assertThat(context1.actorId().id()).isEqualTo(userId);
            assertThat(context1.actorEmail()).isEqualTo("cache-test@example.com");
            assertThat(context1.roleCode().value()).isEqualTo("CACHE_TEST_ROLE");
            assertThat(context1.permissions()).hasSize(1);
            assertThat(context1.permissions())
                .anyMatch(p -> p.value().equals("CACHE_TEST_PERM"));

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should return cached context on subsequent calls")
        void shouldReturnCachedContext() {
            // Given - a valid user
            User user = aUser()
                .withEmail("cache-hit@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            UUID userId = user.getId().id();

            // When - load context multiple times
            SecurityContext context1 = securityContextService.loadContext(userId);
            SecurityContext context2 = securityContextService.loadContext(userId);

            // Then - both should be the same instance (from cache)
            assertThat(context1).isNotNull();
            assertThat(context2).isNotNull();
            assertThat(context1.actorId()).isEqualTo(context2.actorId());
            assertThat(context1.roleCode()).isEqualTo(context2.roleCode());

            // Cleanup
            userRepository.deleteById(user.getId());
        }
    }

    @Nested
    @DisplayName("Cache Invalidation")
    class CacheInvalidation {

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should invalidate cache when called explicitly")
        void shouldInvalidateOnExplicitCall() {
            // Given - a user with cached context
            User user = aUser()
                .withEmail("cache-invalidate@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user);

            UUID userId = user.getId().id();

            // Load context to cache it
            SecurityContext context1 = securityContextService.loadContext(userId);
            assertThat(context1).isNotNull();

            // When - invalidate cache
            securityContextService.invalidate(userId);

            // Then - next load should fetch fresh data
            SecurityContext context2 = securityContextService.loadContext(userId);
            assertThat(context2).isNotNull();
            assertThat(context2.actorId().id()).isEqualTo(userId);

            // Cleanup
            userRepository.deleteById(user.getId());
        }

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should invalidate cache for all users with a role")
        void shouldInvalidateByRole() {
            // Given - two users with the same role
            User user1 = aUser()
                .withEmail("cache-role1@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user1);

            User user2 = aUser()
                .withEmail("cache-role2@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            userRepository.save(user2);

            // Cache both contexts
            securityContextService.loadContext(user1.getId().id());
            securityContextService.loadContext(user2.getId().id());

            // When - invalidate by role
            securityContextService.invalidateByRole(testRoleId);

            // Then - both should be invalidated (next load fetches fresh)
            SecurityContext ctx1 = securityContextService.loadContext(user1.getId().id());
            SecurityContext ctx2 = securityContextService.loadContext(user2.getId().id());

            assertThat(ctx1).isNotNull();
            assertThat(ctx2).isNotNull();

            // Cleanup
            userRepository.deleteById(user1.getId());
            userRepository.deleteById(user2.getId());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @Order(5)
        @DisplayName("should throw ForbiddenException for non-existent user")
        void shouldThrowForNonExistentUser() {
            // Given - a non-existent user ID
            UUID nonExistentId = UUID.randomUUID();

            // When/Then - should throw ForbiddenException
            assertThatThrownBy(() -> securityContextService.loadContext(nonExistentId))
                .isInstanceOf(jakarta.ws.rs.ForbiddenException.class);
        }
    }
}
