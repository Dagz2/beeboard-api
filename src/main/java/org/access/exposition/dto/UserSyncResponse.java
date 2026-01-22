package org.access.exposition.dto;

import org.shared.domain.AccountStatus;
import org.shared.domain.ApplicationRole;

import java.util.UUID;

/**
 * Response DTO for the /all user synchronization endpoint.
 *
 * <p>This DTO is used by the initial synchronization endpoint to provide
 * all user data needed for Note BC's author_view CQRS read model.</p>
 *
 * <p>Unlike {@link org.shared.domain.UserContext} (Published Language for authorization),
 * this DTO includes fullName which is needed for display purposes in Note BC.</p>
 *
 * <h3>Design Note:</h3>
 * <p>This is intentionally separate from UserContext to maintain the Published Language
 * principle: UserContext is minimal for authorization, while UserSyncResponse includes
 * additional data for synchronization.</p>
 */
public record UserSyncResponse(
    UUID userId,
    ApplicationRole applicationRole,
    AccountStatus accountStatus,
    String fullName
) {
    /**
     * Compact constructor with validation.
     */
    public UserSyncResponse {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (applicationRole == null) {
            throw new IllegalArgumentException("applicationRole is required");
        }
        if (accountStatus == null) {
            throw new IllegalArgumentException("accountStatus is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName is required and cannot be blank");
        }
    }
}
