package com.croman.singlevendorecommerce.dto.products;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminProductDTO {

    private UUID productId;
    private String name;
    private String shortDescription;
    private boolean featured;
    private ProductStatus status;
    private CategoryRef category;
    private BrandRef brand;
    private String imageUrl;
    private String mediumThumbnailUrl;
    private String smallThumbnailUrl;
    private BigDecimal avgPrice;
    private BigDecimal avgDiscountPrice;
    private int totalStock;
    private int variantCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryRef {
        private Long categoryId;
        private String name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BrandRef {
        private Long brandId;
        private String name;
    }
}
