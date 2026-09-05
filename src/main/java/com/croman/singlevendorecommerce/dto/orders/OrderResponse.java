package com.croman.singlevendorecommerce.dto.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
		Long orderId,
		String orderNumber,
		String status,
		BigDecimal subtotal,
		BigDecimal shippingCost,
		BigDecimal total,
		ShippingAddressDTO shippingAddress,
		List<OrderItemResponse> items,
		LocalDateTime createdAt) {
}
