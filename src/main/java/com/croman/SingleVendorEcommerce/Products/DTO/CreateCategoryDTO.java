package com.croman.SingleVendorEcommerce.Products.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryDTO {

	@NotNull(message = "Category english name must not be null")
	@Size(min = 3, max = 60, message = "Categoty english name must be between 3 and 60 characters")
	private String englishName;
	@NotNull(message = "Category spanish name must not be null")
	@Size(min = 3, max = 60, message = "Categoty spanish name must be between 3 and 60 characters")
	private String spanishName;
	
}
