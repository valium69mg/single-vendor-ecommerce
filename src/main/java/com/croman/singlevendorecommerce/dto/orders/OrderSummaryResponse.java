package com.croman.singlevendorecommerce.dto.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
		String orderNumber,
		String status,
		BigDecimal total,
		Integer totalItems,
		LocalDateTime createdAt) {
}
