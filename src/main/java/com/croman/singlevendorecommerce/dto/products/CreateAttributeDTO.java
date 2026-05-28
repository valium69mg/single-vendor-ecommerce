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
public class CreateAttributeDTO {

	@NotBlank
	@Size(min = 3, max = 50)
	private String attributeType;

	@NotBlank
	@Size(min = 2, max = 60)
	private String name;
}
