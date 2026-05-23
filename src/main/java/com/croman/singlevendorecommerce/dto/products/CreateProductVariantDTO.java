package com.croman.singlevendorecommerce.dto.products;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductVariantDTO {

    @NotBlank
    @Size(max = 100)
    private String sku;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @DecimalMin("0.01")
    private BigDecimal discountPrice;

    @NotNull
    @Min(0)
    private Integer stock;

    @Min(1)
    private Integer weightGrams;

    private List<Long> attributeValueIds;

}
