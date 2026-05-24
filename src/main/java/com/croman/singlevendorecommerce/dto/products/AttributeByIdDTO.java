package com.croman.singlevendorecommerce.dto.products;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttributeByIdDTO {

	private long attributeId;
	private String attributeType;
	private String spanishName;
	private List<ValueDTO> attributeValues;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ValueDTO {
		private long attributeValueId;
		private String value;
	}
}
