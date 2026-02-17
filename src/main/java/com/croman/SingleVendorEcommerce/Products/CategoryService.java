package com.croman.SingleVendorEcommerce.Products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Products.DTO.CategoryDTO;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateCategoryDTO;
import com.croman.SingleVendorEcommerce.Products.Entity.Category;
import com.croman.SingleVendorEcommerce.Products.Repository.CategoryRepository;
import com.croman.SingleVendorEcommerce.Translations.LanguageService;
import com.croman.SingleVendorEcommerce.Translations.TranslationService;
import com.croman.SingleVendorEcommerce.Translations.DTO.TranslatorPropertyType;
import com.croman.SingleVendorEcommerce.Translations.Entity.Language;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final TranslationService translationService;
	private final MessageService messageService;

	public List<CategoryDTO> getCategories(String languageName) {

		
		List<Category> allCategories = categoryRepository.findAll();

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

	private CategoryDTO mapCategoryToDTO(Category category, HashMap<Integer, String> batchTranslateHashMap) {
		Integer key = category.getCategoryId().intValue();
		String name = batchTranslateHashMap != null ? batchTranslateHashMap.get(key) : category.getName();
		return CategoryDTO.builder().categoryId(category.getCategoryId()).name(name).build();
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
