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
public class PublicProductDTO {

    private UUID productId;
    private String name;
    private String slug;
    private String shortDescription;
    private boolean featured;
    private CategoryRef category;
    private BrandRef brand;
    private String imageUrl;
    private String mediumThumbnailUrl;
    private String smallThumbnailUrl;
    private BigDecimal minPrice;
    private BigDecimal minDiscountPrice;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryRef {
        private Long categoryId;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BrandRef {
        private Long brandId;
        private String name;
        private String slug;
    }
}
