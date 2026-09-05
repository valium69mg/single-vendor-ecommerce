package com.croman.singlevendorecommerce.dto.orders;

public record StockConflictDTO(
		Long productVariantId,
		String type,
		Integer requestedQuantity,
		Integer availableStock) {

	public static final String TYPE_STOCK_INSUFFICIENT = "STOCK_INSUFFICIENT";
	public static final String TYPE_PRODUCT_UNAVAILABLE = "PRODUCT_UNAVAILABLE";
}
