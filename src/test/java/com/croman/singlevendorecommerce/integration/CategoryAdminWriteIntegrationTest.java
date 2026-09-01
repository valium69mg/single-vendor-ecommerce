package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.integration.support.CategoryFixtures;
import com.croman.singlevendorecommerce.integration.support.CategoryFixtures.SeededCategoryWithProduct;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end coverage of the admin category write surface
 * ({@code POST/PATCH/DELETE /api/v1/admin/products/categories}) on
 * {@code AdminProductsController} -&gt; {@code CategoryService}, through the real
 * Spring Security filter chain (ADMIN JWT) and a real PostgreSQL Testcontainer.
 *
 * <p>Class-level {@link Transactional}: each {@code @Test} runs in a transaction
 * rolled back at method end, so the {@code @BeforeEach} user + category fixtures
 * never leak into sibling integration tests sharing the singleton container. The
 * {@code categories} table always holds 9 Flyway-seeded rows, so every count /
 * list assertion is relative to {@link #preSeedCount} captured in
 * {@code @BeforeEach}.
 */
@Transactional
class CategoryAdminWriteIntegrationTest extends AbstractIntegrationTest {

	private static final String BASE_URL = "/api/v1/admin/products/categories";
	private static final String ADMIN_EMAIL = "cat-admin-it@test.com";
	private static final String USER_EMAIL = "cat-user-it@test.com";
	private static final String PASSWORD = "correct-horse-battery";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private MessageService messageService;

	private String adminToken;
	private String userToken;
	private long preSeedCount;

	@BeforeEach
	void setUp() {
		AuthFixtures.seedUser(userRepository, userRoleRepository, ADMIN_EMAIL, PASSWORD, RoleType.ADMIN);
		AuthFixtures.seedUser(userRepository, userRoleRepository, USER_EMAIL, PASSWORD, RoleType.USER);
		adminToken = jwtUtil.generateToken(ADMIN_EMAIL, "ADMIN");
		userToken = jwtUtil.generateToken(USER_EMAIL, "USER");
		preSeedCount = categoryRepository.count();
	}

	private static String nameBody(String rawJsonNameValue) {
		return "{\"name\":" + rawJsonNameValue + "}";
	}

	private String message(String key) {
		return messageService.getMessage(key, Locale.of("es"));
	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	/** Names present in the active admin category list (relative to the 9 seeded rows). */
	private List<String> listCategoryNames() throws Exception {
		MvcResult result = mockMvc.perform(get(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.param("size", "500"))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode content = objectMapper.readTree(result.getResponse().getContentAsString()).get("content");
		List<String> names = new ArrayList<>();
		content.forEach(node -> names.add(node.get("name").asText()));
		return names;
	}

	// ---------------------------------------------------------------------
	// Create-name validation (Phase 2)
	// ---------------------------------------------------------------------

	@Test
	void createWithBlankNameReturns400AndCreatesNothing() throws Exception {
		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"   \"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists());

		assertThat(categoryRepository.count()).isEqualTo(preSeedCount);
	}

	@Test
	void createWithEmptyNameReturns400AndCreatesNothing() throws Exception {
		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists());

		assertThat(categoryRepository.count()).isEqualTo(preSeedCount);
	}

	@Test
	void createWithMissingNameReturns400AndCreatesNothing() throws Exception {
		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists());

		assertThat(categoryRepository.count()).isEqualTo(preSeedCount);
	}

	@Test
	void createWithTooShortNameReturns400AndCreatesNothing() throws Exception {
		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"ab\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists());

		assertThat(categoryRepository.count()).isEqualTo(preSeedCount);
	}

	@Test
	void createWithTooLongNameReturns400AndCreatesNothing() throws Exception {
		String sixtyOneChars = "a".repeat(61);

		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"" + sixtyOneChars + "\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists());

		assertThat(categoryRepository.count()).isEqualTo(preSeedCount);
	}

	// ---------------------------------------------------------------------
	// Update-name validation (Phase 3)
	// ---------------------------------------------------------------------

	@Test
	void updateWithBlankNameReturns400AndLeavesNameUnchanged() throws Exception {
		Category target = CategoryFixtures.seedCategory(categoryRepository, "IT Update Blank Target");

		mockMvc.perform(patch(BASE_URL + "/" + target.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"   \"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists());

		assertThat(categoryRepository.findById(target.getCategoryId()).orElseThrow().getName())
				.isEqualTo("IT Update Blank Target");
	}

	@Test
	void updateWithTooShortNameReturns400AndLeavesNameUnchanged() throws Exception {
		Category target = CategoryFixtures.seedCategory(categoryRepository, "IT Update Short Target");

		mockMvc.perform(patch(BASE_URL + "/" + target.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"ab\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists());

		assertThat(categoryRepository.findById(target.getCategoryId()).orElseThrow().getName())
				.isEqualTo("IT Update Short Target");
	}

	@Test
	void updateWithTooLongNameReturns400AndLeavesNameUnchanged() throws Exception {
		Category target = CategoryFixtures.seedCategory(categoryRepository, "IT Update Long Target");
		String sixtyOneChars = "a".repeat(61);

		mockMvc.perform(patch(BASE_URL + "/" + target.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"" + sixtyOneChars + "\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists());

		assertThat(categoryRepository.findById(target.getCategoryId()).orElseThrow().getName())
				.isEqualTo("IT Update Long Target");
	}

	// ---------------------------------------------------------------------
	// Happy CRUD characterization (Phase 5.1)
	// ---------------------------------------------------------------------

	@Test
	void createValidUniqueNameReturns201AndAppearsInList() throws Exception {
		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"IT Happy Create\"")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value(201))
				.andExpect(jsonPath("$.message").value(message("category_created")));

		assertThat(categoryRepository.count()).isEqualTo(preSeedCount + 1);
		assertThat(listCategoryNames()).contains("IT Happy Create");
	}

	@Test
	void updateValidUniqueNameReturns200AndSubsequentGetReflectsIt() throws Exception {
		Category target = CategoryFixtures.seedCategory(categoryRepository, "IT Happy Update Before");

		mockMvc.perform(patch(BASE_URL + "/" + target.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"IT Happy Update After\"")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value(message("category_updated")));

		mockMvc.perform(get(BASE_URL + "/" + target.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("IT Happy Update After"));
	}

	@Test
	void deleteReturns204ThenGetReturns404AndCategoryAbsentFromList() throws Exception {
		Category target = CategoryFixtures.seedCategory(categoryRepository, "IT Happy Delete");

		mockMvc.perform(delete(BASE_URL + "/" + target.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(BASE_URL + "/" + target.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));

		assertThat(listCategoryNames()).doesNotContain("IT Happy Delete");
	}

	// ---------------------------------------------------------------------
	// Duplicate + not-found characterization (Phase 5.2)
	// ---------------------------------------------------------------------

	@Test
	void createDuplicateOfActiveNameReturns400() throws Exception {
		CategoryFixtures.seedCategory(categoryRepository, "IT Dup Active");

		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"it dup active\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));

		assertThat(categoryRepository.count()).isEqualTo(preSeedCount + 1);
	}

	@Test
	void createDuplicateOfSoftDeletedNameReturns409WithCategoryId() throws Exception {
		Category deleted = CategoryFixtures.seedSoftDeletedCategory(categoryRepository, "IT Dup Deleted");

		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"it dup deleted\"")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.categoryId").value(deleted.getCategoryId()));
	}

	@Test
	void updateUnknownIdReturns404() throws Exception {
		mockMvc.perform(patch(BASE_URL + "/999999")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"IT Unknown Update\"")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void deleteUnknownIdReturns404() throws Exception {
		mockMvc.perform(delete(BASE_URL + "/999999")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	// ---------------------------------------------------------------------
	// Delete a category that still has products (Phase 5.3)
	// ---------------------------------------------------------------------

	@Test
	void deleteCategoryWithProductsSoftDeletesCategoryAndLeavesProductReadable() throws Exception {
		SeededCategoryWithProduct seeded = CategoryFixtures.seedCategoryWithProduct(
				categoryRepository, productRepository, "IT Cat With Product");
		long categoryId = seeded.category().getCategoryId();

		mockMvc.perform(delete(BASE_URL + "/" + categoryId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isNoContent());

		assertThat(listCategoryNames()).doesNotContain("IT Cat With Product");

		Category reloaded = categoryRepository.findById(categoryId).orElseThrow();
		assertThat(reloaded.getDeletedAt()).isNotNull();

		Product product = productRepository.findById(seeded.productId()).orElseThrow();
		assertThat(product.getCategory().getCategoryId()).isEqualTo(categoryId);
	}

	// ---------------------------------------------------------------------
	// Write authorization (Phase 5.4)
	// ---------------------------------------------------------------------

	@Test
	void createWithoutAuthorizationHeaderReturns403() throws Exception {
		mockMvc.perform(post(BASE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"IT No Auth\"")))
				.andExpect(status().isForbidden());
	}

	@Test
	void writesWithUserRoleTokenReturn403() throws Exception {
		Category target = CategoryFixtures.seedCategory(categoryRepository, "IT User Role Target");
		String bearerUser = "Bearer " + userToken;

		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, bearerUser)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"IT User Role Create\"")))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch(BASE_URL + "/" + target.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, bearerUser)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"IT User Role Update\"")))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete(BASE_URL + "/" + target.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, bearerUser))
				.andExpect(status().isForbidden());
	}

	@Test
	void validCreateWithAdminTokenIsNotForbidden() throws Exception {
		mockMvc.perform(post(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(nameBody("\"IT Admin Allowed\"")))
				.andExpect(status().isCreated());
	}
}
