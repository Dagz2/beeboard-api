package org.note.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.note.domain.model.AccessLevel;
import org.note.domain.model.AuthorView;
import org.note.domain.repository.AuthorViewRepository;
import org.note.infrastructure.entity.AuthorViewEntity;
import org.note.infrastructure.mapper.AuthorViewMapper;
import org.shared.domain.AccountStatus;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Panache-based implementation of AuthorViewRepository.
 *
 * <p>This implementation uses Panache's simplified repository pattern
 * for cleaner, more concise data access code.</p>
 *
 * <p>All write operations are marked with @Transactional to ensure
 * proper transaction management, especially important for event-driven updates.</p>
 */
@ApplicationScoped
public class AuthorViewRepositoryImpl implements AuthorViewRepository, PanacheRepository<AuthorViewEntity> {

    @Override
    public Optional<AuthorView> findById(UUID userId) {
        return find("userId", userId)
            .firstResultOptional()
            .map(AuthorViewMapper::toDomain);
    }

    @Override
    public List<AuthorView> findAllAuthors() {
        return streamAll()
            .map(AuthorViewMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<AuthorView> findAllByIds(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Use Panache's list() with WHERE IN clause
        // This generates: SELECT * FROM author_view WHERE user_id IN (?, ?, ...)
        return list("userId IN ?1", userIds)
            .stream()
            .map(AuthorViewMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void save(AuthorView authorView) {
        AuthorViewEntity entity = AuthorViewMapper.toEntity(authorView);
        persist(entity);
    }

    @Override
    @Transactional
    public void updateAccessLevel(UUID userId, AccessLevel newAccessLevel) {
        int updated = update(
            "accessLevel = ?1 WHERE userId = ?2",
            newAccessLevel.name(),
            userId
        );

        if (updated == 0) {
            throw new IllegalStateException(
                "Failed to update access level - author not found: " + userId
            );
        }
    }

    @Override
    @Transactional
    public void updateAccountStatus(UUID userId, AccountStatus newStatus) {
        int updated = update(
            "accountStatus = ?1 WHERE userId = ?2",
            newStatus.name(),
            userId
        );

        if (updated == 0) {
            throw new IllegalStateException(
                "Failed to update account status - author not found: " + userId
            );
        }
    }

    @Override
    @Transactional
    public void updateFullName(UUID userId, String fullName) {
        int updated = update(
            "fullName = ?1, updatedAt = ?2 WHERE userId = ?3",
            fullName,
            Instant.now(),
            userId
        );

        if (updated == 0) {
            throw new IllegalStateException(
                "Failed to update full name - author not found: " + userId
            );
        }
    }

    @Override
    @Transactional
    public void deleteById(UUID userId) {
        long deleted = delete("userId", userId);

        if (deleted == 0) {
            // Soft failure - user may have already been deleted
            // Log warning but don't throw exception (idempotent operation)
        }
    }

    @Override
    public boolean existsById(UUID userId) {
        return find("userId", userId).count() > 0;
    }
}
