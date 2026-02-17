package com.croman.SingleVendorEcommerce.Products.DTO;

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
	
}
