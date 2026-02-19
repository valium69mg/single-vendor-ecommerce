package com.croman.SingleVendorEcommerce.Products.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttributesDTO {

	private long attributeId;
	private String name;
	private List<AttributeValueDTO> attributeValues;
	
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AttributeValueDTO {
		private long attributeValueId;
		private String value;
	}
}
