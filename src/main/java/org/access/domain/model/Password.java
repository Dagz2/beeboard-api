package org.access.domain.model;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

/**
 * Value object representing a user's password.
 * Handles secure password hashing and verification using BCrypt.
 */
public class Password {
    private final String hashedPassword;

    /**
     * Private constructor for Password.
     *
     * @param hashedPassword the BCrypt hashed password
     */
    private Password(String hashedPassword) {
        this.hashedPassword = Objects.requireNonNull(hashedPassword, "Hashed password cannot be null.");
    }

    /**
     * Creates a Password from a plain text password by hashing it.
     *
     * @param plainPassword the plain text password
     * @return a new Password instance with hashed password
     * @throws IllegalArgumentException if password doesn't meet security requirements
     */
    public static Password fromPlainText(String plainPassword) {
        validatePasswordRequirements(plainPassword);
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        return new Password(hashed);
    }

    /**
     * Validates that the password meets all security requirements.
     *
     * @param plainPassword the plain text password to validate
     * @throws IllegalArgumentException if password doesn't meet requirements
     */
    private static void validatePasswordRequirements(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }

        if (plainPassword.length() < 32) {
            throw new IllegalArgumentException("Password must be at least 32 characters long.");
        }

        // Check for at least one digit
        if (!plainPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit (0-9).");
        }

        // Check for at least one special character
        if (!plainPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character (!@#$%^&*()_+-=[]{}; ':\"\\|,.<>/?).");
        }
    }

    /**
     * Creates a Password from an already hashed password (for reconstruction from database).
     *
     * @param hashedPassword the BCrypt hashed password
     * @return a new Password instance
     */
    public static Password fromHash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty.");
        }
        return new Password(hashedPassword);
    }

    /**
     * Verifies if the provided plain text password matches this hashed password.
     *
     * @param plainPassword the plain text password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean matches(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * Gets the hashed password value.
     *
     * @return the BCrypt hashed password
     */
    public String hash() {
        return hashedPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password)) return false;
        Password password = (Password) o;
        return hashedPassword.equals(password.hashedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedPassword);
    }

    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
