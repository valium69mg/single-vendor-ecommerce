package com.croman.singlevendorecommerce.products;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.products.dto.BrandByIdDTO;
import com.croman.singlevendorecommerce.products.dto.BrandDTO;
import com.croman.singlevendorecommerce.products.dto.CreateBrandDTO;
import com.croman.singlevendorecommerce.products.entity.Brand;
import com.croman.singlevendorecommerce.products.repository.BrandRepository;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.PaginationUtils;
import com.croman.singlevendorecommerce.utils.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandsService {

	private final BrandRepository brandRepository;
	private final MessageService messageService;
	
	@Transactional(readOnly = true)
	public PageResponse<BrandDTO> getBrands(int page, int size, String term) {
		Pageable pageable;
		Page<Brand> allBrands;
		if (term.isBlank()) {
			pageable = PaginationUtils.getPageable(page, size, "brandId");
			allBrands = brandRepository.findAll(pageable);
		} else {
			pageable = PaginationUtils.getPageable(page, size, "brand_id");
			allBrands = brandRepository.searchByNameOrTranslation(term, pageable);
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
