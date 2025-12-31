package org.access.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Application service for checking user account status.
 *
 * This service encapsulates the logic for verifying if a user account
 * is valid, locked, or has an expired contract. It is used by the
 * exposition layer to enforce access control without directly accessing
 * the domain repository.
 */
@ApplicationScoped
public class UserStatusService {

    private final UserRepository userRepository;

    @Inject
    public UserStatusService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Represents the status of a user account.
     *
     * @param exists          true if the user exists in the system
     * @param locked          true if the user account is locked
     * @param contractExpired true if the user's contract has expired
     */
    public record UserStatus(boolean exists, boolean locked, boolean contractExpired) {

        /**
         * Creates a status indicating the user was not found.
         */
        public static UserStatus notFound() {
            return new UserStatus(false, false, false);
        }

        /**
         * Creates a status from a user domain object.
         */
        public static UserStatus fromUser(User user) {
            return new UserStatus(true, user.isLocked(), user.isContractExpired());
        }

        /**
         * Checks if access should be denied for any reason.
         */
        public boolean isAccessDenied() {
            return !exists || locked || contractExpired;
        }
    }

    /**
     * Checks the status of a user by their ID.
     *
     * @param userId the unique identifier of the user
     * @return the user's account status
     */
    public UserStatus checkStatus(UUID userId) {
        Optional<User> userOpt = userRepository.findById(new UserId(userId));

        if (userOpt.isEmpty()) {
            return UserStatus.notFound();
        }

        return UserStatus.fromUser(userOpt.get());
    }
}
