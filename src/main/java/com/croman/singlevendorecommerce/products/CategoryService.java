package com.croman.singlevendorecommerce.products;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.FileUtils;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.general.PaginationUtils;
import com.croman.singlevendorecommerce.general.dto.PageResponse;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.products.dto.CategoryByIdDTO;
import com.croman.singlevendorecommerce.products.dto.CategoryDTO;
import com.croman.singlevendorecommerce.products.dto.CreateCategoryDTO;
import com.croman.singlevendorecommerce.products.dto.UpdateCategoryDTO;
import com.croman.singlevendorecommerce.products.entity.Category;
import com.croman.singlevendorecommerce.products.repository.CategoryRepository;
import com.croman.singlevendorecommerce.storage.StorageService;
import com.croman.singlevendorecommerce.thumbnail.ThumbnailJobPublisher;
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
	private final StorageService storageService;
	private final ThumbnailJobPublisher thumbnailJobPublisher;
	private static final String CATEGORY_NOT_FOUND_CODE = "category_not_found";
	private static final String CATEGORY_SUB_DIRECTORY = "categories/";

	@Transactional(readOnly = true)
	public PageResponse<CategoryDTO> getCategories(String languageName, int page, int size, String term) {

	    Page<Category> categoryPage;

	    if (!term.isBlank()) {
	    	 Pageable pageable = PaginationUtils.getPageable(page, size, "category_id");
	        categoryPage = categoryRepository.searchByNameOrTranslation(term, pageable);
	    } else {
	    	 Pageable pageable = PaginationUtils.getPageable(page, size, "categoryId");
	        categoryPage = categoryRepository.findAll(pageable);
	    }

	    List<Category> allCategories = categoryPage.getContent();

	    HashMap<Integer, String> tempTranslateHashMap = null;

	    if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
	        List<Long> categoryIds = allCategories.stream()
	                .map(Category::getCategoryId)
	                .distinct()
	                .toList();

	        tempTranslateHashMap = translationService.batchTranslate(
	                languageName,
	                TranslatorPropertyType.CATEGORY,
	                categoryIds
	        );
	    }

	    final HashMap<Integer, String> batchTranslateHashMap = tempTranslateHashMap;

	    List<CategoryDTO> categoryDTOs = allCategories.stream()
	            .map(category -> mapCategoryToDTO(category, batchTranslateHashMap))
	            .toList();

	    return PageResponse.<CategoryDTO>builder()
	            .content(categoryDTOs)
	            .page(categoryPage.getNumber())
	            .size(categoryPage.getSize())
	            .totalElements(categoryPage.getTotalElements())
	            .totalPages(categoryPage.getTotalPages())
	            .last(categoryPage.isLast())
	            .build();
	}
	
	@Transactional(readOnly = true)
	public CategoryByIdDTO getCategoryById(Long categoryId) {

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale())));
		
		HashMap<Integer, String> batchTranslateHashMap = translationService.batchTranslate(LocaleUtils.ES,
				TranslatorPropertyType.CATEGORY, List.of(category.getCategoryId()));
		
		return mapCategoryToByIdDTO(category, batchTranslateHashMap);

	}

	private CategoryDTO mapCategoryToDTO(Category category, HashMap<Integer, String> batchTranslateHashMap) {
		Random random = new Random();
		Integer key = category.getCategoryId().intValue();
		String name = batchTranslateHashMap != null ? batchTranslateHashMap.get(key) : category.getName();
		return CategoryDTO.builder().categoryId(category.getCategoryId()).name(name)
				.products(random.nextInt(101)).unitsSold(random.nextInt(101))
				.revenue(new BigDecimal(random.nextInt(101))).averagePrice(new BigDecimal(random.nextInt(101)))
				.stock(random.nextInt(101)).imageUrl(category.getFileUrl())
				.mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(category.getFileUrl()))
				.smallThumbnailUrl(FileUtils.toSmallThumbnailKey(category.getFileUrl()))
				.build();
	}
	
	private CategoryByIdDTO mapCategoryToByIdDTO(Category category, HashMap<Integer, String> batchTranslateHashMap) {
		Random random = new Random();
		Integer key = category.getCategoryId().intValue();
		String spanishName = batchTranslateHashMap != null ? batchTranslateHashMap.get(key) : category.getName();
		return CategoryByIdDTO.builder().categoryId(category.getCategoryId()).englishName(category.getName())
				.products(random.nextInt(101)).spanishName(spanishName).unitsSold(random.nextInt(101))
				.revenue(new BigDecimal(random.nextInt(101))).averagePrice(new BigDecimal(random.nextInt(101)))
				.stock(random.nextInt(101)).imageUrl(category.getFileUrl())
				.mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(category.getFileUrl()))
				.smallThumbnailUrl(FileUtils.toSmallThumbnailKey(category.getFileUrl()))
				.build();
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
	public void uploadImage(MultipartFile file, Long categoryId) {
	    try {
	        Category category = categoryRepository.findById(categoryId)
	                .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
	                        messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale())));

	        storageService.delete(category.getFileUrl());
	        storageService.delete(FileUtils.toMediumThumbnailKey(category.getFileUrl()));
	        storageService.delete(FileUtils.toSmallThumbnailKey(category.getFileUrl()));
	        
	        String imageId = UUID.randomUUID().toString();
	        String originalFilename = file.getOriginalFilename();
	        String extension = FileUtils.getFileExtension(originalFilename);
	        String key = CATEGORY_SUB_DIRECTORY + imageId + extension;

	        category.setFileUrl(key);
	        
	        storageService.upload(key, file.getInputStream(), file.getSize(), file.getContentType());

	        thumbnailJobPublisher.publishJob(categoryId.toString(), imageId, key);

	        log.debug("Image uploaded for category {} with ID {}", categoryId, imageId);

	    } catch (Exception e) {
	        log.error("Error uploading image for category {}", categoryId, e);
	        throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
	                messageService.getMessage("image_upload_failed", LocaleUtils.getDefaultLocale()));
	    }
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
