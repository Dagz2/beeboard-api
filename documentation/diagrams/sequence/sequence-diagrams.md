# Sequence Diagrams - BeeBoard API

## 9. Sequence Diagram - Assign New Role

### 9.1 Nominal Scenario

```mermaid
sequenceDiagram
    participant C as Client<br/>(HTTP)
    participant AR as AccessResource<br/>(REST - Exposition)
    participant PI as PermissionInterceptor<br/>(Security)
    participant SCS as SecurityContextService<br/>(Application)
    participant UC as AssignNewRole<br/>(Use Case - Application)
    participant PG as PrivilegeEscalationGuard<br/>(Domain Service)
    participant RR as RoleRepository<br/>(Infrastructure)
    participant UR as UserRepository<br/>(Infrastructure)
    participant U as User<br/>(Aggregate - Domain)
    participant AS as AuditService<br/>(Application)
    participant EP as EventPublisher<br/>(Infrastructure)
    participant DB as Database<br/>(PostgreSQL)

    Note over C,DB: NOMINAL SCENARIO: DIRECTOR assigns HR_MANAGER role (TIER_1 → TIER_2)

    C->>+AR: PUT /api/v1/access/users/{userId}/role<br/>{roleId: "hr-manager-id"}<br/>Header: Authorization: Bearer <JWT>
    AR->>+PI: @RequiresPermission interceptor triggered
    PI->>+SCS: getCurrentContext()
    Note over SCS: Extract userId from JWT subject claim
    SCS->>SCS: loadContext(userId)<br/>[Caffeine Cache - 15min TTL]
    SCS->>+DB: SELECT user, role, permissions
    DB-->>-SCS: User + Role (DIRECTOR, TIER_1) + Permissions
    SCS-->>-PI: SecurityContext(actorId, TIER_1, permissions)

    Note over PI: Validate "ASSIGN_STANDARD_ROLES" permission
    PI->>PI: Check permission in context.permissions
    Note over PI: Permission found - Access granted
    PI->>+UC: execute(command, securityContext)

    Note over UC: Step 1: Validate not self-modification
    UC->>+PG: validateNotSelfModification(actorId, targetUserId)
    PG->>PG: Check actorId != targetUserId
    PG-->>-UC: ✓ Validation passed

    Note over UC: Step 2: Load target role to check tier
    UC->>+RR: findById(newRoleId)
    RR->>+DB: SELECT * FROM roles WHERE id = ?
    DB-->>-RR: Role entity (HR_MANAGER, TIER_2)
    RR-->>-UC: Role(HR_MANAGER, TIER_2)

    Note over UC: Step 3: Validate tier hierarchy
    UC->>+PG: validateRoleAssignment(TIER_1, targetRole)
    PG->>PG: Check TIER_1.canManage(TIER_2)<br/>1 < 2 ? YES ✓
    PG-->>-UC: ✓ Validation passed

    Note over UC: Step 4: Load target user
    UC->>+UR: findById(targetUserId)
    UR->>+DB: SELECT * FROM users WHERE id = ?
    DB-->>-UR: User entity (current role: MEMBER)
    UR-->>-UC: User aggregate

    Note over UC: Step 5: Load old role for audit/event
    UC->>+RR: findById(user.oldRoleId)
    RR->>+DB: SELECT * FROM roles WHERE id = ?
    DB-->>-RR: Role entity (MEMBER, TIER_2)
    RR-->>-UC: Role(MEMBER, TIER_2)

    Note over UC: Step 6: Perform role assignment (domain logic)
    UC->>+U: assignRole(newRoleId)
    U->>U: this.roleId = newRoleId<br/>(Aggregate encapsulation)
    U-->>-UC: Role assigned

    Note over UC: Step 7: Persist changes
    UC->>+UR: update(user)
    UR->>+DB: UPDATE users SET role_id = ? WHERE id = ?
    DB-->>-UR: 1 row updated
    UR-->>-UC: Success

    Note over UC: Step 8: Record audit log
    UC->>+AS: record(ctx, "ASSIGN_ROLE", "USER", userId,<br/>oldValue, newValue)
    AS->>AS: Extract IP address + User-Agent from RoutingContext
    AS->>+DB: INSERT INTO audit_log (actor, action, target, old, new, ip, timestamp)
    DB-->>-AS: Audit entry created
    AS-->>-UC: Audit recorded

    Note over UC: Step 9: Invalidate security context cache
    UC->>+SCS: invalidate(userId)
    Note over SCS: @CacheInvalidate removes Caffeine entry
    SCS-->>-UC: Cache invalidated

    Note over UC: Step 10: Publish domain event asynchronously
    UC->>+EP: publishAsync(UserRoleChangedEvent)
    Note over EP: CDI @ObservesAsync - Non-blocking
    EP-->>-UC: Event queued

    UC-->>-PI: Success
    PI-->>-AR: Proceed to response
    AR-->>-C: 200 OK

    Note over EP: Asynchronous event processing
    EP->>EP: Notify Note BC, invalidate other caches, etc.
```

### 9.2 Alternative Scenario - Privilege Escalation Rejected

```mermaid
sequenceDiagram
    participant C as Client<br/>(HTTP)
    participant AR as AccessResource<br/>(REST - Exposition)
    participant PI as PermissionInterceptor<br/>(Security)
    participant SCS as SecurityContextService<br/>(Application)
    participant UC as AssignNewRole<br/>(Use Case - Application)
    participant PG as PrivilegeEscalationGuard<br/>(Domain Service)
    participant RR as RoleRepository<br/>(Infrastructure)
    participant DB as Database<br/>(PostgreSQL)

    Note over C,DB: ALTERNATIVE SCENARIO: HR_MANAGER attempts to assign DIRECTOR role (TIER_2 → TIER_1)

    C->>+AR: PUT /api/v1/access/users/{userId}/role<br/>{roleId: "director-id"}<br/>Header: Authorization: Bearer <JWT>
    AR->>+PI: @RequiresPermission interceptor triggered
    PI->>+SCS: getCurrentContext()
    SCS->>SCS: loadContext(userId) [Cached]
    SCS->>+DB: SELECT user, role, permissions
    DB-->>-SCS: User + Role (HR_MANAGER, TIER_2) + Permissions
    SCS-->>-PI: SecurityContext(actorId, TIER_2, permissions)

    Note over PI: Validate "ASSIGN_BASIC_ROLES" permission
    PI->>PI: Check permission in context.permissions
    Note over PI: Permission found - Access granted<br/>(Permission check passes, tier check happens later)
    PI->>+UC: execute(command, securityContext)

    Note over UC: Step 1: Validate not self-modification
    UC->>+PG: validateNotSelfModification(actorId, targetUserId)
    PG->>PG: Check actorId != targetUserId
    PG-->>-UC: ✓ Validation passed

    Note over UC: Step 2: Load target role to check tier
    UC->>+RR: findById(newRoleId)
    RR->>+DB: SELECT * FROM roles WHERE id = ?
    DB-->>-RR: Role entity (DIRECTOR, TIER_1)
    RR-->>-UC: Role(DIRECTOR, TIER_1)

    Note over UC: Step 3: Validate tier hierarchy - FAILS HERE
    UC->>+PG: validateRoleAssignment(TIER_2, targetRole)
    PG->>PG: Check TIER_2.canManage(TIER_1)<br/>2 < 1 ? NO ✗

    rect rgb(255, 200, 200)
        Note over PG: ⚠️ PRIVILEGE ESCALATION DETECTED<br/>TIER_2 cannot manage TIER_1
        PG->>PG: throw PrivilegeEscalationException<br/>("Cannot assign role with tier TIER_1.<br/>Actor tier TIER_2 insufficient.")
    end

    PG-->>-UC: Exception: PrivilegeEscalationException
    UC-->>-PI: Exception propagated
    PI-->>-AR: Exception propagated

    Note over AR: Exception mapper converts<br/>PrivilegeEscalationException → HTTP 403
    AR->>AR: Map exception to Response
    AR-->>-C: 403 Forbidden<br/>{"error": "Cannot assign role with tier TIER_1.<br/>Actor tier TIER_2 insufficient."}

    Note over C,DB: ❌ Operation rejected - No database changes - No audit log
```

## 10. Sequence Diagram - Lock User

### 10.1 Nominal Scenario

```mermaid
sequenceDiagram
    participant C as Client<br/>(HTTP)
    participant AR as AccessResource<br/>(REST - Exposition)
    participant PI as PermissionInterceptor<br/>(Security)
    participant SCS as SecurityContextService<br/>(Application)
    participant UC as LockUser<br/>(Use Case - Application)
    participant PG as PrivilegeEscalationGuard<br/>(Domain Service)
    participant UR as UserRepository<br/>(Infrastructure)
    participant RR as RoleRepository<br/>(Infrastructure)
    participant U as User<br/>(Aggregate - Domain)
    participant AS as AuditService<br/>(Application)
    participant EP as EventPublisher<br/>(Infrastructure)
    participant DB as Database<br/>(PostgreSQL)

    Note over C,DB: NOMINAL SCENARIO: DIRECTOR locks a TIER_2 user account

    C->>+AR: PUT /api/v1/access/users/{userId}/lock<br/>{userId: "target-user-id"}<br/>Header: Authorization: Bearer <JWT>
    AR->>+PI: @RequiresPermission("USER_LOCK") interceptor
    PI->>+SCS: getCurrentContext()
    SCS->>SCS: loadContext(userId) [Cached]
    SCS->>+DB: SELECT user, role, permissions
    DB-->>-SCS: User + Role (DIRECTOR, TIER_1) + Permissions
    SCS-->>-PI: SecurityContext(actorId, TIER_1, permissions)

    Note over PI: Validate "USER_LOCK" permission
    PI->>PI: Check permission in context.permissions
    Note over PI: Permission found - Access granted
    PI->>+UC: execute(command, securityContext)

    Note over UC: Step 1: Validate not self-locking
    UC->>+PG: validateNotSelfModification(actorId, targetUserId)
    PG->>PG: Check actorId != targetUserId
    PG-->>-UC: ✓ Validation passed

    Note over UC: Step 2: Load target user
    UC->>+UR: findById(targetUserId)
    UR->>+DB: SELECT * FROM users WHERE id = ?
    DB-->>-UR: User entity (isLocked=false)
    UR-->>-UC: User aggregate

    Note over UC: Step 3: Load user's role to check tier
    UC->>+RR: findById(user.roleId)
    RR->>+DB: SELECT * FROM roles WHERE id = ?
    DB-->>-RR: Role entity (HR_MANAGER, TIER_2)
    RR-->>-UC: Role(HR_MANAGER, TIER_2)

    Note over UC: Step 4: Validate tier hierarchy
    UC->>+PG: validateUserManagement(TIER_1, TIER_2)
    PG->>PG: Check TIER_1.canManage(TIER_2)<br/>1 < 2 ? YES ✓
    PG-->>-UC: ✓ Validation passed

    Note over UC: Step 5: Lock the user (domain logic)
    UC->>+U: lock()
    U->>U: this.isLocked = true<br/>this.lockReason = Optional.empty()<br/>(Aggregate encapsulation)
    U-->>-UC: User locked

    Note over UC: Step 6: Persist changes
    UC->>+UR: update(user)
    UR->>+DB: UPDATE users SET is_locked = true WHERE id = ?
    DB-->>-UR: 1 row updated
    UR-->>-UC: Success

    Note over UC: Step 7: Record audit log
    UC->>+AS: record(ctx, "LOCK_USER", "USER", userId,<br/>oldValue: {isLocked: false},<br/>newValue: {isLocked: true})
    AS->>AS: Extract IP address + User-Agent
    AS->>+DB: INSERT INTO audit_log (actor, action, target, old, new, ip, timestamp)
    DB-->>-AS: Audit entry created
    AS-->>-UC: Audit recorded

    Note over UC: Step 8: Publish domain event asynchronously
    UC->>+EP: publishAsync(UserLockedEvent)
    Note over EP: CDI @ObservesAsync - Non-blocking
    EP-->>-UC: Event queued

    UC-->>-PI: Success
    PI-->>-AR: Proceed to response
    AR-->>-C: 200 OK

    Note over EP: Asynchronous event processing
    EP->>EP: Notify Note BC (lock user notes), trigger alerts, etc.
```

### 10.2 Alternative Scenario - Self-Lock Forbidden

```mermaid
sequenceDiagram
    participant C as Client<br/>(HTTP)
    participant AR as AccessResource<br/>(REST - Exposition)
    participant PI as PermissionInterceptor<br/>(Security)
    participant SCS as SecurityContextService<br/>(Application)
    participant UC as LockUser<br/>(Use Case - Application)
    participant PG as PrivilegeEscalationGuard<br/>(Domain Service)

    Note over C,PG: ALTERNATIVE SCENARIO: HR_MANAGER attempts to lock own account (self-modification)

    C->>+AR: PUT /api/v1/access/users/{userId}/lock<br/>{userId: "actor-user-id"}<br/>(userId = actorId from JWT)<br/>Header: Authorization: Bearer <JWT>
    AR->>+PI: @RequiresPermission("USER_LOCK") interceptor
    PI->>+SCS: getCurrentContext()
    SCS->>SCS: loadContext(userId) [Cached]
    Note over SCS: userId from JWT = "actor-user-id"
    SCS-->>-PI: SecurityContext(actorId="actor-user-id", TIER_2, permissions)

    Note over PI: Validate "USER_LOCK" permission
    PI->>PI: Check permission in context.permissions
    Note over PI: Permission found - Access granted<br/>(Self-modification check happens later in domain layer)
    PI->>+UC: execute(command, securityContext)

    Note over UC: Step 1: Validate not self-locking - FAILS HERE
    UC->>+PG: validateNotSelfModification(actorId, targetUserId)
    PG->>PG: Check actorId != targetUserId<br/>"actor-user-id" != "actor-user-id" ? NO ✗

    rect rgb(255, 200, 200)
        Note over PG: ⚠️ SELF-MODIFICATION DETECTED<br/>User attempting to lock own account
        PG->>PG: throw SelfModificationForbiddenException<br/>("Users cannot modify their own role<br/>or account status")
    end

    PG-->>-UC: Exception: SelfModificationForbiddenException
    UC-->>-PI: Exception propagated
    PI-->>-AR: Exception propagated

    Note over AR: Exception mapper converts<br/>SelfModificationForbiddenException → HTTP 403
    AR->>AR: Map exception to Response
    AR-->>-C: 403 Forbidden<br/>{"error": "Self-modification forbidden.<br/>Users cannot modify their own role or account status."}

    Note over C,PG: ❌ Operation rejected immediately - No database access - No audit log
```
