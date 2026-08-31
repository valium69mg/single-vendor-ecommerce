# CLAUDE.md — single-vendor-ecommerce-backend

## Commit conventions

Use conventional commits (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`, `docs:`). Never include references to AI tools, models, or companies in commit messages — no "Claude", "Anthropic", "Sonnet", "GPT", "Co-Authored-By AI", or any equivalent.

---

## Project overview

Backend REST API for a single-vendor jewelry e-commerce platform (Mexico only, MXN currency). Spring Boot 3.x + Java 21 monolith. Full requirements: `../requerimientos-ecommerce-joyeria.md`

Intelligence layer: see `.claude/` directory.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 (eclipse-temurin:21 in Docker) |
| Framework | Spring Boot 3.x (Web, Data JPA, Security, Validation, Mail) |
| Database | PostgreSQL 16 |
| ORM | Hibernate / Spring Data JPA |
| Migrations | Flyway — `src/main/resources/db/migration/` — **never modify existing files** |
| Image processing | Thumbnailator (synchronous, in-process) |
| Auth | JWT HS256, custom `JwtUtil` |
| API docs | Springdoc OpenAPI → `/swagger-ui.html` |
| Build | Maven → `target/single-vendor-ecommerce.jar` |
| Container | Docker + Docker Compose |

---

## Commands

```bash
# Infrastructure (Postgres + pgAdmin)
docker compose up postgres pgadmin -d

# Build
./mvnw clean package -DskipTests

# Run
java -jar target/single-vendor-ecommerce.jar
# or
docker compose up backend -d

# Full stack
docker compose up -d

# Tests
./mvnw test
```

Health: `GET http://localhost:8080/health`
Swagger: `http://localhost:8080/swagger-ui.html`

Setup: copy `.env.example` → `.env`.

---

## Security model

| Role | Access |
|---|---|
| `ADMIN` | `/api/v1/admin/**` |
| `USER` | Authenticated endpoints (JWT required) |
| `GUEST` | Public endpoints only |

**Public (no JWT):** `/health`, `/swagger-ui/**`, `/v3/api-docs/**`, `POST /api/v1/auth/login`, `POST /api/v1/users/register`, `POST /api/v1/users/register/admin`, `GET /api/v1/file/**`

Login lockout: 5 failed attempts in the last 1 hour → HTTP 423 Locked.

---

## What is implemented

- Auth: login, JWT, login-attempt tracking, account lockout
- Users: registration, role assignment
- Categories: CRUD + soft-delete + restore + image upload + thumbnails (admin)
- Materials, Brands: CRUD + soft-delete (admin)
- Attributes + AttributeValues: seeded (COLOR/SIZE/CARAT); create/update name (admin)
- Products: CRUD + soft-delete + restore + image upload (admin); public list + detail
- File storage: local disk + synchronous thumbnail generation (200×200, 400×400)
- Swagger/OpenAPI docs on all endpoints

## What is NOT yet implemented

- Auth: refresh tokens, logout, email verification, password recovery, OAuth, CAPTCHA
- User profile, addresses, wishlist
- Cart (authenticated + guest)
- Orders, checkout, payments (Stripe, PayPal)
- Inventory, shipping, coupons
- Reviews/ratings
- Admin dashboard KPIs and reports
- Email notifications (Brevo/Sendinblue)
- Returns/refunds

---

## Non-negotiable rules (apply every session)

1. **Flyway**: never edit existing `V<n>__*.sql` files — always create a new one. Current high-water mark: **V27**.
2. **No Spanish strings in Java**: all user-facing text through `MessageService` + `messages_es.properties`.
3. **No translation tables**: the translation subsystem was removed in V21. Store Spanish directly on entity columns.
4. **Soft-delete guard**: after `findById`, always check `deletedAt != null` before returning the entity.
5. **No try/catch in controllers**: let `GlobalExceptionHandler` handle all exceptions.
6. **Product PKs are UUID** (`UUID` type in Java), not `Long`. All other entity PKs are `Long`.

For conventions, patterns, and task how-tos — see `.claude/`.
