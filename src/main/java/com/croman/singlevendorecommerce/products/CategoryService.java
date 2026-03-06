package com.croman.singlevendorecommerce.products;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.croman.singlevendorecommerce.products.dto.CategoryByIdDTO;
import com.croman.singlevendorecommerce.products.dto.CategoryDTO;
import com.croman.singlevendorecommerce.products.dto.CreateCategoryDTO;
import com.croman.singlevendorecommerce.products.dto.UpdateCategoryDTO;
import com.croman.singlevendorecommerce.products.entity.Category;
import com.croman.singlevendorecommerce.products.repository.CategoryRepository;
import com.croman.singlevendorecommerce.translations.TranslationService;
import com.croman.singlevendorecommerce.translations.dto.TranslatorPropertyType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final TranslationService translationService;
	private final MessageService messageService;
	private static final String CATEGORY_NOT_FOUND_CODE = "category_not_found";

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
						messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale())));
		
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
	
	@Transactional
	public void updateCategory(Long categoryId, UpdateCategoryDTO updateCategoryDTO) {
		String englishName = updateCategoryDTO.getEnglishName();
		String spanishName = updateCategoryDTO.getSpanishName();

		if (englishName == null && spanishName == null) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("missing_language_names", LocaleUtils.getDefaultLocale()));
		}

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale())));

		if (englishName != null) {
			category.setName(englishName);
			categoryRepository.save(category);
		}

		if (spanishName != null) {
			translationService.updateTranslation(categoryId.intValue(), LocaleUtils.ES, TranslatorPropertyType.CATEGORY,
					spanishName);
		}

	}
	
	@Transactional
	public void deleteCategory(Long categoryId) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale())));
		
		/* TODO: Validate if there are no products related to this category */

		translationService.deleteTranslation(categoryId.intValue(), LocaleUtils.ES, TranslatorPropertyType.CATEGORY);

		categoryRepository.delete(category);

	}

}
