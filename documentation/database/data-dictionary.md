# Data Dictionary - BeeBoard Database

| Attribute | Value |
|-----------|-------|
| Schema Version | V19 |
| Last Updated | 2026-01-24 |
| Total Tables | 13 |

---

## Access BC Tables

### audit_log

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| id | UUID | NOT NULL | gen_random_uuid() | PK | Unique identifier | - |
| occurred_at | TIMESTAMPTZ | NOT NULL | NOW() | - | Event timestamp (with timezone) | - |
| actor_id | UUID | NOT NULL | - | - | User who performed action | - |
| actor_email | VARCHAR(255) | NOT NULL | - | - | Email of actor | - |
| actor_tier | VARCHAR(10) | NOT NULL | - | - | Security tier of actor | TIER_0, TIER_1, TIER_2 |
| action | VARCHAR(100) | NOT NULL | - | - | Action performed | - |
| target_type | VARCHAR(50) | NOT NULL | - | - | Type of resource affected | - |
| target_id | UUID | NULL | - | - | ID of affected resource | - |
| old_value | JSONB | NULL | - | - | State before change | JSON |
| new_value | JSONB | NULL | - | - | State after change | JSON |
| ip_address | VARCHAR(45) | NULL | - | - | Client IP address | IPv4/IPv6 |
| user_agent | TEXT | NULL | - | - | Client user agent string | - |

### password_reset_tokens

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| request_id | UUID | NOT NULL | gen_random_uuid() | PK | Unique identifier | - |
| user_id | UUID | NOT NULL | - | FK | Reference to users table | - |
| token_hash | VARCHAR(64) | NOT NULL | - | UK | SHA-256 hash of reset token | - |
| expires_at | TIMESTAMP | NOT NULL | - | - | Token expiration timestamp (15 min) | - |
| is_used | BOOLEAN | NOT NULL | false | - | Whether token has been used | - |
| created_at | TIMESTAMP | NOT NULL | - | - | Creation timestamp | - |
| updated_at | TIMESTAMP | NOT NULL | - | - | Last update timestamp | - |

### permission

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| permission_id | UUID | NOT NULL | gen_random_uuid() | PK | Unique identifier | - |
| code | VARCHAR(100) | NOT NULL | - | UK | Permission code (e.g., USER_CREATE) | - |
| name | VARCHAR(100) | NOT NULL | - | - | Human-readable name | - |
| description | VARCHAR(100) | NOT NULL | - | - | Permission description | - |
| tier | VARCHAR(10) | NOT NULL | TIER_2 | - | Minimum security tier required | TIER_0, TIER_1, TIER_2 |
| is_system | BOOLEAN | NOT NULL | false | - | System permission flag | - |

### rate_limit_password_reset

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| email | VARCHAR(255) | NOT NULL | - | PK | Email address | - |
| request_count | INTEGER | NOT NULL | 1 | - | Number of requests in current window | - |
| window_start | TIMESTAMP | NOT NULL | - | - | Start of current rate limit window | - |

### role

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| role_id | UUID | NOT NULL | gen_random_uuid() | PK | Unique identifier | - |
| code | VARCHAR(50) | NOT NULL | - | UK | Role code (e.g., SUPER_ADMIN) | - |
| name | VARCHAR(50) | NOT NULL | - | - | Human-readable name | - |
| tier | VARCHAR(10) | NOT NULL | TIER_2 | - | Security tier | TIER_0, TIER_1, TIER_2 |
| is_system | BOOLEAN | NOT NULL | false | - | System role flag | - |
| application_role | VARCHAR(20) | NOT NULL | - | - | Application-level role | ADMIN, SUPERVISOR, MANAGER, MEMBER, GUEST |
| created_at | TIMESTAMP | NOT NULL | - | - | Creation timestamp | - |
| updated_at | TIMESTAMP | NOT NULL | - | - | Last update timestamp | - |

### role_permission

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| role_id | UUID | NOT NULL | - | PK, FK | Role reference | - |
| permission_id | UUID | NOT NULL | - | PK, FK | Permission reference | - |

### users

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| user_id | UUID | NOT NULL | gen_random_uuid() | PK | Unique identifier | - |
| first_name | VARCHAR(100) | NOT NULL | - | - | User's first name | - |
| last_name | VARCHAR(100) | NOT NULL | - | - | User's last name | - |
| email | VARCHAR(255) | NOT NULL | - | UK | Email address | - |
| password_hash | VARCHAR | NOT NULL | - | - | BCrypt hashed password | - |
| contract_end_date | DATE | NOT NULL | - | - | Contract expiration date | - |
| is_locked | BOOLEAN | NOT NULL | - | - | Account lock status | - |
| last_activity_date | TIMESTAMP | NOT NULL | - | - | Last activity timestamp | - |
| role_id | UUID | NOT NULL | - | FK | Assigned role | - |
| created_at | TIMESTAMP | NOT NULL | - | - | Creation timestamp | - |
| updated_at | TIMESTAMP | NOT NULL | - | - | Last update timestamp | - |

---

## Event-Sourced View

### author_view

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| user_id | UUID | NOT NULL | - | PK | User identifier (matches Access BC) | - |
| access_level | VARCHAR(20) | NOT NULL | - | - | Note BC access level | SUPERVISOR, MODERATOR, COMMENTER, VIEWER |
| account_status | VARCHAR(20) | NOT NULL | - | - | Account status | ACTIVE, LOCKED, EXPIRED |
| full_name | VARCHAR(200) | NOT NULL | - | - | User's display name | - |
| created_at | TIMESTAMP | NOT NULL | NOW() | - | Creation timestamp | - |
| updated_at | TIMESTAMP | NOT NULL | NOW() | - | Last update timestamp | - |

---

## Note BC Tables

### comment

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| comment_id | UUID | NOT NULL | gen_random_uuid() | PK | Unique identifier | - |
| note_id | UUID | NOT NULL | - | FK | Parent note reference | - |
| author_id | UUID | NOT NULL | - | FK | Comment author reference | - |
| content | VARCHAR(5000) | NOT NULL | - | - | Comment content | - |
| parent_comment_id | UUID | NULL | - | FK | Parent comment (for threading) | - |
| solution_status | VARCHAR(20) | NOT NULL | REGULAR | - | Solution marker | REGULAR, PROPOSED, VALIDATED |
| moderation_status | VARCHAR(20) | NOT NULL | VISIBLE | - | Moderation state | VISIBLE, CENSORED, UNDER_REVIEW |
| created_at | TIMESTAMP | NOT NULL | NOW() | - | Creation timestamp | - |
| updated_at | TIMESTAMP | NOT NULL | NOW() | - | Last update timestamp | - |

### note

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| note_id | UUID | NOT NULL | gen_random_uuid() | PK | Unique identifier | - |
| title | VARCHAR(200) | NOT NULL | - | - | Note title | - |
| content | TEXT | NOT NULL | - | - | Note content | - |
| content_type | VARCHAR(20) | NOT NULL | TEXT | - | Content type | TEXT, CODE, IMAGE, MIXED |
| category | VARCHAR(50) | NOT NULL | - | - | Category | BUG, TIP, DOCUMENTATION, QUESTION, ANNOUNCEMENT, RECOMMENDATION, BUSINESS_RULE, CODE_SNIPPET |
| visibility | VARCHAR(20) | NOT NULL | PRIVATE | - | Visibility level | PRIVATE, SHARED, PROJECT |
| status | VARCHAR(20) | NOT NULL | ACTIVE | - | Note status | ACTIVE, ARCHIVED, DELETED |
| author_id | UUID | NOT NULL | - | FK | Note author reference | - |
| project_id | UUID | NULL | - | - | External project reference | - |
| created_at | TIMESTAMP | NOT NULL | NOW() | - | Creation timestamp | - |
| updated_at | TIMESTAMP | NOT NULL | NOW() | - | Last update timestamp | - |
| archived_at | TIMESTAMP | NULL | - | - | Archive timestamp | - |

### note_recipient

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| note_id | UUID | NOT NULL | - | PK, FK | Shared note reference | - |
| user_id | UUID | NOT NULL | - | PK, FK | Recipient user ID (FK to author_view) | - |
| shared_at | TIMESTAMP | NOT NULL | - | - | Share timestamp | - |

### note_tag

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| note_id | UUID | NOT NULL | - | PK, FK | Note reference | - |
| tag_id | UUID | NOT NULL | - | PK, FK | Tag reference | - |
| created_at | TIMESTAMP | NOT NULL | NOW() | - | Association timestamp | - |

### tag

| Column | Data Type | Null | Default | Key | Description | Valid Values |
|--------|-----------|------|---------|-----|-------------|--------------|
| tag_id | UUID | NOT NULL | gen_random_uuid() | PK | Unique identifier | - |
| name | VARCHAR(50) | NOT NULL | - | UK | Tag name | - |
| usage_count | INTEGER | NOT NULL | 0 | - | Number of notes using this tag | - |
| created_at | TIMESTAMP | NOT NULL | NOW() | - | Creation timestamp | - |
| updated_at | TIMESTAMP | NOT NULL | NOW() | - | Last update timestamp | - |
