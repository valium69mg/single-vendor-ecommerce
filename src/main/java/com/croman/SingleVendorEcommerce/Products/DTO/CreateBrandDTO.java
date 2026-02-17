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
public class CreateBrandDTO {

	@NotNull(message = "Brand name must not be null")
	@Size(min = 3, max = 60, message = "Brand name must be between 3 and 60 characters")
	private String name;
	
}
