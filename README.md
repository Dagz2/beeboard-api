# BeeBoard API

Identity & Access Management and note management system based on a Domain-Driven Design (DDD) architecture with Bounded Contexts.

## Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Practical Quarkus Commands](#practical-quarkus-commands)
- [Documentation Structure](#documentation-structure)
- [Deployment](#deployment)
- [Project Architecture](#project-architecture)
- [Database](#database)
- [Security](#security)
- [Useful Resources](#useful-resources)

---

## Project Overview

**BeeBoard API** is a comprehensive backend application providing authentication, authorization, and note management services.

### Bounded Contexts

The project is organized into two distinct Bounded Contexts following Domain-Driven Design principles:

#### Access BC (Access Management)
- User, role, and permission management
- JWT authentication with RSA 2048
- Immutable audit trail system
- Password reset with rate limiting
- CQRS: Read Model `author_view` to optimize queries

#### Note BC (Note Management)
- Complete CRUD on notes with lifecycle management
- Visibility system (PUBLIC, PRIVATE, SHARED)
- Comments with threading and replies
- Tags and categorization
- Note sharing with specific recipients

---

## Tech Stack

### Framework & Runtime
- **Quarkus:** 3.30.3
- **Java:** 21 (OpenJDK)
- **Maven:** Wrapper included (`./mvnw`)
- **Maven Compiler Plugin:** 3.14.0

### Persistence
- **PostgreSQL:** 16 (production), 18.1 (local development)
- **Hibernate ORM Panache:** Active Record pattern
- **Flyway:** Database migration management (19 migrations)
- **Agroal connection pool:**
  - Min: 5 connections
  - Max: 20 connections per replica

### Security
- **SmallRye JWT:** JWT management with RSA 2048
- **Hibernate Validator:** Bean validation
- **jBcrypt 0.4:** Secure password hashing
- **5-layer architecture:** Authentication → Authorization → Anti-Escalation → Business Logic → Traceability

### API & Documentation
- **SmallRye OpenAPI:** Automatic OpenAPI 3.1.0 generation
- **Swagger UI:** Interactive API documentation interface

### Communication & Services
- **Quarkus Mailer:** SMTP integration (Resend)
- **Quarkus Scheduler:** Scheduled task execution
- **Quarkus Cache (Caffeine):** Security context cache (15 min TTL, max 1000 entries)
- **REST Client Jackson:** Inter-service communication

### Tests
- **JUnit 5** with **Mockito**
- **Rest-Assured:** REST API testing
- **TestContainers PostgreSQL:** Isolated integration tests
- **AssertJ 3.26.3:** Fluent assertions
- **Awaitility 4.2.0:** Asynchronous testing
- **Bruno:** 27 E2E API tests

#### Test Pyramid
- **70% Unit** (~365 tests)
- **25% Integration** (~130 tests)
- **5% E2E** (27 Bruno tests)

---

## Prerequisites

### Local Development

- **Java:** JDK 21 or higher
- **Maven:** 3.8+ (or use the wrapper `./mvnw`)
- **PostgreSQL:** 16+ (or Docker/Podman container)
- **Docker/Podman:** (optional) for TestContainers and deployment

### Environment Variables

Create a `.env` file at the project root with the following variables:

```bash
DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_URL=jdbc:postgresql://localhost:5432/beeboard
RESEND_API_KEY=your_api_key
FRONTEND_BASE_URL=http://localhost:3000
```

See `documentation/installation/local_setup.md` for detailed configuration.

### Database

1. Create the `beeboard` database in PostgreSQL
2. Flyway migrations run automatically at startup
3. 19 migrations available in `src/main/resources/db/migration/`

---

## Practical Quarkus Commands

### Development

#### Start the project in dev mode (Live Reload)
```bash
./mvnw quarkus:dev
```

Access:
- **Application:** http://localhost:8080
- **Dev UI:** http://localhost:8080/q/dev/
- **Swagger UI:** http://localhost:8080/q/swagger-ui

#### Refresh OpenAPI documentation (swagger.json)
```bash
# The application must be running
curl -s -H "Accept: application/json" http://localhost:8080/q/openapi -o documentation/swagger.json
```

### Build & Packaging

#### Package the application (fast-jar)
```bash
./mvnw clean package
```

Execution:
```bash
java -jar target/quarkus-app/quarkus-run.jar
```

#### Package as über-jar (single JAR with all dependencies)
```bash
./mvnw clean package -Dquarkus.package.jar.type=uber-jar
```

#### Native build (GraalVM)
```bash
# With GraalVM installed locally
./mvnw package -Dnative

# Or in a container (without local GraalVM)
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

### Tests

#### Run all tests
```bash
./mvnw test
```

#### Run integration tests
```bash
./mvnw verify
```

#### E2E tests with Bruno
```bash
# Install Bruno CLI if needed
# Then run tests from the bruno/ folder
```

### Containerization

#### Build Docker/Podman image
```bash
# With Podman
podman build -t beeboard-api:latest -f Containerfile .

# With Docker
docker build -t beeboard-api:latest -f Containerfile .
```

#### Run the container
```bash
podman run -p 8080:8080 \
  -e DB_USERNAME=kode \
  -e DB_PASSWORD=yourpassword \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/beeboard \
  beeboard-api:latest
```

### Health Checks

#### Check application health
```bash
# General health
curl http://localhost:8080/q/health

# Liveness probe
curl http://localhost:8080/q/health/live

# Readiness probe
curl http://localhost:8080/q/health/ready
```

---

## Documentation Structure

The project has extensive documentation organized in the `documentation/` folder:

```
documentation/
├── database/
│   ├── data-dictionary.md      # Complete dictionary of 13 tables (V19)
│   └── schema/
│       └── erd.md              # Entity-Relationship Diagram
│
├── diagrams/
│   ├── class/
│   │   └── class-diagrams.md   # Class diagrams
│   ├── component/
│   │   └── component-diagram.md # Component architecture
│   ├── deployment/
│   │   └── deployment-diagram.md # Deployment architecture
│   ├── sequence/
│   │   └── sequence-diagrams.md # Sequence diagrams
│   └── usecase/
│       └── use-case-diagrams.md # Use case diagrams
│
├── guide_workflow/
│   ├── password-reset-workflow.md # Password reset workflow
│   └── testing-guide.md          # Complete testing guide (pyramid)
│
├── installation/
│   └── local_setup.md            # Local configuration (.env, etc.)
│
├── security/
│   └── security-architecture.md  # 5-layer security architecture
│                                 # Tiers, RBAC, anti-escalation
│
├── troubleshooting/
│   └── port_conflict.md          # Port conflict resolution
│
├── mockup/                       # UI mockups
│
└── swagger.json                  # OpenAPI 3.1.0 specification (116KB)
```

### Key Documents to Consult

- **security-architecture.md**: 5-layer security model, tiers, privilege escalation protection
- **data-dictionary.md**: Complete database schema with 13 tables
- **testing-guide.md**: Testing strategy (pyramid: 70% unit, 25% integration, 5% E2E)
- **swagger.json**: Automatically generated interactive API documentation

---

## Deployment

### OpenShift Deployment

Detailed instructions for OpenShift deployment are available in:

**`openshift/README.md`** (13.6 KB, documentation in French)

This folder contains **8 YAML deployment files**:
- `namespace.yaml`: Namespace `beeboard`
- `configmap.yaml`: Application configuration
- `secret.yaml`: Sensitive data (JWT keys, DB credentials, Resend API)
- `postgresql.yaml`: PostgreSQL 16 deployment
- `deployment.yaml`: Application deployment with init containers
- `service.yaml`: Kubernetes service
- `route.yaml`: OpenShift route for external access
- `README.md`: Complete deployment guide

#### Key Deployment Points

- **Base image:** Red Hat UBI 9 OpenJDK 21 Runtime (1.20)
- **Port:** 8080 (HTTP)
- **Non-root user:** UID 185 (OpenShift compatible)
- **Health checks:** Readiness & Liveness probes configured
- **Supported registries:** Quay.io, Docker Hub, OpenShift Internal Registry
- **Build:** Podman/Buildah recommended

#### Quick Deployment

```bash
# Refer to openshift/README.md for complete steps
# Build the image, push to registry, then:
oc apply -f openshift/
```

---

## Project Architecture

### Bounded Contexts

#### Access BC (`src/main/java/org/access/`)
- User, role, and permission management
- JWT authentication
- Immutable audit trail
- Password reset with rate limiting
- CQRS: `author_view` (read model)

#### Note BC (`src/main/java/org/note/`)
- Note management (CRUD, lifecycle, visibility)
- Comment system with threading
- Tags and categorization
- Note sharing

#### Shared Kernel (`src/main/java/org/shared/`)
- Shared infrastructure
- Domain events (CDI)
- Common utilities

### Main API Endpoints

- **Access Management:** `/api/v1/access/*`
- **Authentication:** `/api/v1/auth/*`
- **Notes:** `/api/v1/notes/*`
- **Comments:** `/api/v1/comments/*`
- **Tags:** `/api/v1/tags/*`
- **Internal API:** `/api/v1/internal/*` (inter-BC, without JWT)

---

## Database

**Type:** PostgreSQL 16+
**Schema Version:** V19 (Flyway)
**Tables:** 13 main tables

### Access BC
- `users`, `role`, `permission`, `role_permission`
- `password_reset_tokens`, `rate_limit_password_reset`
- `audit_log` (immutable)

### Note BC
- `note`, `comment`, `tag`, `note_tag`, `note_recipient`

### CQRS Read Model
- `author_view` (synchronized via CDI Events)

See `documentation/database/data-dictionary.md` for complete details.

---

## Security

### 5-Layer Model

1. **Authentication:** JWT validation (SmallRye JWT)
2. **Authorization:** RBAC with `@RequiresPermission`
3. **Anti-Escalation:** `PrivilegeEscalationGuard` validates sensitive operations
4. **Business Logic:** Domain constraints in use cases
5. **Traceability:** `AuditService` with immutable `audit_log` table

### Security Tiers

- **TIER_0:** Critical (SUPER_ADMIN)
- **TIER_1:** Administrative (DIRECTOR, HR_MANAGER)
- **TIER_2:** Operational (EMPLOYEE, SUPERVISOR)

See `documentation/security/security-architecture.md` for complete details.

---

## Useful Resources

- **Quarkus:** https://quarkus.io/
- **Quarkus Guides:** https://quarkus.io/guides/
- **SmallRye JWT:** https://quarkus.io/guides/security-jwt
- **Hibernate Panache:** https://quarkus.io/guides/hibernate-orm-panache
- **Flyway:** https://quarkus.io/guides/flyway
- **OpenShift:** https://docs.openshift.com/

---

**Version:** 1.0.0
**Last updated:** 2026-01-26
