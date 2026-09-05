package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.orders.Order;
import com.croman.singlevendorecommerce.entity.orders.OrderStatus;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.integration.support.CartFixtures;
import com.croman.singlevendorecommerce.repository.cart.CartItemRepository;
import com.croman.singlevendorecommerce.repository.cart.CartRepository;
import com.croman.singlevendorecommerce.repository.orders.OrderRepository;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Proves the auth gate on all three {@code /api/v1/orders} endpoints and
 * ownership isolation of order detail, through the real Spring Security
 * filter chain and a real PostgreSQL Testcontainer.
 *
 * <p>Status contract mirrors {@code CartSecurityIntegrationTest} (locked
 * decision T1: {@code Http403ForbiddenEntryPoint}): no / non-Bearer header
 * -&gt; 403; malformed / expired Bearer token -&gt; 401. Ownership: user B
 * requesting user A's {@code orderNumber} gets a 404, never the order data.
 * Class-level {@link Transactional} rolls back each test.
 */
@Transactional
class OrderSecurityIntegrationTest extends AbstractIntegrationTest {

	private static final String ORDERS_URL = "/api/v1/orders";
	private static final String EMAIL_A = "order-sec-a-it@test.com";
	private static final String EMAIL_B = "order-sec-b-it@test.com";
	private static final String PASSWORD = "correct-horse-battery";
	private static final String ADDRESS_JSON = "{\"shippingAddress\":{\"recipient\":\"Jane Doe\","
			+ "\"line1\":\"Av. Reforma 123\",\"city\":\"CDMX\",\"state\":\"CDMX\",\"postalCode\":\"01000\","
			+ "\"country\":\"MX\",\"phone\":\"5555555555\"}}";

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
	private OrderRepository orderRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private MessageService messageService;

	@Value("${JWT_SECRET}")
	private String jwtSecret;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String tokenA;
	private String tokenB;
	private String orderANumber;

	@BeforeEach
	void setUp() {
		User userA = AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL_A, PASSWORD, RoleType.USER);
		User userB = AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL_B, PASSWORD, RoleType.USER);
		tokenA = jwtUtil.generateToken(EMAIL_A, "USER");
		tokenB = jwtUtil.generateToken(EMAIL_B, "USER");

		ProductVariant variant = CartFixtures.seedActiveVariant(productRepository, categoryRepository,
				productVariantRepository, "SEC-ORD-V1", new BigDecimal("10.00"), 10);
		CartFixtures.seedCartLine(cartRepository, cartItemRepository, userA, variant, 1);

		Order orderA = orderRepository.save(Order.builder()
				.user(userA)
				.status(OrderStatus.PENDING)
				.orderNumber("ORD-20260101-999999")
				.shippingRecipient("Jane").shippingLine1("Line 1").shippingCity("CDMX").shippingState("CDMX")
				.shippingPostalCode("01000").shippingCountry("MX").shippingPhone("555")
				.subtotal(new BigDecimal("10.00")).shippingCost(new BigDecimal("99.00"))
				.total(new BigDecimal("109.00")).items(new ArrayList<>())
				.build());
		orderANumber = orderA.getOrderNumber();
	}

	@Test
	void mutationsWithoutAuthorizationHeaderReturn403() throws Exception {
		mockMvc.perform(post(ORDERS_URL).contentType(MediaType.APPLICATION_JSON).content(ADDRESS_JSON))
				.andExpect(status().isForbidden());

		mockMvc.perform(get(ORDERS_URL)).andExpect(status().isForbidden());

		mockMvc.perform(get(ORDERS_URL + "/" + orderANumber)).andExpect(status().isForbidden());
	}

	@Test
	void mutationsWithMalformedBearerTokenReturn401() throws Exception {
		String malformed = "Bearer not.a.jwt";

		mockMvc.perform(post(ORDERS_URL).header(AUTHORIZATION, malformed)
				.contentType(MediaType.APPLICATION_JSON).content(ADDRESS_JSON))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get(ORDERS_URL).header(AUTHORIZATION, malformed))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get(ORDERS_URL + "/" + orderANumber).header(AUTHORIZATION, malformed))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void mutationsWithExpiredBearerTokenReturn401() throws Exception {
		String expired = "Bearer " + new JwtUtil(jwtSecret, -1000L).generateToken(EMAIL_A, "USER");

		mockMvc.perform(post(ORDERS_URL).header(AUTHORIZATION, expired)
				.contentType(MediaType.APPLICATION_JSON).content(ADDRESS_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void userBCannotSeeUserAsOrderInOwnListing() throws Exception {
		JsonNode list = body(mockMvc.perform(get(ORDERS_URL).header(AUTHORIZATION, "Bearer " + tokenB))
				.andExpect(status().isOk()));

		assertThat(list.isArray()).isTrue();
		assertThat(list.size()).isEqualTo(0);
	}

	@Test
	void userBCannotFetchUserAsOrderByNumberAndGets404() throws Exception {
		mockMvc.perform(get(ORDERS_URL + "/" + orderANumber).header(AUTHORIZATION, "Bearer " + tokenB))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value(messageService.getMessage("order_not_found",
						Locale.of("es"))));
	}

	@Test
	void userAOwnOrderIsReturnedByOrderNumber() throws Exception {
		mockMvc.perform(get(ORDERS_URL + "/" + orderANumber).header(AUTHORIZATION, "Bearer " + tokenA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderNumber").value(orderANumber));
	}

	private JsonNode body(ResultActions actions) throws Exception {
		String content = actions.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(content);
	}
}
