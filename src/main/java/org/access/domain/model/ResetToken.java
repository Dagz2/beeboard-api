package org.access.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Value Object representing a password reset token.
 *
 * A reset token is a cryptographically secure random string used for
 * password reset operations. It provides 256 bits of entropy and is
 * represented as 64 hexadecimal characters.
 *
 * For security, tokens are stored as SHA-256 hashes in the database,
 * never in plaintext.
 */
public record ResetToken(String value) {

    private static final int TOKEN_LENGTH = 64; // 64 hex chars = 32 bytes = 256 bits
    private static final String HEX_PATTERN = "^[a-f0-9]{64}$";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Compact constructor that validates the token format.
     *
     * @param value the token value (must be 64 hexadecimal characters)
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if value doesn't match the required format
     */
    public ResetToken {
        Objects.requireNonNull(value, "Token value cannot be null");

        if (value.length() != TOKEN_LENGTH) {
            throw new IllegalArgumentException(
                "Token must be exactly " + TOKEN_LENGTH + " characters long");
        }

        if (!value.matches(HEX_PATTERN)) {
            throw new IllegalArgumentException(
                "Token must contain only lowercase hexadecimal characters (a-f, 0-9)");
        }
    }

    /**
     * Generates a new cryptographically secure random token.
     *
     * Uses {@link SecureRandom} to generate 32 random bytes (256 bits),
     * which are then converted to 64 hexadecimal characters.
     *
     * @return a new {@link ResetToken} with a randomly generated value
     */
    public static ResetToken generate() {
        byte[] randomBytes = new byte[32]; // 32 bytes = 256 bits
        SECURE_RANDOM.nextBytes(randomBytes);

        StringBuilder hexString = new StringBuilder();
        for (byte b : randomBytes) {
            hexString.append(String.format("%02x", b));
        }

        return new ResetToken(hexString.toString());
    }

    /**
     * Computes the SHA-256 hash of this token.
     *
     * This hash should be stored in the database instead of the plaintext token
     * to prevent token theft in case of database compromise.
     *
     * @return the SHA-256 hash as a 64-character hexadecimal string
     */
    public String hash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available in all Java platforms
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Returns a placeholder to avoid revealing the actual token value in logs.
     *
     * @return a constant placeholder string
     */
    @Override
    public String toString() {
        return "[RESET_TOKEN]";
    }
}
