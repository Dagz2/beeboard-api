package org.note.domain.port;

import java.util.UUID;

/**
 * Port interface to communicate with the Project BC.
 * Used to verify project membership for PROJECT-visibility notes.
 */
public interface ProjectAccessPort {

    /**
     * Checks if a user is a contributor of the specified project.
     *
     * @param projectId the project identifier
     * @param userId the user identifier
     * @return true if the user is a contributor
     */
    boolean isContributor(UUID projectId, UUID userId);

    /**
     * Checks if a user is the owner of the specified project.
     *
     * @param projectId the project identifier
     * @param userId the user identifier
     * @return true if the user is the owner
     */
    boolean isOwner(UUID projectId, UUID userId);

    /**
     * Checks if a project exists.
     *
     * @param projectId the project identifier
     * @return true if the project exists
     */
    boolean exists(UUID projectId);
}
