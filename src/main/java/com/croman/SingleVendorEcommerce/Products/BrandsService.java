package com.croman.SingleVendorEcommerce.Products;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.asn1.LocaleUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.General.PaginationUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Products.DTO.BrandDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateBrandDTO;
import com.croman.SingleVendorEcommerce.Products.Entity.Brand;
import com.croman.SingleVendorEcommerce.Products.Repository.BrandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandsService {

	private final BrandRepository brandRepository;
	private final MessageService messageService;
	
	public List<BrandDTO> getBrands(int page, int size) {
		Pageable pageable = PaginationUtils.getPageable(page, size, "brandId");
		List<Brand> allBrands = brandRepository.findAll(pageable).getContent();
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
	
	@Transactional
	public void createBrand(CreateBrandDTO createBrandDTO) {

		String name = createBrandDTO.getName();

		Optional<Brand> brandOpt = brandRepository.findByName(name);

		if (brandOpt.isPresent()) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("brand_exists", LocaleUtils.getDefaultLocale()));
		}

		Brand brand = Brand.builder().name(name).build();

		brandRepository.save(brand);

	}

}
