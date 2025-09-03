package org.access.infrastructure.mapper;

import org.access.domain.model.*;
import org.access.infrastructure.entity.UserEntity;
import org.access.infrastructure.entity.RoleEntity;
import org.access.infrastructure.entity.AuditInfoEmbeddable;

import java.util.Objects;

/**
 * Mapper responsible for converting between {@link UserEntity} (infrastructure)
 * and {@link User} (domain model).
 *
 * <p>This class ensures isolation between the domain layer and the persistence layer,
 * handling the conversion between technical representations (JPA) and business logic objects.</p>
 */
public class UserMapper {

    /**
     * Converts a JPA {@link UserEntity} to a domain {@link User} aggregate.
     *
     * @param entity the UserEntity from the persistence layer
     * @return the domain User object
     * @throws NullPointerException if the input entity is null
     */
    public static User toDomain(UserEntity entity) {
        Objects.requireNonNull(entity, "UserEntity cannot be null");

        return new User(
            new UserId(entity.getUserId()),
            new FullName(entity.getFirstName(), entity.getLastName()),
            new EmailAddress(entity.getEmail()),
            new RoleId(entity.getRole().getRoleId()),
            new ContractEndDate(entity.getContractEndDate()),
            entity.getIsLocked(),
            new AuditInfo(
                entity.getAuditInfo().getCreatedAt(),
                entity.getAuditInfo().getUpdatedAt()
            ),
            entity.getLastActivityDate()
        );
    }

    /**
     * Converts a domain {@link User} to a JPA {@link UserEntity}.
     *
     * @param user the domain user aggregate
     * @return the infrastructure-level UserEntity
     * @throws NullPointerException if the input user is null
     */
    public static UserEntity toEntity(User user) {
        Objects.requireNonNull(user, "User cannot be null");

        UserEntity entity = new UserEntity();
        entity.setUserId(user.getId().id());
        entity.setFirstName(user.getFullName().firstName());
        entity.setLastName(user.getFullName().lastName());
        entity.setEmail(user.getEmail().value());
        entity.setContractEndDate(user.getContractEndDate().date());
        entity.setIsLocked(user.isLocked());
        entity.setLastActivityDate(user.getLastActivityDate());
        // Create a lightweight RoleEntity reference with just the ID
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleId(user.getRoleId().id());
        entity.setRole(roleEntity);

        AuditInfoEmbeddable audit = new AuditInfoEmbeddable();
        audit.setCreatedAt(user.getAuditInfo().getCreatedAt());
        audit.setUpdatedAt(user.getAuditInfo().getUpdatedAt());
        entity.setAuditInfo(audit);

        return entity;
    }
}
