package com.croman.singlevendorecommerce.dto.cart;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MergeCartResultDTO {

	private CartDTO cart;
	private List<MergeAdjustmentDTO> adjustedLines;
	private List<MergeSkipDTO> skippedLines;

}
