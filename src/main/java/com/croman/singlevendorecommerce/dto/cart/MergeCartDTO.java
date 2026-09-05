package com.croman.singlevendorecommerce.dto.cart;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MergeCartDTO {

	// No @NotEmpty: an empty list is a no-op merge (returns the current cart
	// with empty adjusted/skipped lists), not a validation error.
	@NotNull
	@Valid
	private List<MergeCartLineDTO> items;

}
