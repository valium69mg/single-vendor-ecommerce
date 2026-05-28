package com.croman.singlevendorecommerce.controller.products;


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
import com.croman.singlevendorecommerce.dto.products.CreateAttributeDTO;
import com.croman.singlevendorecommerce.dto.products.CreateBrandDTO;
import com.croman.singlevendorecommerce.dto.products.CreateCategoryDTO;
import com.croman.singlevendorecommerce.dto.products.CreateMaterialDTO;
import com.croman.singlevendorecommerce.dto.products.CreateProductDTO;
import com.croman.singlevendorecommerce.dto.products.ProductBasicInfoDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateProductMaterialsDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateAttributeDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateBrandDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateCategoryDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateMaterialDTO;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.products.AttributesService;
import com.croman.singlevendorecommerce.service.products.BrandsService;
import com.croman.singlevendorecommerce.service.products.CategoryService;
import com.croman.singlevendorecommerce.service.products.MaterialsService;
import com.croman.singlevendorecommerce.service.products.ProductService;
import com.croman.singlevendorecommerce.utils.ApiResponseService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;

import jakarta.validation.Valid;
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
	private final AttributesService attributesService;
	private final ProductService productService;
	private final ApiResponseService apiResponseService;
	private final MessageService messageService;

	@PostMapping
	@Operation(summary = "Create product", responses = {
		    @ApiResponse(
		        responseCode = "201",
		        description = "Product created successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request / duplicate SKU",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Category, brand, material or attribute value not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> createProduct(@Valid @RequestBody CreateProductDTO createProductDTO) {
		productService.createProduct(createProductDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(apiResponseService.getApiResponseMessage("product_created", HttpStatus.CREATED));
	}
	
	@PatchMapping("{productId}")
	@Operation(summary = "Update product", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Product updated successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request / duplicate SKU",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Category or brand not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> updateProduct(@Valid @RequestBody ProductBasicInfoDTO dto,
			@PathVariable UUID productId) {
		productService.updateProduct(productId, dto);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(apiResponseService.getApiResponseMessage("product_updated", HttpStatus.CREATED));
	}

	@PatchMapping("{productId}/materials")
	@Operation(summary = "Update product materials", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Product materials updated successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Product or material not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> updateProductMaterials(@PathVariable UUID productId,
			@Valid @RequestBody UpdateProductMaterialsDTO dto) {
		productService.updateMaterials(productId, dto.getMaterialIds());
		return ResponseEntity.ok(apiResponseService.getApiResponseMessage("product_materials_updated", HttpStatus.OK));
	}

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
	
	@PatchMapping("materials/{id}")
	@Operation(summary = "Update material", responses = {
		    @ApiResponse(
		        responseCode = "200", 
		        description = "Material updated successfully",
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
	public ResponseEntity<DefaultApiResponse> patchMaterial(@PathVariable long id, 
			@RequestBody UpdateMaterialDTO updateMaterialDTO) {
		materialsService.updateMaterial(id, updateMaterialDTO);
		return ResponseEntity.status(HttpStatus.OK)
				.body(apiResponseService.getApiResponseMessage("material_updated", HttpStatus.OK));
	}
	
	@DeleteMapping("materials/{id}")
	@Operation(summary = "Delete material", responses = {
		    @ApiResponse(
		        responseCode = "200", 
		        description = "Material deleted successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Material not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> deleteMaterial(@PathVariable long id) {
		materialsService.deleteMaterial(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.body(apiResponseService.getApiResponseMessage("category_deleted", HttpStatus.NO_CONTENT));
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
	
	@PatchMapping("brands")
	@Operation(summary = "Update brand", responses = {
		    @ApiResponse(
		        responseCode = "200", 
		        description = "Brand updated successfully",
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
	public ResponseEntity<DefaultApiResponse> createBrand(@RequestBody UpdateBrandDTO updateBrandDTO) {
		brandsService.updateBrand(updateBrandDTO);
		return ResponseEntity.status(HttpStatus.OK)
				.body(apiResponseService.getApiResponseMessage("brand_updated", HttpStatus.CREATED));
	}
	
	@DeleteMapping("brands/{id}")
	@Operation(summary = "Delete brand", responses = {
		    @ApiResponse(
		        responseCode = "204",
		        description = "Material deleted successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Material not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> deleteBrand(@PathVariable long id) {
		brandsService.deleteBrand(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("attributes")
	@Operation(summary = "Create attribute", responses = {
		    @ApiResponse(
		        responseCode = "201",
		        description = "Attribute created successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request / duplicate attribute type",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> createAttribute(@Valid @RequestBody CreateAttributeDTO createAttributeDTO) {
		attributesService.createAttribute(createAttributeDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(apiResponseService.getApiResponseMessage("attribute_created", HttpStatus.CREATED));
	}

	@PatchMapping("attributes/{id}")
	@Operation(summary = "Update attribute", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Attribute updated successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Attribute not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> patchAttribute(@PathVariable long id,
			@RequestBody UpdateAttributeDTO updateAttributeDTO) {
		attributesService.updateAttribute(id, updateAttributeDTO);
		return ResponseEntity.status(HttpStatus.OK)
				.body(apiResponseService.getApiResponseMessage("attribute_updated", HttpStatus.OK));
	}

	@DeleteMapping("attributes/{id}")
	@Operation(summary = "Delete attribute", responses = {
		    @ApiResponse(
		        responseCode = "204",
		        description = "Attribute deleted successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Attribute not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> deleteAttribute(@PathVariable long id) {
		attributesService.deleteAttribute(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
