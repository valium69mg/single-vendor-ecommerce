package com.croman.singlevendorecommerce.dto.products;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicProductByIdDTO {

    private UUID productId;
    private String name;
    private String shortDescription;
    private String longDescription;
    private boolean featured;
    private CategoryRef category;
    private BrandRef brand;
    private String imageUrl;
    private String mediumThumbnailUrl;
    private String smallThumbnailUrl;
    private BigDecimal minPrice;
    private BigDecimal minDiscountPrice;
    private LocalDateTime createdAt;
    private List<MaterialRef> materials;
    private List<VariantDTO> variants;

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

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaterialRef {
        private Long materialId;
        private String name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VariantDTO {
        private Long productVariantId;
        private BigDecimal price;
        private BigDecimal discountPrice;
        private int stock;
        private List<AttributeValueDTO> attributeValues;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttributeValueDTO {
        private Long attributeValueId;
        private String value;
    }
}
