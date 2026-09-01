package com.croman.singlevendorecommerce.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;

/**
 * Verifies role-based access enforcement on the ADMIN-only route
 * {@code GET /api/v1/admin/products} ({@code requestMatchers("/api/v1/admin/**").hasRole("ADMIN")}
 * in {@code SecurityConfig}) through the real filter chain and a real PostgreSQL
 * Testcontainer.
 *
 * <p>Class-level {@link Transactional} rolls back the seeded users after each test.
 */
@Transactional
class RoleEnforcementIntegrationTest extends AbstractIntegrationTest {

	private static final String ADMIN_URL = "/api/v1/admin/products";
	private static final String ADMIN_EMAIL = "admin-it@test.com";
	private static final String USER_EMAIL = "user-it@test.com";
	private static final String PASSWORD = "correct-horse-battery";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@BeforeEach
	void seedUsers() {
		AuthFixtures.seedUser(userRepository, userRoleRepository, ADMIN_EMAIL, PASSWORD, RoleType.ADMIN);
		AuthFixtures.seedUser(userRepository, userRoleRepository, USER_EMAIL, PASSWORD, RoleType.USER);
	}

	@Test
	void userTokenOnAdminRouteReturns403() throws Exception {
		String userToken = jwtUtil.generateToken(USER_EMAIL, "USER");

		mockMvc.perform(get(ADMIN_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminTokenOnAdminRouteIsNotForbidden() throws Exception {
		String adminToken = jwtUtil.generateToken(ADMIN_EMAIL, "ADMIN");

		mockMvc.perform(get(ADMIN_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk());
	}
}
