package org.note.domain.repository;

import org.note.domain.model.Tag;
import org.note.domain.model.TagId;
import org.note.domain.model.TagName;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Tag aggregate persistence.
 * Defines the contract for Tag storage and retrieval.
 */
public interface TagRepository {

    // -------------------- Query methods --------------------

    /**
     * Finds a tag by its ID.
     *
     * @param id the tag ID
     * @return the tag if found, empty otherwise
     */
    Optional<Tag> findById(TagId id);

    /**
     * Finds a tag by its name.
     *
     * @param name the tag name
     * @return the tag if found, empty otherwise
     */
    Optional<Tag> findByName(TagName name);

    /**
     * Finds all tags.
     *
     * @return list of all tags
     */
    List<Tag> findAllTags();

    /**
     * Finds all unused tags (usageCount = 0).
     *
     * @return list of unused tags
     */
    List<Tag> findUnusedTags();

    // -------------------- Persistence methods --------------------

    /**
     * Saves a new tag.
     *
     * @param tag the tag to save
     */
    void save(Tag tag);

    /**
     * Updates an existing tag.
     *
     * @param tag the tag to update
     */
    void update(Tag tag);

    // -------------------- Existence methods --------------------

    /**
     * Checks if a tag exists with the given ID.
     *
     * @param id the tag ID
     * @return true if exists, false otherwise
     */
    boolean existsById(TagId id);

    /**
     * Checks if a tag exists with the given name.
     *
     * @param name the tag name
     * @return true if exists, false otherwise
     */
    boolean existsByName(TagName name);

    // -------------------- Deletion methods --------------------

    /**
     * Deletes a tag by its ID.
     * Should only delete if usageCount = 0.
     *
     * @param id the tag ID
     */
    void deleteById(TagId id);
}
