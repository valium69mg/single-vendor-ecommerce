package com.croman.singlevendorecommerce.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductResponse {
    private int status;
    private String message;
    private String productId;
}
