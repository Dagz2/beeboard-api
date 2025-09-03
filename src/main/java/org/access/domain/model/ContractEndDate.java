package org.access.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object representing the end date of a user's contract.
 * Ensures the date is not null and not in the past.
 */
public record ContractEndDate(LocalDate date) {
     
    // -------------------- Creation -------------------- 

    /**
     * Compact constructor.
     * Validates that {@code date} is not null
     *
     * @param date the contract end date
     * @throws NullPointerException if {@code date} is null
     */
    public ContractEndDate {
        Objects.requireNonNull(date, "Contract end date cannot be null.");
    }
    
    /**
     * Static factory method for creating a {@code ContractEndDate}.
     * Validates that {@code date} is not null and not before {@link LocalDate#now()}.
     *
     * @param date the contract end date
     * @return a new instance of {@code ContractEndDate}
     * @throws NullPointerException if {@code date} is null
     * @throws IllegalArgumentException if {@code date} is before the current date
     */
    public static ContractEndDate newContractEndDate(LocalDate date) {
        Objects.requireNonNull(date, "Contract end date cannot be null.");
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                String.format("Contract end date cannot be in the past: %s.", date)
            );
        }
        return new ContractEndDate(date);
    }

    // -------------------- Domain methods -------------------- 

    /**
     * Checks whether this contract end date is after the specified one.
     *
     * @param other the other contract end date to compare with (must not be null)
     * @return {@code true} if this date is strictly after the other, {@code false} otherwise
     * @throws NullPointerException if {@code other} is null
     */
    public boolean isAfter(ContractEndDate other) {
        Objects.requireNonNull(other, "Other contract end date must not be null.");
        return this.date.isAfter(other.date());
    }

    /**
     * Checks whether this contract end date is before the specified one.
     *
     * @param other the other contract end date to compare with (must not be null)
     * @return {@code true} if this date is strictly before the other, {@code false} otherwise
     * @throws NullPointerException if {@code other} is null
     */
    public boolean isBefore(ContractEndDate other) {
        Objects.requireNonNull(other, "Other contract end date must not be null.");
        return this.date.isBefore(other.date());
    }
}