package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.integration.support.CartFixtures;
import com.croman.singlevendorecommerce.repository.cart.CartRepository;
import com.croman.singlevendorecommerce.repository.orders.OrderRepository;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end coverage of {@code POST/GET /api/v1/orders} through
 * {@code OrderController} -&gt; {@code OrderService}, the real Spring Security
 * filter chain (USER JWT), and a real PostgreSQL Testcontainer (proves the
 * V30 Flyway migration applies cleanly).
 *
 * <p>Class-level {@link Transactional} rolls back each test; {@code BIGSERIAL}
 * ids are always read from the response body, never asserted as absolute
 * values. {@code entityManager.clear()} runs after each direct-repository
 * cart seed: {@link CartFixtures#seedCartLine} persists the {@code Cart} with
 * an in-memory empty {@code items} list and a separate {@code CartItem} save,
 * so without clearing the persistence context the test and the controller
 * call share one Hibernate session and {@code OrderService} would see the
 * stale (empty) collection instead of the freshly inserted line.
 */
@Transactional
class OrderMutationIntegrationTest extends AbstractIntegrationTest {

	private static final String EMAIL = "order-mut-it@test.com";
	private static final String PASSWORD = "correct-horse-battery";
	private static final String ORDERS_URL = "/api/v1/orders";

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
	private com.croman.singlevendorecommerce.repository.cart.CartItemRepository cartItemRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private jakarta.persistence.EntityManager entityManager;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private User shopper;
	private String token;

	@BeforeEach
	void setUp() {
		shopper = AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL, PASSWORD, RoleType.USER);
		token = jwtUtil.generateToken(EMAIL, "USER");
	}

	private static final String ADDRESS_JSON = "{\"recipient\":\"Jane Doe\",\"line1\":\"Av. Reforma 123\","
			+ "\"city\":\"CDMX\",\"state\":\"CDMX\",\"postalCode\":\"01000\",\"country\":\"MX\","
			+ "\"phone\":\"5555555555\"}";

	@Test
	void createOrderHappyPathPersistsOrderDecrementsStockAndClearsCart() throws Exception {
		ProductVariant variant = CartFixtures.seedActiveVariant(productRepository, categoryRepository,
				productVariantRepository, "ORD-V1", new BigDecimal("50.00"), 10);
		CartFixtures.seedCartLine(cartRepository, cartItemRepository, shopper, variant, 2);
		entityManager.clear();

		JsonNode created = postOrder(ADDRESS_JSON, 201);

		assertThat(created.get("orderNumber").asText()).matches("ORD-\\d{8}-\\d+");
		assertThat(money(created, "subtotal")).isEqualByComparingTo("100.00");
		assertThat(created.get("items").size()).isEqualTo(1);
		assertThat(created.get("items").get(0).get("sku").asText()).isEqualTo("CART-IT-ORD-V1");

		ProductVariant reloaded = productVariantRepository.findById(variant.getProductVariantId()).orElseThrow();
		assertThat(reloaded.getStock()).isEqualTo(8);

		assertThat(cartRepository.findByUser_UserId(shopper.getUserId()).orElseThrow().getItems()).isEmpty();
		assertThat(orderRepository.findByUser_UserIdOrderByCreatedAtDesc(shopper.getUserId())).hasSize(1);
	}

	@Test
	void createOrderRejectsEmptyCartWith400() throws Exception {
		JsonNode error = postOrder(ADDRESS_JSON, 400);

		assertThat(error.get("status").asInt()).isEqualTo(400);
		assertThat(error.get("error").isTextual()).isTrue();
	}

	@Test
	void createOrderStockConflictReturns409WithConflictsArray() throws Exception {
		ProductVariant variant = CartFixtures.seedActiveVariant(productRepository, categoryRepository,
				productVariantRepository, "ORD-V2", new BigDecimal("30.00"), 1);
		CartFixtures.seedCartLine(cartRepository, cartItemRepository, shopper, variant, 5);
		entityManager.clear();

		JsonNode error = postOrder(ADDRESS_JSON, 409);

		assertThat(error.get("status").asInt()).isEqualTo(409);
		JsonNode conflicts = error.get("conflicts");
		assertThat(conflicts.isArray()).isTrue();
		assertThat(conflicts.size()).isEqualTo(1);
		assertThat(conflicts.get(0).get("productVariantId").asLong()).isEqualTo(variant.getProductVariantId());
		assertThat(conflicts.get(0).get("availableStock").asInt()).isEqualTo(1);
		assertThat(conflicts.get(0).get("type").asText()).isEqualTo("STOCK_INSUFFICIENT");

		assertThat(orderRepository.findByUser_UserIdOrderByCreatedAtDesc(shopper.getUserId())).isEmpty();
		ProductVariant reloaded = productVariantRepository.findById(variant.getProductVariantId()).orElseThrow();
		assertThat(reloaded.getStock()).isEqualTo(1);
	}

	@Test
	void listAndGetReturnCreatedOrder() throws Exception {
		ProductVariant variant = CartFixtures.seedActiveVariant(productRepository, categoryRepository,
				productVariantRepository, "ORD-V3", new BigDecimal("20.00"), 5);
		CartFixtures.seedCartLine(cartRepository, cartItemRepository, shopper, variant, 1);
		entityManager.clear();
		JsonNode created = postOrder(ADDRESS_JSON, 201);
		String orderNumber = created.get("orderNumber").asText();

		JsonNode list = body(mockMvc.perform(get(ORDERS_URL).header(AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk()));
		assertThat(list.size()).isEqualTo(1);
		assertThat(list.get(0).get("orderNumber").asText()).isEqualTo(orderNumber);

		JsonNode detail = body(mockMvc.perform(get(ORDERS_URL + "/" + orderNumber)
				.header(AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk()));
		assertThat(detail.get("orderNumber").asText()).isEqualTo(orderNumber);
		assertThat(detail.get("items").size()).isEqualTo(1);
	}

	private JsonNode postOrder(String addressJson, int expectedStatus) throws Exception {
		String body = "{\"shippingAddress\":" + addressJson + "}";
		return body(mockMvc.perform(post(ORDERS_URL)
				.header(AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().is(expectedStatus)));
	}

	private JsonNode body(ResultActions actions) throws Exception {
		String content = actions.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(content);
	}

	private static BigDecimal money(JsonNode node, String field) {
		return new BigDecimal(node.get(field).asText());
	}
}
