package org.access.domain.model.value;

import org.access.domain.model.FullName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FullName Value Object")
class FullNameTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create full name with valid first and last name")
        void shouldCreateFullNameWithValidNames() {
            FullName fullName = new FullName("John", "Doe");

            assertThat(fullName.firstName()).isEqualTo("John");
            assertThat(fullName.lastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("should trim whitespace from names")
        void shouldTrimWhitespaceFromNames() {
            FullName fullName = new FullName("  John  ", "  Doe  ");

            assertThat(fullName.firstName()).isEqualTo("John");
            assertThat(fullName.lastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("should accept names with special characters")
        void shouldAcceptNamesWithSpecialCharacters() {
            FullName fullName = new FullName("Jean-Pierre", "O'Connor");

            assertThat(fullName.firstName()).isEqualTo("Jean-Pierre");
            assertThat(fullName.lastName()).isEqualTo("O'Connor");
        }

        @Test
        @DisplayName("should accept unicode names")
        void shouldAcceptUnicodeNames() {
            FullName fullName = new FullName("François", "Müller");

            assertThat(fullName.firstName()).isEqualTo("François");
            assertThat(fullName.lastName()).isEqualTo("Müller");
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should reject null first name")
        void shouldRejectNullFirstName() {
            assertThatThrownBy(() -> new FullName(null, "Doe"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("First name cannot be null.");
        }

        @Test
        @DisplayName("should reject null last name")
        void shouldRejectNullLastName() {
            assertThatThrownBy(() -> new FullName("John", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Last name cannot be null.");
        }

        @Test
        @DisplayName("should reject empty first name")
        void shouldRejectEmptyFirstName() {
            assertThatThrownBy(() -> new FullName("", "Doe"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name cannot be empty.");
        }

        @Test
        @DisplayName("should reject empty last name")
        void shouldRejectEmptyLastName() {
            assertThatThrownBy(() -> new FullName("John", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Last name cannot be empty.");
        }

        @Test
        @DisplayName("should reject blank first name")
        void shouldRejectBlankFirstName() {
            assertThatThrownBy(() -> new FullName("   ", "Doe"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name cannot be empty.");
        }

        @Test
        @DisplayName("should reject blank last name")
        void shouldRejectBlankLastName() {
            assertThatThrownBy(() -> new FullName("John", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Last name cannot be empty.");
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when first and last names are equal")
        void shouldBeEqualWhenNamesAreEqual() {
            FullName name1 = new FullName("John", "Doe");
            FullName name2 = new FullName("John", "Doe");

            assertThat(name1).isEqualTo(name2);
            assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when first names differ")
        void shouldNotBeEqualWhenFirstNamesDiffer() {
            FullName name1 = new FullName("John", "Doe");
            FullName name2 = new FullName("Jane", "Doe");

            assertThat(name1).isNotEqualTo(name2);
        }

        @Test
        @DisplayName("should not be equal when last names differ")
        void shouldNotBeEqualWhenLastNamesDiffer() {
            FullName name1 = new FullName("John", "Doe");
            FullName name2 = new FullName("John", "Smith");

            assertThat(name1).isNotEqualTo(name2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("should return 'FirstName LastName' format")
        void shouldReturnFirstNameLastNameFormat() {
            FullName fullName = new FullName("John", "Doe");

            assertThat(fullName.toString()).isEqualTo("John Doe");
        }
    }
}
