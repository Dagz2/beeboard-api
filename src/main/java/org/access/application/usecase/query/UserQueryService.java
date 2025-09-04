package org.access.application.usecase.query;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.query.UserDto;
import org.access.application.mapper.UserMapper;
import org.access.domain.model.EmailAddress;
import org.access.domain.model.FullName;
import org.access.domain.model.RoleId;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for querying {@link User} data.
 *
 * <p>This service provides read-only operations on users, converting domain objects
 * into {@link UserDto} representations suitable for the application layer.
 */
@ApplicationScoped
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * Constructs the query service with the required repository.
     *
     * @param userRepository the repository for accessing user aggregates
     */
    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all users in the system.
     *
     * @return list of {@link UserDto} representations
     */
    public List<UserDto> findAllUsers() {
        return userRepository.findAllUsers().stream()
            .map(UserMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Finds a user by their unique identifier.
     *
     * @param userId the ID of the user to retrieve
     * @return an optional {@link UserDto} if found
     */
    public Optional<UserDto> findById(UUID userId) {
        return userRepository.findById(new UserId(userId))
            .map(UserMapper::toDto);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an optional {@link UserDto} if found
     */
    public Optional<UserDto> findByEmail(EmailAddress email) {
        return userRepository.findByMail(email)
            .map(UserMapper::toDto);
    }

    /**
     * Finds a user by their full name.
     *
     * @param fullName the full name to search for
     * @return an optional {@link UserDto} if found
     */
    public Optional<UserDto> findByFullName(FullName fullName) {
        return userRepository.findByName(fullName)
            .map(UserMapper::toDto);
    }

    /**
     * Determines whether the user's contract has expired.
     *
     * @param userId the user ID
     * @return {@code true} if expired, {@code false} if still active
     * @throws IllegalArgumentException if the user doesn't exist
     */
    public boolean isContractExpired(UserId userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId.id()));
        return user.isContractExpired();
    }

    /**
     * Finds all users assigned to a specific role.
     *
     * @param roleId the role ID to search for
     * @return list of {@link UserDto} representations of users with the specified role
     */
    public List<UserDto> findUsersByRole(RoleId roleId) {
        return userRepository.findByRoleId(roleId).stream()
            .map(UserMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Finds all users assigned to a specific role.
     *
     * @param roleId the role ID as UUID to search for
     * @return list of {@link UserDto} representations of users with the specified role
     */
    public List<UserDto> findUsersByRole(UUID roleId) {
        return findUsersByRole(new RoleId(roleId));
    }
}
