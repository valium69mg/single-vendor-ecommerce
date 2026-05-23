package com.croman.singlevendorecommerce.dto.products;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class CreateProductDTO {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 500)
    private String shortDescription;

    private String longDescription;

    @NotNull
    private ProductStatus status;

    private boolean featured;

    private Long brandId;

    private Long categoryId;

    private List<Long> materialIds;

    @Valid
    @NotEmpty
    private List<CreateProductVariantDTO> variants;

}
