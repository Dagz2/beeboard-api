package org.access.application.usecase.command.auth;

import org.access.application.dto.command.auth.LoginCommand;
import org.access.application.dto.response.AuthenticationResponse;
import org.access.application.service.JwtService;
import org.access.domain.model.EmailAddress;
import org.access.domain.model.User;
import org.access.domain.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;

/**
 * Use case for authenticating a user.
 * Validates credentials and generates a JWT token on successful authentication.
 */
@ApplicationScoped
public class AuthenticateUser {

    @Inject
    UserRepository userRepository;

    @Inject
    JwtService jwtService;

    /**
     * Executes the authentication process.
     *
     * @param command the login command containing email and password
     * @return an AuthenticationResponse with the JWT token
     * @throws NotAuthorizedException if authentication fails
     */
    @Transactional
    public AuthenticationResponse execute(LoginCommand command) {
        // Find user by email
        EmailAddress email = new EmailAddress(command.email());
        User user = userRepository.findByMail(email)
                .orElseThrow(() -> new NotAuthorizedException("Invalid email or password"));

        // Check if user is locked
        if (user.isLocked()) {
            throw new NotAuthorizedException("Account is locked. Please contact support.");
        }

        // Verify password
        if (!user.verifyPassword(command.password())) {
            throw new NotAuthorizedException("Invalid email or password");
        }

        // Check if contract has expired
        if (user.isContractExpired()) {
            throw new NotAuthorizedException("Account contract has expired. Please contact support.");
        }

        // Record activity
        user.recordActivity();
        userRepository.update(user);

        // Generate JWT token
        String token = jwtService.generateToken(user);

        // Return response with token and expiration
        return AuthenticationResponse.of(token, 3600L); // 1 hour expiration
    }
}