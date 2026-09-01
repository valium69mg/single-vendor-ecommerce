package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.integration.support.CategoryFixtures;
import com.croman.singlevendorecommerce.integration.support.CategoryFixtures.SeededCategoryWithProduct;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;

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
}
