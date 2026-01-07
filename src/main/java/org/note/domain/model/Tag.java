package org.note.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root representing a Tag for classifying and organizing Notes.
 *
 * <p><strong>DDD Pattern:</strong> This class follows the Aggregate Root pattern from
 * Domain-Driven Design. Tags are standalone aggregates with their own identity and lifecycle,
 * independent of Notes. They are referenced by Notes through TagIds, not embedded within them.
 *
 * <p><strong>Design Philosophy:</strong> Tags are simplified flexible keywords without
 * predefined categories, allowing organic vocabulary growth driven by user needs.
 *
 * <p><strong>Invariants:</strong>
 * <ul>
 *   <li>A Tag MUST have a unique TagName (case-insensitive, enforced at repository level)</li>
 *   <li>Tags are shared across all notes (global vocabulary, not project-specific)</li>
 *   <li>Tag names MUST be lowercase, alphanumeric with hyphens only</li>
 *   <li>Usage count MUST accurately reflect the number of notes using this tag</li>
 * </ul>
 *
 * <p><strong>Lifecycle:</strong> Tags are created when first used and can be deleted only
 * when their usage count reaches zero (no notes reference them).
 *
 * <p><strong>Aggregate Boundary:</strong> Tag is a simple aggregate containing only
 * value objects (TagId, TagName, AuditInfo), with no child entities.
 */
public class Tag {

    private final TagId id;
    private TagName name;
    private int usageCount;
    private final AuditInfo auditInfo;

    // -------------------- Creation --------------------

    private Tag(TagId id, TagName name, int usageCount, AuditInfo auditInfo) {
        this.id = Objects.requireNonNull(id, "Tag ID is required");
        this.name = Objects.requireNonNull(name, "Tag name is required");
        this.usageCount = usageCount;
        this.auditInfo = Objects.requireNonNull(auditInfo, "Audit info is required");
    }

    /**
     * Factory method to create a new Tag aggregate.
     *
     * <p>This is the primary way to create new tags in the system. The tag is
     * created with a newly generated ID, zero usage count (no notes reference it yet),
     * and current timestamp for audit information.
     *
     * <p>Tags are typically created either:
     * <ul>
     *   <li>Explicitly by users through the tag management interface</li>
     *   <li>Implicitly when adding a non-existent tag name to a note</li>
     * </ul>
     *
     * @param name the unique tag name (must not be null, validated for format)
     * @return a new Tag instance with zero usage count
     * @throws NullPointerException if name is null
     */
    public static Tag create(TagName name) {
        return new Tag(
                TagId.newId(),
                name,
                0,
                AuditInfo.of(Instant.now())
        );
    }

    /**
     * Factory method to reconstitute a Tag from persistence.
     *
     * <p>This method is used by the infrastructure layer to rebuild Tag aggregates
     * from database records. Unlike {@link #create(TagName)}, this preserves the
     * existing ID, usage count, and audit information.
     *
     * @param id the existing tag identifier (must not be null)
     * @param name the tag name (must not be null)
     * @param usageCount the number of notes currently using this tag (must be >= 0)
     * @param auditInfo the audit information with creation/modification timestamps (must not be null)
     * @return a reconstituted Tag instance with all historical data preserved
     * @throws NullPointerException if any parameter is null
     */
    public static Tag reconstitute(TagId id, TagName name, int usageCount, AuditInfo auditInfo) {
        return new Tag(id, name, usageCount, auditInfo);
    }

    // -------------------- Domain methods --------------------

    /**
     * Increments the usage count when this tag is added to a note.
     *
     * <p>This method is called by the Note aggregate when a tag is added through
     * {@link Note#addTag(TagId)}. The usage count tracks how many notes currently
     * reference this tag, which determines whether the tag can be safely deleted.
     *
     * <p>Side effects:
     * <ul>
     *   <li>Increments usageCount by 1</li>
     *   <li>Updates audit timestamp (lastModifiedAt)</li>
     * </ul>
     */
    public void incrementUsage() {
        this.usageCount++;
        this.auditInfo.touch();
    }

    /**
     * Decrements the usage count when this tag is removed from a note.
     *
     * <p>This method is called by the Note aggregate when a tag is removed through
     * {@link Note#removeTag(TagId)}. The usage count cannot go below zero.
     *
     * <p>When the usage count reaches zero, the tag becomes eligible for deletion
     * (checked via {@link #isUnused()}).
     *
     * <p>Side effects:
     * <ul>
     *   <li>Decrements usageCount by 1 (if currently > 0)</li>
     *   <li>Updates audit timestamp (lastModifiedAt)</li>
     * </ul>
     */
    public void decrementUsage() {
        if (this.usageCount > 0) {
            this.usageCount--;
            this.auditInfo.touch();
        }
    }

    /**
     * Renames the tag to a new name.
     *
     * <p>This operation updates the tag name while preserving its identity and usage count.
     * The new name must be unique across all tags (enforced at the application layer
     * before calling this method).
     *
     * <p>Use case: Correcting typos or standardizing tag vocabulary.
     *
     * @param newName the new tag name (must not be null, validated for format)
     * @throws NullPointerException if newName is null
     */
    public void rename(TagName newName) {
        this.name = Objects.requireNonNull(newName, "Tag name cannot be null");
        this.auditInfo.touch();
    }

    // -------------------- Query methods --------------------

    /**
     * Checks if this tag is currently unused (not referenced by any notes).
     *
     * <p>Unused tags are eligible for deletion. This prevents accumulation of
     * orphaned tags in the system while ensuring tags with active references
     * cannot be accidentally deleted.
     *
     * @return true if usage count is zero (no notes reference this tag), false otherwise
     */
    public boolean isUnused() {
        return usageCount == 0;
    }

    // -------------------- Getters --------------------

    public TagId getId() {
        return id;
    }

    public TagName getName() {
        return name;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    // -------------------- Overrides --------------------

    /**
     * Two Tag instances are considered equal if they share the same business identifier (TagId).
     *
     * <p>This implements the DDD Entity identity pattern where entity equality is based solely
     * on the unique identifier, not on mutable attributes like name or usage count. This ensures
     * that a Tag remains the "same" tag even as its name is changed through {@link #rename(TagName)}
     * or its usage count changes.
     *
     * @param o the object to compare with
     * @return true if both objects are the same instance or have equal TagIds, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag tag)) return false;
        return id.equals(tag.id);
    }

    /**
     * Returns a hash code based solely on the TagId.
     *
     * <p>This is consistent with {@link #equals(Object)} and ensures Tags can be used
     * in hash-based collections while maintaining correct equality semantics.
     *
     * @return hash code based on the tag identifier
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this Tag for debugging purposes.
     *
     * <p>Format: {@code Tag{id=..., name=..., usageCount=...}}
     *
     * @return a string representation containing id, name, and usage count
     */
    @Override
    public String toString() {
        return "Tag{id=" + id + ", name=" + name + ", usageCount=" + usageCount + "}";
    }
}
