package org.note.infrastructure.mapper;

import org.note.domain.model.AccessLevel;
import org.note.domain.model.AuthorView;
import org.note.infrastructure.entity.AuthorViewEntity;
import org.shared.domain.AccountStatus;

/**
 * Mapper for converting between AuthorView domain model and AuthorViewEntity JPA entity.
 *
 * <p>This mapper handles the translation between:</p>
 * <ul>
 *   <li>Domain enums (AccessLevel, AccountStatus) ↔ String database columns</li>
 *   <li>Immutable domain records ↔ Mutable JPA entities</li>
 * </ul>
 *
 * <p>Note: This is a utility class with static methods, following the pattern
 * used by other mappers in the codebase (NoteMapper, CommentMapper, RoleMapper).</p>
 */
public final class AuthorViewMapper {

    /**
     * Private constructor to prevent instantiation.
     */
    private AuthorViewMapper() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    /**
     * Converts a JPA entity to a domain model.
     *
     * <p>This method is used when reading data from the database.</p>
     *
     * @param entity the JPA entity to convert
     * @return the corresponding domain model
     * @throws IllegalArgumentException if entity is null or contains invalid data
     */
    public static AuthorView toDomain(AuthorViewEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("AuthorViewEntity cannot be null");
        }

        return new AuthorView(
            entity.getUserId(),
            AccessLevel.valueOf(entity.getAccessLevel()),
            AccountStatus.valueOf(entity.getAccountStatus()),
            entity.getFullName()
        );
    }

    /**
     * Converts a domain model to a JPA entity.
     *
     * <p>This method is used when persisting data to the database.</p>
     *
     * @param domain the domain model to convert
     * @return the corresponding JPA entity
     * @throws IllegalArgumentException if domain is null
     */
    public static AuthorViewEntity toEntity(AuthorView domain) {
        if (domain == null) {
            throw new IllegalArgumentException("AuthorView cannot be null");
        }

        AuthorViewEntity entity = new AuthorViewEntity();
        entity.setUserId(domain.userId());
        entity.setAccessLevel(domain.accessLevel().name());
        entity.setAccountStatus(domain.accountStatus().name());
        entity.setFullName(domain.fullName());

        return entity;
    }
}
