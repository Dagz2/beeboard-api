package org.access.application.service;

import io.smallrye.jwt.build.Jwt;
import org.access.domain.model.Role;
import org.access.domain.model.User;
import org.access.domain.repository.RoleRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Service for generating and managing JWT tokens.
 * Uses SmallRye JWT for token generation with RSA key signing.
 */
@ApplicationScoped
public class JwtService {

    @Inject
    RoleRepository roleRepository;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "beeboard-api")
    String issuer;

    @ConfigProperty(name = "jwt.duration", defaultValue = "3600")
    Long tokenDuration; // Duration in seconds (default: 1 hour)

    /**
     * Generates a JWT token for the authenticated user.
     *
     * @param user the authenticated user
     * @return a signed JWT token string
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(tokenDuration);

        // Retrieve role to get the role code (DIRECTOR, HR_MANAGER, etc.)
        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalStateException("Role not found for user: " + user.getId().id()));

        return Jwt.issuer(issuer)
                .upn(user.getEmail().value()) // User Principal Name (email)
                .subject(user.getId().id().toString()) // Subject (user ID)
                .groups(new HashSet<>(Arrays.asList(role.code().value()))) // Groups (role CODE for @RolesAllowed)
                .issuedAt(now)
                .expiresAt(expirationTime)
                .claim("fullName", user.getFullName().firstName() + " " + user.getFullName().lastName())
                .sign();
    }

    /**
     * Generates a JWT token with custom expiration time.
     *
     * @param user the authenticated user
     * @param durationInSeconds custom duration in seconds
     * @return a signed JWT token string
     */
    public String generateToken(User user, long durationInSeconds) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(durationInSeconds);

        // Retrieve role to get the role code (DIRECTOR, HR_MANAGER, etc.)
        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalStateException("Role not found for user: " + user.getId().id()));

        return Jwt.issuer(issuer)
                .upn(user.getEmail().value())
                .subject(user.getId().id().toString())
                .groups(new HashSet<>(Arrays.asList(role.code().value()))) // Groups (role CODE for @RolesAllowed)
                .issuedAt(now)
                .expiresAt(expirationTime)
                .claim("fullName", user.getFullName().firstName() + " " + user.getFullName().lastName())
                .sign();
    }
}
