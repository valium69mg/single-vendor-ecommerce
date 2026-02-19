package com.croman.SingleVendorEcommerce.Products;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.General.PaginationUtils;
import com.croman.SingleVendorEcommerce.Products.DTO.AttributesDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.BrandDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CategoryByIdDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CategoryDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.MaterialDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/")
public class ProductsController {

	private final CategoryService categoryService;
	private final MaterialsService materialsService;
	private final BrandsService brandsService;
	private final AttributesService attributesService;

	@GetMapping("categories")
	public ResponseEntity<List<CategoryDTO>> getCategories(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(categoryService.getCategories(LocaleUtils.APP_DEFAULT_LANG, page, size));
	}
	
	@GetMapping("categories/{id}")
	public ResponseEntity<CategoryByIdDTO> getCategoryById(@PathVariable long id) {
		return ResponseEntity.status(HttpStatus.OK).body(categoryService.getCategoryById(id));
	}

	@GetMapping("materials")
	public ResponseEntity<List<MaterialDTO>> getMaterials(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.status(HttpStatus.OK).body(materialsService.getMaterials(LocaleUtils.APP_DEFAULT_LANG,
				page, size));
	}

	@GetMapping("brands")
	public ResponseEntity<List<BrandDTO>> getBrands(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.status(HttpStatus.OK).body(brandsService.getBrands(page, size));
	}
	
	@GetMapping("attributes")
	public ResponseEntity<List<AttributesDTO>> getAttributes(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(attributesService.getAttributes(LocaleUtils.APP_DEFAULT_LANG, page, size));
	}

}
