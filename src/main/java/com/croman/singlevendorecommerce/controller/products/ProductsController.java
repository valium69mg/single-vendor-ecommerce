package com.croman.singlevendorecommerce.controller.products;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.croman.singlevendorecommerce.dto.DefaultApiResponse;
import com.croman.singlevendorecommerce.dto.products.AttributeByIdDTO;
import com.croman.singlevendorecommerce.dto.products.AttributesDTO;
import com.croman.singlevendorecommerce.dto.products.BrandByIdDTO;
import com.croman.singlevendorecommerce.dto.products.BrandDTO;
import com.croman.singlevendorecommerce.dto.products.BrandPageResponse;
import com.croman.singlevendorecommerce.dto.products.PublicCategoriesPageResponse;
import com.croman.singlevendorecommerce.dto.products.PublicCategoryByIdDTO;
import com.croman.singlevendorecommerce.dto.products.PublicCategoryDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialByIdDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialsPageResponse;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.service.products.AttributesService;
import com.croman.singlevendorecommerce.service.products.BrandsService;
import com.croman.singlevendorecommerce.service.products.CategoryService;
import com.croman.singlevendorecommerce.service.products.MaterialsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/")
public class ProductsController {

	private final CategoryService categoryService;
	private final MaterialsService materialsService;
	private final BrandsService brandsService;
	private final AttributesService attributesService;

	@GetMapping("categories")
	@Operation(
	    summary = "Get categories by offset pagination",
	    responses = {
	        @ApiResponse(
	            responseCode = "200",
	            description = "Content successfully returned",
	            content = @Content(
	                mediaType = "application/json",
	                schema = @Schema(implementation = PublicCategoriesPageResponse.class)
	            )
	        )
	    }
	)
	public ResponseEntity<PageResponse<PublicCategoryDTO>> getCategories(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "50") int size,
	        @RequestParam(defaultValue = "")
	        @Valid
	        @Size(min = 0, max = 60, message = "Search term must max of 60 characters")
	        String term
	) {
	    return ResponseEntity.status(HttpStatus.OK)
	            .body(categoryService.getPublicCategories(page, size, term));
	}

	@GetMapping("categories/{id}")
	@Operation(
		    summary = "Get category by id",
		    responses = {
		        @ApiResponse(
		            responseCode = "200",
		            description = "Content successfully returned",
		            content = @Content(
		                mediaType = "application/json",
		                schema = @Schema(implementation = PublicCategoryByIdDTO.class)
		            )
		        ),
		        @ApiResponse(
		            responseCode = "404",
		            description = "Category not found",
		            content = @Content(
		                mediaType = "application/json",
		                schema = @Schema(implementation = DefaultApiResponse.class)
		            )
		        )
		    }
		)
	public ResponseEntity<PublicCategoryByIdDTO> getCategoryById(@PathVariable long id) {
		return ResponseEntity.status(HttpStatus.OK).body(categoryService.getPublicCategoryById(id));
	}

	@GetMapping("materials")
	@Operation(summary = "Get materials by offset pagination", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Content successfully returned",
        		content = @Content(
		                mediaType = "application/json",
		                schema = @Schema(implementation = MaterialsPageResponse.class)
		            )
		    )
		})
	public ResponseEntity<PageResponse<MaterialDTO>> getMaterials(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size,
			@RequestParam(defaultValue = "")
		    @Valid
		    @Size(min = 0, max = 60, message = "Search term must max of 60 characters")
		    String term) {
		return ResponseEntity.status(HttpStatus.OK).body(materialsService.getMaterials(page, size, term));
	}
	
	@GetMapping("materials/{id}")
	@Operation(
		    summary = "Get material by id",
		    responses = {
		        @ApiResponse(
		            responseCode = "200",
		            description = "Content successfully returned",
		            content = @Content(
		                mediaType = "application/json",
		                schema = @Schema(implementation = MaterialByIdDTO.class)
		            )
		        ),
		        @ApiResponse(
		            responseCode = "404",
		            description = "Material not found",
		            content = @Content(
		                mediaType = "application/json",
		                schema = @Schema(implementation = DefaultApiResponse.class)
		            )
		        )
		    }
		)
	public ResponseEntity<MaterialByIdDTO> getMaterialById(@PathVariable long id) {
		return ResponseEntity.status(HttpStatus.OK).body(materialsService.getMaterialById(id));
	}
	
	@GetMapping("brands")
	@Operation(summary = "Get brands by offset pagination", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Content successfully returned",
		        content = @Content(
		            mediaType = "application/json",
		            array = @ArraySchema(schema = @Schema(implementation = BrandPageResponse.class))
		        )
		    )
		})
	public ResponseEntity<PageResponse<BrandDTO>> getBrands(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size,
			@RequestParam(defaultValue = "")
		    @Valid
		    @Size(min = 0, max = 60, message = "Search term must max of 60 characters")
		    String term) {
		return ResponseEntity.status(HttpStatus.OK).body(brandsService.getBrands(page, size, term));
	}
	
	@GetMapping("brands/{id}")
	@Operation(summary = "Get brand by id", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Content successfully returned",
		        content = @Content(
		            mediaType = "application/json",
		            array = @ArraySchema(schema = @Schema(implementation = BrandByIdDTO.class))
		        )
		    )
		})
	public ResponseEntity<BrandByIdDTO> getBrand(@PathVariable long id) {
		return ResponseEntity.status(HttpStatus.OK).body(brandsService.getBrandById(id));
	}
	
	@GetMapping("attributes")
	@Operation(summary = "Get attributes by offset pagination", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Content successfully returned",
		        content = @Content(
		            mediaType = "application/json",
		            array = @ArraySchema(schema = @Schema(implementation = AttributesDTO.class))
		        )
		    )
		})
	public ResponseEntity<List<AttributesDTO>> getAttributes(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(attributesService.getAttributes(page, size));
	}

	@GetMapping("attributes/{id}")
	@Operation(summary = "Get attribute by id", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Content successfully returned",
		        content = @Content(
		            mediaType = "application/json",
		            schema = @Schema(implementation = AttributeByIdDTO.class)
		        )
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Attribute not found",
		        content = @Content(
		            mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)
		        )
		    )
		})
	public ResponseEntity<AttributeByIdDTO> getAttributeById(@PathVariable long id) {
		return ResponseEntity.status(HttpStatus.OK).body(attributesService.getAttributeById(id));
	}

}
