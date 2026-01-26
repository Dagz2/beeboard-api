# Use Case Diagrams - BeeBoard API

## 4. Use Case Diagram - Global View

```plantuml
@startuml
skinparam actorStyle awesome
skinparam defaultFontSize 12
skinparam packageStyle rectangle

' Actors with hierarchy (EXTERNAL to system)
actor "Anonymous User" as Anonymous
actor "Authenticated User" as Auth
actor "HR_MANAGER\n(TIER_2)" as HR #line:green;line.bold
actor "DIRECTOR\n(TIER_1)" as Dir #line:orange;line.bold
actor "SUPER_ADMIN\n(TIER_0)" as Super #line:red;line.bold

' Generalization relationships (3 levels justified by business architecture)
HR --|> Auth
Dir --|> HR
Super --|> Dir

note right of Super
  <b>Actor Inheritance Chain (3 levels)</b>
  Exceeds UML 2-level guideline but
  reflects business security tiers.

  TIER_0 > TIER_1 > TIER_2 > Base
end note

rectangle "BeeBoard API - Access Control System" <<System>> #ffffff {

  package "Authentication Package" #e8f5e9 {
    note as AuthNote
      Public authentication endpoints
      and profile management
      <b>4 use cases</b>
    end note
  }

  package "User Management Package" #e3f2fd {
    note as UserNote
      User lifecycle, self-service,
      and HR operations
      <b>12 use cases</b>
    end note
  }

  package "Role Management Package" #fff3e0 {
    note as RoleNote
      Role administration and
      permission assignment
      <b>7 use cases</b>
    end note
  }

  package "Permission & Audit Package" #fce4ec {
    note as PermNote
      Permission management and
      immutable audit trail
      <b>6 use cases</b>
    end note
  }
}

@enduml
```

## 5. Use Case Diagram - Authentication

```plantuml
@startuml
skinparam actorStyle awesome
skinparam defaultFontSize 12
skinparam defaultTextAlignment center

actor "Anonymous User" as Anonymous
actor "Authenticated User" as Auth

rectangle "BeeBoard API" <<Subsystem>> #ffffff {
  package "Authentication Package" #e8f5e9 {
    usecase "**UC-AUTH-01**\nAuthenticate User" as AUTH01 #e8f5e9
    usecase "**UC-AUTH-02**\nRequest Password Reset" as AUTH02 #e8f5e9
    usecase "**UC-AUTH-03**\nComplete Password Reset" as AUTH03 #e8f5e9
    usecase "**UC-AUTH-04**\nGet Current User Profile" as AUTH04 #e8f5e9
  }
}

Anonymous --> AUTH01
Anonymous --> AUTH02
Anonymous --> AUTH03
Auth --> AUTH04

note right of AUTH01
  All authenticated use cases
  implicitly require successful
  authentication (JWT validation).
end note

@enduml
```

## 6. Use Case Diagram - User Management

### 6.1 Use Case Diagram - User Management (Overview)

```plantuml
@startuml
skinparam actorStyle awesome
skinparam packageStyle rectangle
skinparam defaultFontSize 12

actor "Authenticated User" as Auth
actor "HR_MANAGER\n(TIER_2)" as HR
actor "DIRECTOR\n(TIER_1)" as Dir
actor "SUPER_ADMIN\n(TIER_0)" as Super

' Generalization relationships
HR --|> Auth
Dir --|> HR
Super --|> Dir

rectangle "BeeBoard API" <<Subsystem>> #ffffff {
  package "User Management Package" #ffffff {
    ' Self-Service Operations (Blue - Top)
    usecase "**UC-USER-10**\nChange Password" as USER10 #e3f2fd
    usecase "**UC-USER-11**\nChange Full Name" as USER11 #e3f2fd
    usecase "**UC-USER-12**\nChange Email" as USER12 #e3f2fd

    ' HR Operations (Green - Middle)
    usecase "**UC-USER-01**\nCreate User" as USER01 #c8e6c9
    usecase "**UC-USER-02**\nView Users" as USER02 #c8e6c9
    usecase "**UC-USER-03**\nView User Details" as USER03 #c8e6c9
    usecase "**UC-USER-04**\nAssign New Role" as USER04 #c8e6c9
    usecase "**UC-USER-05**\nLock User" as USER05 #c8e6c9
    usecase "**UC-USER-06**\nUnlock User" as USER06 #c8e6c9
    usecase "**UC-USER-08**\nExtend Contract" as USER08 #c8e6c9

    ' Business Operations (Orange - Bottom)
    usecase "**UC-USER-07**\nDelete User" as USER07 #ffe0b2
    usecase "**UC-USER-09**\nTerminate Contract" as USER09 #ffe0b2
  }
}

' Associations Self-Service
Auth --> USER10
Auth --> USER11
Auth --> USER12

' Associations HR Operations
HR --> USER01
HR --> USER02
HR --> USER03
HR --> USER04
HR --> USER05
HR --> USER06
HR --> USER08

' Associations Business Operations
Dir --> USER07
Dir --> USER09

' Relations <<include>> (Critical security dependencies)
USER07 ..> USER05 : <<include>>
USER09 ..> USER05 : <<include>>

note bottom of USER12
  <b>Navigation:</b>
  Voir sections 6.2-6.3 pour les contraintes de sécurité
  et règles d'autorisation détaillées.
end note

@enduml
```

### 6.2 Use Case Diagram - User Administration (Detail)

```plantuml
@startuml
skinparam actorStyle awesome
skinparam packageStyle rectangle
skinparam defaultFontSize 12

actor "HR_MANAGER\n(TIER_2)" as HR
actor "DIRECTOR\n(TIER_1)" as Dir
actor "SUPER_ADMIN\n(TIER_0)" as Super

' Generalization relationships
Dir --|> HR
Super --|> Dir

rectangle "BeeBoard API" <<Subsystem>> #ffffff {
  package "User Administration Package" {

    package "HR Operations (TIER_2+)" #c8e6c9 {
      usecase "**UC-USER-01**\nCreate User" as USER01 #c8e6c9
      usecase "**UC-USER-02**\nView Users" as USER02 #c8e6c9
      usecase "**UC-USER-03**\nView User Details" as USER03 #c8e6c9
      usecase "**UC-USER-04**\nAssign New Role" as USER04 #c8e6c9
      usecase "**UC-USER-05**\nLock User" as USER05 #c8e6c9
      usecase "**UC-USER-06**\nUnlock User" as USER06 #c8e6c9
      usecase "**UC-USER-08**\nExtend Contract" as USER08 #c8e6c9
    }

    package "Business Operations (TIER_1+)" #ffe0b2 {
      usecase "**UC-USER-07**\nDelete User" as USER07 #ffe0b2
      usecase "**UC-USER-09**\nTerminate Contract" as USER09 #ffe0b2
    }
  }
}

' Associations HR Operations
HR --> USER01
HR --> USER02
HR --> USER03
HR --> USER04
HR --> USER05
HR --> USER06
HR --> USER08

' Associations Business Operations
Dir --> USER07
Dir --> USER09

' Relations <<include>>
USER07 ..> USER05 : <<include>>
USER09 ..> USER05 : <<include>>

note right of USER07
  <b>Mandatory Precondition:</b>
  Users must be locked before deletion.
  The <<include>> enforces this safety rule.
end note

note bottom of USER04
  <b>PrivilegeEscalationGuard Enforcement:</b>
  1. No self-modification (cannot change own role)
  2. Tier validation (TIER_2 can only assign TIER_2 roles)
  3. System resource protection
end note

note bottom of Dir
  Higher-tier actors (SUPER_ADMIN) inherit
  all capabilities from lower tiers.
  Diagram shows only direct associations.
end note

@enduml
```

### 6.3 Use Case Diagram - User Self-Service (Detail)

```plantuml
@startuml
skinparam actorStyle awesome
skinparam packageStyle rectangle
skinparam defaultFontSize 12

actor "Authenticated User" as Auth
actor "HR_MANAGER\n(TIER_2)" as HR
actor "DIRECTOR\n(TIER_1)" as Dir
actor "SUPER_ADMIN\n(TIER_0)" as Super

' Generalization relationships (showing all tiers inherit self-service)
HR --|> Auth
Dir --|> HR
Super --|> Dir

rectangle "BeeBoard API" <<Subsystem>> #ffffff {
  package "Self-Service Operations" #e3f2fd {
    usecase "**UC-USER-10**\nChange Password" as USER10 #e3f2fd
    usecase "**UC-USER-11**\nChange Full Name" as USER11 #e3f2fd
    usecase "**UC-USER-12**\nChange Email" as USER12 #e3f2fd
  }
}

' Association unique: Auth → tous les UC
Auth --> USER10
Auth --> USER11
Auth --> USER12

note right of USER10
  <b>Password Policy Validation:</b>
  - Minimum 32 characters
  - At least 1 digit
  - At least 1 special character
  - No reuse of last 5 passwords
end note

note bottom of Auth
  <b>No special permissions required.</b>
  All authenticated users can manage
  their own profile data.
  Self-modification of role/tier is FORBIDDEN.
end note

@enduml
```

## 7. Use Case Diagram - Role Management

```plantuml
@startuml
skinparam actorStyle awesome

actor "HR_MANAGER\n(TIER_2)" as HR
actor "DIRECTOR\n(TIER_1)" as Dir
actor "SUPER_ADMIN\n(TIER_0)" as Super

' Generalization relationships
Dir --|> HR
Super --|> Dir

rectangle "BeeBoard API" <<Subsystem>> #ffffff {
  package "Role Management Package" {

    package "View Operations (TIER_2+)" #c8e6c9 {
      usecase "**UC-ROLE-02**\nView Roles" as ROLE02 #c8e6c9
      usecase "**UC-ROLE-03**\nView Role Details" as ROLE03 #c8e6c9
    }

    package "Standard Management (TIER_1+)" #ffe0b2 {
      usecase "**UC-ROLE-01**\nCreate Role" as ROLE01 #ffe0b2
      usecase "**UC-ROLE-04**\nUpdate Role" as ROLE04 #ffe0b2
    }

    package "Permission Assignment (TIER_0)" #ffcdd2 {
      usecase "**UC-ROLE-06**\nGrant Permission" as ROLE06 #ffcdd2
      usecase "**UC-ROLE-07**\nRevoke Permission" as ROLE07 #ffcdd2
    }

    package "System Operations (TIER_0)" #ffcdd2 {
      usecase "**UC-ROLE-05**\nDelete Role" as ROLE05 #ffcdd2
    }
  }
}

HR --> ROLE02
HR --> ROLE03

Dir --> ROLE01
Dir --> ROLE04

Super --> ROLE05
Super --> ROLE06
Super --> ROLE07

' Relations <<include>> (CORRECT: Base → Included)
ROLE06 ..> ROLE03 : <<include>>
ROLE07 ..> ROLE03 : <<include>>

note right of ROLE06
  <b>Include Rationale:</b>
  Permission changes require viewing
  current role state to prevent
  accidental misconfigurations.
end note

@enduml
```

## 8. Use Case Diagram - Permission & Audit

```plantuml
@startuml
skinparam actorStyle awesome

actor "DIRECTOR\n(TIER_1)" as Dir
actor "SUPER_ADMIN\n(TIER_0)" as Super

' Generalization relationship
Super --|> Dir

rectangle "BeeBoard API" <<Subsystem>> #ffffff {
  package "Permission & Audit Package" {

    package "View Operations (TIER_1+)" #ffe0b2 {
      usecase "**UC-PERM-02**\nView Permissions" as PERM02 #ffe0b2
      usecase "**UC-PERM-03**\nView Permission Details" as PERM03 #ffe0b2
    }

    package "Permission Management (TIER_0)" #ffcdd2 {
      usecase "**UC-PERM-01**\nCreate Permission" as PERM01 #ffcdd2
      usecase "**UC-PERM-04**\nUpdate Permission" as PERM04 #ffcdd2
      usecase "**UC-PERM-05**\nDelete Permission" as PERM05 #ffcdd2
    }

    package "Audit Operations (TIER_0)" #ffcdd2 {
      usecase "**UC-AUDIT-01**\nView Audit Logs" as AUDIT01 #ffcdd2
    }
  }
}

Dir --> PERM02
Dir --> PERM03

Super --> PERM01
Super --> PERM04
Super --> PERM05
Super --> AUDIT01

note bottom of AUDIT01
  <b>Critical Security:</b>
  Audit logs are immutable and contain
  sensitive security information.
  TIER_0 access only.
end note

@enduml
```
