package com.croman.singlevendorecommerce.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MergeSkipDTO {

	private Long productVariantId;

	// Localized via MessageService, reusing the existing
	// cart_variant_not_found / cart_product_unavailable / cart_stock_exceeded keys.
	private String reason;

}
