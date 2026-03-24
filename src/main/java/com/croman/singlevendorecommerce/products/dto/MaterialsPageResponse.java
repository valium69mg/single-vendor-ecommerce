package com.croman.singlevendorecommerce.products.dto;

import java.util.List;

import com.croman.singlevendorecommerce.utils.dto.PageResponse;

public class MaterialsPageResponse extends PageResponse<MaterialDTO>{

	public MaterialsPageResponse(List<MaterialDTO> content, int page, int size, long totalElements, int totalPages,
			boolean last) {
		super(content, page, size, totalElements, totalPages, last);
	}

}
