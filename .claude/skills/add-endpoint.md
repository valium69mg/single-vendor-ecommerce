# Skill: Add an API Endpoint

## Layer order
Controller → Service → Repository → Entity. Always work bottom-up: Entity/Repository first, then Service, then Controller.

## File locations

| Layer | Path pattern |
|---|---|
| Entity | `src/main/java/.../entity/<domain>/MyEntity.java` |
| Repository | `src/main/java/.../repository/<domain>/MyEntityRepository.java` |
| Service | `src/main/java/.../service/<domain>/MyEntityService.java` |
| DTO (request) | `src/main/java/.../dto/<domain>/CreateMyEntityDTO.java` |
| DTO (response) | `src/main/java/.../dto/<domain>/MyEntityDTO.java` |
| Controller | `src/main/java/.../controller/<domain>/AdminMyEntityController.java` (or `MyEntityController.java` for public) |

## Step-by-step

### 1. Create or update the DTO(s)
- Request DTOs: use `@NotBlank`, `@Size`, `@NotNull` for validation
- Response DTOs: use `@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`
- Add to `messages_es.properties` (and `messages.properties`) any new validation messages

### 2. Add repository method if needed
- Extend `JpaRepository<Entity, IdType>`
- Custom queries: use JPQL in `@Query` annotations, not native SQL
- Soft-delete filter example: `@Query("SELECT e FROM Entity e WHERE e.deletedAt IS NULL")`

### 3. Implement the service method
Key rules:
- Annotate with `@Transactional` (or `@Transactional(readOnly = true)` for reads)
- Fetch entities with `repository.findById(...).orElseThrow(() -> new ApiServiceException(404, messageService.getMessage("key", LocaleUtils.getDefaultLocale())))`
- Check `entity.getDeletedAt() != null` after `findById` to guard soft-deleted records
- Map entity → DTO inside the service, not the controller
- Throw `ApiServiceException(statusCode, messageService.getMessage(key, locale))` for all errors
- Never hardcode Spanish strings — use message keys

### 4. Add the controller method
Key rules:
- Return `ResponseEntity<T>` with explicit status
- Use `apiResponseService.getApiResponseMessage("key", HttpStatus.XXX)` for mutation responses
- Use `messageService.getMessage("key", LocaleUtils.getDefaultLocale())` only when building custom response bodies directly (e.g., image upload)
- No try/catch in controllers — let `GlobalExceptionHandler` handle exceptions
- Add `@Operation` with `@ApiResponse` annotations for each HTTP status the endpoint can return
- Admin endpoints go under `/api/v1/admin/` — Spring Security enforces `ADMIN` role for the entire `/api/v1/admin/**` path

### 5. Add message keys
Add to `src/main/resources/messages_es.properties`:
```properties
my_entity_created=Entidad creada exitosamente
my_entity_not_found=Entidad no encontrada
```
And the English fallback in `messages.properties`:
```properties
my_entity_created=Entity created successfully
my_entity_not_found=Entity not found
```

### 6. Register the bean (if new service)
Spring auto-detects `@Service` and `@RestController` — no manual bean registration needed.

## Response status conventions

| Action | HTTP status |
|---|---|
| GET (found) | 200 |
| POST (created) | 201 |
| PATCH / PUT | 200 |
| DELETE (no body) | 204 |
| Not found | 404 |
| Duplicate / bad input | 400 |
| Soft-deleted conflict | 409 |
| Auth failure | 401 / 423 |

## Swagger documentation
Every controller method needs:
```java
@Operation(summary = "Short description", responses = {
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MyDTO.class))),
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
})
```

## Before you commit
- [ ] Message keys added to both `messages_es.properties` and `messages.properties`
- [ ] Service method is `@Transactional` (or `readOnly = true` for reads)
- [ ] Controller returns correct HTTP status codes
- [ ] Soft-delete check present in service methods that use `findById`
- [ ] `@Operation` / `@ApiResponse` annotations on the controller method
- [ ] Unit test written for the new service method (see `skills/write-test.md`)
- [ ] Update `.claude/CODEBASE.md` routes table if it's a new endpoint
