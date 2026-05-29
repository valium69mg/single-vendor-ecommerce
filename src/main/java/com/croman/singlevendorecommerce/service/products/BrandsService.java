package com.croman.singlevendorecommerce.service.products;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.products.BrandByIdDTO;
import com.croman.singlevendorecommerce.dto.products.BrandDTO;
import com.croman.singlevendorecommerce.dto.products.CreateBrandDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateBrandDTO;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.entity.products.Brand;
import com.croman.singlevendorecommerce.repository.products.BrandRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.PaginationUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandsService {

	private final BrandRepository brandRepository;
	private final MessageService messageService;
	private static final String BRAND_EXISTS_MESSAGE_KEY = "brand_exists";
	private static final String BRAND_NOT_EXISTS_MESSAGE_KEY = "brand_does_not_exists";
	
	@Transactional(readOnly = true)
	public PageResponse<BrandDTO> getBrands(int page, int size, String term) {
		Pageable pageable;
		Page<Brand> allBrands;
		pageable = PaginationUtils.getPageable(page, size, "brandId");
		if (term.isBlank()) {
			allBrands = brandRepository.findAll(pageable);
		} else {
			allBrands = brandRepository.searchByName(term, pageable);
		}
		
		List<BrandDTO> brandDTOs = new ArrayList<>();
		for (Brand brand : allBrands) {
			brandDTOs.add(mapBrandToBrandDTO(brand));
		}
		
		return PageResponse.<BrandDTO>builder()
	            .content(brandDTOs)
	            .page(allBrands.getNumber())
	            .size(allBrands.getSize())
	            .totalElements(allBrands.getTotalElements())
	            .totalPages(allBrands.getTotalPages())
	            .last(allBrands.isLast())
	            .build();
	}
	
	private BrandDTO mapBrandToBrandDTO(Brand brand) {
		return BrandDTO.builder().brandId(brand.getBrandId())
				.name(brand.getName()).build();
	}
	
	@Transactional(readOnly = true)
	public BrandByIdDTO getBrandById(long brandId) {
		Brand brand = brandRepository.findById(brandId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(BRAND_NOT_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale())));
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
					messageService.getMessage(BRAND_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale()));
		}

		Brand brand = Brand.builder().name(name).build();

		brandRepository.save(brand);

	}
	
	@Transactional
	public void updateBrand(long brandId, UpdateBrandDTO updateBrandDTO) {
		String newName = updateBrandDTO.getName();
		Brand brand = brandRepository.findById(brandId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(BRAND_NOT_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale())));
		
		Optional<Brand> existingBrandOpt = brandRepository.findByName(newName);
	
		boolean brandExists = existingBrandOpt.isPresent() && !existingBrandOpt.get().equals(brand);
	
		if (brandExists) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage(BRAND_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale()));
		}
		
		brand.setName(newName);
		brandRepository.save(brand);
	}

	@Transactional
	public void deleteBrand(Long brandId) {
		Brand brand = brandRepository.findById(brandId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(BRAND_NOT_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale())));

		brandRepository.delete(brand);
	}
	
}
