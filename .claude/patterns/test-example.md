# Reference: Unit Test

**File:** `src/test/java/com/croman/singlevendorecommerce/service/products/CategoryServiceTest.java`

**Why this file:** Largest and most thorough test in the project. Demonstrates: fixture setup with `@BeforeEach`, ASCII section dividers for readability, happy path + not-found + soft-deleted path coverage, `ArgumentCaptor` for asserting save arguments, `assertThatThrownBy` for exception assertions including metadata, mocking `MessageService` for error paths, `never().save()` for confirming no side effect on error paths, and file upload testing with `InputStream` mocking.

**Key things to notice:**

- `@ExtendWith(MockitoExtension.class)` — no Spring context loaded, tests are fast
- ASCII comment blocks (`// ─── methodName ─────`) group tests by method — keep this style
- Static `ENTITY_ID` and `NAME` constants as fixtures — no magic strings inside tests
- `@BeforeEach` builds entities with `.builder()` — not with `new`
- `MessageService` always mocked on error paths with `when(messageService.getMessage(eq("key"), any(Locale.class))).thenReturn("text")`
- `assertThatThrownBy` over `assertThrows` — provides fluent chaining
- `verify(repository, never()).save(any())` on every test where save must NOT happen
- Soft-delete path is tested separately from not-found path (`category.setDeletedAt(LocalDateTime.now())`)
- Upload test uses `ArgumentCaptor<String>` to capture the generated key and asserts UUID regex without caring about the specific value
- `assertDoesNotThrow` for the happy-path upload test instead of a try/catch
