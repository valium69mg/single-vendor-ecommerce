package com.croman.singlevendorecommerce.products.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

	private long categoryId;
	private String name;
	private int products;
	private int unitsSold;
	private BigDecimal revenue;
	private BigDecimal averagePrice;
	private int stock;
	
}
