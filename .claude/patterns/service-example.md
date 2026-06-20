# Reference: Service Layer

**File:** `src/main/java/com/croman/singlevendorecommerce/service/products/CategoryService.java`

**Why this file:** Most complete service in the project. Demonstrates: constructor injection, `@Transactional` / `readOnly`, soft-delete guard after `findById`, `ApiServiceException` with message keys, entity-to-DTO mapping in private helpers, `StorageService` + `ThumbnailService` integration for file upload, `PageResponse` construction.

**Key things to notice:**

- `@RequiredArgsConstructor` + `private final` fields — no `@Autowired`
- `@Transactional(readOnly = true)` on all read methods
- After `findById`, always check `if (category.getDeletedAt() != null)` and throw 404 — soft-deleted records still come back from `findById`
- Every error message goes through `messageService.getMessage(key, LocaleUtils.getDefaultLocale())` — never a raw Spanish string
- `ApiServiceException` accepts an optional `Map<String, Object> metadata` third argument for structured context (see `createCategoryDTO` when a name belongs to a soft-deleted category — returns `categoryId` so the frontend can offer a restore action)
- Entity→DTO mapping is done in private methods (`mapCategoryToDTO`, `mapCategoryToByIdDTO`) — never in the controller
- `PageResponse.<T>builder()` for paginated responses
- `PaginationUtils.getPageable(page, size, sortField)` for consistent Pageable construction
- File upload: delete old thumbnails, generate new UUID key, upload, then call `thumbnailService.generateThumbnails(key)`
