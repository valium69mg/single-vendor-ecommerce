package com.croman.singlevendorecommerce.dto.products;

import java.util.List;

import com.croman.singlevendorecommerce.dto.utils.PageResponse;

public class AdminProductsPageResponse extends PageResponse<AdminProductDTO> {

    public AdminProductsPageResponse(List<AdminProductDTO> content, int page, int size,
            long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }
}
