package org.access.application.usecase.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import org.access.application.dto.response.UserProfileResponse;
import org.access.application.dto.response.UserProfileResponse.RoleInfo;
import org.access.domain.model.Permission;
import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.repository.PermissionRepository;
import org.access.domain.repository.RoleRepository;

import java.util.List;

/**
 * Use case for retrieving the current authenticated user's profile.
 *
 * This query uses the JWT claims (email, fullName, roleCode) and loads
 * only the permissions from the database. This approach is lightweight
 * since user validation is handled by the SecurityFilter.
 */
@ApplicationScoped
public class GetCurrentUserProfile {

    @Inject
    RoleRepository roleRepository;

    @Inject
    PermissionRepository permissionRepository;

    /**
     * Executes the query to retrieve the current user's profile.
     *
     * Uses data from JWT claims and loads permissions from the database.
     * User status (locked, contract) is already validated by the SecurityFilter.
     *
     * @param userId the user's unique identifier (from JWT subject)
     * @param email the user's email (from JWT upn)
     * @param fullName the user's full name (from JWT claim)
     * @param roleCode the user's role code (from JWT groups)
     * @return a {@link UserProfileResponse} containing user info, role, and permissions
     * @throws NotFoundException if the role is not found
     */
    @Transactional
    public UserProfileResponse execute(String userId, String email, String fullName, String roleCode) {
        // Retrieve role by code
        Role role = roleRepository.findByCode(new RoleCode(roleCode))
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleCode));

        // Map permission IDs to permission codes
        List<String> permissionCodes = role.permissions().stream()
                .map(permissionId -> permissionRepository.findById(permissionId)
                        .map(Permission::code)
                        .map(code -> code.value())
                        .orElse(null))
                .filter(code -> code != null)
                .sorted()
                .toList();

        // Build response
        RoleInfo roleInfo = new RoleInfo(role.code().value(), role.name().value());

        return new UserProfileResponse(
                java.util.UUID.fromString(userId),
                email,
                fullName,
                roleInfo,
                permissionCodes
        );
    }
}
