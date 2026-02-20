package com.croman.SingleVendorEcommerce.Products.DTO;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryDTO {

	@Size(min = 3, max = 60, message = "Categoty english name must be between 3 and 60 characters")
	private String englishName;
	@Size(min = 3, max = 60, message = "Categoty spanish name must be between 3 and 60 characters")
	private String spanishName;

}
