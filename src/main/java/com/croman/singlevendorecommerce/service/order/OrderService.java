package com.croman.singlevendorecommerce.service.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.orders.CreateOrderRequest;
import com.croman.singlevendorecommerce.dto.orders.OrderItemResponse;
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
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

	private static final String CART_EMPTY = "order_cart_empty";
	private static final String STOCK_INSUFFICIENT = "order_stock_insufficient";
	private static final String PRODUCT_UNAVAILABLE = "order_product_unavailable";
	private static final String ORDER_NOT_FOUND = "order_not_found";
	private static final String ORDER_NUMBER_DATE_PATTERN = "yyyyMMdd";

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ProductVariantRepository productVariantRepository;
	private final ProductVariantAttributeRepository productVariantAttributeRepository;
	private final CurrentUserService currentUserService;
	private final MessageService messageService;

	@Value("${app.shipping.flat-rate:99.00}")
	private BigDecimal shippingFlatRate = new BigDecimal("99.00");

	public OrderResponse createOrder(CreateOrderRequest request) {
		User user = currentUserService.getCurrentUser();
		Cart cart = cartRepository.findByUser_UserId(user.getUserId()).orElse(null);

		if (cart == null || cart.getItems().isEmpty()) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage(CART_EMPTY, LocaleUtils.getDefaultLocale()));
		}

		List<StockConflictDTO> conflicts = collectConflicts(cart);
		if (!conflicts.isEmpty()) {
			boolean anyUnavailable = conflicts.stream()
					.anyMatch(c -> StockConflictDTO.TYPE_PRODUCT_UNAVAILABLE.equals(c.type()));
			String key = anyUnavailable ? PRODUCT_UNAVAILABLE : STOCK_INSUFFICIENT;
			throw new ApiServiceException(HttpStatus.CONFLICT.value(),
					messageService.getMessage(key, LocaleUtils.getDefaultLocale()), Map.of("conflicts", conflicts));
		}

		Map<Long, String> variantLabels = buildVariantLabels(cart.getItems());

		BigDecimal subtotal = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();
		for (CartItem item : cart.getItems()) {
			ProductVariant variant = item.getProductVariant();
			BigDecimal unitPrice = variant.getPrice();
			BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
			subtotal = subtotal.add(lineTotal);

			orderItems.add(OrderItem.builder()
					.productVariant(variant)
					.productName(variant.getProduct().getName())
					.variantLabel(variantLabels.get(variant.getProductVariantId()))
					.sku(variant.getSku())
					.unitPrice(unitPrice)
					.quantity(item.getQuantity())
					.build());
		}

		BigDecimal shippingCost = shippingFlatRate;
		BigDecimal total = subtotal.add(shippingCost);
		ShippingAddressDTO address = request.shippingAddress();

		Order order = Order.builder()
				.user(user)
				.status(OrderStatus.PENDING)
				.shippingRecipient(address.recipient())
				.shippingLine1(address.line1())
				.shippingLine2(address.line2())
				.shippingCity(address.city())
				.shippingState(address.state())
				.shippingPostalCode(address.postalCode())
				.shippingCountry(address.country())
				.shippingPhone(address.phone())
				.subtotal(subtotal)
				.shippingCost(shippingCost)
				.total(total)
				.items(new ArrayList<>())
				.build();

		order = orderRepository.saveAndFlush(order);
		order.setOrderNumber(buildOrderNumber(order.getOrderId()));

		for (OrderItem orderItem : orderItems) {
			orderItem.setOrder(order);
			order.getItems().add(orderItem);
		}
		orderItemRepository.saveAll(orderItems);

		for (CartItem item : cart.getItems()) {
			ProductVariant variant = item.getProductVariant();
			variant.setStock(variant.getStock() - item.getQuantity());
			productVariantRepository.save(variant);
		}

		cartItemRepository.deleteAll(cart.getItems());
		cart.getItems().clear();

		return toResponse(order);
	}

	@Transactional(readOnly = true)
	public List<OrderSummaryResponse> getMyOrders() {
		User user = currentUserService.getCurrentUser();
		return orderRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId()).stream()
				.map(OrderService::toSummary)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getMyOrder(String orderNumber) {
		User user = currentUserService.getCurrentUser();
		Order order = orderRepository.findByOrderNumberAndUser_UserId(orderNumber, user.getUserId())
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(ORDER_NOT_FOUND, LocaleUtils.getDefaultLocale())));
		return toResponse(order);
	}

	// ─── helpers ─────────────────────────────────────────────────────────────

	private List<StockConflictDTO> collectConflicts(Cart cart) {
		List<StockConflictDTO> conflicts = new ArrayList<>();
		for (CartItem item : cart.getItems()) {
			ProductVariant variant = item.getProductVariant();
			Product product = variant.getProduct();
			boolean unavailable = product == null || product.getDeletedAt() != null
					|| product.getStatus() != ProductStatus.ACTIVE;

			if (unavailable) {
				conflicts.add(new StockConflictDTO(variant.getProductVariantId(),
						StockConflictDTO.TYPE_PRODUCT_UNAVAILABLE, item.getQuantity(), 0));
				continue;
			}

			Integer stock = variant.getStock();
			int available = stock == null ? 0 : stock;
			if (item.getQuantity() > available) {
				conflicts.add(new StockConflictDTO(variant.getProductVariantId(),
						StockConflictDTO.TYPE_STOCK_INSUFFICIENT, item.getQuantity(), available));
			}
		}
		return conflicts;
	}

	private Map<Long, String> buildVariantLabels(List<CartItem> items) {
		List<ProductVariant> variants = items.stream().map(CartItem::getProductVariant).toList();
		List<ProductVariantAttribute> attributes = productVariantAttributeRepository.findByVariantIn(variants);

		Map<Long, List<ProductVariantAttribute>> byVariant = attributes.stream()
				.collect(Collectors.groupingBy(a -> a.getVariant().getProductVariantId()));

		Map<Long, String> labels = new HashMap<>();
		byVariant.forEach((variantId, attrs) -> labels.put(variantId, attrs.stream()
				.map(a -> a.getAttributeValue().getValue())
				.collect(Collectors.joining(" / "))));
		return labels;
	}

	private String buildOrderNumber(Long orderId) {
		String date = LocalDate.now().format(DateTimeFormatter.ofPattern(ORDER_NUMBER_DATE_PATTERN));
		return "ORD-" + date + "-" + orderId;
	}

	private OrderResponse toResponse(Order order) {
		ShippingAddressDTO address = new ShippingAddressDTO(order.getShippingRecipient(), order.getShippingLine1(),
				order.getShippingLine2(), order.getShippingCity(), order.getShippingState(),
				order.getShippingPostalCode(), order.getShippingCountry(), order.getShippingPhone());
		List<OrderItemResponse> items = order.getItems().stream().map(OrderService::toItemResponse).toList();
		return new OrderResponse(order.getOrderId(), order.getOrderNumber(), order.getStatus().name(),
				order.getSubtotal(), order.getShippingCost(), order.getTotal(), address, items,
				order.getCreatedAt());
	}

	private static OrderItemResponse toItemResponse(OrderItem item) {
		BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
		return new OrderItemResponse(item.getOrderItemId(), item.getProductVariant().getProductVariantId(),
				item.getProductName(), item.getVariantLabel(), item.getSku(), item.getUnitPrice(),
				item.getQuantity(), lineTotal);
	}

	private static OrderSummaryResponse toSummary(Order order) {
		int totalItems = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
		return new OrderSummaryResponse(order.getOrderNumber(), order.getStatus().name(), order.getTotal(),
				totalItems, order.getCreatedAt());
	}
}
