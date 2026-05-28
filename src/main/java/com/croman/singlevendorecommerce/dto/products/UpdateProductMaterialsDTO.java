package com.croman.singlevendorecommerce.dto.products;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductMaterialsDTO {

	@NotNull
	private List<Long> materialIds;

}
