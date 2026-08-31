package com.croman.singlevendorecommerce.dto.cart;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {

	private Long cartItemId;
	private Long productVariantId;
	private UUID productId;
	private String productName;
	private String sku;
	private String imageUrl;
	private BigDecimal unitPrice;
	private BigDecimal discountPrice;
	private Integer quantity;
	private Integer availableStock;
	private BigDecimal lineTotal;

}
