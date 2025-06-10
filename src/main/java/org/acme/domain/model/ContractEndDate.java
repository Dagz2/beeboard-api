package org.acme.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object representing the end date of a user's contract.
 * Ensures the date is not null and not in the past.
 */
public record ContractEndDate(LocalDate date) {

    /**
     * Canonical constructor with validation.
     *
     * @param date the contract end date
     * @throws NullPointerException if date is null
     * @throws IllegalArgumentException if date is in the past
     */
    public ContractEndDate {
        Objects.requireNonNull(date, "Contract end date cannot be null");
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                String.format("Contract end date cannot be in the past: %s", date)
            );
        }
    }
}