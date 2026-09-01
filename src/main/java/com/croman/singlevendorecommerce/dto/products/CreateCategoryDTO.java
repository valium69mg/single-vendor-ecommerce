package com.croman.singlevendorecommerce.dto.products;

import jakarta.validation.constraints.NotBlank;
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

	@NotBlank(message = "Category name must not be blank")
	@Size(min = 3, max = 60, message = "Category name must be between 3 and 60 characters")
	private String name;

}
