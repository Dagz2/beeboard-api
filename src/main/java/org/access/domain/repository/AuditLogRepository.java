package org.access.domain.repository;

import org.access.domain.model.AuditLog;
import org.access.domain.model.AuditLogId;
import org.access.domain.model.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for audit log persistence.
 *
 * This repository only supports append operations. Update and delete
 * operations are intentionally not supported to maintain audit integrity.
 */
public interface AuditLogRepository {

    /**
     * Appends a new audit log entry.
     * This is the only write operation supported.
     *
     * @param auditLog the audit log entry to persist
     */
    void append(AuditLog auditLog);

    /**
     * Finds an audit log entry by its ID.
     *
     * @param id the audit log ID
     * @return the audit log entry if found
     */
    Optional<AuditLog> findById(AuditLogId id);

    /**
     * Finds all audit log entries for a specific actor.
     *
     * @param actorId the actor's user ID
     * @return list of audit logs for the actor
     */
    List<AuditLog> findByActorId(UserId actorId);

    /**
     * Finds all audit log entries for a specific action.
     *
     * @param action the action type (e.g., "CREATE_USER", "ASSIGN_ROLE")
     * @return list of audit logs for the action
     */
    List<AuditLog> findByAction(String action);

    /**
     * Finds all audit log entries within a time range.
     *
     * @param from start of the time range (inclusive)
     * @param to   end of the time range (inclusive)
     * @return list of audit logs within the range
     */
    List<AuditLog> findByTimeRange(Instant from, Instant to);

    /**
     * Finds all audit log entries for a specific target.
     *
     * @param targetType the type of target (e.g., "USER", "ROLE", "PERMISSION")
     * @param targetId   the target's ID
     * @return list of audit logs for the target
     */
    List<AuditLog> findByTarget(String targetType, String targetId);

    /**
     * Finds all audit log entries with pagination.
     *
     * @param offset the number of entries to skip
     * @param limit  the maximum number of entries to return
     * @return list of audit logs
     */
    List<AuditLog> findAll(int offset, int limit);

    /**
     * Counts the total number of audit log entries.
     *
     * @return the total count
     */
    long count();
}
