package org.note.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.note.domain.model.Tag;
import org.note.domain.model.TagId;
import org.note.domain.model.TagName;
import org.note.domain.repository.TagRepository;
import org.note.infrastructure.entity.TagEntity;
import org.note.infrastructure.mapper.TagMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA/Panache implementation of TagRepository.
 */
@ApplicationScoped
public class TagRepositoryImpl implements TagRepository, PanacheRepository<TagEntity> {

    @Override
    public Optional<Tag> findById(TagId id) {
        return find("tagId", id.id())
                .firstResultOptional()
                .map(TagMapper::toDomain);
    }

    @Override
    public Optional<Tag> findByName(TagName name) {
        return find("name", name.value())
                .firstResultOptional()
                .map(TagMapper::toDomain);
    }

    @Override
    public List<Tag> findAllTags() {
        return findAll().stream()
                .map(TagMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Tag> findUnusedTags() {
        return find("usageCount", 0).stream()
                .map(TagMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Tag tag) {
        TagEntity entity = TagMapper.toEntity(tag);
        if (!existsById(tag.getId())) {
            persist(entity);
        } else {
            getEntityManager().merge(entity);
        }
    }

    @Override
    public void update(Tag tag) {
        TagEntity entity = TagMapper.toEntity(tag);
        getEntityManager().merge(entity);
    }

    @Override
    public boolean existsById(TagId id) {
        return count("tagId", id.id()) > 0;
    }

    @Override
    public boolean existsByName(TagName name) {
        return count("name", name.value()) > 0;
    }

    @Override
    public void deleteById(TagId id) {
        delete("tagId", id.id());
    }
}
