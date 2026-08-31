package com.croman.singlevendorecommerce.dto.cart;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

	private Long cartId;
	private List<CartItemDTO> items;
	private BigDecimal subtotal;
	private int totalItems;

}
