package org.note.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.infrastructure.entity.NoteEntity;
import org.note.infrastructure.mapper.NoteMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA/Panache implementation of NoteRepository.
 */
@ApplicationScoped
public class NoteRepositoryImpl implements NoteRepository, PanacheRepository<NoteEntity> {

    @Override
    public Optional<Note> findById(NoteId id) {
        return find("noteId", id.id())
                .firstResultOptional()
                .map(NoteMapper::toDomain);
    }

    @Override
    public List<Note> findByAuthorId(AuthorId authorId) {
        return find("authorId", authorId.id()).stream()
                .map(NoteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByCategory(NoteCategory category) {
        return find("category", category.name()).stream()
                .map(NoteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByVisibility(NoteVisibility visibility) {
        return find("visibility", visibility.name()).stream()
                .map(NoteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByProjectId(ProjectId projectId) {
        return find("projectId", projectId.id()).stream()
                .map(NoteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByTagId(TagId tagId) {
        // Use JSONB contains operator for PostgreSQL
        return getEntityManager()
                .createQuery("SELECT n FROM NoteEntity n WHERE ?1 MEMBER OF n.tagIds", NoteEntity.class)
                .setParameter(1, tagId.id())
                .getResultList()
                .stream()
                .map(NoteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByStatus(NoteStatus status) {
        return find("status", status.name()).stream()
                .map(NoteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findAllActive() {
        return find("status", NoteStatus.ACTIVE.name()).stream()
                .map(NoteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Note note) {
        NoteEntity entity = NoteMapper.toEntity(note);
        if (!existsById(note.getId())) {
            persist(entity);
        } else {
            getEntityManager().merge(entity);
        }
    }

    @Override
    public void update(Note note) {
        NoteEntity entity = NoteMapper.toEntity(note);
        getEntityManager().merge(entity);
    }

    @Override
    public boolean existsById(NoteId id) {
        return count("noteId", id.id()) > 0;
    }

    @Override
    public void deleteById(NoteId id) {
        delete("noteId", id.id());
    }
}
