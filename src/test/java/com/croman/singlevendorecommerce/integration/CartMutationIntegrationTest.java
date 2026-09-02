package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
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
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end coverage of the authenticated cart mutation surface
 * ({@code POST/PATCH/DELETE /api/v1/cart/items}) on {@code CartController} -&gt;
 * {@code CartService}, through the real Spring Security filter chain (USER JWT)
 * and a real PostgreSQL Testcontainer.
 *
 * <p>Documents the current HTTP contract: every mutation returns {@code 200} with
 * a {@code CartDTO}, the cart row is lazy-created on first add, re-adding the same
 * variant increments a single line, and totals are recomputed on every mutation.
 * Class-level {@link Transactional} rolls back each test; {@code BIGSERIAL} ids are
 * always read from the response body, never asserted as absolute values.
 */
@Transactional
class CartMutationIntegrationTest extends AbstractIntegrationTest {

	private static final String EMAIL = "cart-mut-it@test.com";
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

	private final ObjectMapper objectMapper = new ObjectMapper();

	private User shopper;
	private String token;
	private long v1;
	private long v2;
	private long vd;

	@BeforeEach
	void setUp() {
		shopper = AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL, PASSWORD, RoleType.USER);
		token = jwtUtil.generateToken(EMAIL, "USER");

		v1 = CartFixtures.seedActiveVariant(productRepository, categoryRepository, productVariantRepository,
				"MUT-V1", new BigDecimal("50.00"), 10).getProductVariantId();
		v2 = CartFixtures.seedActiveVariant(productRepository, categoryRepository, productVariantRepository,
				"MUT-V2", new BigDecimal("20.00"), 10).getProductVariantId();
		vd = CartFixtures.seedActiveVariant(productRepository, categoryRepository, productVariantRepository,
				"MUT-VD", new BigDecimal("100.00"), new BigDecimal("80.00"), 10).getProductVariantId();
	}

	@Test
	void firstAddLazyCreatesCartAndStoresLine() throws Exception {
		assertThat(cartRepository.findByUser_UserId(shopper.getUserId())).isEmpty();

		JsonNode cart = postItem(addBody(v1, 2), 200);

		assertThat(cart.get("cartId").isNull()).isFalse();
		assertThat(cart.get("items").size()).isEqualTo(1);
		JsonNode line = line(cart, v1);
		assertThat(line.get("quantity").asInt()).isEqualTo(2);
		assertThat(cart.get("totalItems").asInt()).isEqualTo(2);
		assertThat(money(cart, "subtotal")).isEqualByComparingTo(money(line, "lineTotal"));
		assertThat(cartRepository.findByUser_UserId(shopper.getUserId())).isPresent();
	}

	@Test
	void reAddingSameVariantIncrementsSingleLine() throws Exception {
		postItem(addBody(v1, 2), 200);

		JsonNode cart = postItem(addBody(v1, 3), 200);

		assertThat(cart.get("items").size()).isEqualTo(1);
		assertThat(line(cart, v1).get("quantity").asInt()).isEqualTo(5);
		assertThat(cart.get("totalItems").asInt()).isEqualTo(5);
	}

	@Test
	void addingDistinctVariantCreatesSecondLineAndSumsTotals() throws Exception {
		postItem(addBody(v1, 2), 200);

		JsonNode cart = postItem(addBody(v2, 1), 200);

		assertThat(cart.get("items").size()).isEqualTo(2);
		BigDecimal expectedSubtotal = money(line(cart, v1), "lineTotal").add(money(line(cart, v2), "lineTotal"));
		assertThat(money(cart, "subtotal")).isEqualByComparingTo(expectedSubtotal);
		assertThat(cart.get("totalItems").asInt()).isEqualTo(3);
	}

	@Test
	void patchSetsAbsoluteQuantityAndRecomputesTotals() throws Exception {
		JsonNode created = postItem(addBody(v1, 5), 200);
		long lineId = line(created, v1).get("cartItemId").asLong();

		JsonNode cart = patchItem(lineId, qtyBody(4), 200);

		JsonNode line = line(cart, v1);
		assertThat(line.get("quantity").asInt()).isEqualTo(4);
		assertThat(cart.get("totalItems").asInt()).isEqualTo(4);
		assertThat(money(cart, "subtotal"))
				.isEqualByComparingTo(money(line, "unitPrice").multiply(BigDecimal.valueOf(4)));
	}

	@Test
	void patchCartItemIdNotInCallersCartReturns404() throws Exception {
		postItem(addBody(v1, 1), 200);

		JsonNode error = patchItem(999_999_999L, qtyBody(1), 404);

		assertThat(error.get("status").asInt()).isEqualTo(404);
		assertThat(error.get("error").isTextual()).isTrue();
	}

	@Test
	void patchWhenCallerHasNoCartReturns404() throws Exception {
		JsonNode error = patchItem(1L, qtyBody(1), 404);

		assertThat(error.get("status").asInt()).isEqualTo(404);
		assertThat(error.get("error").isTextual()).isTrue();
	}

	@Test
	void deleteRemovesLineAndRecomputesTotalsDownToEmpty() throws Exception {
		JsonNode withV1 = postItem(addBody(v1, 2), 200);
		postItem(addBody(v2, 3), 200);
		long v1LineId = line(withV1, v1).get("cartItemId").asLong();

		JsonNode afterFirstDelete = deleteItem(v1LineId, 200);

		assertThat(afterFirstDelete.get("items").size()).isEqualTo(1);
		JsonNode v2Line = line(afterFirstDelete, v2);
		assertThat(money(afterFirstDelete, "subtotal")).isEqualByComparingTo(money(v2Line, "lineTotal"));
		assertThat(afterFirstDelete.get("totalItems").asInt()).isEqualTo(3);

		JsonNode afterLastDelete = deleteItem(v2Line.get("cartItemId").asLong(), 200);

		assertThat(afterLastDelete.get("items").size()).isEqualTo(0);
		assertThat(money(afterLastDelete, "subtotal")).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(afterLastDelete.get("totalItems").asInt()).isEqualTo(0);
	}

	@Test
	void deleteCartItemIdNotInCallersCartReturns404() throws Exception {
		postItem(addBody(v1, 1), 200);

		JsonNode error = deleteItem(999_999_999L, 404);

		assertThat(error.get("status").asInt()).isEqualTo(404);
		assertThat(error.get("error").isTextual()).isTrue();
	}

	@Test
	void discountedVariantDrivesUnitPriceAndLineTotal() throws Exception {
		JsonNode cart = postItem(addBody(vd, 2), 200);

		JsonNode line = line(cart, vd);
		assertThat(money(line, "unitPrice")).isEqualByComparingTo("80.00");
		assertThat(money(line, "discountPrice")).isEqualByComparingTo("80.00");
		assertThat(money(line, "lineTotal")).isEqualByComparingTo("160.00");
		assertThat(money(cart, "subtotal")).isEqualByComparingTo("160.00");
		assertThat(cart.get("totalItems").asInt()).isEqualTo(2);
	}

	@Test
	void nonDiscountedVariantUsesBasePrice() throws Exception {
		JsonNode cart = postItem(addBody(v1, 3), 200);

		JsonNode line = line(cart, v1);
		assertThat(money(line, "unitPrice")).isEqualByComparingTo("50.00");
		assertThat(line.get("discountPrice").isNull()).isTrue();
		assertThat(money(line, "lineTotal")).isEqualByComparingTo("150.00");
	}

	@Test
	void sameTokenGetReturnsPersistedLinesAfterMutations() throws Exception {
		postItem(addBody(v1, 2), 200);
		postItem(addBody(v2, 1), 200);

		JsonNode cart = body(mockMvc.perform(get("/api/v1/cart")
				.header(AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk()));

		assertThat(cart.get("items").size()).isEqualTo(2);
		assertThat(line(cart, v1).get("quantity").asInt()).isEqualTo(2);
		assertThat(line(cart, v2).get("quantity").asInt()).isEqualTo(1);
	}

	private static String addBody(long productVariantId, int quantity) {
		return "{\"productVariantId\":%d,\"quantity\":%d}".formatted(productVariantId, quantity);
	}

	private static String qtyBody(int quantity) {
		return "{\"quantity\":%d}".formatted(quantity);
	}

	private JsonNode postItem(String json, int expectedStatus) throws Exception {
		return body(mockMvc.perform(post("/api/v1/cart/items")
				.header(AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().is(expectedStatus)));
	}

	private JsonNode patchItem(long cartItemId, String json, int expectedStatus) throws Exception {
		return body(mockMvc.perform(patch("/api/v1/cart/items/" + cartItemId)
				.header(AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().is(expectedStatus)));
	}

	private JsonNode deleteItem(long cartItemId, int expectedStatus) throws Exception {
		return body(mockMvc.perform(delete("/api/v1/cart/items/" + cartItemId)
				.header(AUTHORIZATION, "Bearer " + token))
				.andExpect(status().is(expectedStatus)));
	}

	private JsonNode body(ResultActions actions) throws Exception {
		String content = actions.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(content);
	}

	private static JsonNode line(JsonNode cart, long productVariantId) {
		for (JsonNode item : cart.get("items")) {
			if (item.get("productVariantId").asLong() == productVariantId) {
				return item;
			}
		}
		throw new AssertionError("no cart line for productVariantId " + productVariantId);
	}

	private static BigDecimal money(JsonNode node, String field) {
		return new BigDecimal(node.get(field).asText());
	}
}
