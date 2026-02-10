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
@RequestMapping("/api/products")
public class ProductsController {

	private final CategoryService categoryService;
	
	@GetMapping("/v1/products/categories")
	public ResponseEntity<Object> getCategories() {
		return ResponseEntity.status(HttpStatus.OK).body(categoryService.getCategories(LocaleUtils.APP_DEFAULT_LANG));
	}
	
}
