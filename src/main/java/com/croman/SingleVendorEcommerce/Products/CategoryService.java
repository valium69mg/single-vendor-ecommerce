package com.croman.SingleVendorEcommerce.Products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Products.DTO.CategoryDTO;
import com.croman.SingleVendorEcommerce.Products.Entity.Category;
import com.croman.SingleVendorEcommerce.Products.Repository.CategoryRepository;
import com.croman.SingleVendorEcommerce.Translations.DTO.LanguageService;
import com.croman.SingleVendorEcommerce.Translations.DTO.TranslationService;
import com.croman.SingleVendorEcommerce.Translations.DTO.TranslatorPropertyType;
import com.croman.SingleVendorEcommerce.Translations.Entity.Language;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final TranslationService translationService;

	public List<CategoryDTO> getCategories(String languageName) {

		HashMap<Integer, String> batchTranslateHashMap = null;

		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			batchTranslateHashMap = translationService.batchTranslate(languageName, TranslatorPropertyType.CATEGORY);
		}

		List<Category> allCategories = categoryRepository.findAll();

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

}
