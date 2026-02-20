package com.croman.SingleVendorEcommerce.Products;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.DTO.DefaultApiResponse;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Products.DTO.AttributesDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.BrandDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CategoryByIdDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CategoryDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.MaterialDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
	@Operation(summary = "Get categories by offset pagination", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Content successfully returned",
		        content = @Content(
		            mediaType = "application/json",
		            array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class))
		        )
		    )
		})
	public ResponseEntity<List<CategoryDTO>> getCategories(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(categoryService.getCategories(LocaleUtils.APP_DEFAULT_LANG, page, size));
	}
	
	@Operation(
		    summary = "Get category by id",
		    responses = {
		        @ApiResponse(
		            responseCode = "200",
		            description = "Content successfully returned",
		            content = @Content(
		                mediaType = "application/json",
		                schema = @Schema(implementation = CategoryByIdDTO.class)
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
	@GetMapping("categories/{id}")
	public ResponseEntity<CategoryByIdDTO> getCategoryById(@PathVariable long id) {
		return ResponseEntity.status(HttpStatus.OK).body(categoryService.getCategoryById(id));
	}

	@GetMapping("materials")
	@Operation(summary = "Get materials by offset pagination", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Content successfully returned",
		        content = @Content(
		            mediaType = "application/json",
		            array = @ArraySchema(schema = @Schema(implementation = MaterialDTO.class))
		        )
		    )
		})
	public ResponseEntity<List<MaterialDTO>> getMaterials(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.status(HttpStatus.OK).body(materialsService.getMaterials(LocaleUtils.APP_DEFAULT_LANG,
				page, size));
	}

	@GetMapping("brands")
	@Operation(summary = "Get brands by offset pagination", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Content successfully returned",
		        content = @Content(
		            mediaType = "application/json",
		            array = @ArraySchema(schema = @Schema(implementation = BrandDTO.class))
		        )
		    )
		})
	public ResponseEntity<List<BrandDTO>> getBrands(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.status(HttpStatus.OK).body(brandsService.getBrands(page, size));
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
				.body(attributesService.getAttributes(LocaleUtils.APP_DEFAULT_LANG, page, size));
	}

}
