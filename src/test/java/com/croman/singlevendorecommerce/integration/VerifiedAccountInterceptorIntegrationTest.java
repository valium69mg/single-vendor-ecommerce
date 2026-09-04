package com.croman.singlevendorecommerce.integration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.roles.UserRole;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.integration.support.CartFixtures;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.utils.PasswordUtils;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;

/**
 * End-to-end coverage of {@link com.croman.singlevendorecommerce.web.interceptor.VerifiedAccountInterceptor}
 * gating {@code POST/PATCH/DELETE /api/v1/cart/items/**} through the real Spring Security
 * filter chain and a real PostgreSQL Testcontainer: unverified callers are rejected on the
 * gated cart-mutation surface, verified callers and {@code GET /api/v1/cart} are unaffected,
 * and a non-gated pre-auth route stays reachable regardless of verification state.
 */
@Transactional
class VerifiedAccountInterceptorIntegrationTest extends AbstractIntegrationTest {

	private static final String UNVERIFIED_EMAIL = "gate-unverified-it@test.com";
	private static final String VERIFIED_EMAIL = "gate-verified-it@test.com";
	private static final String PASSWORD = "correct-horse-battery";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductVariantRepository productVariantRepository;

	@Autowired
	private JwtUtil jwtUtil;

	private String unverifiedToken;
	private String verifiedToken;
	private long variantId;

	@BeforeEach
	void seedUsersAndVariant() {
		UserRole role = userRoleRepository.findByRoleType(RoleType.USER)
				.orElseThrow(() -> new IllegalStateException("Flyway V6 role seed missing role: USER"));
		User unverified = userRepository.save(User.builder().email(UNVERIFIED_EMAIL).username(UNVERIFIED_EMAIL)
				.password(PasswordUtils.hashPassword(PASSWORD)).isActive(true).isValidated(false).userRole(role)
				.build());
		unverifiedToken = jwtUtil.generateToken(unverified.getEmail(), "USER");

		User verified = AuthFixtures.seedUser(userRepository, userRoleRepository, VERIFIED_EMAIL, PASSWORD,
				RoleType.USER);
		verifiedToken = jwtUtil.generateToken(verified.getEmail(), "USER");

		variantId = CartFixtures.seedActiveVariant(productRepository, categoryRepository, productVariantRepository,
				"GATE-V1", new BigDecimal("50.00"), 10).getProductVariantId();
	}

	@Test
	void unverifiedCaller_postCartItems_returns403WithLocalizedAccountNotVerified() throws Exception {
		mockMvc.perform(post("/api/v1/cart/items").header(AUTHORIZATION, "Bearer " + unverifiedToken)
				.contentType(MediaType.APPLICATION_JSON).content(addBody(variantId, 1)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.errorCode").value("account_not_verified"))
				.andExpect(jsonPath("$.error").isNotEmpty());
	}

	@Test
	void unverifiedCaller_patchCartItems_returns403WithLocalizedAccountNotVerified() throws Exception {
		mockMvc.perform(patch("/api/v1/cart/items/1").header(AUTHORIZATION, "Bearer " + unverifiedToken)
				.contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.errorCode").value("account_not_verified"));
	}

	@Test
	void unverifiedCaller_deleteCartItems_returns403WithLocalizedAccountNotVerified() throws Exception {
		mockMvc.perform(delete("/api/v1/cart/items/1").header(AUTHORIZATION, "Bearer " + unverifiedToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.errorCode").value("account_not_verified"));
	}

	@Test
	void verifiedCaller_postCartItems_isNotBlockedByTheGate() throws Exception {
		mockMvc.perform(post("/api/v1/cart/items").header(AUTHORIZATION, "Bearer " + verifiedToken)
				.contentType(MediaType.APPLICATION_JSON).content(addBody(variantId, 1))).andExpect(status().isOk());
	}

	@Test
	void unverifiedCaller_getCart_isNotBlockedByTheGate() throws Exception {
		mockMvc.perform(get("/api/v1/cart").header(AUTHORIZATION, "Bearer " + unverifiedToken))
				.andExpect(status().isOk());
	}

	@Test
	void unverifiedCaller_nonGatedPreAuthRoute_isNotBlockedByTheGate() throws Exception {
		mockMvc.perform(post("/api/v1/users/verify/resend").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + UNVERIFIED_EMAIL + "\"}")).andExpect(status().isOk());
	}

	private static String addBody(long productVariantId, int quantity) {
		return "{\"productVariantId\":%d,\"quantity\":%d}".formatted(productVariantId, quantity);
	}
}
