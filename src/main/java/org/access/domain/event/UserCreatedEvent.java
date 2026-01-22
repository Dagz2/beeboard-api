package org.access.domain.event;

import org.shared.domain.AccountStatus;
import org.shared.domain.ApplicationRole;
import org.shared.event.DomainEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event published when a new user is created in Access BC.
 *
 * <p>This event is consumed by Note BC to synchronize the author_view CQRS read model.</p>
 *
 * <p>Published by: {@link org.access.application.usecase.command.user.CreateUser}</p>
 * <p>Consumed by: {@link org.note.application.event.UserEventHandler}</p>
 */
public record UserCreatedEvent(
    UUID eventId,
    UUID userId,
    String fullName,
    ApplicationRole applicationRole,
    AccountStatus accountStatus,
    UUID createdBy,
    Instant occurredAt
) implements DomainEvent {

    public UserCreatedEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(fullName, "fullName is required");
        Objects.requireNonNull(applicationRole, "applicationRole is required");
        Objects.requireNonNull(accountStatus, "accountStatus is required");
        Objects.requireNonNull(createdBy, "createdBy is required");

        if (fullName.isBlank()) {
            throw new IllegalArgumentException("fullName cannot be blank");
        }
    }

    @Override
    public String eventType() {
        return "UserCreated";
    }

    @Override
    public UUID aggregateId() {
        return userId;
    }
}
