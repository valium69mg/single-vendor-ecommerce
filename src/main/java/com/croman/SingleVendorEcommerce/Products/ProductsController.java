package com.croman.SingleVendorEcommerce.Products;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.General.LocaleUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/")
public class ProductsController {

	private final CategoryService categoryService;
	private final MaterialsService materialsService;
	
	@GetMapping("categories")
	public ResponseEntity<Object> getCategories() {
		return ResponseEntity.status(HttpStatus.OK).body(categoryService.getCategories(LocaleUtils.APP_DEFAULT_LANG));
	}
	
	@GetMapping("materials")
	public ResponseEntity<Object> getMaterials() {
		return ResponseEntity.status(HttpStatus.OK).body(materialsService.getMaterials(LocaleUtils.APP_DEFAULT_LANG));
	}

}
