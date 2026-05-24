# CLAUDE.md — single-vendor-ecommerce-backend

## Commit conventions

Never include references to Claude, Sonnet, Anthropic, or any AI model in commit messages (no `Co-Authored-By` lines, no model names).

---

## Project overview

Backend REST API for a single-vendor jewelry e-commerce platform (Mexico only, MXN currency). Built as a monolithic layered application using Spring Boot 3.x + Java 21. The full system also includes a React frontend and a separate thumbnail-worker microservice; this repo is the backend only.

Full requirements document: `../requerimientos-ecommerce-joyeria.md`

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 (eclipse-temurin:21 in Docker) |
| Framework | Spring Boot 3.x (Web, Data JPA, Security, Validation, Mail) |
| Database | PostgreSQL 16 |
| ORM | Hibernate / Spring Data JPA |
| Migrations | Flyway (`src/main/resources/db/migration/`) |
| Cache / queues | Redis 7 (Spring Data Redis) |
| Auth | JWT (HS256, custom `JwtUtil`) |
| API docs | Springdoc OpenAPI / Swagger UI (`/swagger-ui.html`) |
| Build | Maven (`target/single-vendor-ecommerce.jar`) |
| Containerization | Docker + Docker Compose |
| Thumbnail worker | Separate service (`carlostranquilinocr98/single-vendor-ecommerce-thumbnail-worker`) communicates via Redis |

---

## Running locally

### Prerequisites
- Docker + Docker Compose
- Java 21 + Maven (for building only)
- Copy `.env.example` → `.env` and adjust values

### Start infrastructure (Postgres, Redis, pgAdmin)
```bash
docker compose up postgres redis pgadmin -d
```

### Build and run the backend
```bash
./mvnw clean package -DskipTests
docker compose up backend -d
# OR run directly:
java -jar target/single-vendor-ecommerce.jar
```

### Full stack (including frontend and thumbnail worker)
```bash
docker compose up -d
```

### Health check
```
GET http://localhost:8080/health
```

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## Environment variables

Key vars (see `.env.example` for all):

| Variable | Description |
|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL connection |
| `REDIS_HOST` / `REDIS_PORT` | Redis connection |
| `JWT_SECRET` | HMAC secret for JWT signing |
| `JWT_EXPIRATION` | Token TTL in milliseconds (default 3600000 = 1h) |
| `FILE_DIRECTORY` | Absolute path on host for file storage (must match volume mount) |
| `CORS_ORIGIN` | Allowed CORS origin (e.g. `http://frontend:80`) |
| `SPRING_PROFILES_ACTIVE` | `dev` or `prod` |

---

## Project structure

```
src/main/java/com/croman/singlevendorecommerce/
├── config/          # Spring beans: SecurityConfig, RedisConfig, WebConfig, MessageConfig
├── controller/      # REST controllers grouped by domain
│   ├── auth/        # AuthController  → /api/v1/auth/
│   ├── products/    # ProductsController (public) + AdminProductsController (admin)
│   ├── storage/     # FileController  → /api/v1/file/
│   ├── users/       # UserController  → /api/v1/users/
│   └── health/      # HealthController → /health
├── dto/             # Request/Response DTOs (no domain logic)
├── entity/          # JPA entities grouped by domain
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic grouped by domain
└── utils/           # Cross-cutting: JWT, exceptions, pagination, file utils, etc.

src/main/resources/
├── application.yaml              # All config with env-var placeholders
├── db/migration/                 # Flyway scripts (V1__ … V19__)
├── messages.properties           # i18n keys (English base)
└── messages_es.properties        # i18n keys (Spanish)
```

---

## Architecture patterns

### Layered monolith
`Controller → Service → Repository → Entity`

- **Controllers** only handle HTTP (parsing, status codes, calling services). No business logic.
- **Services** contain all business logic and throw `ApiServiceException` for domain errors.
- **Repositories** are Spring Data JPA interfaces; custom queries use JPQL.
- **DTOs** are separate from entities; always map entities → DTOs in the service layer.

### API versioning
All endpoints are prefixed `/api/v1/`.

### Response format
Success responses return the DTO directly (no wrapper). Error responses go through `GlobalExceptionHandler`:
- `ApiServiceException` → `{ "status": <code>, "error": "<message>" }`
- `MethodArgumentNotValidException` → `{ "status": 400, "errors": { "<field>": "<msg>" } }`
- `ConstraintViolationException` → same shape as above

`DefaultApiResponse` (`{ status, message }`) is used for mutation operations (create/update/delete).

### Internationalization
All user-facing strings come from `MessageService` (delegates to Spring's `MessageSource`). Always use `LocaleUtils.getDefaultLocale()` (currently `es`). Never hardcode Spanish strings in Java code.

### File storage
`StorageService` (interface) → `LocalStorageService` (implementation). Files are stored at `FILE_DIRECTORY` on disk; the path is persisted in the DB. After upload, a Redis job is published via `ThumbnailJobPublisher` for the thumbnail-worker to process asynchronously.

### JWT authentication
`JwtUtil` signs/verifies tokens. `JwtAuthenticationFilter` extracts the token from the `Authorization: Bearer <token>` header and sets the `SecurityContext`. Roles come from the `user_roles` table and are used for Spring Security `hasRole()` checks.

---

## Security model

| Role | Description |
|---|---|
| `ADMIN` | Single administrator; accesses `/api/v1/admin/**` |
| `USER` | Registered and verified customer |
| `GUEST` | Unauthenticated user (cart managed client-side) |

Public (no JWT required):
- `/health`, `/swagger-ui/**`, `/v3/api-docs/**`
- `POST /api/v1/auth/login`
- `POST /api/v1/users/register`, `POST /api/v1/users/register/admin`
- `GET /api/v1/file/**`

Admin-only: `/api/v1/admin/**`

Everything else requires a valid JWT.

Login lockout: 5 failed attempts within the last 1 hour → HTTP 423 Locked.

---

## Database migrations (Flyway)

Migrations live in `src/main/resources/db/migration/` and run automatically on startup. **Never modify an existing migration.** Always create a new `V<n>__description.sql` file.

Current schema includes: `users`, `refresh_tokens`, `login_attempts`, `user_roles`, `languages`, `translations`, `categories`, `brands`, `materials`, `attributes`, `attribute_values`, `products`, `product_materials`, `product_variants`, `product_variant_attributes`.

Translation system: names for categories, materials, and attribute values are stored in the `translations` table with `(register_id, language_id, type)`. The `type` column is a `TranslatorPropertyType` enum value.

---

## Testing

Tests are in `src/test/java/` mirroring the main package structure. Run with:
```bash
./mvnw test
```

Tests use Mockito (`@ExtendWith(MockitoExtension.class)`). Integration tests are not yet written — only unit tests for services and utils. Target coverage: 70%.

---

## What is implemented

- Authentication: login with email/password, JWT issuance, login-attempt tracking, account lockout
- User registration (email/password), role assignment
- Categories: CRUD (admin), paginated list with search + translation, image upload with thumbnail queuing
- Materials: CRUD (admin), paginated list with search + translation
- Brands: CRUD (admin), paginated list with search
- Attributes & AttributeValues: read-only endpoints (SIZE, COLOR, CARAT seeded)
- Product entity + variants + product_materials schema (migrations done)
- File storage service (local disk) + thumbnail job publishing via Redis
- Swagger/OpenAPI documentation on all existing endpoints

## What is NOT yet implemented (pending per requirements)

- Product CRUD endpoints (entity/migration exists, no service/controller yet)
- Auth: refresh tokens, logout, email verification, password recovery, OAuth (Google/Facebook), CAPTCHA
- User profile management, addresses, wishlist
- Cart (authenticated + guest)
- Orders, checkout, payments (Stripe, PayPal)
- Inventory management
- Shipping management
- Coupons
- Reviews/ratings
- Search & filtering
- Admin dashboard KPIs and reports
- Email notifications (Brevo/Sendinblue)
- Returns/refunds

---

## Coding conventions

- Use Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@AllArgsConstructor`, `@NoArgsConstructor`) on DTOs and entities.
- Inject dependencies via constructor (Lombok `@RequiredArgsConstructor`), never `@Autowired` on fields.
- Throw `ApiServiceException(int statusCode, String message)` for all domain errors. Do not catch and re-wrap unless adding context.
- Use `ApiResponseService.getApiResponseMessage(String key, HttpStatus)` for mutation success responses.
- Paginated responses use the generic `PageResponse<T>` DTO; domain-specific page response classes exist for Swagger schema documentation only (e.g., `CategoriesPageResponse`).
- Controller methods return `ResponseEntity<T>` explicitly with the correct HTTP status.
- Do not add `try/catch` blocks in controllers; let `GlobalExceptionHandler` handle exceptions.
