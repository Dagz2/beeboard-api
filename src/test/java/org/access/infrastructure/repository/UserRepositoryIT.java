package org.access.infrastructure.repository;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.access.domain.model.*;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@QuarkusTest
@DisplayName("UserRepository Integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserRepositoryIT {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    static RoleId testRoleId;
    static UserId testUserId;
    static boolean roleCreated = false;

    @Transactional
    void ensureRoleExists() {
        if (!roleCreated) {
            Role role = Role.create(
                new RoleCode("USER_TEST_ROLE"),
                new RoleName("User Test Role")
            );
            testRoleId = role.id();
            roleRepository.save(role);
            roleCreated = true;
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CrudOperations {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should save and retrieve user")
        void shouldSaveAndRetrieveUser() {
            // Setup - ensure role exists
            ensureRoleExists();

            // Given
            User user = aUser()
                .withEmail("it-user@example.com")
                .withFullName("Integration", "Test")
                .withRoleId(testRoleId)
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();
            testUserId = user.getId();

            // When
            userRepository.save(user);
            Optional<User> found = userRepository.findById(testUserId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail().value()).isEqualTo("it-user@example.com");
            assertThat(found.get().getFullName().firstName()).isEqualTo("Integration");
            assertThat(found.get().getFullName().lastName()).isEqualTo("Test");
            assertThat(found.get().isLocked()).isFalse();
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should find user by email")
        void shouldFindUserByEmail() {
            // Given
            EmailAddress email = new EmailAddress("it-user@example.com");

            // When
            Optional<User> found = userRepository.findByMail(email);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(testUserId);
        }

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should find user by full name")
        void shouldFindUserByFullName() {
            // Given
            FullName name = new FullName("Integration", "Test");

            // When
            Optional<User> found = userRepository.findByName(name);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(testUserId);
        }

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should update user")
        void shouldUpdateUser() {
            // Given
            Optional<User> userOpt = userRepository.findById(testUserId);
            assertThat(userOpt).isPresent();
            User user = userOpt.get();

            // When - lock the user
            user.lock();
            userRepository.update(user);

            // Then
            Optional<User> found = userRepository.findById(testUserId);
            assertThat(found).isPresent();
            assertThat(found.get().isLocked()).isTrue();
        }

        @Test
        @Order(5)
        @Transactional
        @DisplayName("should check user existence")
        void shouldCheckUserExistence() {
            // When/Then
            assertThat(userRepository.existsById(testUserId)).isTrue();
            assertThat(userRepository.existsById(UserId.newId())).isFalse();
        }

        @Test
        @Order(6)
        @Transactional
        @DisplayName("should find all users")
        void shouldFindAllUsers() {
            // When
            List<User> allUsers = userRepository.findAllUsers();

            // Then
            assertThat(allUsers).isNotEmpty();
            assertThat(allUsers).anyMatch(u -> u.getEmail().value().equals("it-user@example.com"));
        }

        @Test
        @Order(7)
        @Transactional
        @DisplayName("should find users by role id")
        void shouldFindUsersByRoleId() {
            // When
            List<User> usersWithRole = userRepository.findByRoleId(testRoleId);

            // Then
            assertThat(usersWithRole).isNotEmpty();
            assertThat(usersWithRole).allMatch(u -> u.getRoleId().equals(testRoleId));
        }

        @Test
        @Order(8)
        @Transactional
        @DisplayName("should check users exist by role id")
        void shouldCheckUsersExistByRoleId() {
            // When/Then
            assertThat(userRepository.existsByRoleId(testRoleId)).isTrue();
            assertThat(userRepository.existsByRoleId(RoleId.newId())).isFalse();
        }

        @Test
        @Order(9)
        @Transactional
        @DisplayName("should return empty when user not found by id")
        void shouldReturnEmptyWhenUserNotFoundById() {
            // Given
            UserId nonExistentId = UserId.newId();

            // When
            Optional<User> found = userRepository.findById(nonExistentId);

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @Order(10)
        @Transactional
        @DisplayName("should return empty when user not found by email")
        void shouldReturnEmptyWhenUserNotFoundByEmail() {
            // Given
            EmailAddress nonExistentEmail = new EmailAddress("nonexistent@example.com");

            // When
            Optional<User> found = userRepository.findByMail(nonExistentEmail);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Contract and Lock Status")
    class ContractAndLockStatus {

        @Test
        @Transactional
        @DisplayName("should persist locked status")
        void shouldPersistLockedStatus() {
            // Setup
            ensureRoleExists();

            // Given
            User lockedUser = aUser()
                .withEmail("locked-it@example.com")
                .withRoleId(testRoleId)
                .locked()
                .build();

            // When
            userRepository.save(lockedUser);

            // Then
            Optional<User> found = userRepository.findById(lockedUser.getId());
            assertThat(found).isPresent();
            assertThat(found.get().isLocked()).isTrue();

            // Cleanup
            userRepository.deleteById(lockedUser.getId());
        }

        @Test
        @Transactional
        @DisplayName("should persist contract end date")
        void shouldPersistContractEndDate() {
            // Setup
            ensureRoleExists();

            // Given
            LocalDate futureDate = LocalDate.of(2030, 12, 31);
            User user = aUser()
                .withEmail("contract-it@example.com")
                .withRoleId(testRoleId)
                .withContractEndDate(futureDate)
                .build();

            // When
            userRepository.save(user);

            // Then
            Optional<User> found = userRepository.findById(user.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getContractEndDate().date()).isEqualTo(futureDate);

            // Cleanup
            userRepository.deleteById(user.getId());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        @Transactional
        @DisplayName("should delete user by id")
        void shouldDeleteUserById() {
            // Setup
            ensureRoleExists();

            // Given
            User user = aUser()
                .withEmail("delete-it@example.com")
                .withRoleId(testRoleId)
                .build();
            userRepository.save(user);
            assertThat(userRepository.existsById(user.getId())).isTrue();

            // When
            userRepository.deleteById(user.getId());

            // Then
            assertThat(userRepository.existsById(user.getId())).isFalse();
        }
    }
}
