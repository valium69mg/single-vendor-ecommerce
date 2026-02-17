package com.croman.SingleVendorEcommerce.Products;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Products.DTO.BrandDTO;
import com.croman.SingleVendorEcommerce.Products.Entity.Brand;
import com.croman.SingleVendorEcommerce.Products.Repository.BrandRepository;
import com.croman.SingleVendorEcommerce.Translations.TranslationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandsService {

	private final BrandRepository brandRepository;
	private final TranslationService translationService;
	private final MessageService messageService;
	
	public List<BrandDTO> getBrands() {
		List<Brand> allBrands = brandRepository.findAll();
		List<BrandDTO> brandDTOs = new ArrayList<>();
		for (Brand brand : allBrands) {
			brandDTOs.add(mapBrandToBrandDTO(brand));
		}
		
		return brandDTOs;
	}
	
	private BrandDTO mapBrandToBrandDTO(Brand brand) {
		return BrandDTO.builder().brandId(brand.getBrandId())
				.name(brand.getName()).build();
	}
	
}
