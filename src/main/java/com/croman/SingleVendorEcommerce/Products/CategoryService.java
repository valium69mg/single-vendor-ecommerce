package com.croman.SingleVendorEcommerce.Products;

import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.Products.Repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	
	
	
}
