package org.access.application.dto.response;

/**
 * Response DTO for successful authentication.
 *
 * @param token the JWT access token
 * @param tokenType the type of token (typically "Bearer")
 * @param expiresIn the token expiration time in seconds
 */
public record AuthenticationResponse(
    String token,
    String tokenType,
    Long expiresIn
) {
    /**
     * Creates an AuthenticationResponse with Bearer token type.
     *
     * @param token the JWT token
     * @param expiresIn the expiration time in seconds
     * @return a new AuthenticationResponse instance
     */
    public static AuthenticationResponse of(String token, Long expiresIn) {
        return new AuthenticationResponse(token, "Bearer", expiresIn);
    }
}
