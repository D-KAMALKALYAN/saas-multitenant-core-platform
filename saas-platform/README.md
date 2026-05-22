# SaaS Multi-Tenant Core Platform

> A production-grade multi-tenant SaaS infrastructure backend built with
> Spring Boot, PostgreSQL, and JWT authentication.
> Designed as a reusable backend framework that any SaaS product can build on top of.

---

## Architecture Overview

```
Client Request
      ↓
CorrelationIdFilter          → assigns request tracing ID
      ↓
JwtAuthenticationFilter      → validates JWT, sets TenantContext
      ↓
ApiKeyAuthenticationFilter   → validates API keys (machine clients)
      ↓
TenantResolutionFilter       → validates tenant from header (public endpoints)
      ↓
RateLimitFilter              → enforces per-tenant token bucket limits
      ↓
UsageTrackingFilter          → records API usage metrics async
      ↓
Spring Security (@PreAuthorize) → RBAC enforcement
      ↓
Controller → Service → Repository
      ↓
Database (PostgreSQL) — all queries scoped by tenant_id
```

---

## Features

| Category | Feature |
|----------|---------|
| Multi-Tenancy | Shared DB, shared schema with tenant_id isolation |
| Authentication | JWT access tokens + refresh tokens + logout |
| Machine Auth | API keys with SHA-256 hashing + Caffeine cache |
| Authorization | Role-based access control (SUPER_ADMIN, ADMIN, MEMBER, VIEWER) |
| Security | BCrypt passwords, security headers, HSTS, correlation IDs |
| Rate Limiting | Token bucket per tenant, plan-based limits (Basic/Pro/Enterprise) |
| Audit Logging | Async AOP-based audit trail, immutable, tenant-scoped |
| Observability | MDC structured logging, Spring Actuator, custom health indicators |
| Schema Management | Flyway versioned migrations, baseline strategy |
| Configuration | Spring Profiles (dev/prod), environment variable secrets |
| Tenant Config | Per-tenant key-value settings store |
| Usage Tracking | API usage metrics per tenant per day |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x |
| Language | Java 21 |
| Database | PostgreSQL 18 |
| Authentication | JWT (jjwt 0.12.x) + BCrypt |
| Cache | Caffeine |
| Rate Limiting | Bucket4j (Token Bucket) |
| Migrations | Flyway 11.x |
| Documentation | Swagger UI / OpenAPI 3.0 |
| Build | Maven |

---

## Quick Start

### Prerequisites
- Java 21+
- PostgreSQL 18+
- Maven 3.8+

### Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|----------|
| `SERVER_PORT` | Application server port | No | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | No | `dev` |
| `DB_URL` | PostgreSQL JDBC URL | Yes | — |
| `DB_USERNAME` | PostgreSQL username | Yes | — |
| `DB_PASSWORD` | PostgreSQL password | Yes | — |
| `JWT_SECRET` | JWT signing secret key | Yes | — |
| `JWT_ACCESS_EXPIRY` | JWT access token expiry (ms) | No | `3600000` |
| `JWT_REFRESH_EXPIRY` | JWT refresh token expiry (ms) | No | `2592000000` |
| `BCRYPT_STRENGTH` | BCrypt hashing strength | No | `10` |
| `RATE_LIMIT_BASIC` | Requests/min for Basic plan | No | `100` |
| `RATE_LIMIT_PRO` | Requests/min for Pro plan | No | `500` |
| `RATE_LIMIT_ENTERPRISE` | Requests/min for Enterprise plan | No | `2000` |
### Run Locally

```bash
export DB_URL=jdbc:postgresql://localhost:5432/saas_platform
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

mvn spring-boot:run
```

API docs available at: `http://localhost:8080/swagger-ui/index.html`

---

## API Overview

| Module | Endpoint |
|--------|-----------|
| Auth | `POST /api/v1/auth/login` |
| Auth | `POST /api/v1/auth/refresh` |
| Auth | `POST /api/v1/auth/logout` |
| Tenant | `POST /api/v1/tenants` |
| Tenant | `GET /api/v1/tenants` |
| Tenant | `PATCH /api/v1/tenants/{id}` |
| Tenant | `DELETE /api/v1/tenants/{id}` |
| User | `POST /api/v1/users` |
| User | `GET /api/v1/users` |
| User | `PATCH /api/v1/users/{id}` |
| User | `DELETE /api/v1/users/{id}` |
| API Keys | `POST /api/v1/apikeys` |
| API Keys | `GET /api/v1/apikeys` |
| API Keys | `DELETE /api/v1/apikeys/{id}` |
| Audit Logs | `GET /api/v1/audit-logs` |
| Tenant Config | `GET /api/v1/tenant-config` |
| Tenant Config | `PUT /api/v1/tenant-config/{key}` |
| Tenant Config | `DELETE /api/v1/tenant-config/{key}` |
| Usage Tracking | `GET /api/v1/usage` |
| Health | `GET /actuator/health` |

---

## Database Schema

```
tenants
│
├── users
│   └── refresh_tokens
│
├── api_keys
├── audit_logs
├── tenant_configs
└── usage_records
```

6 Flyway migrations — fully versioned and reproducible.

---

## Security Design

- Passwords hashed with BCrypt (cost factor 10)
- API keys stored as SHA-256 hash — never retrievable after creation
- JWT tokens are short-lived (1 hour) with revocable refresh tokens
- All queries tenant-scoped — cross-tenant data access architecturally impossible
- Rate limiting prevents abuse and noisy neighbor problems
- Security headers: X-Frame-Options, X-Content-Type-Options, HSTS