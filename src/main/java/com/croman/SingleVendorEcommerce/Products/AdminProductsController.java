package com.croman.SingleVendorEcommerce.Products;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.DTO.ApiResponse;
import com.croman.SingleVendorEcommerce.General.ApiResponseService;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateBrandDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateCategoryDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateMaterialDTO;

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
	public ResponseEntity<ApiResponse> createCategory(@RequestBody CreateCategoryDTO createCategoryDTO) {
		categoryService.createCategoryDTO(createCategoryDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(apiResponseService.getApiResponseMessage("category_created", HttpStatus.CREATED));
	}
	
	@PostMapping("materials")
	public ResponseEntity<ApiResponse> createMaterial(@RequestBody CreateMaterialDTO createMaterialDTO) {
		materialsService.createMaterial(createMaterialDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(apiResponseService.getApiResponseMessage("material_created", HttpStatus.CREATED));
	}
	
	@PostMapping("brands")
	public ResponseEntity<ApiResponse> createBrand(@RequestBody CreateBrandDTO createBrandDTO) {
		brandsService.createBrand(createBrandDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(apiResponseService.getApiResponseMessage("brand_created", HttpStatus.CREATED));
	}
	
}
