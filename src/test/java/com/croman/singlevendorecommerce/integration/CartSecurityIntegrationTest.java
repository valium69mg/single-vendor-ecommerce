package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.cart.CartItem;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.integration.support.CartFixtures;
import com.croman.singlevendorecommerce.repository.cart.CartItemRepository;
import com.croman.singlevendorecommerce.repository.cart.CartRepository;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;

/**
 * Proves the auth gate on the three mutating cart endpoints and cross-user
 * isolation of cart lines, through the real Spring Security filter chain and a
 * real PostgreSQL Testcontainer.
 *
 * <p>Status contract (locked decision T1: {@code Http403ForbiddenEntryPoint}, no
 * {@code AuthenticationEntryPoint}): no / non-Bearer header -&gt; 403; malformed /
 * expired Bearer token -&gt; 401. Isolation: {@code findByCartItemIdAndCart_CartId}
 * scopes every lookup to the caller's own cart, so user B patching / deleting user
 * A's {@code cartItemId} gets a 404 and A's line is untouched. Class-level
 * {@link Transactional} rolls back each test; {@code GET /api/v1/cart}'s auth
 * matrix is already covered by {@code JwtFilterIntegrationTest}.
 */
@Transactional
class CartSecurityIntegrationTest extends AbstractIntegrationTest {

	private static final String ITEMS_URL = "/api/v1/cart/items";
	private static final String SOME_ITEM_URL = ITEMS_URL + "/1";
	private static final String MERGE_URL = "/api/v1/cart/merge";
	private static final String EMAIL_A = "cart-sec-a-it@test.com";
	private static final String EMAIL_B = "cart-sec-b-it@test.com";
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
	private CartRepository cartRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private MessageService messageService;

	@Value("${JWT_SECRET}")
	private String jwtSecret;

	private String tokenA;
	private String tokenB;
	private Long bCartId;
	private Long aLineId;

	@BeforeEach
	void setUp() {
		User userA = AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL_A, PASSWORD, RoleType.USER);
		User userB = AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL_B, PASSWORD, RoleType.USER);
		tokenA = jwtUtil.generateToken(EMAIL_A, "USER");
		tokenB = jwtUtil.generateToken(EMAIL_B, "USER");

		ProductVariant variant = CartFixtures.seedActiveVariant(productRepository, categoryRepository,
				productVariantRepository, "SEC-V1", new BigDecimal("50.00"), 10);

		CartItem aLine = CartFixtures.seedCartLine(cartRepository, cartItemRepository, userA, variant, 2);
		aLineId = aLine.getCartItemId();
		bCartId = CartFixtures.seedCart(cartRepository, userB).getCartId();
	}

	private String message(String key) {
		return messageService.getMessage(key, Locale.of("es"));
	}

	@Test
	void mutationsWithoutAuthorizationHeaderReturn403() throws Exception {
		mockMvc.perform(post(ITEMS_URL).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productVariantId\":1,\"quantity\":1}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch(SOME_ITEM_URL).contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete(SOME_ITEM_URL))
				.andExpect(status().isForbidden());

		mockMvc.perform(post(MERGE_URL).contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"productVariantId\":1,\"quantity\":1}]}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void mutationsWithMalformedBearerTokenReturn401() throws Exception {
		String malformed = "Bearer not.a.jwt";

		mockMvc.perform(post(ITEMS_URL).header(AUTHORIZATION, malformed)
				.contentType(MediaType.APPLICATION_JSON).content("{\"productVariantId\":1,\"quantity\":1}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(patch(SOME_ITEM_URL).header(AUTHORIZATION, malformed)
				.contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(delete(SOME_ITEM_URL).header(AUTHORIZATION, malformed))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post(MERGE_URL).header(AUTHORIZATION, malformed)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"productVariantId\":1,\"quantity\":1}]}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void mutationsWithExpiredBearerTokenReturn401() throws Exception {
		String expired = "Bearer " + new JwtUtil(jwtSecret, -1000L).generateToken(EMAIL_A, "USER");

		mockMvc.perform(post(ITEMS_URL).header(AUTHORIZATION, expired)
				.contentType(MediaType.APPLICATION_JSON).content("{\"productVariantId\":1,\"quantity\":1}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(patch(SOME_ITEM_URL).header(AUTHORIZATION, expired)
				.contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(delete(SOME_ITEM_URL).header(AUTHORIZATION, expired))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post(MERGE_URL).header(AUTHORIZATION, expired)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"productVariantId\":1,\"quantity\":1}]}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void mergeSucceedsForAuthenticatedUserAndReturnsUpdatedCart() throws Exception {
		mockMvc.perform(post(MERGE_URL).header(AUTHORIZATION, "Bearer " + tokenB)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cart.cartId").value(bCartId))
				.andExpect(jsonPath("$.adjustedLines").isEmpty())
				.andExpect(jsonPath("$.skippedLines").isEmpty());
	}

	@Test
	void userBSeesOwnEmptyCartNotUserAsLine() throws Exception {
		mockMvc.perform(get("/api/v1/cart").header(AUTHORIZATION, "Bearer " + tokenB))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cartId").value(bCartId))
				.andExpect(jsonPath("$.items").isEmpty())
				.andExpect(jsonPath("$.subtotal").value(0))
				.andExpect(jsonPath("$.totalItems").value(0));
	}

	@Test
	void userBCannotPatchUserAsLineAndAsLineIsUnchanged() throws Exception {
		mockMvc.perform(patch(ITEMS_URL + "/" + aLineId).header(AUTHORIZATION, "Bearer " + tokenB)
				.contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value(message("cart_item_not_found")));

		CartItem reloaded = cartItemRepository.findById(aLineId).orElseThrow();
		assertThat(reloaded.getQuantity()).isEqualTo(2);
	}

	@Test
	void userBCannotDeleteUserAsLineAndAsLineIsUnchanged() throws Exception {
		mockMvc.perform(delete(ITEMS_URL + "/" + aLineId).header(AUTHORIZATION, "Bearer " + tokenB))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value(message("cart_item_not_found")));

		CartItem reloaded = cartItemRepository.findById(aLineId).orElseThrow();
		assertThat(reloaded.getQuantity()).isEqualTo(2);
	}
}
