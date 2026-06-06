package com.croman.singlevendorecommerce.dto.products;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
	private String imageUrl;
	private String mediumThumbnailUrl;
	private String smallThumbnailUrl;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
