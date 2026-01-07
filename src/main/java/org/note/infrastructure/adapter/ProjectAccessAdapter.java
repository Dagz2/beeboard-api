package org.note.infrastructure.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.note.domain.port.ProjectAccessPort;

import java.util.UUID;

/**
 * Adapter implementing ProjectAccessPort.
 * Provides access to project information from the Project BC.
 *
 * This implementation uses direct database queries to the project table.
 * In a microservices architecture, this could be replaced with REST API calls.
 */
@ApplicationScoped
public class ProjectAccessAdapter implements ProjectAccessPort {

    @Inject
    EntityManager entityManager;

    @Override
    public boolean isContributor(UUID projectId, UUID userId) {
        try {
            // Query project_contributors table (assuming it exists)
            Long count = entityManager.createQuery(
                    "SELECT COUNT(pc) FROM ProjectContributorEntity pc " +
                    "WHERE pc.projectId = :projectId AND pc.userId = :userId",
                    Long.class
            )
            .setParameter("projectId", projectId)
            .setParameter("userId", userId)
            .getSingleResult();

            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public boolean isOwner(UUID projectId, UUID userId) {
        try {
            // Query project table for owner
            UUID ownerId = entityManager.createQuery(
                    "SELECT p.ownerId FROM ProjectEntity p WHERE p.projectId = :projectId",
                    UUID.class
            )
            .setParameter("projectId", projectId)
            .getSingleResult();

            return userId.equals(ownerId);
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public boolean exists(UUID projectId) {
        try {
            Long count = entityManager.createQuery(
                    "SELECT COUNT(p) FROM ProjectEntity p WHERE p.projectId = :projectId",
                    Long.class
            )
            .setParameter("projectId", projectId)
            .getSingleResult();

            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }
}
