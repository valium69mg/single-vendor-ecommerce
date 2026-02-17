package com.croman.SingleVendorEcommerce.Products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.General.PaginationUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Products.DTO.CategoryByIdDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CategoryDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateCategoryDTO;
import com.croman.SingleVendorEcommerce.Products.Entity.Category;
import com.croman.SingleVendorEcommerce.Products.Repository.CategoryRepository;
import com.croman.SingleVendorEcommerce.Translations.LanguageService;
import com.croman.SingleVendorEcommerce.Translations.TranslationService;
import com.croman.SingleVendorEcommerce.Translations.DTO.TranslatorPropertyType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final TranslationService translationService;
	private final MessageService messageService;

	public List<CategoryDTO> getCategories(String languageName, int page, int size) {
	
		Pageable pageable = PaginationUtils.getPageable(page, size, "categoryId");
		
		List<Category> allCategories = categoryRepository.findAll(pageable).getContent();
		
		HashMap<Integer, String> batchTranslateHashMap = null;
		
		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			List<Long> categoryIds = allCategories.stream().map(Category::getCategoryId).distinct()
					.toList();
			batchTranslateHashMap = translationService.batchTranslate(languageName, TranslatorPropertyType.CATEGORY,
					categoryIds);
		}

		List<CategoryDTO> categoryDTOs = new ArrayList<>();

		for (Category category : allCategories) {
			categoryDTOs.add(mapCategoryToDTO(category, batchTranslateHashMap));
		}

		return categoryDTOs;
	}
	
	public CategoryByIdDTO getCategoryById(Long categoryId) {

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage("category_not_found", LocaleUtils.getDefaultLocale())));
		
		HashMap<Integer, String> batchTranslateHashMap = translationService.batchTranslate(LocaleUtils.ES,
				TranslatorPropertyType.CATEGORY, List.of(category.getCategoryId()));
		
		return mapCategoryToByIdDTO(category, batchTranslateHashMap);

	}

	private CategoryDTO mapCategoryToDTO(Category category, HashMap<Integer, String> batchTranslateHashMap) {
		Integer key = category.getCategoryId().intValue();
		String name = batchTranslateHashMap != null ? batchTranslateHashMap.get(key) : category.getName();
		return CategoryDTO.builder().categoryId(category.getCategoryId()).name(name).build();
	}
	
	private CategoryByIdDTO mapCategoryToByIdDTO(Category category, HashMap<Integer, String> batchTranslateHashMap) {
		Integer key = category.getCategoryId().intValue();
		String spanishName = batchTranslateHashMap != null ? batchTranslateHashMap.get(key) : category.getName();
		return CategoryByIdDTO.builder().categoryId(category.getCategoryId()).englishName(category.getName())
				.spanishName(spanishName).build();
	}
	
	@Transactional
	public void createCategoryDTO(CreateCategoryDTO createCategoryDTO) {
		String englishName = createCategoryDTO.getEnglishName();
		String spanishName = createCategoryDTO.getSpanishName();
			
		Optional<Category> categoryOpt = categoryRepository.findByName(englishName);
		
		if (categoryOpt.isPresent()) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("category_already_exists", LocaleUtils.getDefaultLocale()));
		}
		
		Category category = Category.builder().name(englishName).build();
		
		category = categoryRepository.save(category);
		
		translationService.createTranslation(category.getCategoryId().intValue(), LocaleUtils.ES,
				TranslatorPropertyType.CATEGORY, spanishName);
		
		
	}

}
