package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.integration.support.CategoryFixtures;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end coverage of the admin category read surface
 * ({@code GET /api/v1/admin/products/categories} and
 * {@code GET /api/v1/admin/products/categories/{id}}) on
 * {@code AdminProductsController} -&gt; {@code CategoryService}, through the real
 * Spring Security filter chain (ADMIN JWT) and a real PostgreSQL Testcontainer.
 *
 * <p>Class-level {@link Transactional}: each {@code @Test} runs in a transaction
 * rolled back at method end, so the {@code @BeforeEach} user + category fixtures
 * never leak into sibling integration tests sharing the singleton container. The
 * {@code categories} table always holds 9 Flyway-seeded rows, so list / count
 * assertions are relative to {@link #preSeedActiveTotal} captured in
 * {@code @BeforeEach}.
 *
 * <p>Note: {@code GET /categories/{id}} returns {@code CategoryByIdDTO}, which
 * (unlike the list's {@code CategoryDTO}) carries no {@code createdAt} /
 * {@code updatedAt}; only {@code categoryId} and {@code name} are asserted here.
 */
@Transactional
class CategoryAdminReadIntegrationTest extends AbstractIntegrationTest {

	private static final String BASE_URL = "/api/v1/admin/products/categories";
	private static final String ADMIN_EMAIL = "cat-read-admin-it@test.com";
	private static final String PASSWORD = "correct-horse-battery";
	private static final String ACTIVE_NAME = "IT Read Active Category";
	private static final String DELETED_NAME = "IT Read Deleted Category";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private JwtUtil jwtUtil;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String adminToken;
	private long preSeedActiveTotal;
	private Category active;
	private Category softDeleted;

	@BeforeEach
	void setUp() throws Exception {
		AuthFixtures.seedUser(userRepository, userRoleRepository, ADMIN_EMAIL, PASSWORD, RoleType.ADMIN);
		adminToken = jwtUtil.generateToken(ADMIN_EMAIL, "ADMIN");
		preSeedActiveTotal = listBody().get("totalElements").asLong();
		active = CategoryFixtures.seedCategory(categoryRepository, ACTIVE_NAME);
		softDeleted = CategoryFixtures.seedSoftDeletedCategory(categoryRepository, DELETED_NAME);
	}

	private JsonNode listBody() throws Exception {
		MvcResult result = mockMvc.perform(get(BASE_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.param("size", "500"))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

	private List<String> listNames() throws Exception {
		List<String> names = new ArrayList<>();
		listBody().get("content").forEach(node -> names.add(node.get("name").asText()));
		return names;
	}

	@Test
	void listReturns200AndIncludesTheActiveSeededCategory() throws Exception {
		JsonNode body = listBody();

		assertThat(body.get("totalElements").asLong()).isGreaterThanOrEqualTo(preSeedActiveTotal + 1);

		List<String> names = new ArrayList<>();
		body.get("content").forEach(node -> names.add(node.get("name").asText()));
		assertThat(names).contains(ACTIVE_NAME);
	}

	@Test
	void listExcludesSoftDeletedCategories() throws Exception {
		assertThat(listNames()).doesNotContain(DELETED_NAME);
	}

	@Test
	void getByIdReturnsTheActiveCategory() throws Exception {
		mockMvc.perform(get(BASE_URL + "/" + active.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categoryId").value(active.getCategoryId()))
				.andExpect(jsonPath("$.name").value(ACTIVE_NAME));
	}

	@Test
	void getByUnknownIdReturns404() throws Exception {
		mockMvc.perform(get(BASE_URL + "/999999")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void getBySoftDeletedIdReturns404() throws Exception {
		mockMvc.perform(get(BASE_URL + "/" + softDeleted.getCategoryId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}
}
