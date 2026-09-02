package com.croman.singlevendorecommerce.controller.products;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import com.croman.singlevendorecommerce.dto.DefaultApiResponse;
import com.croman.singlevendorecommerce.dto.products.AdminProductByIdDTO;
import com.croman.singlevendorecommerce.dto.products.AdminProductDTO;
import com.croman.singlevendorecommerce.dto.products.AdminProductsPageResponse;
import com.croman.singlevendorecommerce.dto.products.AttributeByIdDTO;
import com.croman.singlevendorecommerce.dto.products.AttributesDTO;
import com.croman.singlevendorecommerce.dto.products.BrandByIdDTO;
import com.croman.singlevendorecommerce.dto.products.BrandDTO;
import com.croman.singlevendorecommerce.dto.products.BrandPageResponse;
import com.croman.singlevendorecommerce.dto.products.PublicCategoriesPageResponse;
import com.croman.singlevendorecommerce.dto.products.PublicCategoryByIdDTO;
import com.croman.singlevendorecommerce.dto.products.PublicCategoryDTO;
import com.croman.singlevendorecommerce.dto.products.PublicProductByIdDTO;
import com.croman.singlevendorecommerce.dto.products.PublicProductDTO;
import com.croman.singlevendorecommerce.dto.products.PublicProductsPageResponse;
import com.croman.singlevendorecommerce.dto.products.MaterialByIdDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialsPageResponse;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.service.products.AttributesService;
import com.croman.singlevendorecommerce.service.products.BrandsService;
import com.croman.singlevendorecommerce.service.products.CategoryService;
import com.croman.singlevendorecommerce.service.products.MaterialsService;
import com.croman.singlevendorecommerce.service.products.ProductService;

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
@RequestMapping("/api/v1/products")
public class ProductsController {

	private final CategoryService categoryService;
	private final MaterialsService materialsService;
	private final BrandsService brandsService;
	private final AttributesService attributesService;
	private final ProductService productService;

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

	@GetMapping("categories/by-slug/{slug}")
	@Operation(
		    summary = "Get category by slug",
		    responses = {
		        @ApiResponse(responseCode = "200", description = "Content successfully returned",
		            content = @Content(mediaType = "application/json",
		                schema = @Schema(implementation = PublicCategoryByIdDTO.class))),
		        @ApiResponse(responseCode = "301", description = "Slug superseded; Location points to the canonical slug"),
		        @ApiResponse(responseCode = "404", description = "Category not found",
		            content = @Content(mediaType = "application/json",
		                schema = @Schema(implementation = DefaultApiResponse.class)))
		    }
		)
	public ResponseEntity<PublicCategoryByIdDTO> getCategoryBySlug(@PathVariable String slug) {
		return ResponseEntity.status(HttpStatus.OK).body(categoryService.resolveCategoryBySlug(slug));
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

	@GetMapping("brands/by-slug/{slug}")
	@Operation(summary = "Get brand by slug", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = BrandByIdDTO.class))),
		    @ApiResponse(responseCode = "301", description = "Slug superseded; Location points to the canonical slug"),
		    @ApiResponse(responseCode = "404", description = "Brand not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<BrandByIdDTO> getBrandBySlug(@PathVariable String slug) {
		return ResponseEntity.status(HttpStatus.OK).body(brandsService.resolveBrandBySlug(slug));
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

	@GetMapping
	@Operation(summary = "Get products (public)", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = PublicProductsPageResponse.class)))
		})
	public ResponseEntity<PageResponse<PublicProductDTO>> getProducts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size,
			@RequestParam(defaultValue = "newest") String sortBy,
			@RequestParam(defaultValue = "") @Valid @Size(min = 0, max = 200) String term,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) Long brandId,
			@RequestParam(required = false) Long materialId,
			@RequestParam(defaultValue = "false") boolean featured,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtStart,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtEnd) {
		return ResponseEntity.ok(productService.getPublicProducts(
				page, size, sortBy, term, categoryId, brandId, materialId,
				featured, createdAtStart, createdAtEnd));
	}

	@GetMapping("by-slug/{slug}")
	@Operation(summary = "Get product by slug (public)", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = PublicProductByIdDTO.class))),
		    @ApiResponse(responseCode = "301", description = "Slug superseded; Location points to the canonical slug"),
		    @ApiResponse(responseCode = "404", description = "Product not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<PublicProductByIdDTO> getProductBySlug(@PathVariable String slug) {
		return ResponseEntity.ok(productService.resolveProductBySlug(slug));
	}

	@GetMapping("{productId}")
	@Operation(summary = "Get product by id (public)", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = PublicProductByIdDTO.class))),
		    @ApiResponse(responseCode = "404", description = "Product not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<PublicProductByIdDTO> getProductById(@PathVariable UUID productId) {
		return ResponseEntity.ok(productService.getPublicProductById(productId));
	}

}
