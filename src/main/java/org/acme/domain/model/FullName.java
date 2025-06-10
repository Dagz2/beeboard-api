package org.acme.domain.model;

import java.util.Objects;

/**
 * Value Object encapsulating a user's full name.
 * Ensures first and last names are non-null, non-blank, and trimmed.
 */
public record FullName(String firstName, String lastName) {

    /**
     * Canonical constructor with validation.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @throws NullPointerException if firstName or lastName is null
     * @throws IllegalArgumentException if firstName or lastName is blank
     */
    public FullName {
        Objects.requireNonNull(firstName, "First name cannot be null");
        Objects.requireNonNull(lastName, "Last name cannot be null");
        String f = firstName.trim();
        String l = lastName.trim();
        if (f.isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (l.isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        // assign trimmed values
        firstName = f;
        lastName = l;
    }

    /**
     * Returns the full name in "FirstName LastName" format.
     *
     * @return concatenated firstName and lastName
     */
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
