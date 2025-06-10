package org.acme.domain.service;
import org.acme.domain.model.HashedPassword;

/**
* Domain interface for hashing and verifying passwords.
*/
public interface PasswordHasher {

    /**
     * Hashes a raw (plain-text) password.
     *
     * @param rawPassword the plain-text password to hash; must not be null or empty
     * @return a {@link HashedPassword} containing the salted hash
     * @throws NullPointerException     if rawPassword is null
     * @throws IllegalArgumentException if rawPassword is blank
     */
    HashedPassword hash(String rawPassword);

    /**
     * Verifies a raw (plain-text) password against its hashed representation.
     *
     * @param hashedPassword the {@link HashedPassword} instance; must not be null
     * @param rawPassword    the plain-text password to verify; must not be null or empty
     * @return {@code true} if the raw password matches the hashed password, {@code false} otherwise
     * @throws NullPointerException if hashedPassword or rawPassword is null
     */
    boolean verify(HashedPassword hashedPassword, String rawPassword);
}