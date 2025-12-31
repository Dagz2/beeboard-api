# Class Diagrams - Beeboard API

This document provides class diagrams for the Beeboard API, organized by architectural layer following Domain-Driven Design (DDD) principles.

## Table of Contents

1. [DDD Layered Architecture Overview](#1-ddd-layered-architecture-overview)
2. [Domain Layer - Aggregates](#2-domain-layer---aggregates)
   - [2.1 User Aggregate](#21-user-aggregate)
   - [2.2 Role Aggregate](#22-role-aggregate)
   - [2.3 Permission Aggregate](#23-permission-aggregate)
   - [2.4 AuditLog Aggregate](#24-auditlog-aggregate)
   - [2.5 PasswordResetRequest Aggregate](#25-passwordresetrequest-aggregate)
   - [2.6 RateLimit (Technical Domain Model)](#26-ratelimit-technical-domain-model)
   - [2.7 Domain Services](#27-domain-services)
3. [Application Layer](#3-application-layer)
   - [3.1 Use Cases (Commands)](#31-use-cases-commands)
   - [3.2 Use Cases (Queries)](#32-use-cases-queries)
   - [3.3 Application Services](#33-application-services)
4. [Infrastructure Layer](#4-infrastructure-layer)
   - [4.1 JPA Entities](#41-jpa-entities)
   - [4.2 Repository Implementations](#42-repository-implementations)
5. [Exposition Layer](#5-exposition-layer)
   - [5.1 REST Controllers](#51-rest-controllers)
   - [5.2 Security Components](#52-security-components)

---

## 1. DDD Layered Architecture Overview

This diagram illustrates the four-layer architecture following Domain-Driven Design principles. Each layer has a specific responsibility and dependencies flow inward (outer layers depend on inner layers).

```mermaid
block-beta
    columns 1

    block:EXPO:1
        columns 4
        E0["EXPOSITION LAYER\n(Entry points & Security)"]
        E1["AccessRessource"]
        E2["AuthenticationRessource"]
        E3["PermissionInterceptor"]
    end

    space

    block:APP:1
        columns 4
        A0["APPLICATION LAYER\n(Use Cases & Orchestration)"]
        A1["Commands:\nCreateUser, LockUser\nGrantPermission..."]
        A2["Queries:\nUserQueryService\nRoleQueryService"]
        A3["Services:\nSecurityContextService\nUserStatusService\nAuditService, JwtService"]
    end

    space

    block:DOMAIN:1
        columns 5
        D0["DOMAIN LAYER\n(Business Logic & Rules)"]
        D1["Aggregates:\nUser, Role\nPermission, AuditLog"]
        D2["Value Objects:\nUserId, EmailAddress\nPassword, SecurityTier"]
        D3["Domain Services:\nPrivilegeEscalationGuard"]
        D4["Repository Interfaces:\nUserRepository\nRoleRepository"]
    end

    space

    block:INFRA:1
        columns 4
        I0["INFRASTRUCTURE LAYER\n(Technical Implementation)"]
        I1["JPA Entities:\nUserEntity\nRoleEntity"]
        I2["Repository Impls:\nUserRepositoryImpl\nRoleRepositoryImpl"]
        I3["External:\nPostgreSQL\nSMTP Server"]
    end

    EXPO --> APP
    APP --> DOMAIN
    INFRA --> DOMAIN

    style E0 fill:#1565c0,color:#ffffff
    style A0 fill:#ef6c00,color:#ffffff
    style D0 fill:#2e7d32,color:#ffffff
    style I0 fill:#c2185b,color:#ffffff
```

### Layer Responsibilities

| Layer | Responsibility | Key Components |
|-------|----------------|----------------|
| **Exposition** | HTTP endpoints, authentication, authorization | REST Controllers, Interceptors, Filters |
| **Application** | Use case orchestration, transaction management | Commands, Queries, Application Services |
| **Domain** | Business logic, validation, domain rules | Aggregates, Value Objects, Domain Services, Repository Interfaces |
| **Infrastructure** | Technical implementation, persistence, external systems | JPA Entities, Repository Implementations, Database, SMTP |

### Dependency Rules

```mermaid
flowchart LR
    subgraph Rules["<b>Dependency Direction</b>"]
        direction LR
        R1[Exposition] --> R2[Application]
        R2 --> R3[Domain]
        R4[Infrastructure] --> R3
    end

    style R1 fill:#e3f2fd,stroke:#1565c0,color:#0d47a1
    style R2 fill:#fff3e0,stroke:#ef6c00,color:#e65100
    style R3 fill:#e8f5e9,stroke:#2e7d32,color:#1b5e20
    style R4 fill:#fce4ec,stroke:#c2185b,color:#880e4f
```

- **Domain** is the core - it has no dependencies on other layers
- **Application** depends only on Domain
- **Exposition** depends on Application (and transitively on Domain)
- **Infrastructure** implements Domain interfaces (Dependency Inversion)

---

## 2. Domain Layer - Aggregates

### DDD Glossary (Domain-Driven Design)

Before exploring the domain class diagrams, here are the key DDD concepts used in this architecture:

| Concept | Description | Beeboard Example |
|---------|-------------|------------------|
| **Entity** | Object with a unique identity that persists over time. Two entities are equal if they have the same ID, even if their attributes differ. | `User`, `Role`, `Permission` |
| **Value Object** | Immutable object defined only by its attributes. Two Value Objects are equal if all their attributes are identical. Has no identity of its own. | `EmailAddress`, `Password`, `FullName`, `SecurityTier` |
| **Aggregate** | Group of entities and Value Objects treated as a cohesive unit. Ensures data consistency within its boundaries. | `User` (with its Value Objects), `Role` (with its permissions) |
| **Aggregate Root** | Main entity of an Aggregate. Single entry point to modify the Aggregate. External objects can only reference the Aggregate Root. | `User` is the Aggregate Root of the User aggregate |
| **Repository** | Abstract interface for accessing Aggregates. Hides persistence details (database). | `UserRepository`, `RoleRepository` |
| **Domain Service** | Business logic that doesn't naturally belong to any Entity. Operations involving multiple Aggregates. | `PrivilegeEscalationGuard` |
| **Factory Method** | Static method to create instances of complex objects with validation. | `User.create(...)`, `Role.create(...)` |

```mermaid
flowchart LR
    subgraph Aggregate
        AR[Aggregate Root, also an Entity] --> VO1[Value Object]
        AR --> VO2[Value Object]
        AR --> E[Entity]
    end

    Repo[Repository Interface] -.-> AR
    DS[Domain Service] -.-> AR

    style AR fill:#4caf50,color:#fff
    style VO1 fill:#2196f3,color:#fff
    style VO2 fill:#2196f3,color:#fff
    style E fill:#ff9800,color:#fff
    style Repo fill:#9c27b0,color:#fff
    style DS fill:#607d8b,color:#fff
```

**Important Rules:**
- References between Aggregates are made only by **ID** (e.g., `User` contains `RoleId`, not `Role`)
- Only the **Aggregate Root** can be retrieved via a Repository
- **Value Objects** are always immutable and entirely replaced when modified

---

### 2.1 User Aggregate

```mermaid
classDiagram
    direction TB

    class User {
        <<Aggregate Root>>
        -UserId id
        -FullName fullName
        -EmailAddress email
        -Password password
        -RoleId roleId
        -ContractEndDate contractEndDate
        -boolean isLocked
        -AuditInfo auditInfo
        -Instant lastActivityDate
        +create(FullName, EmailAddress, Password, RoleId, ContractEndDate)$ User
        +changeEmail(EmailAddress) void
        +changeFullName(FullName) void
        +assignRole(RoleId) void
        +extendContract(ContractEndDate) void
        +terminateContract() void
        +lock() void
        +unlock() void
        +changePassword(Password) void
        +verifyPassword(String) boolean
        +isContractExpired() boolean
        +recordActivity() void
    }

    class UserId {
        <<Value Object>>
        -UUID id
        +newId()$ UserId
        +id() UUID
    }

    class FullName {
        <<Value Object>>
        -String firstName
        -String lastName
        +firstName() String
        +lastName() String
        +fullName() String
    }

    class EmailAddress {
        <<Value Object>>
        -String value
        +value() String
    }

    class Password {
        <<Value Object>>
        -String hashedValue
        +fromPlainText(String)$ Password
        +fromHash(String)$ Password
        +matches(String) boolean
        +value() String
    }

    class ContractEndDate {
        <<Value Object>>
        -LocalDate date
        +newContractEndDate(LocalDate)$ ContractEndDate
        +date() LocalDate
        +isAfter(ContractEndDate) boolean
    }

    class AuditInfo {
        <<Value Object>>
        -Instant createdAt
        -Instant updatedAt
        +of(Instant)$ AuditInfo
        +touch() void
        +createdAt() Instant
        +updatedAt() Instant
    }

    class RoleId {
        <<Value Object>>
        -UUID id
        +newId()$ RoleId
        +id() UUID
    }

    class UserRepository {
        <<Interface>>
        +save(User) void
        +update(User) void
        +findById(UserId) Optional~User~
        +findByEmail(EmailAddress) Optional~User~
        +findAll() List~User~
        +delete(UserId) void
        +existsByEmail(EmailAddress) boolean
    }

    User *-- UserId
    User *-- FullName
    User *-- EmailAddress
    User *-- Password
    User *-- ContractEndDate
    User *-- AuditInfo
    User o-- RoleId : references

    UserRepository ..> User : manages
```

---

### 2.2 Role Aggregate

```mermaid
classDiagram
    direction TB

    class Role {
        <<Aggregate Root>>
        -RoleId id
        -RoleCode code
        -RoleName name
        -Set~PermissionId~ permissions
        -AuditInfo auditInfo
        -SecurityTier tier
        -boolean isSystem
        +create(RoleCode, RoleName)$ Role
        +create(RoleCode, RoleName, SecurityTier, boolean)$ Role
        +grantPermission(PermissionId) boolean
        +revokePermission(PermissionId) boolean
        +hasPermission(PermissionId) boolean
        +updateCode(RoleCode) void
        +updateName(RoleName) void
        +permissions() Set~PermissionId~
        +tier() SecurityTier
        +isSystem() boolean
    }

    class RoleId {
        <<Value Object>>
        -UUID id
        +newId()$ RoleId
        +id() UUID
    }

    class RoleCode {
        <<Value Object>>
        -String value
        +value() String
    }

    class RoleName {
        <<Value Object>>
        -String value
        +value() String
    }

    class SecurityTier {
        <<Enumeration>>
        TIER_0
        TIER_1
        TIER_2
        +canManage(SecurityTier) boolean
        +isAtLeast(SecurityTier) boolean
        +defaultTier()$ SecurityTier
    }

    class PermissionId {
        <<Value Object>>
        -UUID id
        +newId()$ PermissionId
        +id() UUID
    }

    class RoleRepository {
        <<Interface>>
        +save(Role) void
        +update(Role) void
        +findById(RoleId) Optional~Role~
        +findByCode(RoleCode) Optional~Role~
        +findAll() List~Role~
        +delete(RoleId) void
    }

    class AuditInfo {
        <<Value Object>>
        -Instant createdAt
        -Instant updatedAt
        +of(Instant)$ AuditInfo
        +touch() void
        +createdAt() Instant
        +updatedAt() Instant
    }

    Role *-- RoleId
    Role *-- RoleCode
    Role *-- RoleName
    Role *-- SecurityTier
    Role *-- AuditInfo
    Role o-- PermissionId : contains many

    RoleRepository ..> Role : manages
```

---

### 2.3 Permission Aggregate

```mermaid
classDiagram
    direction TB

    class Permission {
        <<Aggregate Root>>
        -PermissionId id
        -PermissionCode code
        -PermissionName name
        -String description
        -SecurityTier tier
        -boolean isSystem
        +create(PermissionCode, PermissionName, String)$ Permission
        +create(PermissionCode, PermissionName, String, SecurityTier, boolean)$ Permission
        +updateCode(PermissionCode) void
        +updateName(PermissionName) void
        +updateDescription(String) void
        +tier() SecurityTier
        +isSystem() boolean
    }

    class PermissionId {
        <<Value Object>>
        -UUID id
        +newId()$ PermissionId
        +id() UUID
    }

    class PermissionCode {
        <<Value Object>>
        -String value
        +value() String
    }

    class PermissionName {
        <<Value Object>>
        -String value
        +value() String
    }

    class PermissionRepository {
        <<Interface>>
        +save(Permission) void
        +update(Permission) void
        +findById(PermissionId) Optional~Permission~
        +findByCode(PermissionCode) Optional~Permission~
        +findAll() List~Permission~
        +findAllByIds(Set~PermissionId~) List~Permission~
        +delete(PermissionId) void
    }

    class SecurityTier {
        <<Enumeration>>
        TIER_0
        TIER_1
        TIER_2
        +canManage(SecurityTier) boolean
        +isAtLeast(SecurityTier) boolean
        +defaultTier()$ SecurityTier
    }

    Permission *-- PermissionId
    Permission *-- PermissionCode
    Permission *-- PermissionName
    Permission *-- SecurityTier

    PermissionRepository ..> Permission : manages
```

---

### 2.4 AuditLog Aggregate

```mermaid
classDiagram
    direction TB

    class AuditLog {
        <<Aggregate Root - Immutable>>
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
        +create(UserId, String, SecurityTier, String, String, String, String, String, String, String)$ AuditLog
        +id() AuditLogId
        +occurredAt() Instant
        +actorId() UserId
        +actorEmail() String
        +actorTier() SecurityTier
        +action() String
        +targetType() String
        +targetId() String
        +oldValue() String
        +newValue() String
    }

    class AuditLogId {
        <<Value Object>>
        -UUID id
        +newId()$ AuditLogId
        +id() UUID
    }

    class AuditLogRepository {
        <<Interface>>
        +save(AuditLog) void
        +findById(AuditLogId) Optional~AuditLog~
        +findByActorId(UserId) List~AuditLog~
        +findByAction(String) List~AuditLog~
        +findAll() List~AuditLog~
    }

    class UserId {
        <<Value Object>>
        -UUID id
        +newId()$ UserId
        +id() UUID
    }

    class SecurityTier {
        <<Enumeration>>
        TIER_0
        TIER_1
        TIER_2
        +canManage(SecurityTier) boolean
        +isAtLeast(SecurityTier) boolean
    }

    AuditLog *-- AuditLogId
    AuditLog o-- UserId : references actor
    AuditLog *-- SecurityTier

    AuditLogRepository ..> AuditLog : manages

    note for AuditLog "Immutable: Once created, cannot be modified or deleted"
```

---

### 2.5 PasswordResetRequest Aggregate

```mermaid
classDiagram
    direction TB

    class PasswordResetRequest {
        <<Aggregate Root>>
        -PasswordResetRequestId id
        -UserId userId
        -ResetToken token
        -Instant expiresAt
        -boolean isUsed
        -AuditInfo auditInfo
        -TOKEN_VALIDITY_SECONDS$ long
        +create(UserId, ResetToken)$ PasswordResetRequest
        +isExpired() boolean
        +canBeUsed() boolean
        +markAsUsed() void
        +getId() PasswordResetRequestId
        +getUserId() UserId
        +getToken() ResetToken
        +getExpiresAt() Instant
        +getIsUsed() boolean
    }

    class PasswordResetRequestId {
        <<Value Object>>
        -UUID id
        +newId()$ PasswordResetRequestId
        +id() UUID
    }

    class ResetToken {
        <<Value Object>>
        -String value
        +generate()$ ResetToken
        +fromString(String)$ ResetToken
        +hash() String
        +toString() String
    }

    class PasswordResetRequestRepository {
        <<Interface>>
        +save(PasswordResetRequest) void
        +update(PasswordResetRequest) void
        +findByToken(String hashedToken) Optional~PasswordResetRequest~
        +invalidateAllForUser(UserId) void
        +deleteExpiredTokens() void
    }

    class UserId {
        <<Value Object>>
        -UUID id
        +newId()$ UserId
        +id() UUID
    }

    class AuditInfo {
        <<Value Object>>
        -Instant createdAt
        -Instant updatedAt
        +of(Instant)$ AuditInfo
        +touch() void
        +createdAt() Instant
        +updatedAt() Instant
    }

    PasswordResetRequest *-- PasswordResetRequestId
    PasswordResetRequest *-- ResetToken
    PasswordResetRequest *-- AuditInfo
    PasswordResetRequest o-- UserId : references

    PasswordResetRequestRepository ..> PasswordResetRequest : manages

    note for ResetToken "256-bit entropy 64 hex characters SHA-256 hashed for storage"
```

---

### 2.6 RateLimit (Technical Domain Model)

```mermaid
classDiagram
    direction TB

    class RateLimit {
        <<Domain Model>>
        -String email
        -int requestCount
        -Instant windowStart
        +firstRequest(String email)$ RateLimit
        +email() String
        +requestCount() int
        +windowStart() Instant
        +isLimitExceeded(int maxRequests) boolean
        +isWindowExpired(int windowHours) boolean
        +increment() RateLimit
        +resetWindow() RateLimit
    }

    class RateLimitRepository {
        <<Interface>>
        +findByEmail(String) Optional~RateLimit~
        +save(RateLimit) void
        +deleteExpired(Instant cutoff) long
    }

    RateLimitRepository ..> RateLimit : manages

    note for RateLimit "Immutable - operations return new instances"
```

---

### 2.7 Domain Services

#### PrivilegeEscalationGuard

```mermaid
classDiagram
    direction TB

    class PrivilegeEscalationGuard {
        <<Domain Service>>
        +validateNotSelfModification(UserId actorId, UserId targetUserId) void
        +validateRoleAssignment(SecurityTier actorTier, Role targetRole) void
        +validatePermissionGrant(SecurityTier actorTier, Permission targetPermission) void
        +validateSystemRoleModification(SecurityTier actorTier, Role role) void
        +validateRoleDeletion(Role role) void
        +validateSystemPermissionModification(SecurityTier actorTier, Permission permission) void
        +validatePermissionDeletion(Permission permission) void
        +validateMinimumTier(SecurityTier actorTier, SecurityTier requiredTier) void
        +validateTierZeroRequired(SecurityTier actorTier, String operation) void
        +validateUserManagement(SecurityTier actorTier, SecurityTier targetUserTier) void
    }

    class SecurityTier {
        <<Enumeration>>
        TIER_0
        TIER_1
        TIER_2
        +canManage(SecurityTier) boolean
        +isAtLeast(SecurityTier) boolean
    }

    class SelfModificationForbiddenException {
        <<Exception>>
        +cannotModifyOwnRole()$ SelfModificationForbiddenException
    }

    class PrivilegeEscalationException {
        <<Exception>>
        +cannotAssignHigherTierRole(String, String)$ PrivilegeEscalationException
        +cannotGrantHigherTierPermission(String, String)$ PrivilegeEscalationException
        +cannotManageHigherTierUser(String, String)$ PrivilegeEscalationException
    }

    class SystemResourceProtectedException {
        <<Exception>>
        +cannotModifySystemRole(String)$ SystemResourceProtectedException
        +cannotDeleteSystemRole(String)$ SystemResourceProtectedException
        +cannotModifySystemPermission(String)$ SystemResourceProtectedException
        +cannotDeleteSystemPermission(String)$ SystemResourceProtectedException
    }

    class InsufficientTierException {
        <<Exception>>
        +requiresTier(SecurityTier, SecurityTier)$ InsufficientTierException
        +onlyTierZeroCanPerform(String)$ InsufficientTierException
    }

    PrivilegeEscalationGuard ..> SecurityTier : uses
    PrivilegeEscalationGuard ..> SelfModificationForbiddenException : throws
    PrivilegeEscalationGuard ..> PrivilegeEscalationException : throws
    PrivilegeEscalationGuard ..> SystemResourceProtectedException : throws
    PrivilegeEscalationGuard ..> InsufficientTierException : throws
```

#### SecurityContext

```mermaid
classDiagram
    direction TB

    class SecurityContext {
        <<Value Object>>
        -UserId actorId
        -String actorEmail
        -RoleId roleId
        -RoleCode roleCode
        -SecurityTier actorTier
        -Set~PermissionCode~ permissions
        +actorId() UserId
        +actorEmail() String
        +roleId() RoleId
        +roleCode() RoleCode
        +actorTier() SecurityTier
        +permissions() Set~PermissionCode~
        +hasPermission(PermissionCode) boolean
        +hasPermission(String) boolean
        +isSuperAdmin() boolean
        +canManageTier(SecurityTier) boolean
    }

    class SecurityTier {
        <<Enumeration>>
        TIER_0
        TIER_1
        TIER_2
        +canManage(SecurityTier) boolean
        +isAtLeast(SecurityTier) boolean
    }

    class UserId {
        <<Value Object>>
        -UUID id
        +newId()$ UserId
        +id() UUID
    }

    class RoleId {
        <<Value Object>>
        -UUID id
        +newId()$ RoleId
        +id() UUID
    }

    class RoleCode {
        <<Value Object>>
        -String value
        +value() String
    }

    class PermissionCode {
        <<Value Object>>
        -String value
        +value() String
    }

    SecurityContext *-- SecurityTier
    SecurityContext o-- UserId
    SecurityContext o-- RoleId
    SecurityContext o-- RoleCode
    SecurityContext o-- PermissionCode
```

---

## 3. Application Layer

### 3.1 Use Cases (Commands)

#### User Commands

```mermaid
classDiagram
    direction TB

    class CreateUser {
        <<Use Case>>
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(CreateUserCommand, SecurityContext) void
    }

    class AssignNewRole {
        <<Use Case>>
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(AssignNewRoleCommand, SecurityContext) void
    }

    class LockUser {
        <<Use Case>>
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(LockUserCommand, SecurityContext) void
    }

    class UnlockUser {
        <<Use Case>>
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(UnlockUserCommand, SecurityContext) void
    }

    class DeleteUser {
        <<Use Case>>
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(DeleteUserCommand, SecurityContext) void
    }

    CreateUser ..> UserRepository
    CreateUser ..> RoleRepository
    CreateUser ..> PrivilegeEscalationGuard
    CreateUser ..> AuditService
```

```mermaid
classDiagram
    direction TB

    class ExtendContract {
        <<Use Case>>
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(ExtendContractCommand, SecurityContext) void
    }

    class TerminateContract {
        <<Use Case>>
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(TerminateContractCommand, SecurityContext) void
    }

    class ChangePassword {
        <<Use Case>>
        -UserRepository userRepository
        -AuditService auditService
        +execute(ChangePasswordCommand, SecurityContext) void
    }

    class ChangeFullName {
        <<Use Case>>
        -UserRepository userRepository
        +execute(ChangeFullNameCommand) void
    }

    class ChangeMail {
        <<Use Case>>
        -UserRepository userRepository
        +execute(ChangeMailCommand) void
    }
```

#### Role Commands

```mermaid
classDiagram
    direction TB

    class CreateRole {
        <<Use Case>>
        -RoleRepository roleRepository
        -AuditService auditService
        +execute(CreateRoleCommand, SecurityContext) void
    }

    class UpdateRole {
        <<Use Case>>
        -RoleRepository roleRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(UpdateRoleCommand, SecurityContext) void
    }

    class DeleteRole {
        <<Use Case>>
        -RoleRepository roleRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(DeleteRoleCommand, SecurityContext) void
    }

    class GrantPermission {
        <<Use Case>>
        -RoleRepository roleRepository
        -PermissionRepository permissionRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(GrantPermissionCommand, SecurityContext) void
    }

    class RevokePermissionFromRole {
        <<Use Case>>
        -RoleRepository roleRepository
        -PermissionRepository permissionRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(RevokePermissionCommand, SecurityContext) void
    }
```

#### Permission Commands

```mermaid
classDiagram
    direction TB

    class CreatePermission {
        <<Use Case>>
        -PermissionRepository permissionRepository
        -AuditService auditService
        +execute(CreatePermissionCommand, SecurityContext) void
    }

    class UpdatePermission {
        <<Use Case>>
        -PermissionRepository permissionRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(UpdatePermissionCommand, SecurityContext) void
    }

    class DeletePermission {
        <<Use Case>>
        -PermissionRepository permissionRepository
        -PrivilegeEscalationGuard privilegeGuard
        -AuditService auditService
        +execute(DeletePermissionCommand, SecurityContext) void
    }
```

#### Auth Commands

```mermaid
classDiagram
    direction TB

    class AuthenticateUser {
        <<Use Case>>
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PermissionRepository permissionRepository
        -JwtService jwtService
        +execute(AuthenticateUserCommand) AuthenticationResponse
    }

    class RequestPasswordReset {
        <<Use Case>>
        -UserRepository userRepository
        -PasswordResetRequestRepository resetRepository
        -RateLimitService rateLimitService
        -EmailService emailService
        +execute(RequestPasswordResetCommand) void
    }

    class CompletePasswordReset {
        <<Use Case>>
        -UserRepository userRepository
        -PasswordResetRequestRepository resetRepository
        +execute(CompletePasswordResetCommand) void
    }

    AuthenticateUser ..> JwtService
    RequestPasswordReset ..> EmailService
    RequestPasswordReset ..> RateLimitService
```

---

### 3.2 Use Cases (Queries)

```mermaid
classDiagram
    direction TB

    class UserQueryService {
        <<Query Service>>
        -UserRepository userRepository
        +findById(UUID) UserResponse
        +findAll() List~UserResponse~
        +findByEmail(String) UserResponse
    }

    class RoleQueryService {
        <<Query Service>>
        -RoleRepository roleRepository
        -PermissionRepository permissionRepository
        +findById(UUID) RoleResponse
        +findAll() List~RoleResponse~
        +findByCode(String) RoleResponse
    }

    class PermissionQueryService {
        <<Query Service>>
        -PermissionRepository permissionRepository
        +findById(UUID) PermissionResponse
        +findAll() List~PermissionResponse~
        +findByCode(String) PermissionResponse
    }

    class GetCurrentUserProfile {
        <<Query>>
        -UserRepository userRepository
        -RoleRepository roleRepository
        -PermissionRepository permissionRepository
        +execute(UserId) UserProfileResponse
    }

    UserQueryService ..> UserRepository
    RoleQueryService ..> RoleRepository
    RoleQueryService ..> PermissionRepository
    PermissionQueryService ..> PermissionRepository
    GetCurrentUserProfile ..> UserRepository
    GetCurrentUserProfile ..> RoleRepository
    GetCurrentUserProfile ..> PermissionRepository
```

---

### 3.3 Application Services

#### Core Services (Audit & Security)

```mermaid
classDiagram
    direction TB

    class AuditService {
        <<Application Service>>
        -AuditLogRepository auditLogRepository
        +record(SecurityContext, String action, String targetType, String targetId, String oldValue, String newValue) void
    }

    class JwtService {
        <<Application Service>>
        -String secretKey
        -long expirationMs
        +generateToken(UserId, String email, RoleCode, SecurityTier, Set~PermissionCode~) String
        +validateToken(String) boolean
        +extractUserId(String) UserId
        +extractEmail(String) String
        +extractRoleCode(String) RoleCode
        +extractTier(String) SecurityTier
        +extractPermissions(String) Set~PermissionCode~
    }

    AuditService ..> AuditLogRepository
    AuditService ..> SecurityContext

    JwtService ..> UserId
    JwtService ..> RoleCode
    JwtService ..> SecurityTier
    JwtService ..> PermissionCode
```

#### Support Services (Password Reset & User Status)

```mermaid
classDiagram
    direction TB

    class EmailService {
        <<Application Service>>
        -Mailer mailer
        -String frontendBaseUrl
        +sendPasswordResetEmail(User, ResetToken) void
    }

    class RateLimitService {
        <<Application Service>>
        -RateLimitRepository rateLimitRepository
        -int rateLimit
        -int windowHours
        +isRateLimitExceeded(String email) boolean
        +incrementRateLimit(String email) void
        +cleanupExpiredLimits() void
    }

    class UserStatusService {
        <<Application Service>>
        -UserRepository userRepository
        +checkStatus(UUID userId) UserStatus
    }

    class UserStatus {
        <<Record>>
        +exists boolean
        +locked boolean
        +contractExpired boolean
        +notFound()$ UserStatus
        +fromUser(User)$ UserStatus
        +isAccessDenied() boolean
    }

    class PasswordResetCleanupService {
        <<Scheduled Service>>
        -PasswordResetRequestRepository resetRepository
        -RateLimitRepository rateLimitRepository
        +cleanupExpiredTokens() void
        +cleanupExpiredRateLimits() void
    }

    EmailService ..> User
    EmailService ..> ResetToken

    RateLimitService ..> RateLimitRepository
    RateLimitService ..> RateLimit

    UserStatusService ..> UserRepository
    UserStatusService ..> UserStatus

    PasswordResetCleanupService ..> PasswordResetRequestRepository
    PasswordResetCleanupService ..> RateLimitRepository
```

---

## 4. Infrastructure Layer

### 4.1 JPA Entities

```mermaid
classDiagram
    direction TB

    class UserEntity {
        <<JPA Entity>>
        -UUID id
        -String firstName
        -String lastName
        -String email
        -String passwordHash
        -UUID roleId
        -LocalDate contractEndDate
        -boolean isLocked
        -Instant createdAt
        -Instant updatedAt
        -Instant lastActivityDate
    }

    class RoleEntity {
        <<JPA Entity>>
        -UUID id
        -String code
        -String name
        -Set~UUID~ permissionIds
        -Instant createdAt
        -Instant updatedAt
        -String tier
        -boolean isSystem
    }

    class PermissionEntity {
        <<JPA Entity>>
        -UUID id
        -String code
        -String name
        -String description
        -String tier
        -boolean isSystem
    }

    class AuditLogEntity {
        <<JPA Entity>>
        -UUID id
        -Instant occurredAt
        -UUID actorId
        -String actorEmail
        -String actorTier
        -String action
        -String targetType
        -String targetId
        -String oldValue
        -String newValue
        -String ipAddress
        -String userAgent
    }

    class PasswordResetRequestEntity {
        <<JPA Entity>>
        -UUID id
        -UUID userId
        -String tokenHash
        -Instant expiresAt
        -boolean isUsed
        -Instant createdAt
        -Instant updatedAt
    }

    class RateLimitEntity {
        <<JPA Entity>>
        -String email
        -int requestCount
        -Instant windowStart
    }

    UserEntity --> RoleEntity : roleId FK
    RoleEntity --> PermissionEntity : permissionIds
    PasswordResetRequestEntity --> UserEntity : userId FK
```

---

### 4.2 Repository Implementations

```mermaid
classDiagram
    direction TB

    class UserRepository {
        <<Interface>>
    }

    class UserRepositoryImpl {
        <<Implementation>>
        -EntityManager entityManager
        +save(User) void
        +update(User) void
        +findById(UserId) Optional~User~
        +findByEmail(EmailAddress) Optional~User~
        +findAll() List~User~
        +delete(UserId) void
        +existsByEmail(EmailAddress) boolean
        -toDomain(UserEntity) User
        -toEntity(User) UserEntity
    }

    class RoleRepository {
        <<Interface>>
    }

    class RoleRepositoryImpl {
        <<Implementation>>
        -EntityManager entityManager
        +save(Role) void
        +update(Role) void
        +findById(RoleId) Optional~Role~
        +findByCode(RoleCode) Optional~Role~
        +findAll() List~Role~
        +delete(RoleId) void
        -toDomain(RoleEntity) Role
        -toEntity(Role) RoleEntity
    }

    class PermissionRepository {
        <<Interface>>
    }

    class PermissionRepositoryImpl {
        <<Implementation>>
        -EntityManager entityManager
        +save(Permission) void
        +update(Permission) void
        +findById(PermissionId) Optional~Permission~
        +findByCode(PermissionCode) Optional~Permission~
        +findAll() List~Permission~
        +findAllByIds(Set~PermissionId~) List~Permission~
        +delete(PermissionId) void
        -toDomain(PermissionEntity) Permission
        -toEntity(Permission) PermissionEntity
    }

    class AuditLogRepository {
        <<Interface>>
    }

    class AuditLogRepositoryImpl {
        <<Implementation>>
        -EntityManager entityManager
        +save(AuditLog) void
        +findById(AuditLogId) Optional~AuditLog~
        +findByActorId(UserId) List~AuditLog~
        +findByAction(String) List~AuditLog~
        +findAll() List~AuditLog~
        -toDomain(AuditLogEntity) AuditLog
        -toEntity(AuditLog) AuditLogEntity
    }

    class PasswordResetRequestRepository {
        <<Interface>>
    }

    class PasswordResetRequestRepositoryImpl {
        <<Implementation>>
        -EntityManager entityManager
        +save(PasswordResetRequest) void
        +update(PasswordResetRequest) void
        +findByToken(String) Optional~PasswordResetRequest~
        +invalidateAllForUser(UserId) void
        +deleteExpiredTokens() void
        -toDomain(PasswordResetRequestEntity) PasswordResetRequest
        -toEntity(PasswordResetRequest) PasswordResetRequestEntity
    }

    class RateLimitRepository {
        <<Interface>>
    }

    class RateLimitRepositoryImpl {
        <<Implementation>>
        -EntityManager entityManager
        +findByEmail(String) Optional~RateLimit~
        +save(RateLimit) void
        +deleteExpired(Instant) long
        -toDomain(RateLimitEntity) RateLimit
        -toEntity(RateLimit) RateLimitEntity
    }

    UserRepository <|.. UserRepositoryImpl
    RoleRepository <|.. RoleRepositoryImpl
    PermissionRepository <|.. PermissionRepositoryImpl
    AuditLogRepository <|.. AuditLogRepositoryImpl
    PasswordResetRequestRepository <|.. PasswordResetRequestRepositoryImpl
    RateLimitRepository <|.. RateLimitRepositoryImpl

    UserRepositoryImpl ..> UserEntity : maps
    RoleRepositoryImpl ..> RoleEntity : maps
    PermissionRepositoryImpl ..> PermissionEntity : maps
    AuditLogRepositoryImpl ..> AuditLogEntity : maps
    PasswordResetRequestRepositoryImpl ..> PasswordResetRequestEntity : maps
    RateLimitRepositoryImpl ..> RateLimitEntity : maps
```

---

## 5. Exposition Layer

### 5.1 REST Controllers

```mermaid
classDiagram
    direction TB

    class AccessRessource {
        <<REST Controller>>
        -CreateUser createUser
        -AssignNewRole assignNewRole
        -LockUser lockUser
        -UnlockUser unlockUser
        -DeleteUser deleteUser
        -ExtendContract extendContract
        -TerminateContract terminateContract
        -UserQueryService userQueryService
        -CreateRole createRole
        -UpdateRole updateRole
        -DeleteRole deleteRole
        -GrantPermission grantPermission
        -RevokePermissionFromRole revokePermission
        -RoleQueryService roleQueryService
        -CreatePermission createPermission
        -UpdatePermission updatePermission
        -DeletePermission deletePermission
        -PermissionQueryService permissionQueryService
        -SecurityContextService securityContextService
        +createUser(CreateUserRequest) Response
        +getUsers() Response
        +getUserById(UUID) Response
        +assignRole(UUID, AssignRoleRequest) Response
        +lockUser(UUID) Response
        +unlockUser(UUID) Response
        +deleteUser(UUID) Response
        +extendContract(UUID, ExtendContractRequest) Response
        +terminateContract(UUID) Response
        +createRole(CreateRoleRequest) Response
        +getRoles() Response
        +getRoleById(UUID) Response
        +updateRole(UUID, UpdateRoleRequest) Response
        +deleteRole(UUID) Response
        +grantPermission(UUID, GrantPermissionRequest) Response
        +revokePermission(UUID, UUID) Response
        +createPermission(CreatePermissionRequest) Response
        +getPermissions() Response
        +getPermissionById(UUID) Response
        +updatePermission(UUID, UpdatePermissionRequest) Response
        +deletePermission(UUID) Response
    }

    class AuthenticationRessource {
        <<REST Controller>>
        -AuthenticateUser authenticateUser
        -RequestPasswordReset requestPasswordReset
        -CompletePasswordReset completePasswordReset
        -GetCurrentUserProfile getCurrentUserProfile
        -SecurityContextService securityContextService
        +login(LoginRequest) Response
        +requestPasswordReset(PasswordResetRequest) Response
        +completePasswordReset(CompleteResetRequest) Response
        +getCurrentProfile() Response
    }

    AccessRessource ..> SecurityContextService
    AccessRessource ..> CreateUser
    AccessRessource ..> AssignNewRole
    AccessRessource ..> LockUser
    AccessRessource ..> UnlockUser
    AccessRessource ..> DeleteUser
    AccessRessource ..> CreateRole
    AccessRessource ..> UpdateRole
    AccessRessource ..> DeleteRole
    AccessRessource ..> CreatePermission
    AccessRessource ..> UpdatePermission
    AccessRessource ..> DeletePermission

    AuthenticationRessource ..> SecurityContextService
    AuthenticationRessource ..> AuthenticateUser
    AuthenticationRessource ..> RequestPasswordReset
    AuthenticationRessource ..> CompletePasswordReset
    AuthenticationRessource ..> GetCurrentUserProfile
```

---

### 5.2 Security Components

```mermaid
classDiagram
    direction TB

    class RequiresPermission {
        <<Annotation>>
        +value() String[]
        +operator() LogicalOperator
    }

    class LogicalOperator {
        <<Enumeration>>
        AND
        OR
    }

    class PermissionInterceptor {
        <<CDI Interceptor>>
        -SecurityContextService securityContextService
        +intercept(InvocationContext) Object
        -checkPermissions(RequiresPermission annotation) void
        -hasAllPermissions(SecurityContext, String[]) boolean
        -hasAnyPermission(SecurityContext, String[]) boolean
    }

    class UserStatusFilter {
        <<JAX-RS Filter>>
        -UserStatusService userStatusService
        -JsonWebToken jwt
        +filter(ContainerRequestContext) void
    }

    PermissionInterceptor ..> RequiresPermission : reads
    PermissionInterceptor ..> LogicalOperator : uses
    PermissionInterceptor ..> SecurityContextService : uses
    PermissionInterceptor ..> SecurityContext : validates

    UserStatusFilter ..> UserStatusService : uses

    note for PermissionInterceptor "SecurityContextService is in Application layer"
    note for UserStatusFilter "UserStatusService is in Application layer"
```

---

## Legend

| Symbol | Meaning |
|--------|---------|
| `<<Aggregate Root>>` | Main domain entity that owns other objects |
| `<<Value Object>>` | Immutable object identified by its values |
| `<<Interface>>` | Contract for dependency inversion |
| `<<Implementation>>` | Concrete implementation of an interface |
| `<<Use Case>>` | Application service handling a specific command |
| `<<Query Service>>` | Read-only service for data retrieval |
| `<<Application Service>>` | Cross-cutting application concern |
| `<<JPA Entity>>` | Database persistence model |
| `<<REST Controller>>` | HTTP endpoint handler |
| `<<Annotation>>` | Java annotation for metadata |
| `<<Enumeration>>` | Fixed set of constants |
| `*--` | Composition (owns lifecycle) |
| `o--` | Aggregation (references) |
| `..>` | Dependency (uses) |
| `<\|..` | Implementation (implements) |
