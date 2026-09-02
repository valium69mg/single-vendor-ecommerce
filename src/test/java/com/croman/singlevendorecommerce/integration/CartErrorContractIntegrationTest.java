package com.croman.singlevendorecommerce.integration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
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
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pins the HTTP status AND the exact error-body key for every documented cart
 * failure case, through the real Spring Security filter chain (USER JWT) and a
 * real PostgreSQL Testcontainer.
 *
 * <p>{@code GlobalExceptionHandler} produces two shapes and each case asserts the
 * right one: bean-validation ({@code @NotNull} / {@code @Min}) -&gt;
 * {@code {status:400, errors:{<field>:<msg>}}} (map key {@code errors}), and
 * {@code ApiServiceException} -&gt; {@code {status:<code>, error:<msg>, <flattened-metadata>}}
 * (key {@code error}). Request bodies are raw JSON strings so {@code null} /
 * missing-field cases stay expressible; class-level {@link Transactional} rolls
 * back each test.
 */
@Transactional
class CartErrorContractIntegrationTest extends AbstractIntegrationTest {

	private static final String ITEMS_URL = "/api/v1/cart/items";
	private static final String EMAIL = "cart-err-it@test.com";
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

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String token;
	private long v1;
	private long lowStock;
	private long inactiveVariant;
	private long softDeletedVariant;

	@BeforeEach
	void setUp() {
		AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL, PASSWORD, RoleType.USER);
		token = jwtUtil.generateToken(EMAIL, "USER");

		v1 = id(CartFixtures.seedActiveVariant(productRepository, categoryRepository, productVariantRepository,
				"ERR-V1", new BigDecimal("50.00"), 10));
		lowStock = id(CartFixtures.seedActiveVariant(productRepository, categoryRepository, productVariantRepository,
				"ERR-VS", new BigDecimal("10.00"), 3));
		inactiveVariant = id(CartFixtures.seedInactiveProductVariant(productRepository, categoryRepository,
				productVariantRepository, "ERR-VI", new BigDecimal("10.00"), 10));
		softDeletedVariant = id(CartFixtures.seedSoftDeletedProductVariant(productRepository, categoryRepository,
				productVariantRepository, "ERR-VX", new BigDecimal("10.00"), 10));
	}

	private String message(String key) {
		return messageService.getMessage(key, Locale.of("es"));
	}

	@Test
	void addWithZeroQuantityIsBeanValidation400NotServiceBranch() throws Exception {
		mockMvc.perform(withAuth(post(ITEMS_URL)).content("{\"productVariantId\":" + v1 + ",\"quantity\":0}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.quantity").exists())
				.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void patchWithZeroQuantityIsBeanValidation400() throws Exception {
		long lineId = seedLineViaHttp(v1, 1);

		mockMvc.perform(withAuth(patch(ITEMS_URL + "/" + lineId)).content("{\"quantity\":0}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.quantity").exists())
				.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void addWithMissingQuantityReturns400ErrorsQuantity() throws Exception {
		mockMvc.perform(withAuth(post(ITEMS_URL)).content("{\"productVariantId\":" + v1 + "}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.quantity").exists());
	}

	@Test
	void patchWithMissingQuantityReturns400ErrorsQuantity() throws Exception {
		long lineId = seedLineViaHttp(v1, 1);

		mockMvc.perform(withAuth(patch(ITEMS_URL + "/" + lineId)).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.quantity").exists());
	}

	@Test
	void addWithNullProductVariantIdReturns400ErrorsProductVariantId() throws Exception {
		mockMvc.perform(withAuth(post(ITEMS_URL)).content("{\"productVariantId\":null,\"quantity\":1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.productVariantId").exists());
	}

	@Test
	void addQuantityExceedingStockReturns400WithAvailableStock() throws Exception {
		mockMvc.perform(withAuth(post(ITEMS_URL)).content("{\"productVariantId\":" + lowStock + ",\"quantity\":5}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").isString())
				.andExpect(jsonPath("$.availableStock").value(3));
	}

	@Test
	void patchQuantityExceedingStockReturns400WithAvailableStock() throws Exception {
		long lineId = seedLineViaHttp(lowStock, 2);

		mockMvc.perform(withAuth(patch(ITEMS_URL + "/" + lineId)).content("{\"quantity\":5}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.availableStock").value(3));
	}

	@Test
	void addUnknownProductVariantReturns404WithVariantNotFoundMessage() throws Exception {
		mockMvc.perform(withAuth(post(ITEMS_URL)).content("{\"productVariantId\":999999999,\"quantity\":1}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value(message("cart_variant_not_found")));
	}

	@Test
	void addVariantOfInactiveProductReturns409ProductUnavailable() throws Exception {
		mockMvc.perform(withAuth(post(ITEMS_URL)).content("{\"productVariantId\":" + inactiveVariant + ",\"quantity\":1}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value(message("cart_product_unavailable")));
	}

	@Test
	void addVariantOfSoftDeletedProductReturns409ProductUnavailable() throws Exception {
		mockMvc.perform(
				withAuth(post(ITEMS_URL)).content("{\"productVariantId\":" + softDeletedVariant + ",\"quantity\":1}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value(message("cart_product_unavailable")));
	}

	private static long id(ProductVariant variant) {
		return variant.getProductVariantId();
	}

	private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder) {
		return builder.header(AUTHORIZATION, "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
	}

	/** Create a real cart line for the caller through the API and return its id. */
	private long seedLineViaHttp(long productVariantId, int quantity) throws Exception {
		MvcResult result = mockMvc.perform(withAuth(post(ITEMS_URL))
				.content("{\"productVariantId\":" + productVariantId + ",\"quantity\":" + quantity + "}"))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString())
				.get("items").get(0).get("cartItemId").asLong();
	}
}
