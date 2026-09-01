package com.croman.singlevendorecommerce.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;

/**
 * Exercises {@link com.croman.singlevendorecommerce.utils.jwt.JwtAuthenticationFilter}
 * on a protected non-admin route ({@code GET /api/v1/cart}) through the real
 * security filter chain and a real PostgreSQL Testcontainer.
 *
 * <p>Status contract asserted here:
 * <ul>
 *   <li>valid Bearer token -&gt; 200 (empty cart for a freshly seeded user)</li>
 *   <li>no / non-Bearer Authorization header -&gt; 403 ({@code Http403ForbiddenEntryPoint};
 *       no {@code AuthenticationEntryPoint} is configured, locked decision T1 #2)</li>
 *   <li>expired / malformed / signature-tampered Bearer token -&gt; 401 (filter writes it directly)</li>
 * </ul>
 *
 * <p>Class-level {@link Transactional} rolls back the {@code @BeforeEach} user
 * fixture (and any cart row the 200 path creates) after each test.
 */
@Transactional
class JwtFilterIntegrationTest extends AbstractIntegrationTest {

	private static final String CART_URL = "/api/v1/cart";
	private static final String EMAIL = "jwt-it@test.com";
	private static final String PASSWORD = "correct-horse-battery";
	// Distinct HS256 secret, >= 32 bytes so Keys.hmacShaKeyFor does not throw WeakKeyException.
	private static final String OTHER_SECRET = "an-entirely-different-signing-secret-0123456789";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Value("${JWT_SECRET}")
	private String jwtSecret;

	@BeforeEach
	void seedUser() {
		AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL, PASSWORD, RoleType.USER);
	}

	@Test
	void validTokenReturns200() throws Exception {
		String token = jwtUtil.generateToken(EMAIL, "USER");

		mockMvc.perform(get(CART_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void noAuthorizationHeaderReturns403() throws Exception {
		mockMvc.perform(get(CART_URL))
				.andExpect(status().isForbidden());
	}

	@Test
	void nonBearerAuthorizationHeaderReturns403() throws Exception {
		mockMvc.perform(get(CART_URL).header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz"))
				.andExpect(status().isForbidden());
	}

	@Test
	void emptyBearerTokenReturns401() throws Exception {
		mockMvc.perform(get(CART_URL).header(HttpHeaders.AUTHORIZATION, "Bearer "))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void blankBearerTokenReturns401() throws Exception {
		mockMvc.perform(get(CART_URL).header(HttpHeaders.AUTHORIZATION, "Bearer    "))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void expiredTokenReturns401() throws Exception {
		String expired = new JwtUtil(jwtSecret, -1000L).generateToken(EMAIL, "USER");

		mockMvc.perform(get(CART_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void malformedTokenReturns401() throws Exception {
		mockMvc.perform(get(CART_URL).header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void signatureTamperedTokenReturns401() throws Exception {
		String tampered = new JwtUtil(OTHER_SECRET, 60000L).generateToken(EMAIL, "USER");

		mockMvc.perform(get(CART_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
				.andExpect(status().isUnauthorized());
	}
}
