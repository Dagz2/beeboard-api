package org.access.application.mapper;

import org.access.domain.model.User;
import org.access.application.dto.query.UserDto;
import org.access.application.dto.query.AuditInfoDto;

/**
 * Utility class responsible for converting domain User entities to UserDto representations.
 *
 * <p>This mapper bridges the domain and application layers by exposing only the data
 * needed for external consumers (e.g. REST APIs, UI, reporting tools), while preserving
 * domain encapsulation.</p>
 *
 * <p>Only mapping from domain to DTO is supported for now. Mapping in the other direction
 * (DTO → domain) may be added later for write use cases.</p>
 */
public class UserMapper {

    /**
     * Converts a {@link User} domain object to a {@link UserDto} used in the application/query layer.
     *
     * <p>The mapping extracts individual attributes from domain value objects (e.g. {@code FullName}, {@code EmailAddress}),
     * and converts {@code AuditInfo} into a simpler DTO format.</p>
     *
     * @param user the domain User entity to convert (must not be {@code null})
     * @return a fully populated {@link UserDto} representing the user
     * @throws IllegalArgumentException if {@code user} is {@code null}
     */
    public static UserDto toDto(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return new UserDto(
            user.getId().id(),
            user.getFullName().firstName(),
            user.getFullName().lastName(),
            user.getEmail().value(),
            user.getContractEndDate().date(),
            user.isLocked(),
            user.getLastActivityDate(),
            new AuditInfoDto(
                user.getAuditInfo().getCreatedAt(),
                user.getAuditInfo().getUpdatedAt()
            ),
            user.getRoleId().id()
        );
    }
}
