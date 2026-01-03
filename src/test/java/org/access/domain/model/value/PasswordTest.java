package org.access.domain.model.value;

import org.access.domain.model.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Password Value Object")
class PasswordTest {

    private static final String VALID_PASSWORD = "SecurePassword123!@#$%^&*()_+abcd";

    @Nested
    @DisplayName("Creation from plain text")
    class CreationFromPlainText {

        @Test
        @DisplayName("should create password from valid plain text")
        void shouldCreatePasswordFromValidPlainText() {
            Password password = Password.fromPlainText(VALID_PASSWORD);

            assertThat(password).isNotNull();
            assertThat(password.hash()).isNotBlank();
            assertThat(password.hash()).isNotEqualTo(VALID_PASSWORD);
        }

        @Test
        @DisplayName("should hash password with BCrypt")
        void shouldHashPasswordWithBCrypt() {
            Password password = Password.fromPlainText(VALID_PASSWORD);

            assertThat(password.hash()).startsWith("$2a$");
        }

        @Test
        @DisplayName("should generate different hashes for same password")
        void shouldGenerateDifferentHashesForSamePassword() {
            Password password1 = Password.fromPlainText(VALID_PASSWORD);
            Password password2 = Password.fromPlainText(VALID_PASSWORD);

            assertThat(password1.hash()).isNotEqualTo(password2.hash());
        }
    }

    @Nested
    @DisplayName("Creation from hash")
    class CreationFromHash {

        @Test
        @DisplayName("should create password from existing hash")
        void shouldCreatePasswordFromExistingHash() {
            Password original = Password.fromPlainText(VALID_PASSWORD);
            String hash = original.hash();

            Password reconstructed = Password.fromHash(hash);

            assertThat(reconstructed.hash()).isEqualTo(hash);
        }

        @Test
        @DisplayName("should reject null hash")
        void shouldRejectNullHash() {
            assertThatThrownBy(() -> Password.fromHash(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hashed password cannot be null or empty.");
        }

        @Test
        @DisplayName("should reject empty hash")
        void shouldRejectEmptyHash() {
            assertThatThrownBy(() -> Password.fromHash(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hashed password cannot be null or empty.");
        }
    }

    @Nested
    @DisplayName("Validation requirements")
    class ValidationRequirements {

        @Test
        @DisplayName("should reject null password")
        void shouldRejectNullPassword() {
            assertThatThrownBy(() -> Password.fromPlainText(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty.");
        }

        @Test
        @DisplayName("should reject empty password")
        void shouldRejectEmptyPassword() {
            assertThatThrownBy(() -> Password.fromPlainText(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password cannot be null or empty.");
        }

        @Test
        @DisplayName("should reject password shorter than 32 characters")
        void shouldRejectPasswordShorterThan32Characters() {
            String shortPassword = "Short1!";

            assertThatThrownBy(() -> Password.fromPlainText(shortPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 32 characters long.");
        }

        @Test
        @DisplayName("should reject password without digit")
        void shouldRejectPasswordWithoutDigit() {
            String noDigitPassword = "SecurePasswordWithoutDigit!@#$%^";

            assertThatThrownBy(() -> Password.fromPlainText(noDigitPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must contain at least one digit (0-9).");
        }

        @Test
        @DisplayName("should reject password without special character")
        void shouldRejectPasswordWithoutSpecialCharacter() {
            String noSpecialPassword = "SecurePassword1234567890abcdefgh";

            assertThatThrownBy(() -> Password.fromPlainText(noSpecialPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must contain at least one special character");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "ValidPassword123!@#$%^&*()_+abcd",
            "AnotherSecure1Password!WithSpecial",
            "Password123!PasswordPassword1234"
        })
        @DisplayName("should accept valid passwords meeting all requirements")
        void shouldAcceptValidPasswords(String validPassword) {
            Password password = Password.fromPlainText(validPassword);

            assertThat(password).isNotNull();
        }
    }

    @Nested
    @DisplayName("Verification")
    class Verification {

        @Test
        @DisplayName("should verify correct password")
        void shouldVerifyCorrectPassword() {
            Password password = Password.fromPlainText(VALID_PASSWORD);

            assertThat(password.matches(VALID_PASSWORD)).isTrue();
        }

        @Test
        @DisplayName("should reject incorrect password")
        void shouldRejectIncorrectPassword() {
            Password password = Password.fromPlainText(VALID_PASSWORD);

            assertThat(password.matches("WrongPassword123!@#$%^&*()_")).isFalse();
        }

        @Test
        @DisplayName("should reject null password for verification")
        void shouldRejectNullPasswordForVerification() {
            Password password = Password.fromPlainText(VALID_PASSWORD);

            assertThat(password.matches(null)).isFalse();
        }

        @Test
        @DisplayName("should reject empty password for verification")
        void shouldRejectEmptyPasswordForVerification() {
            Password password = Password.fromPlainText(VALID_PASSWORD);

            assertThat(password.matches("")).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when hashes are equal")
        void shouldBeEqualWhenHashesAreEqual() {
            Password original = Password.fromPlainText(VALID_PASSWORD);
            Password reconstructed = Password.fromHash(original.hash());

            assertThat(original).isEqualTo(reconstructed);
        }

        @Test
        @DisplayName("should not be equal when hashes differ")
        void shouldNotBeEqualWhenHashesDiffer() {
            Password password1 = Password.fromPlainText(VALID_PASSWORD);
            Password password2 = Password.fromPlainText(VALID_PASSWORD);

            assertThat(password1).isNotEqualTo(password2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should return protected placeholder")
        void shouldReturnProtectedPlaceholder() {
            Password password = Password.fromPlainText(VALID_PASSWORD);

            assertThat(password.toString()).isEqualTo("[PROTECTED]");
            assertThat(password.toString()).doesNotContain(VALID_PASSWORD);
        }
    }
}
