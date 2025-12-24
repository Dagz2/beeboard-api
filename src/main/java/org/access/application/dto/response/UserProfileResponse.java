package org.access.application.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for the current user's profile.
 *
 * Contains user identity, role information, and the list of permission codes
 * associated with the user's role. This response is used by the frontend
 * to determine UI visibility and feature access.
 *
 * @param id the unique identifier of the user
 * @param email the user's email address
 * @param fullName the user's full name (first + last)
 * @param role the role information (code and name)
 * @param permissions list of permission codes granted to the user's role
 */
public record UserProfileResponse(
    UUID id,
    String email,
    String fullName,
    RoleInfo role,
    List<String> permissions
) {
    /**
     * Nested record representing role information.
     *
     * @param code the role code (e.g., "DIRECTOR", "HR_MANAGER")
     * @param name the human-readable role name
     */
    public record RoleInfo(
        String code,
        String name
    ) {}
}
