package com.croman.SingleVendorEcommerce.Products;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.Auth.DTO.LoginResponseDTO;
import com.croman.SingleVendorEcommerce.DTO.DefaultApiResponse;
import com.croman.SingleVendorEcommerce.General.ApiResponseService;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateBrandDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateCategoryDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateMaterialDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.UpdateCategoryDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
public class AdminProductsController {
	
	private final CategoryService categoryService;
	private final MaterialsService materialsService;
	private final BrandsService brandsService;
	private final ApiResponseService apiResponseService;

	@PostMapping("categories")
	@Operation(summary = "Create category", responses = {
		    @ApiResponse(
		        responseCode = "200", 
		        description = "Category created successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> createCategory(@RequestBody CreateCategoryDTO createCategoryDTO) {
		categoryService.createCategoryDTO(createCategoryDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(apiResponseService.getApiResponseMessage("category_created", HttpStatus.CREATED));
	}
	
	@PatchMapping("categories/{id}")
	@Operation(summary = "Update category", responses = {
		    @ApiResponse(
		        responseCode = "200", 
		        description = "Category updated successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> patchCategory(@PathVariable long id, 
			@RequestBody UpdateCategoryDTO updateCategoryDTO) {
		categoryService.updateCategory(id, updateCategoryDTO);
		return ResponseEntity.status(HttpStatus.OK)
				.body(apiResponseService.getApiResponseMessage("category_updated", HttpStatus.OK));
	}
	
	@DeleteMapping("categories/{id}")
	@Operation(summary = "Delete category", responses = {
		    @ApiResponse(
		        responseCode = "200", 
		        description = "Category deleted successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Category not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> deleteCategory(@PathVariable long id) {
		categoryService.deleteCategory(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.body(apiResponseService.getApiResponseMessage("category_deleted", HttpStatus.NO_CONTENT));
	}
	
	@PostMapping("materials")
	@Operation(summary = "Create material", responses = {
		    @ApiResponse(
		        responseCode = "200", 
		        description = "Material created successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> createMaterial(@RequestBody CreateMaterialDTO createMaterialDTO) {
		materialsService.createMaterial(createMaterialDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(apiResponseService.getApiResponseMessage("material_created", HttpStatus.CREATED));
	}
	
	@PostMapping("brands")
	@Operation(summary = "Create brand", responses = {
		    @ApiResponse(
		        responseCode = "200", 
		        description = "Brand created successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> createBrand(@RequestBody CreateBrandDTO createBrandDTO) {
		brandsService.createBrand(createBrandDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(apiResponseService.getApiResponseMessage("brand_created", HttpStatus.CREATED));
	}
	
}
