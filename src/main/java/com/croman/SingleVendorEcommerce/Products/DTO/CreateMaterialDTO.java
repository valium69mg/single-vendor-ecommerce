package com.croman.SingleVendorEcommerce.Products.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMaterialDTO {

	private String englishName;
	private String spanishName;
	
}
