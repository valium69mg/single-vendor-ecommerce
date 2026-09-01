package com.croman.singlevendorecommerce.controller.products;


import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.croman.singlevendorecommerce.dto.DefaultApiResponse;
import com.croman.singlevendorecommerce.dto.products.AttributeByIdDTO;
import com.croman.singlevendorecommerce.dto.products.AttributesDTO;
import com.croman.singlevendorecommerce.dto.products.BrandByIdDTO;
import com.croman.singlevendorecommerce.dto.products.BrandDTO;
import com.croman.singlevendorecommerce.dto.products.BrandPageResponse;
import com.croman.singlevendorecommerce.dto.products.CategoriesPageResponse;
import com.croman.singlevendorecommerce.dto.products.CategoryByIdDTO;
import com.croman.singlevendorecommerce.dto.products.CategoryDTO;
import com.croman.singlevendorecommerce.dto.products.CreateAttributeDTO;
import com.croman.singlevendorecommerce.dto.products.CreateBrandDTO;
import com.croman.singlevendorecommerce.dto.products.CreateCategoryDTO;
import com.croman.singlevendorecommerce.dto.products.AdminProductByIdDTO;
import com.croman.singlevendorecommerce.dto.products.AdminProductDTO;
import com.croman.singlevendorecommerce.dto.products.AdminProductsPageResponse;
import com.croman.singlevendorecommerce.dto.products.CreateMaterialDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialByIdDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialsPageResponse;
import com.croman.singlevendorecommerce.dto.products.ProductStatus;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
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
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

	@GetMapping
	@Operation(summary = "Get products (admin)", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = AdminProductsPageResponse.class)))
		})
	public ResponseEntity<PageResponse<AdminProductDTO>> getProducts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size,
			@RequestParam(defaultValue = "newest") String sortBy,
			@RequestParam(defaultValue = "") @Valid @Size(min = 0, max = 200) String term,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) Long brandId,
			@RequestParam(required = false) Long materialId,
			@RequestParam(defaultValue = "false") boolean featured,
			@RequestParam(required = false) ProductStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtStart,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtEnd) {
		return ResponseEntity.ok(productService.getAdminProducts(
				page, size, sortBy, term, categoryId, brandId, materialId,
				featured, status, createdAtStart, createdAtEnd));
	}

	@GetMapping("{productId}")
	@Operation(summary = "Get product by id (admin)", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = AdminProductByIdDTO.class))),
		    @ApiResponse(responseCode = "404", description = "Product not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<AdminProductByIdDTO> getProductById(@PathVariable UUID productId) {
		return ResponseEntity.ok(productService.getAdminProductById(productId));
	}

	@PostMapping("{productId}/image")
	@Operation(summary = "Upload product image", responses = {
		    @ApiResponse(responseCode = "200", description = "Upload successful",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))),
		    @ApiResponse(responseCode = "400", description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))),
		    @ApiResponse(responseCode = "404", description = "Product not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<DefaultApiResponse> uploadProductImage(
			@PathVariable UUID productId,
			@RequestParam("file") MultipartFile file) {
		productService.uploadProductImage(file, productId);
		DefaultApiResponse response = DefaultApiResponse.builder()
				.status(HttpStatus.OK.value())
				.message(messageService.getMessage("image_uploaded", LocaleUtils.getDefaultLocale()))
				.build();
		return ResponseEntity.ok(response);
	}

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
		return ResponseEntity.status(HttpStatus.OK)
				.body(apiResponseService.getApiResponseMessage("product_updated", HttpStatus.OK));
	}

	@DeleteMapping("{productId}")
	@Operation(summary = "Delete product (soft delete)", responses = {
		    @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
		    @ApiResponse(responseCode = "404", description = "Product not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
		productService.deleteProduct(productId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("{productId}/restore")
	@Operation(summary = "Restore deleted product", responses = {
		    @ApiResponse(responseCode = "200", description = "Product restored successfully",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))),
		    @ApiResponse(responseCode = "400", description = "Product is not deleted",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))),
		    @ApiResponse(responseCode = "404", description = "Product not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<DefaultApiResponse> restoreProduct(@PathVariable UUID productId) {
		productService.restoreProduct(productId);
		return ResponseEntity.ok(apiResponseService.getApiResponseMessage("product_restored", HttpStatus.OK));
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

	@GetMapping("categories")
	@Operation(summary = "Get categories by offset pagination", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = CategoriesPageResponse.class)))
		})
	public ResponseEntity<PageResponse<CategoryDTO>> getCategories(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size,
			@RequestParam(defaultValue = "")
			@Valid
			@Size(min = 0, max = 60, message = "Search term must max of 60 characters")
			String term) {
		return ResponseEntity.ok(categoryService.getCategories(page, size, term));
	}

	@GetMapping("categories/{id}")
	@Operation(summary = "Get category by id", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = CategoryByIdDTO.class))),
		    @ApiResponse(responseCode = "404", description = "Category not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<CategoryByIdDTO> getCategoryById(@PathVariable long id) {
		return ResponseEntity.ok(categoryService.getCategoryById(id));
	}

	@GetMapping("materials")
	@Operation(summary = "Get materials by offset pagination", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = MaterialsPageResponse.class)))
		})
	public ResponseEntity<PageResponse<MaterialDTO>> getMaterials(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size,
			@RequestParam(defaultValue = "")
			@Valid
			@Size(min = 0, max = 60, message = "Search term must max of 60 characters")
			String term) {
		return ResponseEntity.ok(materialsService.getMaterials(page, size, term));
	}

	@GetMapping("materials/{id}")
	@Operation(summary = "Get material by id", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = MaterialByIdDTO.class))),
		    @ApiResponse(responseCode = "404", description = "Material not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<MaterialByIdDTO> getMaterialById(@PathVariable long id) {
		return ResponseEntity.ok(materialsService.getMaterialById(id));
	}

	@GetMapping("brands")
	@Operation(summary = "Get brands by offset pagination", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            array = @ArraySchema(schema = @Schema(implementation = BrandPageResponse.class))))
		})
	public ResponseEntity<PageResponse<BrandDTO>> getBrands(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size,
			@RequestParam(defaultValue = "")
			@Valid
			@Size(min = 0, max = 60, message = "Search term must max of 60 characters")
			String term) {
		return ResponseEntity.ok(brandsService.getBrands(page, size, term));
	}

	@GetMapping("brands/{id}")
	@Operation(summary = "Get brand by id", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = BrandByIdDTO.class))),
		    @ApiResponse(responseCode = "404", description = "Brand not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<BrandByIdDTO> getBrandById(@PathVariable long id) {
		return ResponseEntity.ok(brandsService.getBrandById(id));
	}

	@GetMapping("attributes")
	@Operation(summary = "Get attributes by offset pagination", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            array = @ArraySchema(schema = @Schema(implementation = AttributesDTO.class))))
		})
	public ResponseEntity<List<AttributesDTO>> getAttributes(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.ok(attributesService.getAttributes(page, size));
	}

	@GetMapping("attributes/{id}")
	@Operation(summary = "Get attribute by id", responses = {
		    @ApiResponse(responseCode = "200", description = "Content successfully returned",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = AttributeByIdDTO.class))),
		    @ApiResponse(responseCode = "404", description = "Attribute not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class)))
		})
	public ResponseEntity<AttributeByIdDTO> getAttributeById(@PathVariable long id) {
		return ResponseEntity.ok(attributesService.getAttributeById(id));
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
	public ResponseEntity<DefaultApiResponse> createCategory(@Valid @RequestBody CreateCategoryDTO createCategoryDTO) {
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
	
	@PatchMapping("categories/{id}/restore")
	@Operation(summary = "Restore category", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Category restored successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Category is not deleted",
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
	public ResponseEntity<DefaultApiResponse> restoreCategory(@PathVariable long id) {
		categoryService.restoreCategory(id);
		return ResponseEntity.ok(apiResponseService.getApiResponseMessage("category_restored", HttpStatus.OK));
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
	
	@PatchMapping("materials/{id}/restore")
	@Operation(summary = "Restore material", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Material restored successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Material is not deleted",
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
	public ResponseEntity<DefaultApiResponse> restoreMaterial(@PathVariable long id) {
		materialsService.restoreMaterial(id);
		return ResponseEntity.ok(apiResponseService.getApiResponseMessage("material_restored", HttpStatus.OK));
	}

	@DeleteMapping("materials/{id}")
	@Operation(summary = "Delete material", responses = {
		    @ApiResponse(
		        responseCode = "204",
		        description = "Material deleted successfully"
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Material not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<Void> deleteMaterial(@PathVariable long id) {
		materialsService.deleteMaterial(id);
		return ResponseEntity.noContent().build();
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
	
	@PatchMapping("brands/{id}")
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
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Brand not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> updateBrand(@PathVariable long id,
			@RequestBody UpdateBrandDTO updateBrandDTO) {
		brandsService.updateBrand(id, updateBrandDTO);
		return ResponseEntity.status(HttpStatus.OK)
				.body(apiResponseService.getApiResponseMessage("brand_updated", HttpStatus.OK));
	}
	
	@PatchMapping("brands/{id}/restore")
	@Operation(summary = "Restore brand", responses = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Brand restored successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Brand is not deleted",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Brand not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<DefaultApiResponse> restoreBrand(@PathVariable long id) {
		brandsService.restoreBrand(id);
		return ResponseEntity.ok(apiResponseService.getApiResponseMessage("brand_restored", HttpStatus.OK));
	}

	@DeleteMapping("brands/{id}")
	@Operation(summary = "Delete brand", responses = {
		    @ApiResponse(
		        responseCode = "204",
		        description = "Brand deleted successfully"
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Brand not found",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<Void> deleteBrand(@PathVariable long id) {
		brandsService.deleteBrand(id);
		return ResponseEntity.noContent().build();
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
