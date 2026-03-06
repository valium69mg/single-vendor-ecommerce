package com.croman.singlevendorecommerce.products;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.general.PaginationUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.products.dto.BrandByIdDTO;
import com.croman.singlevendorecommerce.products.dto.BrandDTO;
import com.croman.singlevendorecommerce.products.dto.CreateBrandDTO;
import com.croman.singlevendorecommerce.products.entity.Brand;
import com.croman.singlevendorecommerce.products.repository.BrandRepository;

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
	
	public BrandByIdDTO getBrandById(long brandId) {
		Brand brand = brandRepository.findById(brandId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage("brand_does_not_exists", LocaleUtils.getDefaultLocale())));
		return mapBrandTBrandByIdDTO(brand);
	}
	
	private BrandByIdDTO mapBrandTBrandByIdDTO(Brand brand){
		return BrandByIdDTO.builder().brandId(brand.getBrandId())
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
