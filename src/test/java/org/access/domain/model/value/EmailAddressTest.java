package org.access.domain.model.value;

import org.access.domain.model.EmailAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EmailAddress Value Object")
class EmailAddressTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create email with valid format")
        void shouldCreateEmailWithValidFormat() {
            EmailAddress email = new EmailAddress("user@example.com");

            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("should trim whitespace from email")
        void shouldTrimWhitespaceFromEmail() {
            EmailAddress email = new EmailAddress("  user@example.com  ");

            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "simple@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user_name@example.com",
            "user-name@example.com",
            "user@sub.example.com",
            "user123@example123.com"
        })
        @DisplayName("should accept valid email formats")
        void shouldAcceptValidEmailFormats(String validEmail) {
            EmailAddress email = new EmailAddress(validEmail);

            assertThat(email.value()).isEqualTo(validEmail);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should reject null email")
        void shouldRejectNullEmail() {
            assertThatThrownBy(() -> new EmailAddress(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Email cannot be null");
        }

        @Test
        @DisplayName("should reject empty email")
        void shouldRejectEmptyEmail() {
            assertThatThrownBy(() -> new EmailAddress(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be empty");
        }

        @Test
        @DisplayName("should reject blank email")
        void shouldRejectBlankEmail() {
            assertThatThrownBy(() -> new EmailAddress("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be empty");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid",
            "missing-at-sign.com",
            "@missing-local.com",
            "missing-domain@",
            "spaces in@email.com",
            "double@@at.com"
        })
        @DisplayName("should reject invalid email formats")
        void shouldRejectInvalidEmailFormats(String invalidEmail) {
            assertThatThrownBy(() -> new EmailAddress(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when values are equal")
        void shouldBeEqualWhenValuesAreEqual() {
            EmailAddress email1 = new EmailAddress("user@example.com");
            EmailAddress email2 = new EmailAddress("user@example.com");

            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when values differ")
        void shouldNotBeEqualWhenValuesDiffer() {
            EmailAddress email1 = new EmailAddress("user1@example.com");
            EmailAddress email2 = new EmailAddress("user2@example.com");

            assertThat(email1).isNotEqualTo(email2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should return the email value")
        void shouldReturnTheEmailValue() {
            EmailAddress email = new EmailAddress("user@example.com");

            assertThat(email.toString()).isEqualTo("user@example.com");
        }
    }
}
