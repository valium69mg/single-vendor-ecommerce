package com.croman.singlevendorecommerce.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MergeAdjustmentDTO {

	private Long productVariantId;
	private int requestedQuantity;
	private int finalQuantity;

}
