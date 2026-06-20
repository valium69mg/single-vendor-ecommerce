# Conventions

## Constructor injection via Lombok @RequiredArgsConstructor
**Rule:** Never use `@Autowired` on fields. Declare dependencies as `private final` fields; `@RequiredArgsConstructor` generates the constructor.
**Why:** Field injection hides dependencies and makes unit tests harder to write (you must use reflection to inject mocks). Constructor injection is explicit and allows `@InjectMocks` in Mockito without reflection hacks.
**Example:** `private final CategoryRepository categoryRepository;` — no `@Autowired`.

## All domain errors thrown as ApiServiceException
**Rule:** Throw `new ApiServiceException(statusCode, messageService.getMessage(key, locale))` for every business-rule violation. Do not throw raw `RuntimeException` or Spring's `ResponseStatusException` from service classes.
**Why:** `GlobalExceptionHandler` catches `ApiServiceException` and shapes the error response consistently. Any other uncaught exception returns a 500.
**Example:** `throw new ApiServiceException(HttpStatus.NOT_FOUND.value(), messageService.getMessage("category_not_found", LocaleUtils.getDefaultLocale()));`

## Message keys, not raw strings
**Rule:** Every user-facing string (error message, success message) must come from `messages_es.properties` via `MessageService.getMessage(key, LocaleUtils.getDefaultLocale())`. Never hardcode Spanish text in Java.
**Why:** Centralizes all copy; see architecture.md.
**Example:** `messageService.getMessage("category_already_exists", LocaleUtils.getDefaultLocale())` ✅ — `"La categoría ya existe"` hardcoded ❌

## Mutation success responses via ApiResponseService
**Rule:** For create/update/delete endpoints, return `apiResponseService.getApiResponseMessage("key", HttpStatus.XXX)`. Do not construct `DefaultApiResponse` manually in controllers.
**Why:** Keeps response construction consistent and ensures message key lookup happens in one place.
**Example:** `return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseService.getApiResponseMessage("category_created", HttpStatus.CREATED));`

## No try/catch in controllers
**Rule:** Controllers must not catch exceptions. Let `GlobalExceptionHandler` handle all exceptions thrown by services.
**Why:** Controllers catching exceptions breaks the centralized error format and leads to inconsistent responses.
**Exception:** The only exception is service code that wraps checked exceptions (e.g., IOException from file uploads) — those are caught in the service and re-thrown as `ApiServiceException`.

## Paginated responses use PageResponse<T>
**Rule:** All paginated list endpoints return `PageResponse<SpecificDTO>`. Domain-specific page response classes (e.g., `CategoriesPageResponse`) exist only for Swagger schema documentation — they extend or annotate `PageResponse` but are never instantiated.
**Why:** A single generic type reduces code duplication. Swagger needs concrete types to generate accurate schema docs.

## @Transactional annotation placement
**Rule:** `@Transactional` goes on service methods, not repositories or controllers. Read-only operations use `@Transactional(readOnly = true)`.
**Why:** Services own business transactions. Controllers must not manage transactions. `readOnly = true` is a performance hint that prevents dirty checks on entities that should not change.

## Entity-to-DTO mapping in the service layer
**Rule:** Never expose JPA entities from controllers. Map entities to DTOs inside service methods using private `mapXxxToDTO()` helper methods.
**Why:** Prevents accidental lazy-load exceptions in the controller/serialization layer and decouples the API contract from the DB schema.

## Lombok on entities and DTOs
**Rule:** Use `@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor` on all entities and DTOs. Use `@Slf4j` on service classes that log.
**Why:** Eliminates boilerplate. `@Builder` is required so tests can construct objects with `.builder()...build()`.

## Test method naming: testVerbNounExpectation
**Rule:** Test method names follow `test` + camelCase description, e.g. `testGetCategoryByIdReturnsName`. Do not use underscores.
**Why:** Project convention derived from existing tests. Consistent naming makes test reports easier to read.
**Example:** `testCreateCategoryDTOThrowsWhenCategoryAlreadyExists` ✅ — `createCategory_throwsWhenExists()` ❌

## Repository custom queries use JPQL, not native SQL
**Rule:** Write custom repository queries in JPQL inside `@Query` annotations. Use native SQL only when JPQL cannot express the required query.
**Why:** JPQL is DB-agnostic and works with Hibernate's entity mapping. Native SQL bypasses type safety and entity mapping.
**Example:** `@Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")` ✅

## Commit message convention
**Rule:** Use conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `chore:`, `docs:`. Never reference AI tools, models, or companies in commit messages.
**Why:** Clean git history that describes work, not tooling. See CLAUDE.md for the full rule.
