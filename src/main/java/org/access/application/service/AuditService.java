package org.access.application.service;

import io.quarkus.logging.Log;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.access.domain.model.AuditLog;
import org.access.domain.model.SecurityContext;
import org.access.domain.repository.AuditLogRepository;

/**
 * Application service responsible for recording audit log entries.
 *
 * This service captures security-sensitive operations with full context
 * including actor information, target details, and HTTP metadata.
 */
@ApplicationScoped
public class AuditService {

    @Inject
    AuditLogRepository auditLogRepository;

    @Inject
    RoutingContext routingContext;

    /**
     * Records an audit log entry for a security-sensitive operation.
     *
     * @param securityContext the security context of the actor
     * @param action the action being performed (e.g., "CREATE_USER", "ASSIGN_ROLE")
     * @param targetType the type of resource being affected (e.g., "USER", "ROLE")
     * @param targetId the ID of the resource being affected (nullable)
     * @param oldValue JSON snapshot of the resource before modification (nullable)
     * @param newValue JSON snapshot of the resource after modification (nullable)
     */
    public void record(SecurityContext securityContext, String action, String targetType,
                       String targetId, String oldValue, String newValue) {
        String ipAddress = extractIpAddress();
        String userAgent = extractUserAgent();

        AuditLog auditLog = AuditLog.create(
            securityContext.actorId(),
            securityContext.actorEmail(),
            securityContext.actorTier(),
            action,
            targetType,
            targetId,
            oldValue,
            newValue,
            ipAddress,
            userAgent
        );

        auditLogRepository.append(auditLog);

        Log.infof("Audit: %s on %s[%s] by %s from %s",
            action, targetType, targetId, securityContext.actorEmail(), ipAddress);
    }

    /**
     * Records an audit log entry without old/new values.
     */
    public void record(SecurityContext securityContext, String action, String targetType, String targetId) {
        record(securityContext, action, targetType, targetId, null, null);
    }

    /**
     * Records an audit log entry without target ID or values.
     */
    public void record(SecurityContext securityContext, String action, String targetType) {
        record(securityContext, action, targetType, null, null, null);
    }

    /**
     * Extracts the client IP address from the HTTP request.
     * Handles X-Forwarded-For header for proxied requests.
     */
    private String extractIpAddress() {
        try {
            String forwardedFor = routingContext.request().getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }
            return routingContext.request().remoteAddress().host();
        } catch (Exception e) {
            Log.warnf("Failed to extract IP address: %s", e.getMessage());
            return "unknown";
        }
    }

    /**
     * Extracts the User-Agent header from the HTTP request.
     */
    private String extractUserAgent() {
        try {
            String userAgent = routingContext.request().getHeader("User-Agent");
            return userAgent != null ? userAgent : "unknown";
        } catch (Exception e) {
            Log.warnf("Failed to extract User-Agent: %s", e.getMessage());
            return "unknown";
        }
    }
}
