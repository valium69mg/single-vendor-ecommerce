package com.croman.singlevendorecommerce.service.products;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.croman.singlevendorecommerce.repository.products.BrandSlugHistoryRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.PaginationUtils;
import com.croman.singlevendorecommerce.utils.SlugUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandsService {

	private final BrandRepository brandRepository;
	private final MessageService messageService;
	private final BrandSlugHistoryRepository brandSlugHistoryRepository;
	private static final String BRAND_SLUG_PREFIX = "brand";
	private static final String BRAND_EXISTS_MESSAGE_KEY = "brand_exists";
	private static final String BRAND_NOT_EXISTS_MESSAGE_KEY = "brand_does_not_exists";

	@Transactional(readOnly = true)
	public PageResponse<BrandDTO> getBrands(int page, int size, String term) {
		Pageable pageable = PaginationUtils.getPageable(page, size, "brandId");
		Page<Brand> allBrands;
		if (term.isBlank()) {
			allBrands = brandRepository.findAllNotDeleted(pageable);
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
				.name(brand.getName()).slug(brand.getSlug()).build();
	}

	@Transactional(readOnly = true)
	public BrandByIdDTO getBrandById(long brandId) {
		Brand brand = brandRepository.findById(brandId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(BRAND_NOT_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale())));

		if (brand.getDeletedAt() != null) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage(BRAND_NOT_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale()));
		}

		return mapBrandTBrandByIdDTO(brand);
	}

	private BrandByIdDTO mapBrandTBrandByIdDTO(Brand brand) {
		return BrandByIdDTO.builder().brandId(brand.getBrandId())
				.name(brand.getName()).slug(brand.getSlug()).build();
	}

	@Transactional
	public void createBrand(CreateBrandDTO createBrandDTO) {
		String name = createBrandDTO.getName();

		Optional<Brand> brandOpt = brandRepository.findByName(name);

		if (brandOpt.isPresent()) {
			Brand existing = brandOpt.get();
			if (existing.getDeletedAt() != null) {
				throw new ApiServiceException(HttpStatus.CONFLICT.value(),
						messageService.getMessage("brand_was_deleted", LocaleUtils.getDefaultLocale()),
						Map.of("brandId", existing.getBrandId()));
			}
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage(BRAND_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale()));
		}

		Brand brand = Brand.builder().name(name).build();

		String base = SlugUtils.slugify(name);
		if (base.isEmpty()) {
			// No usable slug from the name yet: persist once to obtain the id, then
			// derive the deterministic "brand-{id}" fallback and persist again.
			brand.setSlug(BRAND_SLUG_PREFIX + "-" + java.util.UUID.randomUUID());
			brandRepository.save(brand);
			brand.setSlug(generateUniqueSlug(null, brand.getBrandId()));
			brandRepository.save(brand);
		} else {
			brand.setSlug(generateUniqueSlug(name, null));
			brandRepository.save(brand);
		}
	}

	/**
	 * Derives a slug from {@code name} (or the {@code brand-{id}} fallback when the
	 * name yields nothing) and appends {@code -2}, {@code -3}, ... until the
	 * candidate collides with neither an active {@code slug} column nor a
	 * {@code brand_slug_history} row.
	 */
	private String generateUniqueSlug(String name, Long idOrNull) {
		String base = SlugUtils.slugify(name);
		if (base.isEmpty()) {
			base = SlugUtils.fallback(BRAND_SLUG_PREFIX, idOrNull);
		}
		String candidate = base;
		int counter = 2;
		while (brandRepository.existsBySlug(candidate) || brandSlugHistoryRepository.existsBySlug(candidate)) {
			candidate = SlugUtils.withCounter(base, counter++);
		}
		return candidate;
	}

	@Transactional
	public void updateBrand(long brandId, UpdateBrandDTO updateBrandDTO) {
		String newName = updateBrandDTO.getName();
		Brand brand = brandRepository.findById(brandId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(BRAND_NOT_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale())));

		Optional<Brand> existingOpt = brandRepository.findByName(newName);
		boolean nameConflict = existingOpt.isPresent() && !existingOpt.get().equals(brand);

		if (nameConflict) {
			Brand existing = existingOpt.get();
			if (existing.getDeletedAt() != null) {
				throw new ApiServiceException(HttpStatus.CONFLICT.value(),
						messageService.getMessage("brand_was_deleted", LocaleUtils.getDefaultLocale()),
						Map.of("brandId", existing.getBrandId()));
			}
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

		brand.setDeletedAt(LocalDateTime.now());
		brandRepository.save(brand);
	}

	@Transactional
	public void restoreBrand(Long brandId) {
		Brand brand = brandRepository.findById(brandId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(BRAND_NOT_EXISTS_MESSAGE_KEY, LocaleUtils.getDefaultLocale())));

		if (brand.getDeletedAt() == null) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("brand_not_deleted", LocaleUtils.getDefaultLocale()));
		}

		brand.setDeletedAt(null);
		brandRepository.save(brand);
	}

}
