package org.note.application.usecase.query;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.query.TagDto;
import org.note.application.mapper.TagMapper;
import org.note.domain.model.TagId;
import org.note.domain.model.TagName;
import org.note.domain.repository.TagRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Query service for retrieving tags.
 * All methods are transactional with read-only semantics.
 */
@ApplicationScoped
public class TagQueryService {

    @Inject
    TagRepository tagRepository;

    /**
     * Finds a tag by its ID.
     *
     * @param tagId the tag ID
     * @return the tag DTO if found, empty otherwise
     */
    @Transactional
    public Optional<TagDto> findById(UUID tagId) {
        return tagRepository.findById(new TagId(tagId))
                .map(TagMapper::toDto);
    }

    /**
     * Finds a tag by its name.
     *
     * @param name the tag name
     * @return the tag DTO if found, empty otherwise
     */
    @Transactional
    public Optional<TagDto> findByName(String name) {
        return tagRepository.findByName(new TagName(name))
                .map(TagMapper::toDto);
    }

    /**
     * Finds all tags.
     *
     * @return list of all tag DTOs
     */
    @Transactional
    public List<TagDto> findAllTags() {
        return tagRepository.findAllTags()
                .stream()
                .map(TagMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds all unused tags (usageCount = 0).
     *
     * @return list of unused tag DTOs
     */
    @Transactional
    public List<TagDto> findUnusedTags() {
        return tagRepository.findUnusedTags()
                .stream()
                .map(TagMapper::toDto)
                .collect(Collectors.toList());
    }
}
