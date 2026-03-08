package com.croman.singlevendorecommerce.products;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.croman.singlevendorecommerce.dto.DefaultApiResponse;
import com.croman.singlevendorecommerce.general.ApiResponseService;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.products.dto.CreateBrandDTO;
import com.croman.singlevendorecommerce.products.dto.CreateCategoryDTO;
import com.croman.singlevendorecommerce.products.dto.CreateMaterialDTO;
import com.croman.singlevendorecommerce.products.dto.UpdateCategoryDTO;

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
	private final MessageService messageService;

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
	
	@PostMapping("/categories/{categoryId}/image")
	@Operation(
	    summary = "Upload category image",
	    description = "Uploads an image for a category, stores it locally, and queues a thumbnail generation job.",
	    responses = {
	        @ApiResponse(
	            responseCode = "200",
	            description = "Upload successful",
	            content = @Content(
	                mediaType = "application/json",
	                schema = @Schema(implementation = DefaultApiResponse.class)
	            )
	        ),
	        @ApiResponse(
	            responseCode = "400",
	            description = "Bad request",
	            content = @Content(
	                mediaType = "application/json",
	                schema = @Schema(implementation = DefaultApiResponse.class)
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
	public ResponseEntity<DefaultApiResponse> uploadCategoryImage(
	        @PathVariable Long categoryId,
	        @RequestParam("file") MultipartFile file) {

	    categoryService.uploadImage(file, categoryId);

	    DefaultApiResponse response = DefaultApiResponse.builder()
	            .status(HttpStatus.OK.value())
	            .message(messageService.getMessage("image_uploaded", LocaleUtils.getDefaultLocale()))
	            .build();

	    return ResponseEntity.ok(response);
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
