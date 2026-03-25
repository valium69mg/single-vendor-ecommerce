package com.croman.singlevendorecommerce.products.dto;

import java.util.List;

import com.croman.singlevendorecommerce.utils.dto.PageResponse;

public class BrandPageResponse extends PageResponse<BrandDTO> {

	public BrandPageResponse(List<BrandDTO> content, int page, int size, long totalElements, int totalPages,
			boolean last) {
		super(content, page, size, totalElements, totalPages, last);
	}

}
