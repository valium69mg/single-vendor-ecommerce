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
public class CategoryByIdDTO {

	private long categoryId;
	private String englishName;
	private String spanishName;
	private int products;
	private int unitsSold;
	private BigDecimal revenue;
	private BigDecimal averagePrice;
	private int stock;
	
}
