package com.croman.SingleVendorEcommerce.Products.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrandDTO {

	private long brandId;
	private String name;

}
