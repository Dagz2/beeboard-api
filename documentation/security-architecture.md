# Security Architecture - Tiered Administration

## 1. Security Layers Overview

```mermaid
flowchart TB
    subgraph Request["HTTP Request"]
        REQ[Client Request]
    end

    subgraph Layer1["LAYER 1: AUTHENTICATION"]
        JWT[JWT Token Validation<br/>SmallRye JWT / MicroProfile]
        USF[UserStatusFilter<br/>• Check if user is locked<br/>• Check if contract expired]
        JWT --> USF
    end

    subgraph Layer2["LAYER 2: AUTHORIZATION"]
        RP["@RequiresPermission"]
        PI[PermissionInterceptor<br/>• Check permissions via cache<br/>• Verify required permission]
        SCC[SecurityContextCache<br/>• Build SecurityContext<br/>• Caffeine cache 15min TTL<br/>• Proactive invalidation]
        RP --> PI --> SCC
    end

    subgraph Layer3["LAYER 3: ANTI-ESCALATION"]
        PEG[PrivilegeEscalationGuard<br/>• validateNotSelfModification<br/>• validateRoleAssignment<br/>• validatePermissionGrant<br/>• validateSystemResource]
    end

    subgraph Layer4["LAYER 4: BUSINESS"]
        UC[Use Cases<br/>CreateUser, AssignNewRole,<br/>GrantPermission, LockUser...]
    end

    subgraph Layer5["LAYER 5: TRACEABILITY"]
        AS[AuditService<br/>• Capture actor, action<br/>• Capture IP, User-Agent<br/>• Snapshot old/new values]
        DB[(PostgreSQL audit_log<br/>TRIGGER: Immutability<br/>• Block UPDATE<br/>• Block DELETE)]
        AS --> DB
    end

    REQ --> Layer1
    Layer1 -->|"✓ Authenticated"| Layer2
    Layer2 -->|"✓ Authorized"| Layer3
    Layer3 -->|"✓ Validated"| Layer4
    Layer4 -->|"✓ Executed"| Layer5

    style Layer1 fill:#e8f5e9,stroke:#4caf50
    style Layer2 fill:#e3f2fd,stroke:#2196f3
    style Layer3 fill:#fff3e0,stroke:#ff9800
    style Layer4 fill:#f3e5f5,stroke:#9c27b0
    style Layer5 fill:#fce4ec,stroke:#e91e63
```

## 2. Tier Hierarchy

```mermaid
flowchart TB
    subgraph TIER0["TIER_0 - Critical"]
        SA[SUPER_ADMIN<br/>Technical administration]
    end

    subgraph TIER1["TIER_1 - Sensitive"]
        DIR[DIRECTOR<br/>Business management]
    end

    subgraph TIER2["TIER_2 - Standard"]
        HR[HR_MANAGER<br/>HR operations]
    end

    SA -->|"can manage"| DIR
    SA -->|"can manage"| HR
    DIR -->|"can manage"| HR

    style TIER0 fill:#ffcdd2,stroke:#c62828,stroke-width:3px
    style TIER1 fill:#ffe0b2,stroke:#ef6c00,stroke-width:2px
    style TIER2 fill:#c8e6c9,stroke:#2e7d32,stroke-width:1px
```

## 3. Sensitive Operation Flow

```mermaid
sequenceDiagram
    autonumber
    participant D as DIRECTOR (TIER_1)
    participant API as PUT /users/{id}/role
    participant JWT as JWT Validation
    participant PI as PermissionInterceptor
    participant PEG as PrivilegeEscalationGuard
    participant UC as AssignNewRole
    participant AS as AuditService
    participant DB as PostgreSQL

    D->>API: Assign HR_MANAGER to user
    API->>JWT: Validate token
    JWT-->>API: ✓ Token valid

    API->>PI: @RequiresPermission("ASSIGN_*_ROLES")
    PI->>PI: Load DIRECTOR permissions
    PI-->>API: ✓ ASSIGN_STANDARD_ROLES present

    API->>PEG: validateNotSelfModification()
    PEG-->>API: ✓ No self-modification

    API->>PEG: validateRoleAssignment(TIER_1, HR_MANAGER)
    PEG->>PEG: TIER_1.canManage(TIER_2)?
    PEG-->>API: ✓ Hierarchy respected

    API->>UC: execute(command, securityContext)
    UC->>DB: UPDATE users SET role_id = ...
    DB-->>UC: ✓ User updated

    UC->>AS: record("ASSIGN_ROLE", ...)
    AS->>DB: INSERT INTO audit_log
    DB-->>AS: ✓ Audit recorded

    UC-->>API: ✓ Success
    API-->>D: 200 OK
```

## 4. Security Rules

```mermaid
flowchart TB
    subgraph Rules[" "]
        direction LR
        R1["🚫 RULE 1: NO SELF-MODIFICATION: A user cannot modify their own role"]
        R2["📊 RULE 2: STRICT HIERARCHY: TIER_N can only manage TIER_N+1 and below"]
        R3["🔒 RULE 3: SYSTEM PROTECTION: Resources with isSystem=true cannot be deleted"]
        R4["📝 RULE 4: IMMUTABLE AUDIT: Logs cannot be modified via PostgreSQL trigger"]
    end

    R1 --> R2
    R2 --> R3
    R3 --> R4

    style R1 fill:#c62828,stroke:#b71c1c,color:#ffffff,stroke-width:2px
    style R2 fill:#ef6c00,stroke:#e65100,color:#ffffff,stroke-width:2px
    style R3 fill:#2e7d32,stroke:#1b5e20,color:#ffffff,stroke-width:2px
    style R4 fill:#1565c0,stroke:#0d47a1,color:#ffffff,stroke-width:2px
    style Rules fill:transparent,stroke:transparent
```

## 5. Permissions Matrix

```mermaid
flowchart TB
    subgraph T0["TIER_0 SUPER_ADMIN"]
        direction TB
        P0_1["MANAGE_PERMISSIONS"]
        P0_2["MANAGE_SYSTEM_ROLES"]
        P0_3["VIEW_AUDIT_LOGS"]
        P0_4["ASSIGN_CRITICAL_ROLES"]
    end

    subgraph T1["TIER_1 DIRECTOR"]
        direction TB
        P1_1["MANAGE_USERS"]
        P1_2["USER_DELETE"]
        P1_3["MANAGE_ROLES"]
        P1_4["ASSIGN_STANDARD_ROLES"]
    end

    subgraph T2["TIER_2 HR_MANAGER"]
        direction TB
        P2_1["USER_CREATE"]
        P2_2["USER_READ"]
        P2_3["USER_UPDATE"]
        P2_4["USER_LOCK"]
        P2_5["ASSIGN_BASIC_ROLES"]
    end

    T0 ==>|"inherits all"| T1
    T1 ==>|"inherits all"| T2

    style T0 fill:#c62828,stroke:#b71c1c,color:#ffffff
    style T1 fill:#ef6c00,stroke:#e65100,color:#ffffff
    style T2 fill:#2e7d32,stroke:#1b5e20,color:#ffffff
    style P0_1 fill:#ffcdd2,stroke:#c62828,color:#333333
    style P0_2 fill:#ffcdd2,stroke:#c62828,color:#333333
    style P0_3 fill:#ffcdd2,stroke:#c62828,color:#333333
    style P0_4 fill:#ffcdd2,stroke:#c62828,color:#333333
    style P1_1 fill:#ffe0b2,stroke:#ef6c00,color:#333333
    style P1_2 fill:#ffe0b2,stroke:#ef6c00,color:#333333
    style P1_3 fill:#ffe0b2,stroke:#ef6c00,color:#333333
    style P1_4 fill:#ffe0b2,stroke:#ef6c00,color:#333333
    style P2_1 fill:#c8e6c9,stroke:#2e7d32,color:#333333
    style P2_2 fill:#c8e6c9,stroke:#2e7d32,color:#333333
    style P2_3 fill:#c8e6c9,stroke:#2e7d32,color:#333333
    style P2_4 fill:#c8e6c9,stroke:#2e7d32,color:#333333
    style P2_5 fill:#c8e6c9,stroke:#2e7d32,color:#333333
```

## 6. Component Architecture

```mermaid
classDiagram
    class SecurityTier {
        <<enumeration>>
        TIER_0 : Critical
        TIER_1 : Sensitive
        TIER_2 : Standard
        +canManage(SecurityTier) boolean
        +isAtLeast(SecurityTier) boolean
    }

    class SecurityContext {
        -UserId actorId
        -String actorEmail
        -RoleId roleId
        -RoleCode roleCode
        -SecurityTier actorTier
        -Set~PermissionCode~ permissions
        +hasPermission(String) boolean
        +isSuperAdmin() boolean
        +canManageTier(SecurityTier) boolean
    }

    class PrivilegeEscalationGuard {
        +validateNotSelfModification(UserId, UserId)
        +validateRoleAssignment(SecurityTier, Role)
        +validatePermissionGrant(SecurityTier, Permission)
        +validateSystemRoleModification(SecurityTier, Role)
        +validateRoleDeletion(Role)
        +validatePermissionDeletion(Permission)
    }

    class AuditService {
        -AuditLogRepository repository
        -RoutingContext routingContext
        +record(SecurityContext, String, String, String, String, String)
    }

    class AuditLog {
        -AuditLogId id
        -Instant occurredAt
        -UserId actorId
        -String actorEmail
        -SecurityTier actorTier
        -String action
        -String targetType
        -String targetId
        -String oldValue
        -String newValue
        -String ipAddress
        -String userAgent
    }

    class PermissionInterceptor {
        -SecurityContextCache cache
        +checkPermission(InvocationContext) Object
        -getAnnotation(InvocationContext) RequiresPermission
        -checkAccess(Set, String[], LogicalOperator) boolean
    }

    class SecurityContextCache {
        -JsonWebToken jwt
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PermissionRepository permissionRepository
        +getCurrentContext() SecurityContext
        +loadContext(UUID) SecurityContext
        +invalidate(UUID) void
        +invalidateByRole(RoleId) void
    }

    class RequiresPermission {
        <<annotation>>
        +value() String[]
        +operator() LogicalOperator
    }

    SecurityContext --> SecurityTier
    PrivilegeEscalationGuard --> SecurityTier
    AuditService --> AuditLog
    AuditLog --> SecurityTier
    PermissionInterceptor ..> RequiresPermission
    PermissionInterceptor --> SecurityContextCache
    SecurityContextCache --> SecurityContext
```

## 7. Audit Data Flow

```mermaid
flowchart LR
    subgraph UseCase["Use Case"]
        UC[AssignNewRole<br/>CreateUser<br/>LockUser<br/>...]
    end

    subgraph Service["AuditService"]
        AS[record]
        IP[extractIpAddress]
        UA[extractUserAgent]
    end

    subgraph Context["RoutingContext"]
        REQ[HTTP Request<br/>Headers]
    end

    subgraph Domain["AuditLog"]
        AL[Create AuditLog<br/>• id: UUID<br/>• occurredAt: now<br/>• actorId, actorEmail<br/>• actorTier<br/>• action, targetType<br/>• oldValue, newValue<br/>• ipAddress, userAgent]
    end

    subgraph Repository["AuditLogRepository"]
        REPO[append only]
    end

    subgraph Database["PostgreSQL"]
        TBL[(audit_log table)]
        TRG[/TRIGGER\<br/>prevent_audit_modification/]
    end

    UC -->|"1. securityContext"| AS
    AS --> IP
    AS --> UA
    IP --> REQ
    UA --> REQ
    AS -->|"2. create"| AL
    AL -->|"3. append"| REPO
    REPO -->|"4. INSERT"| TBL
    TBL --- TRG

    style TRG fill:#ffcdd2,stroke:#c62828
```

## 8. User States and Transitions

```mermaid
stateDiagram-v2
    [*] --> Active: CreateUser

    Active --> Locked: LockUser
    Locked --> Active: UnlockUser

    Active --> ContractExpired: Contract expired
    ContractExpired --> Active: ExtendContract

    Locked --> Deleted: DeleteUser
    ContractExpired --> Deleted: DeleteUser

    Deleted --> [*]

    note right of Active
        Can login and
        use the system
    end note

    note right of Locked
        Blocked by admin
        UserStatusFilter rejects
    end note

    note right of ContractExpired
        Contract ended
        UserStatusFilter rejects
    end note
```

## 9. Token Lifecycle & Permission Caching

### JWT Token Configuration

| Property | Value | Description |
|----------|-------|-------------|
| Duration | 1 hour (3600s) | Token expiration time |
| Issuer | beeboard-api | Token issuer identifier |
| Algorithm | RS256 | RSA signature with SHA-256 |
| Refresh | Not implemented | User must re-authenticate |

### Permission Caching (Caffeine)

| Property | Value | Description |
|----------|-------|-------------|
| TTL | 15 minutes | Cache entry expiration |
| Max Size | 1000 entries | Maximum cached users |
| Invalidation | Proactive | Triggered on role/permission changes |

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant PI as PermissionInterceptor
    participant SCC as SecurityContextCache
    participant CACHE as Caffeine Cache
    participant DB as PostgreSQL

    C->>PI: HTTP Request with JWT
    PI->>SCC: getCurrentContext()
    SCC->>SCC: Extract userId from JWT

    alt Cache HIT
        SCC->>CACHE: loadContext(userId)
        CACHE-->>SCC: SecurityContext (cached)
        Note over CACHE: 0 DB queries
    else Cache MISS
        SCC->>CACHE: loadContext(userId)
        CACHE->>DB: SELECT user, role, permissions
        DB-->>CACHE: User + Role + Permissions
        CACHE-->>SCC: SecurityContext (new)
        Note over CACHE: 3 DB queries, cached 15min
    end

    SCC-->>PI: SecurityContext
    PI->>PI: Check permissions
    PI-->>C: Allow/Deny
```

### Cache Invalidation Triggers

| Trigger | Use Case | Invalidation Scope |
|---------|----------|-------------------|
| Role assignment | AssignNewRole | Single user |
| Role update | UpdateRole | All users with role |
| Permission grant | GrantPermission | All users with role |
| Permission revoke | RevokePermissionFromRole | All users with role |

```mermaid
flowchart LR
    subgraph Triggers["Invalidation Triggers"]
        T1[AssignNewRole]
        T2[UpdateRole]
        T3[GrantPermission]
        T4[RevokePermissionFromRole]
    end

    subgraph Cache["SecurityContextCache"]
        INV1[invalidate userId]
        INV2[invalidateByRole roleId]
    end

    subgraph Effect["Cache Effect"]
        E1[Remove single entry]
        E2[Remove all users with role]
    end

    T1 --> INV1 --> E1
    T2 --> INV2 --> E2
    T3 --> INV2 --> E2
    T4 --> INV2 --> E2
```

### Performance Comparison

| Metric | Before (No Cache) | After (Caffeine 15min) |
|--------|-------------------|------------------------|
| DB queries/request | 6 | 0 (if cached) |
| DB queries/15min (100 requests) | 600 | 3 |
| Permission check latency | ~50ms | ~1ms |
| Memory overhead | None | ~1KB/user |
