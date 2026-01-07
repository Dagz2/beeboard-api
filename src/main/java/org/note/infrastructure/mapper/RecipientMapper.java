package org.note.infrastructure.mapper;

import org.note.domain.model.AuthorId;
import org.note.domain.model.Recipient;
import org.note.infrastructure.entity.RecipientEmbeddable;

import java.util.Objects;

/**
 * Mapper for converting between Recipient domain objects and RecipientEmbeddable.
 */
public class RecipientMapper {

    private RecipientMapper() {
        // Utility class
    }

    /**
     * Converts a RecipientEmbeddable to a Recipient domain object.
     */
    public static Recipient toDomain(RecipientEmbeddable embeddable) {
        Objects.requireNonNull(embeddable, "RecipientEmbeddable cannot be null");

        return new Recipient(
                new AuthorId(embeddable.getUserId()),
                embeddable.getSharedAt()
        );
    }

    /**
     * Converts a Recipient domain object to a RecipientEmbeddable.
     */
    public static RecipientEmbeddable toEmbeddable(Recipient recipient) {
        Objects.requireNonNull(recipient, "Recipient cannot be null");

        return new RecipientEmbeddable(
                recipient.userId().id(),
                recipient.sharedAt()
        );
    }
}
