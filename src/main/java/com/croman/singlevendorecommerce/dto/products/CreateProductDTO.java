package com.croman.singlevendorecommerce.dto.products;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateProductDTO extends ProductBasicInfoDTO {

    private List<Long> materialIds;

    @Valid
    @NotEmpty
    private List<CreateProductVariantDTO> variants;

}