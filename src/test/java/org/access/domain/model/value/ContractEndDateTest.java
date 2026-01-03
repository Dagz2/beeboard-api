package org.access.domain.model.value;

import org.access.domain.model.ContractEndDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ContractEndDate Value Object")
class ContractEndDateTest {

    @Nested
    @DisplayName("Creation with constructor")
    class CreationWithConstructor {

        @Test
        @DisplayName("should create contract end date with valid future date")
        void shouldCreateWithValidFutureDate() {
            LocalDate futureDate = LocalDate.now().plusYears(1);

            ContractEndDate contractEndDate = new ContractEndDate(futureDate);

            assertThat(contractEndDate.date()).isEqualTo(futureDate);
        }

        @Test
        @DisplayName("should create contract end date with today")
        void shouldCreateWithToday() {
            LocalDate today = LocalDate.now();

            ContractEndDate contractEndDate = new ContractEndDate(today);

            assertThat(contractEndDate.date()).isEqualTo(today);
        }

        @Test
        @DisplayName("should create contract end date with past date via constructor")
        void shouldCreateWithPastDateViaConstructor() {
            LocalDate pastDate = LocalDate.now().minusDays(1);

            ContractEndDate contractEndDate = new ContractEndDate(pastDate);

            assertThat(contractEndDate.date()).isEqualTo(pastDate);
        }

        @Test
        @DisplayName("should reject null date")
        void shouldRejectNullDate() {
            assertThatThrownBy(() -> new ContractEndDate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Contract end date cannot be null.");
        }
    }

    @Nested
    @DisplayName("Creation with factory method")
    class CreationWithFactoryMethod {

        @Test
        @DisplayName("should create contract end date with future date")
        void shouldCreateWithFutureDate() {
            LocalDate futureDate = LocalDate.now().plusMonths(6);

            ContractEndDate contractEndDate = ContractEndDate.newContractEndDate(futureDate);

            assertThat(contractEndDate.date()).isEqualTo(futureDate);
        }

        @Test
        @DisplayName("should create contract end date with today")
        void shouldCreateWithToday() {
            LocalDate today = LocalDate.now();

            ContractEndDate contractEndDate = ContractEndDate.newContractEndDate(today);

            assertThat(contractEndDate.date()).isEqualTo(today);
        }

        @Test
        @DisplayName("should reject past date via factory method")
        void shouldRejectPastDateViaFactoryMethod() {
            LocalDate pastDate = LocalDate.now().minusDays(1);

            assertThatThrownBy(() -> ContractEndDate.newContractEndDate(pastDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contract end date cannot be in the past");
        }

        @Test
        @DisplayName("should reject null date via factory method")
        void shouldRejectNullDateViaFactoryMethod() {
            assertThatThrownBy(() -> ContractEndDate.newContractEndDate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Contract end date cannot be null.");
        }
    }

    @Nested
    @DisplayName("Comparison methods")
    class ComparisonMethods {

        @Test
        @DisplayName("isAfter should return true when this date is after other")
        void isAfterShouldReturnTrueWhenAfter() {
            ContractEndDate later = new ContractEndDate(LocalDate.now().plusMonths(6));
            ContractEndDate earlier = new ContractEndDate(LocalDate.now().plusMonths(3));

            assertThat(later.isAfter(earlier)).isTrue();
        }

        @Test
        @DisplayName("isAfter should return false when this date is before other")
        void isAfterShouldReturnFalseWhenBefore() {
            ContractEndDate earlier = new ContractEndDate(LocalDate.now().plusMonths(3));
            ContractEndDate later = new ContractEndDate(LocalDate.now().plusMonths(6));

            assertThat(earlier.isAfter(later)).isFalse();
        }

        @Test
        @DisplayName("isAfter should return false when dates are equal")
        void isAfterShouldReturnFalseWhenEqual() {
            LocalDate date = LocalDate.now().plusMonths(6);
            ContractEndDate date1 = new ContractEndDate(date);
            ContractEndDate date2 = new ContractEndDate(date);

            assertThat(date1.isAfter(date2)).isFalse();
        }

        @Test
        @DisplayName("isAfter should reject null")
        void isAfterShouldRejectNull() {
            ContractEndDate date = new ContractEndDate(LocalDate.now().plusMonths(6));

            assertThatThrownBy(() -> date.isAfter(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Other contract end date must not be null.");
        }

        @Test
        @DisplayName("isBefore should return true when this date is before other")
        void isBeforeShouldReturnTrueWhenBefore() {
            ContractEndDate earlier = new ContractEndDate(LocalDate.now().plusMonths(3));
            ContractEndDate later = new ContractEndDate(LocalDate.now().plusMonths(6));

            assertThat(earlier.isBefore(later)).isTrue();
        }

        @Test
        @DisplayName("isBefore should return false when this date is after other")
        void isBeforeShouldReturnFalseWhenAfter() {
            ContractEndDate later = new ContractEndDate(LocalDate.now().plusMonths(6));
            ContractEndDate earlier = new ContractEndDate(LocalDate.now().plusMonths(3));

            assertThat(later.isBefore(earlier)).isFalse();
        }

        @Test
        @DisplayName("isBefore should return false when dates are equal")
        void isBeforeShouldReturnFalseWhenEqual() {
            LocalDate date = LocalDate.now().plusMonths(6);
            ContractEndDate date1 = new ContractEndDate(date);
            ContractEndDate date2 = new ContractEndDate(date);

            assertThat(date1.isBefore(date2)).isFalse();
        }

        @Test
        @DisplayName("isBefore should reject null")
        void isBeforeShouldRejectNull() {
            ContractEndDate date = new ContractEndDate(LocalDate.now().plusMonths(6));

            assertThatThrownBy(() -> date.isBefore(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Other contract end date must not be null.");
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal when dates are equal")
        void shouldBeEqualWhenDatesAreEqual() {
            LocalDate date = LocalDate.now().plusMonths(6);
            ContractEndDate date1 = new ContractEndDate(date);
            ContractEndDate date2 = new ContractEndDate(date);

            assertThat(date1).isEqualTo(date2);
            assertThat(date1.hashCode()).isEqualTo(date2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when dates differ")
        void shouldNotBeEqualWhenDatesDiffer() {
            ContractEndDate date1 = new ContractEndDate(LocalDate.now().plusMonths(3));
            ContractEndDate date2 = new ContractEndDate(LocalDate.now().plusMonths(6));

            assertThat(date1).isNotEqualTo(date2);
        }
    }
}
