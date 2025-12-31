# Password Reset Workflow

## 1. Overview

The password reset workflow is a secure two-phase process that allows users to regain access to their accounts when they've forgotten their password.

```mermaid
sequenceDiagram
    autonumber
    participant U as User
    participant FE as Frontend
    participant API as Beeboard API
    participant DB as PostgreSQL
    participant SMTP as Email Server

    rect rgb(46, 125, 50)
        Note over U,SMTP: Phase 1: Request Password Reset
        U->>FE: Click "Forgot Password"
        FE->>API: POST /password-reset/request
        API->>DB: Check rate limit
        API->>DB: Find user by email
        API->>DB: Invalidate old tokens
        API->>DB: Save new token (hashed)
        API->>SMTP: Send reset email
        SMTP-->>U: Email with reset link
        API-->>FE: 200 OK (always)
    end

    rect rgb(21, 101, 192)
        Note over U,SMTP: Phase 2: Complete Password Reset
        U->>FE: Click link in email
        FE->>API: POST /password-reset/complete
        API->>DB: Find token (by hash)
        API->>DB: Validate token
        API->>DB: Update password
        API->>DB: Mark token as used
        API-->>FE: 200 OK
        FE-->>U: Success message
    end
```

---

## 2. Phase 1: Request Password Reset

**Endpoint**: `POST /api/v1/auth/password-reset/request`

### Request Body

```json
{
    "email": "user@example.com"
}
```

### Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AuthResource
    participant RL as RateLimitService
    participant UC as RequestPasswordReset
    participant UR as UserRepository
    participant PRR as PasswordResetRepository
    participant ES as EmailService
    participant DB as PostgreSQL

    C->>API: POST /password-reset/request {email}
    API->>UC: execute(command)

    UC->>RL: isAllowed(email)
    RL->>DB: Check request count

    alt Rate limit exceeded (>3/hour)
        RL-->>UC: false
        UC-->>API: throw RateLimitException
        API-->>C: 429 Too Many Requests
    else Rate limit OK
        RL-->>UC: true

        UC->>UR: findByEmail(email)

        alt User not found OR User locked
            Note over UC: Silent fail (prevent enumeration)
            UC-->>API: return (no exception)
            API-->>C: 200 OK
        else User exists and active
            UC->>PRR: invalidateAllForUser(userId)

            UC->>UC: ResetToken.generate()
            Note over UC: 64 hex chars (256 bits)

            UC->>PRR: save(PasswordResetRequest)
            Note over PRR: Token stored as SHA-256 hash

            UC->>ES: sendPasswordResetEmail(user, token)
            ES->>SMTP: Send HTML + Text email

            UC->>RL: increment(email)

            UC-->>API: return
            API-->>C: 200 OK
        end
    end
```

### Rate Limiting Logic

```mermaid
flowchart TD
    A[Request Received] --> B{Check Rate Limit}
    B --> C[Query rate_limit_password_reset]
    C --> D{Window expired?}

    D -->|Yes| E[Reset counter to 0]
    E --> F{Count < 3?}

    D -->|No| F

    F -->|Yes| G[Allow Request]
    G --> H[Increment counter]
    H --> I[Process Request]

    F -->|No| J[Reject Request]
    J --> K[Return 429 Too Many Requests]

    style G fill:#c8e6c9,stroke:#2e7d32,color:#1b5e20
    style J fill:#ffcdd2,stroke:#c62828,color:#b71c1c
    style K fill:#ffcdd2,stroke:#c62828,color:#b71c1c
```

### Security Features

| Feature | Description |
|---------|-------------|
| User Enumeration Prevention | Always returns 200 OK regardless of user existence |
| Rate Limiting | Maximum 3 requests per email per hour |
| Token Invalidation | Previous tokens invalidated when new one is created |
| Silent Fail | Locked accounts receive no email (silent) |

---

## 3. Phase 2: Complete Password Reset

**Endpoint**: `POST /api/v1/auth/password-reset/complete`

### Request Body

```json
{
    "token": "a1b2c3d4e5f6...64_hex_characters",
    "newPassword": "MySecurePassword123!@#$%NewPass2024"
}
```

### Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AuthResource
    participant UC as CompletePasswordReset
    participant PRR as PasswordResetRepository
    participant UR as UserRepository
    participant DB as PostgreSQL

    C->>API: POST /password-reset/complete {token, newPassword}
    API->>UC: execute(command)

    UC->>UC: ResetToken.fromString(token)

    alt Invalid token format
        UC-->>API: throw InvalidTokenException
        API-->>C: 400 Bad Request
    else Valid format
        UC->>UC: token.hash()
        Note over UC: SHA-256 hash for lookup

        UC->>PRR: findByToken(hashedToken)

        alt Token not found
            UC-->>API: throw InvalidTokenException
            API-->>C: 400 Bad Request
        else Token found
            UC->>UC: request.canBeUsed()

            alt Token expired OR already used
                UC-->>API: throw TokenExpiredException
                API-->>C: 400 Bad Request
            else Token valid
                UC->>UR: findById(request.userId)

                alt User locked
                    UC-->>API: throw UserLockedException
                    API-->>C: 400 Bad Request
                else User active
                    UC->>UC: Password.fromPlainText(newPassword)
                    Note over UC: Validates: 32+ chars, digit, special char

                    UC->>UR: update(user with new password)
                    UC->>PRR: update(request marked as used)

                    UC-->>API: return
                    API-->>C: 200 OK
                end
            end
        end
    end
```

### Password Validation Rules

| Rule | Requirement |
|------|-------------|
| Minimum Length | 32 characters |
| Digit | At least one numeric character |
| Special Character | At least one special character |
| Hashing | BCrypt with cost factor |

---

## 4. Token Security

### Token Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Generated: User requests reset

    Generated --> Stored: SHA-256 hash saved to DB

    Stored --> Valid: Token exists and not expired
    Valid --> Used: User completes reset

    Stored --> Expired: 15 minutes elapsed
    Stored --> Invalidated: New token requested

    Used --> Cleaned: Hourly cleanup
    Expired --> Cleaned: Hourly cleanup
    Invalidated --> Cleaned: Hourly cleanup

    Cleaned --> [*]

    note right of Generated
        64 hex characters
        256 bits entropy
        SecureRandom
    end note

    note right of Stored
        Only hash stored
        Original token in email only
    end note

    note right of Valid
        15 minute window
        Single use only
    end note
```

### Token Generation & Storage

```mermaid
flowchart LR
    subgraph Generation["Token Generation"]
        A[SecureRandom] --> B[32 bytes]
        B --> C[Hex encode]
        C --> D[64 char token]
    end

    subgraph Storage["Database Storage"]
        D --> E[SHA-256 hash]
        E --> F[64 char hash]
        F --> G[(PostgreSQL)]
    end

    subgraph Email["Email Delivery"]
        D --> H[Reset Link]
        H --> I[User Email]
    end

    style D fill:#fff3e0,stroke:#ff9800,color:#e65100
    style F fill:#e3f2fd,stroke:#2196f3,color:#0d47a1
    style I fill:#e8f5e9,stroke:#4caf50,color:#1b5e20
```

### Security Properties

| Property | Value | Description |
|----------|-------|-------------|
| Entropy | 256 bits | Cryptographically secure random |
| Format | 64 hex chars | `^[a-f0-9]{64}$` |
| Storage | SHA-256 hash | Token never stored in plaintext |
| Validity | 15 minutes | Short-lived to minimize exposure |
| Usage | Single use | Marked as used after completion |
| Lookup | O(1) | Indexed hash lookup |

---

## 5. Email Template

### Reset Link Format

```
{FRONTEND_BASE_URL}/reset-password?token={64_char_hex_token}
```

### Email Structure

```mermaid
flowchart TB
    subgraph HTML["HTML Email"]
        H1[Header: Password Reset]
        H2[Quote: Dwight Schrute]
        H3[Greeting: Hello firstName]
        H4[Message: Reset instructions]
        H5[Button: Reset Password]
        H6[Warning: Expires in 15 min]
        H7[Security: Single use only]
        H8[Footer: Beeboard]

        H1 --> H2 --> H3 --> H4 --> H5 --> H6 --> H7 --> H8
    end

    subgraph Text["Plain Text Email"]
        T1[Same content]
        T2[Plain text format]
        T3[Direct link]
    end

    style H5 fill:#1976d2,stroke:#0d47a1,color:#ffffff
```

### Configuration

| Property | Environment Variable | Default |
|----------|---------------------|---------|
| Frontend URL | `FRONTEND_BASE_URL` | `http://localhost:3000` |
| Feature Toggle | `PASSWORD_RESET_ENABLED` | `true` |
| SMTP Host | - | `smtp.resend.com` |
| SMTP Port | - | `465` (SSL) |

---

## 6. Scheduled Maintenance

### Cleanup Service

```mermaid
flowchart TB
    subgraph Scheduler["Hourly Schedule"]
        S1[PasswordResetCleanupService]
    end

    subgraph Tasks["Cleanup Tasks"]
        T1[cleanupExpiredTokens]
        T2[cleanupExpiredRateLimits]
    end

    subgraph TokenCleanup["Token Cleanup"]
        TC1{Token expired?}
        TC2{Token used?}
        TC3[DELETE from DB]
    end

    subgraph RateLimitCleanup["Rate Limit Cleanup"]
        RC1{Window > 1 hour old?}
        RC2[DELETE from DB]
    end

    S1 --> T1 & T2

    T1 --> TC1
    TC1 -->|Yes| TC3
    TC1 -->|No| TC2
    TC2 -->|Yes| TC3

    T2 --> RC1
    RC1 -->|Yes| RC2

    style TC3 fill:#ffcdd2,stroke:#c62828,color:#b71c1c
    style RC2 fill:#ffcdd2,stroke:#c62828,color:#b71c1c
```

---

## 7. Component Architecture

### Class Diagram

```mermaid
classDiagram
    class PasswordResetRequest {
        -PasswordResetRequestId id
        -UserId userId
        -ResetToken token
        -Instant expiresAt
        -boolean isUsed
        -AuditInfo auditInfo
        +create(userId, token) PasswordResetRequest
        +isExpired() boolean
        +canBeUsed() boolean
        +markAsUsed() void
    }

    class ResetToken {
        -String value
        +generate() ResetToken
        +fromString(String) ResetToken
        +hash() String
        +toString() String
    }

    class RequestPasswordReset {
        -UserRepository userRepository
        -PasswordResetRequestRepository resetRepository
        -RateLimitService rateLimitService
        -EmailService emailService
        +execute(RequestPasswordResetCommand) void
    }

    class CompletePasswordReset {
        -UserRepository userRepository
        -PasswordResetRequestRepository resetRepository
        +execute(CompletePasswordResetCommand) void
    }

    class RateLimitService {
        -RateLimitRepository rateLimitRepository
        -int rateLimit
        -int windowHours
        +isRateLimitExceeded(String email) boolean
        +incrementRateLimit(String email) void
        +cleanupExpiredLimits() void
    }

    class RateLimit {
        -String email
        -int requestCount
        -Instant windowStart
        +firstRequest(email)$ RateLimit
        +isLimitExceeded(maxRequests) boolean
        +isWindowExpired(windowHours) boolean
        +increment() RateLimit
        +resetWindow() RateLimit
    }

    class RateLimitRepository {
        <<Interface>>
        +findByEmail(email) Optional~RateLimit~
        +save(RateLimit) void
        +deleteExpired(Instant) long
    }

    class EmailService {
        -Mailer mailer
        -String frontendBaseUrl
        +sendPasswordResetEmail(User, ResetToken) void
    }

    RequestPasswordReset --> PasswordResetRequest
    RequestPasswordReset --> ResetToken
    RequestPasswordReset --> RateLimitService
    RequestPasswordReset --> EmailService

    CompletePasswordReset --> PasswordResetRequest
    CompletePasswordReset --> ResetToken

    RateLimitService --> RateLimitRepository
    RateLimitService --> RateLimit
    RateLimitRepository --> RateLimit

    PasswordResetRequest --> ResetToken
```

### File Reference

| Layer | File | Responsibility |
|-------|------|----------------|
| **Exposition** | `AuthenticationRessource.java` | REST endpoints |
| **Application** | `RequestPasswordReset.java` | Request use case |
| **Application** | `CompletePasswordReset.java` | Complete use case |
| **Application** | `EmailService.java` | Email sending |
| **Application** | `RateLimitService.java` | Rate limiting |
| **Application** | `PasswordResetCleanupService.java` | Scheduled cleanup |
| **Domain** | `PasswordResetRequest.java` | Aggregate root |
| **Domain** | `ResetToken.java` | Token value object |
| **Domain** | `RateLimit.java` | Rate limit domain model |
| **Domain** | `RateLimitRepository.java` | Rate limit repository interface |
| **Infrastructure** | `PasswordResetRequestRepositoryImpl.java` | JPA persistence |
| **Infrastructure** | `PasswordResetRequestEntity.java` | JPA entity |
| **Infrastructure** | `RateLimitRepositoryImpl.java` | Rate limit JPA persistence |
| **Infrastructure** | `RateLimitEntity.java` | Rate limit JPA entity |
| **Infrastructure** | `RateLimitMapper.java` | Entity/Domain mapper |
| **Database** | `V6__create_password_reset_tokens_table.sql` | Schema migration |

---

## 8. Database Schema

### Tables

```mermaid
erDiagram
    password_reset_tokens {
        UUID request_id PK
        UUID user_id FK
        VARCHAR(64) token_hash UK
        TIMESTAMP expires_at
        BOOLEAN is_used
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    rate_limit_password_reset {
        VARCHAR(255) email PK
        INTEGER request_count
        TIMESTAMP window_start
    }

    users {
        UUID user_id PK
        VARCHAR email
        VARCHAR password_hash
    }

    password_reset_tokens ||--|| users : "belongs to"
```

### Indexes

| Table | Index | Columns | Purpose |
|-------|-------|---------|---------|
| `password_reset_tokens` | `idx_password_reset_user_id` | `user_id` | User lookup |
| `password_reset_tokens` | `idx_password_reset_token_hash` | `token_hash` | Token validation |
| `password_reset_tokens` | `idx_password_reset_expires_at` | `expires_at` | Cleanup queries |
| `password_reset_tokens` | `idx_password_reset_expired` | `expires_at, is_used` | Composite cleanup |
| `rate_limit_password_reset` | `idx_rate_limit_window` | `window_start` | Cleanup queries |

---

## 9. Error Handling

| Error | HTTP Code | Cause |
|-------|-----------|-------|
| Rate limit exceeded | 429 | More than 3 requests in 1 hour |
| Invalid token format | 400 | Token not 64 hex characters |
| Token not found | 400 | Token hash not in database |
| Token expired | 400 | Token older than 15 minutes |
| Token already used | 400 | Token marked as used |
| User locked | 400 | Target user account is locked |
| Invalid password | 400 | Password doesn't meet requirements |
