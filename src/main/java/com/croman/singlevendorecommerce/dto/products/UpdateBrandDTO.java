package com.croman.singlevendorecommerce.dto.products;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBrandDTO {

	@NotNull(message = "brandId is required")
	@Positive(message = "brandId must be a positive number")
	private Long brandId;
	@NotNull(message = "Brand name must not be null")
	@Size(min = 3, max = 60, message = "Brand name must be between 3 and 60 characters")
	private String name;
}
