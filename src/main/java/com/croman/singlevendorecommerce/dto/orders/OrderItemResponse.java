package com.croman.singlevendorecommerce.dto.orders;

import java.math.BigDecimal;

public record OrderItemResponse(
		Long orderItemId,
		Long productVariantId,
		String productName,
		String variantLabel,
		String sku,
		BigDecimal unitPrice,
		Integer quantity,
		BigDecimal lineTotal) {
}
