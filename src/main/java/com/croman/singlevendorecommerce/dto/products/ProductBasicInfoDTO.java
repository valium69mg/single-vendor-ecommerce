package com.croman.singlevendorecommerce.dto.products;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProductBasicInfoDTO {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 500)
    private String shortDescription;

    @Size(max = 5000)
    private String longDescription;

    @NotNull
    private ProductStatus status;

    private boolean featured;

    private Long brandId;

    private Long categoryId;
}
