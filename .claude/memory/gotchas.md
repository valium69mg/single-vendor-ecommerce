# Gotchas

## V21 removed the translation subsystem — do not reference it
**What happened:** V9–V12 created `languages` and `translations` tables with a `TranslatorPropertyType` enum and supporting services. V21 removed them all. Any reference to `TranslationService`, `LanguageService`, `Language` entity, or `translations` table in new code will cause a compile/runtime error.
**Root cause:** The translation system was overkill for a Spanish-only store.
**How to avoid:** Store Spanish names directly on each entity's own column. See `memory/architecture.md` — "Spanish-only content".

## Flyway checksums block startup if you edit existing migrations
**What happened:** Modifying any file under `db/migration/V<n>__*.sql` that Flyway has already applied causes a checksum mismatch and Flyway refuses to start the application.
**Root cause:** Flyway records a checksum for each applied migration. Changes after application are detected as corruption.
**How to avoid:** Always create a new `V<n+1>__description.sql`. Current high-water mark: V28. Next migration is V29.

## Product PKs are UUID, not Long — don't mix types
**What happened:** Product-related path variables and service method signatures use `UUID`, not `Long`. Passing a `Long` category ID path variable pattern into product methods or vice versa causes type mismatch at runtime.
**Root cause:** Product entity uses `@GeneratedValue` with UUID strategy; all other entities use `BIGSERIAL`.
**How to avoid:** Check entity PK type before writing service/controller signatures. `@PathVariable UUID productId` for products, `@PathVariable long id` for everything else.

## Soft-deleted records still match findById — always check deletedAt
**What happened:** `categoryRepository.findById(id)` returns soft-deleted categories. If the controller response doesn't check `deletedAt`, it will expose deleted records.
**Root cause:** `findById` has no filter. Only named queries like `findAllNotDeleted` apply the soft-delete filter.
**How to avoid:** After `findById`, always check `if (entity.getDeletedAt() != null) throw ApiServiceException(404, ...)`. See `CategoryService.getCategoryById()` for the reference pattern.

## uploadImage catches Exception too broadly — rethrows as 400
**What happened:** `CategoryService.uploadImage` wraps the entire upload logic in a `try/catch(Exception e)` that converts any error (including 404 for category not found) to a generic 400 Bad Request with "image_upload_failed" message.
**Root cause:** The ApiServiceException thrown for "category not found" is caught by the outer catch block before it can propagate.
**How to avoid:** When reading this code, know the 404 case is swallowed. If refactoring upload logic, either narrow the catch or rethrow `ApiServiceException` before it reaches the outer catch.

## FileUtils thumbnail key methods return null-safe paths
**What happened:** `FileUtils.toSmallThumbnailKey(null)` and `toMediumThumbnailKey(null)` handle null gracefully (return null), which is safe for `storageService.delete(null)`. But constructing a thumbnail URL from a null fileUrl in a DTO mapper without null-checking will produce a null field in the API response.
**Root cause:** Categories don't require an image; `file_url` is nullable.
**How to avoid:** In DTO mappers that produce image URLs, either accept null (frontend must handle) or default to a placeholder URL. The current code returns null — do not change without updating the frontend.
