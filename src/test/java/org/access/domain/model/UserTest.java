package org.access.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.access.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("User Aggregate")
class UserTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create user with factory method")
        void shouldCreateUserWithFactoryMethod() {
            FullName fullName = validFullName();
            EmailAddress email = validEmail();
            Password password = validPassword();
            RoleId roleId = randomRoleId();
            ContractEndDate contractEndDate = futureContractEndDate();

            User user = User.create(fullName, email, password, roleId, contractEndDate);

            assertThat(user.getId()).isNotNull();
            assertThat(user.getFullName()).isEqualTo(fullName);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getRoleId()).isEqualTo(roleId);
            assertThat(user.getContractEndDate()).isEqualTo(contractEndDate);
            assertThat(user.isLocked()).isFalse();
            assertThat(user.getAuditInfo()).isNotNull();
            assertThat(user.getLastActivityDate()).isNotNull();
        }

        @Test
        @DisplayName("should create user with builder")
        void shouldCreateUserWithBuilder() {
            User user = aUser()
                .withFullName("Jane", "Smith")
                .withEmail("jane@example.com")
                .locked()
                .build();

            assertThat(user.getFullName().firstName()).isEqualTo("Jane");
            assertThat(user.getFullName().lastName()).isEqualTo("Smith");
            assertThat(user.getEmail().value()).isEqualTo("jane@example.com");
            assertThat(user.isLocked()).isTrue();
        }
    }

    @Nested
    @DisplayName("Email change")
    class EmailChange {

        @Test
        @DisplayName("should change email")
        void shouldChangeEmail() {
            User user = aUser().withEmail("old@example.com").build();
            EmailAddress newEmail = emailOf("new@example.com");

            user.changeEmail(newEmail);

            assertThat(user.getEmail()).isEqualTo(newEmail);
        }

        @Test
        @DisplayName("should reject null email")
        void shouldRejectNullEmail() {
            User user = aUser().build();

            assertThatThrownBy(() -> user.changeEmail(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New email cannot be null.");
        }
    }

    @Nested
    @DisplayName("Full name change")
    class FullNameChange {

        @Test
        @DisplayName("should change full name")
        void shouldChangeFullName() {
            User user = aUser().build();
            FullName newName = fullNameOf("Jane", "Smith");

            user.changeFullName(newName);

            assertThat(user.getFullName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("should reject null full name")
        void shouldRejectNullFullName() {
            User user = aUser().build();

            assertThatThrownBy(() -> user.changeFullName(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New full name cannot be null.");
        }
    }

    @Nested
    @DisplayName("Role assignment")
    class RoleAssignment {

        @Test
        @DisplayName("should assign new role")
        void shouldAssignNewRole() {
            User user = aUser().build();
            RoleId newRoleId = randomRoleId();

            user.assignRole(newRoleId);

            assertThat(user.getRoleId()).isEqualTo(newRoleId);
        }

        @Test
        @DisplayName("should reject null role ID")
        void shouldRejectNullRoleId() {
            User user = aUser().build();

            assertThatThrownBy(() -> user.assignRole(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New role ID cannot be null.");
        }

        @Test
        @DisplayName("should reject same role assignment")
        void shouldRejectSameRoleAssignment() {
            RoleId roleId = randomRoleId();
            User user = aUser().withRoleId(roleId).build();

            assertThatThrownBy(() -> user.assignRole(roleId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This user already have this role.");
        }
    }

    @Nested
    @DisplayName("Password operations")
    class PasswordOperations {

        @Test
        @DisplayName("should change password")
        void shouldChangePassword() {
            User user = aUser().build();
            Password newPassword = passwordFromPlain("NewSecurePassword123!@#$%^&*()ab");

            user.changePassword(newPassword);

            assertThat(user.getPassword()).isEqualTo(newPassword);
        }

        @Test
        @DisplayName("should reject null password")
        void shouldRejectNullPassword() {
            User user = aUser().build();

            assertThatThrownBy(() -> user.changePassword(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New password cannot be null.");
        }

        @Test
        @DisplayName("should verify correct password")
        void shouldVerifyCorrectPassword() {
            User user = aUser().build();

            assertThat(user.verifyPassword(VALID_PASSWORD)).isTrue();
        }

        @Test
        @DisplayName("should reject incorrect password")
        void shouldRejectIncorrectPassword() {
            User user = aUser().build();

            assertThat(user.verifyPassword("WrongPassword123!@#$%^&*()_")).isFalse();
        }
    }

    @Nested
    @DisplayName("Lock/Unlock operations")
    class LockUnlockOperations {

        @Test
        @DisplayName("should lock active user")
        void shouldLockActiveUser() {
            User user = aUser().unlocked().build();

            user.lock();

            assertThat(user.isLocked()).isTrue();
        }

        @Test
        @DisplayName("should throw when locking already locked user")
        void shouldThrowWhenLockingAlreadyLockedUser() {
            User user = aUser().locked().build();

            assertThatThrownBy(() -> user.lock())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This user is already locked.");
        }

        @Test
        @DisplayName("should unlock locked user")
        void shouldUnlockLockedUser() {
            User user = aUser().locked().build();

            user.unlock();

            assertThat(user.isLocked()).isFalse();
        }

        @Test
        @DisplayName("should throw when unlocking already active user")
        void shouldThrowWhenUnlockingAlreadyActiveUser() {
            User user = aUser().unlocked().build();

            assertThatThrownBy(() -> user.unlock())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This user is already active.");
        }
    }

    @Nested
    @DisplayName("Contract operations")
    class ContractOperations {

        @Test
        @DisplayName("should detect expired contract")
        void shouldDetectExpiredContract() {
            User user = aUser().withExpiredContract().build();

            assertThat(user.isContractExpired()).isTrue();
        }

        @Test
        @DisplayName("should detect valid contract")
        void shouldDetectValidContract() {
            User user = aUser().withContractEndDate(LocalDate.now().plusYears(1)).build();

            assertThat(user.isContractExpired()).isFalse();
        }

        @Test
        @DisplayName("should terminate contract and lock user")
        void shouldTerminateContractAndLockUser() {
            User user = aUser()
                .withContractEndDate(LocalDate.now().plusYears(1))
                .unlocked()
                .build();

            user.terminateContract();

            assertThat(user.getContractEndDate().date()).isEqualTo(LocalDate.now());
            assertThat(user.isLocked()).isTrue();
        }
    }

    @Nested
    @DisplayName("Activity recording")
    class ActivityRecording {

        @Test
        @DisplayName("should record activity with timestamp update")
        void shouldRecordActivityWithTimestampUpdate() {
            User user = aUser().build();
            var originalActivityDate = user.getLastActivityDate();

            user.recordActivity();

            assertThat(user.getLastActivityDate()).isAfterOrEqualTo(originalActivityDate);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when IDs are equal")
        void shouldBeEqualWhenIdsAreEqual() {
            UserId userId = randomUserId();
            User user1 = aUser().withId(userId).withFullName("John", "Doe").build();
            User user2 = aUser().withId(userId).withFullName("Jane", "Smith").build();

            assertThat(user1).isEqualTo(user2);
            assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            User user1 = aUser().build();
            User user2 = aUser().build();

            assertThat(user1).isNotEqualTo(user2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should include user ID")
        void shouldIncludeUserId() {
            User user = aUser().build();

            assertThat(user.toString()).contains("User[");
            assertThat(user.toString()).contains(user.getId().id().toString());
        }
    }
}
