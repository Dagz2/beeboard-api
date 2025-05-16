package org.acme.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Tactical Pattern: Aggregate root representing a system user.
 */

public class User {
    private final UserId id;
    private FullName fullName;
    private EmailAddress email;
    private RoleId roleId;
    private ContractEndDate contractEndDate;
    private boolean locked;
    private final AuditInfo auditInfo;


    private User(UserId id, FullName fullName, EmailAddress email, RoleId roleId, ContractEndDate contractEndDate, boolean locked, AuditInfo auditInfo) {
        this.id = Objects.requireNonNull(id, "UserId cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "FullName cannot be null");
        this.email = Objects.requireNonNull(email, "EmailAddress cannot be null");
        this.roleId = Objects.requireNonNull(roleId, "RoleId cannot be null");
        this.contractEndDate = Objects.requireNonNull(contractEndDate, "ContractEndDate cannot be null");
        this.locked = false;
        this.auditInfo = Objects.requireNonNull(auditInfo, "AuditInfo cannot be null");
    }

    public static User create(UserId id, FullName fullName, EmailAddress email, RoleId roleId, ContractEndDate contractEndDate, boolean locked, AuditInfo auditInfo) {
        return new User(id, fullName, email, roleId, contractEndDate, auditInfo);
    }
}
