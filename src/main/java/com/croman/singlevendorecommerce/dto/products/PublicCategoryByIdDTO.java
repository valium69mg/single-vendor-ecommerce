package com.croman.singlevendorecommerce.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicCategoryByIdDTO {

	private long categoryId;
	private String name;
	private int products;
	private String imageUrl;
	private String mediumThumbnailUrl;
	private String smallThumbnailUrl;

}
