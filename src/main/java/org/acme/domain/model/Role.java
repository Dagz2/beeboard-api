package org.acme.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Role {
    private final Integer id;
    private String label;
    private String description;
    private List<Permission> permissions = new ArrayList<>();

    /**
     * Private constructor for Role..
     *
     * @param id unique identifier of the user
     * @param fullName full name value object
     * @param email email address value object
     * @param roleId role identifier value object
     * @param contractEndDate contract end date value object
     * @param auditInfo audit information value object
     * @param lastActivityDate timestamp of last user activity
     */
    private User(UserId id, FullName fullName, EmailAddress email, RoleId roleId, ContractEndDate contractEndDate, AuditInfo auditInfo, Instant lastActivityDate) {
        this.id = Objects.requireNonNull(id, "UserId cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "FullName cannot be null");
        this.email = Objects.requireNonNull(email, "EmailAddress cannot be null");
        this.roleId = Objects.requireNonNull(roleId, "RoleId cannot be null");
        this.contractEndDate = Objects.requireNonNull(contractEndDate, "ContractEndDate cannot be null");
        this.isLocked = false;
        this.auditInfo = Objects.requireNonNull(auditInfo, "AuditInfo cannot be null");
        this.lastActivityDate = Objects.requireNonNull(lastActivityDate, "LastActivityDate cannot be null");
    }
}
