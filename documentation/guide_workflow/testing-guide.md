# Testing Guide - BC Access

## Overview

This guide describes the testing strategy for the Access Bounded Context, following the test pyramid:

| Level | Percentage | Focus | Tests |
|-------|------------|-------|-------|
| **Unit Tests** | 70% | Domain + Application | ~365 |
| **Integration Tests** | 25% | Security, Controllers, Repositories | ~130 |
| **E2E Tests** | 5% | RBAC via Bruno | 27 |

## Test Structure

```
src/test/java/org/access/
├── domain/
│   ├── model/
│   │   ├── value/                          # Value Object tests
│   │   │   ├── EmailAddressTest.java
│   │   │   ├── PasswordTest.java
│   │   │   ├── FullNameTest.java
│   │   │   └── ContractEndDateTest.java
│   │   ├── SecurityTierTest.java           # Tier hierarchy tests
│   │   ├── SecurityContextTest.java        # Context tests
│   │   ├── UserTest.java                   # User aggregate
│   │   ├── RoleTest.java                   # Role entity
│   │   ├── PermissionTest.java             # Permission aggregate
│   │   └── PasswordResetRequestTest.java
│   └── service/
│       └── PrivilegeEscalationGuardTest.java  # CRITICAL
├── application/
│   ├── mapper/
│   │   ├── UserMapperTest.java
│   │   ├── RoleMapperTest.java
│   │   └── PermissionMapperTest.java
│   ├── usecase/
│   │   ├── command/
│   │   │   ├── auth/
│   │   │   │   ├── AuthenticateUserTest.java
│   │   │   │   ├── RequestPasswordResetTest.java
│   │   │   │   └── CompletePasswordResetTest.java
│   │   │   ├── user/
│   │   │   │   ├── CreateUserTest.java
│   │   │   │   ├── LockUserTest.java
│   │   │   │   ├── DeleteUserTest.java
│   │   │   │   └── AssignNewRoleTest.java
│   │   │   └── role/
│   │   │       ├── CreateRoleTest.java
│   │   │       └── GrantPermissionTest.java
│   │   └── query/
│   │       └── UserQueryServiceTest.java
│   └── service/
│       ├── RateLimitServiceTest.java
│       └── SecurityContextCacheIT.java     # Cache integration tests
├── infrastructure/
│   └── repository/
│       ├── UserRepositoryIT.java
│       ├── RoleRepositoryIT.java
│       └── PermissionRepositoryIT.java
├── exposition/
│   ├── filter/
│   │   └── UserStatusFilterIT.java         # User status filter tests
│   ├── security/
│   │   └── PermissionInterceptorIT.java    # Permission enforcement tests
│   ├── AuthenticationResourceIT.java       # Login & password reset tests
│   ├── UserManagementIT.java               # User CRUD tests
│   ├── RoleManagementIT.java               # Role management tests
│   ├── PermissionManagementIT.java         # Permission management tests
│   └── QueryEndpointsIT.java               # Query & /me endpoint tests
└── support/
    ├── TestFixtures.java                   # Builders for test data
    └── RestTestHelper.java                 # JWT token generation & REST utilities

bruno/                                       # E2E RBAC tests
├── environments/
│   ├── local.bru
│   └── test.bru
├── auth/                                    # 6 tests
│   ├── login-super-admin.bru
│   ├── login-director.bru
│   ├── login-hr-manager.bru
│   ├── login-invalid-credentials.bru
│   ├── login-locked-account.bru
│   └── password-reset-flow.bru
├── users/                                   # 7 tests
│   ├── create-user-authorized.bru
│   ├── create-user-unauthorized.bru
│   ├── list-users.bru
│   ├── update-user-self-forbidden.bru
│   ├── lock-unlock-flow.bru
│   ├── delete-user-must-be-locked.bru
│   └── privilege-escalation-blocked.bru
├── roles/                                   # 6 tests
│   ├── create-role.bru
│   ├── update-system-role-forbidden.bru
│   ├── delete-system-role-forbidden.bru
│   ├── grant-permission.bru
│   ├── revoke-permission.bru
│   └── tier-hierarchy-enforcement.bru
├── permissions/                             # 3 tests
│   ├── list-permissions.bru
│   ├── create-permission.bru
│   └── modify-system-permission-forbidden.bru
└── security/                                # 5 tests
    ├── expired-token.bru
    ├── missing-permission.bru
    ├── tier-escalation-attempts.bru
    ├── rate-limiting.bru
    └── get-current-user.bru
```

## Maven Commands

### Run all unit tests
```bash
mvn test
```

### Run tests for a specific package
```bash
# Domain Layer
mvn test -Dtest="org.access.domain.**"

# Application Layer
mvn test -Dtest="org.access.application.**"

# Specific Use Cases
mvn test -Dtest="org.access.application.usecase.command.auth.*Test"
mvn test -Dtest="org.access.application.usecase.command.user.*Test"
```

### Run integration tests
```bash
mvn verify -DskipITs=false

# Run specific integration test
mvn verify -Dit.test="AuthenticationResourceIT"
mvn verify -Dit.test="UserManagementIT"
```

### Generate coverage report (JaCoCo)
```bash
mvn verify jacoco:report
# Report available at target/site/jacoco/index.html
```

---

## Domain Layer Tests

### Value Objects
Value Objects are tested for:
- Input validation (null, empty, invalid format)
- Business behavior (comparisons, transformations)
- Immutability
- Value-based equality

**Example:**
```java
@Test
void shouldRejectInvalidEmailFormat() {
    assertThatThrownBy(() -> new EmailAddress("invalid"))
        .isInstanceOf(IllegalArgumentException.class);
}
```

### SecurityTier
Critical tests for privilege hierarchy:
- `isAtLeast()` - level comparison
- `isHigherThan()` - strict comparison
- `canManage()` - management validation

**Key Rule:** An actor can only manage strictly lower tiers.

### PrivilegeEscalationGuard (CRITICAL)
Most important service for security:
- `validateNotSelfModification()` - prevents self-modification
- `validateRoleAssignment()` - tier hierarchy
- `validatePermissionGrant()` - permission hierarchy
- `validateSystemRoleModification()` - TIER_0 protection
- `validateRoleDeletion()` - system role protection
- `validateUserManagement()` - user management

### Aggregates
Business behavior tests:
- **User**: lock/unlock, assignRole, isContractExpired, verifyPassword
- **Role**: grantPermission, revokePermission, hasPermission
- **Permission**: updateCode, updateName, tier classification
- **PasswordResetRequest**: isExpired, canBeUsed, markAsUsed (single-use)

---

## Application Layer Tests

### Mappers
Domain to DTO conversion:
- `UserMapperTest`: User to UserDto mapping
- `RoleMapperTest`: Role with permissions mapping
- `PermissionMapperTest`: Permission mapping

### Use Cases - Auth
| Use Case | Tests | Description |
|----------|-------|-------------|
| `AuthenticateUser` | 7 | Credentials, locked, expired contract |
| `RequestPasswordReset` | 6 | Rate limiting, email enumeration prevention |
| `CompletePasswordReset` | 7 | Token validation, expiration, single-use |

### Use Cases - User
| Use Case | Tests | Description |
|----------|-------|-------------|
| `CreateUser` | 4 | Validation, privilege escalation |
| `LockUser` | 5 | Self-lock prevention, tier hierarchy |
| `DeleteUser` | 6 | Must be locked first, tier validation |
| `AssignNewRole` | 7 | Self-modification, cache invalidation |

### Use Cases - Role
| Use Case | Tests | Description |
|----------|-------|-------------|
| `CreateRole` | 4 | Default tier, non-system |
| `GrantPermission` | 7 | System role protection, tier validation |

### Services
| Service | Tests | Description |
|---------|-------|-------------|
| `RateLimitService` | 8 | Sliding window, cleanup |
| `UserQueryService` | 11 | Find operations, contract status |

---

## Integration Tests (25%)

Integration tests verify interaction with the database via DevServices and REST endpoints via RestAssured.

### Security Layer Tests

#### UserStatusFilterIT
Tests the JAX-RS filter that validates user status on every request:
- `shouldAllowRequestForActiveUser` - Active user with valid contract
- `shouldReject403ForLockedUser` - Returns 403 for locked account
- `shouldReject403ForExpiredContract` - Returns 403 for expired contract
- `shouldBypassFilterForPublicEndpoints` - Login/password-reset not filtered
- `shouldHandleNonExistentUser` - User deleted after token issued

#### PermissionInterceptorIT
Tests the CDI interceptor that enforces @RequiresPermission:
- `shouldAllowWithRequiredPermission` - Single permission granted
- `shouldReject403WithoutPermission` - Missing permission
- `shouldAllowWithAnyPermission_OR_Logic` - At least one permission (OR)
- `shouldHandleTierBasedPermissions` - Tier-based permissions
- `shouldRejectInvalidToken` - Invalid token handling
- `shouldRejectExpiredToken` - Expired token handling

#### SecurityContextCacheIT
Tests the Caffeine cache for SecurityContext:
- `shouldCacheSecurityContext` - Cache loading
- `shouldReturnCachedContext` - Same instance from cache
- `shouldInvalidateOnExplicitCall` - Explicit invalidation
- `shouldInvalidateByRole` - Invalidate all users with role
- `shouldThrowForNonExistentUser` - Error handling

### Controller Integration Tests

#### AuthenticationResourceIT
Tests authentication endpoints (`/api/v1/auth`):

**Login Tests:**
- `shouldReturnJwtTokenForValidCredentials`
- `shouldReturn401ForInvalidPassword`
- `shouldReturn401ForNonExistentUser`
- `shouldReturn401ForLockedAccount`
- `shouldReturn401ForExpiredContract`

**Password Reset Tests:**
- `shouldReturn200ForExistingUser`
- `shouldReturn200ForNonExistentUser` (enumeration prevention)
- `shouldReturn429WhenRateLimitExceeded`
- `shouldReturn200WithValidToken`
- `shouldReturn400ForExpiredToken`
- `shouldReturn400ForAlreadyUsedToken`
- `shouldReturn400ForWeakPassword`

#### UserManagementIT
Tests user management endpoints (`/api/v1/access/users`):
- Create, lock/unlock, role assignment, update, delete operations
- Permission enforcement (USER_CREATE, USER_LOCK, USER_UPDATE, USER_DELETE)
- Self-modification prevention
- Tier hierarchy enforcement

#### RoleManagementIT
Tests role management endpoints (`/api/v1/access/roles`):
- Create, update, delete roles
- Grant/revoke permissions
- System role protection
- MANAGE_ROLES permission enforcement

#### PermissionManagementIT
Tests permission management endpoints (`/api/v1/access/permissions`):
- Create, update, delete permissions
- System permission protection
- MANAGE_PERMISSIONS permission enforcement

#### QueryEndpointsIT
Tests query endpoints:
- `/me` endpoint (current user profile, permissions, tier)
- User queries by ID and role
- 404 handling for non-existent resources

### Repository Tests
- `UserRepositoryIT` - User CRUD, queries, status persistence
- `RoleRepositoryIT` - Role CRUD, permission management
- `PermissionRepositoryIT` - Permission CRUD

### Running Integration Tests
```bash
# All integration tests
mvn verify -DskipITs=false

# Specific test class
mvn verify -Dit.test="AuthenticationResourceIT"
mvn verify -Dit.test="UserManagementIT"
mvn verify -Dit.test="RoleManagementIT"
```

### DevServices Configuration
Integration tests use `@QuarkusTest` with DevServices:
```java
@QuarkusTest
@DisplayName("Integration Tests")
class MyIntegrationIT {
    // DevServices auto-starts PostgreSQL
}
```

---

## E2E Tests with Bruno (5%)

### Installation
```bash
npm install -g @usebruno/cli
```

### Execution
```bash
# From the bruno/ folder
bru run --env local

# Run a specific folder
bru run auth/ --env local
bru run security/ --env local
```

### RBAC Scenarios Tested

| Scenario | File | Description |
|----------|------|-------------|
| Self-modification | `users/update-user-self-forbidden.bru` | User cannot modify their own role |
| Privilege escalation | `users/privilege-escalation-blocked.bru` | TIER_1 cannot assign TIER_0 |
| System role protection | `roles/delete-system-role-forbidden.bru` | System roles cannot be deleted |
| Permission check | `security/missing-permission.bru` | Endpoints protected by @RequiresPermission |
| Tier hierarchy | `roles/tier-hierarchy-enforcement.bru` | Tier hierarchy validation |
| Rate limiting | `security/rate-limiting.bru` | 3 requests/hour for password reset |

### Environment Configuration
Tokens are stored in environment variables after login:
- `superAdminToken` - TIER_0
- `directorToken` - TIER_1
- `hrManagerToken` - TIER_2

---

## TestFixtures

Using builders to create test data:

```java
import static org.access.support.TestFixtures.*;

// Create a user
User user = aUser()
    .withFullName("John", "Doe")
    .withEmail("john@example.com")
    .locked()
    .build();

// Create a SUPER_ADMIN role
Role superAdmin = aRole()
    .asSuperAdmin()
    .build();

// Create a security context
SecurityContext ctx = aSecurityContext()
    .asDirector()
    .withPermission("USER_CREATE")
    .build();

// Create a password reset request
PasswordResetRequest request = aPasswordResetRequest()
    .withUserId(userId)
    .expired()
    .build();
```

## RestTestHelper

Using helpers for REST integration tests:

```java
import static org.access.support.RestTestHelper.*;

// Generate JWT token
String token = aToken()
    .forUser(user.getId())
    .withEmail(user.getEmail().value())
    .withRole("DIRECTOR")
    .build();

// Use token in request
given()
    .header("Authorization", "Bearer " + token)
.when()
    .get(ACCESS_PATH + "/me")
.then()
    .statusCode(200);
```

---

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Unit Test | `*Test.java` | `UserTest.java` |
| Integration Test | `*IT.java` | `UserRepositoryIT.java` |
| Test Method | `should[Action][Condition]` | `shouldRejectNullEmail` |

---

## Adding New Tests

### 1. Unit Tests
```java
@DisplayName("My Component")
class MyComponentTest {

    @Nested
    @DisplayName("Feature X")
    class FeatureX {

        @Test
        @DisplayName("should do something when condition")
        void shouldDoSomethingWhenCondition() {
            // Given
            var input = ...;

            // When
            var result = ...;

            // Then
            assertThat(result).isEqualTo(expected);
        }
    }
}
```

### 2. Integration Tests
```java
@QuarkusTest
@DisplayName("MyResource Integration")
class MyResourceIT {

    @Inject
    UserRepository userRepository;

    @Test
    @Transactional
    @DisplayName("should perform operation successfully")
    void shouldPerformOperationSuccessfully() {
        // Given
        User user = aUser().build();
        userRepository.save(user);

        String token = aToken()
            .forUser(user.getId())
            .withEmail(user.getEmail().value())
            .withRole("TEST_ROLE")
            .build();

        // When/Then
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get(ACCESS_PATH + "/endpoint")
        .then()
            .statusCode(200);
    }
}
```

### 3. Bruno Tests
Create a `.bru` file in the appropriate folder:
```
meta {
  name: Test Name
  type: http
  seq: 1
}

post {
  url: {{baseUrl}}/api/{{apiVersion}}/endpoint
  body: json
  auth: bearer
}

auth:bearer {
  token: {{directorToken}}
}

body:json {
  { "key": "value" }
}

tests {
  test("should return expected status", function() {
    expect(res.status).to.equal(200);
  });
}
```

---

## Troubleshooting

### Tests failing due to database issues
```bash
# Verify PostgreSQL is running (DevServices handles this automatically)
# For manual setup:
docker compose up -d postgres

# Clean and re-run migrations
mvn flyway:clean flyway:migrate
```

### Slow tests
Password tests are slow due to BCrypt. This is expected.
For faster dev iteration, use a lower cost factor (not recommended for production).

### Bruno: expired token
Re-run the login requests in the `auth/` folder before other tests.

### DevServices: Docker not available
```bash
# Verify Docker is running
docker info

# Ensure user has permissions
sudo usermod -aG docker $USER
```

---

## Target Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Domain Coverage | > 90% | - |
| Application Coverage | > 80% | - |
| Global Coverage | > 75% | - |
| Unit Test Time | < 30s | ~15s |
| Integration Test Time | < 2min | - |
| Bruno E2E Tests | < 1min | - |
| Total Tests | ~520 | ~495+ |
