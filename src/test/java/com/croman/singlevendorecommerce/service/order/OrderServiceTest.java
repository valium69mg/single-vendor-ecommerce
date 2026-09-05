package com.croman.singlevendorecommerce.service.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.croman.singlevendorecommerce.dto.orders.CreateOrderRequest;
import com.croman.singlevendorecommerce.dto.orders.OrderResponse;
import com.croman.singlevendorecommerce.dto.orders.OrderSummaryResponse;
import com.croman.singlevendorecommerce.dto.orders.ShippingAddressDTO;
import com.croman.singlevendorecommerce.dto.orders.StockConflictDTO;
import com.croman.singlevendorecommerce.dto.products.ProductStatus;
import com.croman.singlevendorecommerce.entity.cart.Cart;
import com.croman.singlevendorecommerce.entity.cart.CartItem;
import com.croman.singlevendorecommerce.entity.orders.Order;
import com.croman.singlevendorecommerce.entity.orders.OrderItem;
import com.croman.singlevendorecommerce.entity.orders.OrderStatus;
import com.croman.singlevendorecommerce.entity.products.AttributeValue;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.entity.products.ProductVariantAttribute;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.repository.cart.CartItemRepository;
import com.croman.singlevendorecommerce.repository.cart.CartRepository;
import com.croman.singlevendorecommerce.repository.orders.OrderItemRepository;
import com.croman.singlevendorecommerce.repository.orders.OrderRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantAttributeRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.users.CurrentUserService;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private CartRepository cartRepository;

	@Mock
	private CartItemRepository cartItemRepository;

	@Mock
	private ProductVariantRepository productVariantRepository;

	@Mock
	private ProductVariantAttributeRepository productVariantAttributeRepository;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private MessageService messageService;

	@InjectMocks
	private OrderService orderService;

	private static final UUID USER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
	private static final Long CART_ID = 1L;
	private static final Long ORDER_ID = 777L;
	private static final Long VARIANT_ID = 100L;
	private static final Long OTHER_VARIANT_ID = 200L;

	private User user;
	private Product product;
	private ProductVariant variant;
	private Cart cart;

	@BeforeEach
	void setUp() {
		user = User.builder().userId(USER_ID).email("shopper@example.com").username("shopper").build();

		product = Product.builder()
				.productId(UUID.fromString("55555555-5555-5555-5555-555555555555"))
				.name("Gold Ring")
				.status(ProductStatus.ACTIVE)
				.build();

		variant = new ProductVariant();
		variant.setProductVariantId(VARIANT_ID);
		variant.setProduct(product);
		variant.setSku("RING-18K-7");
		variant.setPrice(new BigDecimal("100.00"));
		variant.setStock(10);

		cart = Cart.builder().cartId(CART_ID).user(user).items(new ArrayList<>()).build();

		lenient().when(currentUserService.getCurrentUser()).thenReturn(user);
		lenient().when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		lenient().when(productVariantAttributeRepository.findByVariantIn(any())).thenReturn(List.of());
	}

	private CartItem lineFor(ProductVariant v, int quantity) {
		return CartItem.builder().cartItemId(500L).cart(cart).productVariant(v).quantity(quantity).build();
	}

	private void stubMessage(String key, String value) {
		lenient().when(messageService.getMessage(eq(key), any(Locale.class))).thenReturn(value);
	}

	private ShippingAddressDTO validAddress() {
		return new ShippingAddressDTO("Jane Doe", "Av. Reforma 123", null, "CDMX", "CDMX", "01000", "MX", "5555555555");
	}

	private void stubOrderSave() {
		when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(inv -> {
			Order o = inv.getArgument(0);
			o.setOrderId(ORDER_ID);
			return o;
		});
	}

	// ─── Empty cart ──────────────────────────────────────────────────────────

	@Test
	void createOrderRejectsWhenCartMissing() {
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.empty());
		stubMessage("order_cart_empty", "Your cart is empty");

		assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(validAddress())))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Your cart is empty")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(400));

		verify(orderRepository, never()).saveAndFlush(any());
	}

	@Test
	void createOrderRejectsWhenCartHasNoItems() {
		stubMessage("order_cart_empty", "Your cart is empty");

		assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(validAddress())))
				.isInstanceOf(ApiServiceException.class)
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(400));

		verify(orderRepository, never()).saveAndFlush(any());
		verify(cartItemRepository, never()).deleteAll(any());
	}

	// ─── Price drift ─────────────────────────────────────────────────────────

	@Test
	void createOrderSnapshotsCurrentPriceEvenIfItDriftedSinceAddedToCart() {
		// simulate the product's price having changed since the line was added
		variant.setPrice(new BigDecimal("120.00"));
		cart.getItems().add(lineFor(variant, 1));
		stubOrderSave();

		OrderResponse result = orderService.createOrder(new CreateOrderRequest(validAddress()));

		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).unitPrice()).isEqualByComparingTo("120.00");
		assertThat(result.subtotal()).isEqualByComparingTo("120.00");
	}

	// ─── Stock conflicts ─────────────────────────────────────────────────────

	@Test
	void createOrderCollectsAllStockConflictsBeforeThrowingAndCreatesNothing() {
		variant.setStock(10);
		ProductVariant shortVariant = new ProductVariant();
		shortVariant.setProductVariantId(OTHER_VARIANT_ID);
		shortVariant.setProduct(product);
		shortVariant.setSku("RING-14K-8");
		shortVariant.setPrice(new BigDecimal("50.00"));
		shortVariant.setStock(2);

		cart.getItems().add(lineFor(variant, 3));
		cart.getItems().add(lineFor(shortVariant, 5));
		stubMessage("order_stock_insufficient", "Stock insufficient");

		assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(validAddress())))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Stock insufficient")
				.satisfies(ex -> {
					ApiServiceException apiEx = (ApiServiceException) ex;
					assertThat(apiEx.getStatusCode()).isEqualTo(409);
					@SuppressWarnings("unchecked")
					List<StockConflictDTO> conflicts = (List<StockConflictDTO>) apiEx.getMetadata().get("conflicts");
					assertThat(conflicts).hasSize(1);
					assertThat(conflicts.get(0).productVariantId()).isEqualTo(OTHER_VARIANT_ID);
					assertThat(conflicts.get(0).availableStock()).isEqualTo(2);
					assertThat(conflicts.get(0).type()).isEqualTo(StockConflictDTO.TYPE_STOCK_INSUFFICIENT);
				});

		verify(orderRepository, never()).saveAndFlush(any());
		verify(productVariantRepository, never()).save(any());
		verify(cartItemRepository, never()).deleteAll(any());
	}

	@Test
	void createOrderInactiveProductBlocksWholeOrderWithZeroAvailableStock() {
		product.setStatus(ProductStatus.INACTIVE);
		cart.getItems().add(lineFor(variant, 1));
		stubMessage("order_product_unavailable", "Product unavailable");

		assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(validAddress())))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Product unavailable")
				.satisfies(ex -> {
					ApiServiceException apiEx = (ApiServiceException) ex;
					assertThat(apiEx.getStatusCode()).isEqualTo(409);
					@SuppressWarnings("unchecked")
					List<StockConflictDTO> conflicts = (List<StockConflictDTO>) apiEx.getMetadata().get("conflicts");
					assertThat(conflicts).hasSize(1);
					assertThat(conflicts.get(0).availableStock()).isEqualTo(0);
					assertThat(conflicts.get(0).type()).isEqualTo(StockConflictDTO.TYPE_PRODUCT_UNAVAILABLE);
				});

		verify(orderRepository, never()).saveAndFlush(any());
	}

	@Test
	void createOrderSoftDeletedProductBlocksWholeOrder() {
		product.setDeletedAt(LocalDateTime.now());
		cart.getItems().add(lineFor(variant, 1));
		stubMessage("order_product_unavailable", "Product unavailable");

		assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(validAddress())))
				.isInstanceOf(ApiServiceException.class)
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(409));

		verify(orderRepository, never()).saveAndFlush(any());
	}

	// ─── Happy path ──────────────────────────────────────────────────────────

	@Test
	void createOrderHappyPathComputesTotalsSnapshotsAndDecrementsStock() {
		cart.getItems().add(lineFor(variant, 2));
		stubOrderSave();

		OrderResponse result = orderService.createOrder(new CreateOrderRequest(validAddress()));

		assertThat(result.orderId()).isEqualTo(ORDER_ID);
		assertThat(result.orderNumber()).matches("ORD-\\d{8}-" + ORDER_ID);
		assertThat(result.status()).isEqualTo(OrderStatus.PENDING.name());
		assertThat(result.subtotal()).isEqualByComparingTo("200.00");
		assertThat(result.shippingCost()).isEqualByComparingTo("99.00");
		assertThat(result.total()).isEqualByComparingTo("299.00");
		assertThat(result.items()).hasSize(1);
		assertThat(result.items().get(0).sku()).isEqualTo("RING-18K-7");
		assertThat(result.items().get(0).productName()).isEqualTo("Gold Ring");
		assertThat(result.items().get(0).quantity()).isEqualTo(2);
		assertThat(result.items().get(0).lineTotal()).isEqualByComparingTo("200.00");
		assertThat(result.shippingAddress().recipient()).isEqualTo("Jane Doe");

		ArgumentCaptor<ProductVariant> variantCaptor = ArgumentCaptor.forClass(ProductVariant.class);
		verify(productVariantRepository).save(variantCaptor.capture());
		assertThat(variantCaptor.getValue().getStock()).isEqualTo(8);

		verify(cartItemRepository).deleteAll(any());
		verify(orderItemRepository).saveAll(any());
	}

	@Test
	void createOrderMultiLineSumsSubtotalAndSnapshotsEachLineIndependently() {
		ProductVariant second = new ProductVariant();
		second.setProductVariantId(OTHER_VARIANT_ID);
		second.setProduct(product);
		second.setSku("RING-14K-8");
		second.setPrice(new BigDecimal("50.00"));
		second.setStock(20);

		cart.getItems().add(lineFor(variant, 2));
		cart.getItems().add(lineFor(second, 4));
		stubOrderSave();

		OrderResponse result = orderService.createOrder(new CreateOrderRequest(validAddress()));

		assertThat(result.subtotal()).isEqualByComparingTo("400.00");
		assertThat(result.items()).hasSize(2);
		verify(productVariantRepository, org.mockito.Mockito.times(2)).save(any());
	}

	@Test
	void createOrderAttachesVariantLabelFromAttributes() {
		cart.getItems().add(lineFor(variant, 1));
		stubOrderSave();

		ProductVariantAttribute attr = new ProductVariantAttribute();
		attr.setVariant(variant);
		AttributeValue value = new AttributeValue();
		value.setValue("Oro 18k");
		attr.setAttributeValue(value);
		when(productVariantAttributeRepository.findByVariantIn(any())).thenReturn(List.of(attr));

		OrderResponse result = orderService.createOrder(new CreateOrderRequest(validAddress()));

		assertThat(result.items().get(0).variantLabel()).isEqualTo("Oro 18k");
	}

	// ─── getMyOrders / getMyOrder ────────────────────────────────────────────

	@Test
	void getMyOrdersReturnsOnlyCallersOrdersMostRecentFirst() {
		Order order1 = Order.builder().orderId(1L).orderNumber("ORD-1").status(OrderStatus.PENDING)
				.total(new BigDecimal("100.00")).items(new ArrayList<>()).createdAt(LocalDateTime.now()).build();
		Order order2 = Order.builder().orderId(2L).orderNumber("ORD-2").status(OrderStatus.PENDING)
				.total(new BigDecimal("50.00")).items(new ArrayList<>()).createdAt(LocalDateTime.now().minusDays(1))
				.build();
		when(orderRepository.findByUser_UserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of(order1, order2));

		List<OrderSummaryResponse> result = orderService.getMyOrders();

		assertThat(result).hasSize(2);
		assertThat(result.get(0).orderNumber()).isEqualTo("ORD-1");
		assertThat(result.get(1).orderNumber()).isEqualTo("ORD-2");
	}

	@Test
	void getMyOrdersReturnsEmptyListWhenCallerHasNoOrders() {
		when(orderRepository.findByUser_UserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of());

		List<OrderSummaryResponse> result = orderService.getMyOrders();

		assertThat(result).isEmpty();
	}

	@Test
	void getMyOrderReturns404ForAnotherUsersOrderNumber() {
		when(orderRepository.findByOrderNumberAndUser_UserId("ORD-20260101-1", USER_ID)).thenReturn(Optional.empty());
		stubMessage("order_not_found", "Order not found");

		assertThatThrownBy(() -> orderService.getMyOrder("ORD-20260101-1"))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Order not found")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(404));
	}

	@Test
	void getMyOrderReturns404ForUnknownOrderNumber() {
		when(orderRepository.findByOrderNumberAndUser_UserId("ORD-UNKNOWN", USER_ID)).thenReturn(Optional.empty());
		stubMessage("order_not_found", "Order not found");

		assertThatThrownBy(() -> orderService.getMyOrder("ORD-UNKNOWN"))
				.isInstanceOf(ApiServiceException.class)
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(404));
	}

	@Test
	void getMyOrderReturnsDetailWhenOwnedByCaller() {
		Order order = Order.builder().orderId(5L).orderNumber("ORD-20260101-5").status(OrderStatus.PENDING)
				.subtotal(new BigDecimal("100.00")).shippingCost(new BigDecimal("99.00"))
				.total(new BigDecimal("199.00")).shippingRecipient("Jane").shippingLine1("Line 1")
				.shippingCity("CDMX").shippingState("CDMX").shippingPostalCode("01000").shippingCountry("MX")
				.shippingPhone("555").items(new ArrayList<>()).createdAt(LocalDateTime.now()).build();
		when(orderRepository.findByOrderNumberAndUser_UserId("ORD-20260101-5", USER_ID)).thenReturn(Optional.of(order));

		OrderResponse result = orderService.getMyOrder("ORD-20260101-5");

		assertThat(result.orderNumber()).isEqualTo("ORD-20260101-5");
		assertThat(result.total()).isEqualByComparingTo("199.00");
	}
}
