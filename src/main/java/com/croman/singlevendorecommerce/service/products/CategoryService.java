package com.croman.singlevendorecommerce.service.products;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.croman.singlevendorecommerce.dto.products.CategoryByIdDTO;
import com.croman.singlevendorecommerce.dto.products.CategoryDTO;
import com.croman.singlevendorecommerce.dto.products.CreateCategoryDTO;
import com.croman.singlevendorecommerce.dto.products.PublicCategoryByIdDTO;
import com.croman.singlevendorecommerce.dto.products.PublicCategoryDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateCategoryDTO;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.storage.StorageService;
import com.croman.singlevendorecommerce.service.thumbnail.ThumbnailService;
import com.croman.singlevendorecommerce.utils.FileUtils;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.PaginationUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final MessageService messageService;
	private final StorageService storageService;
	private final ThumbnailService thumbnailService;
	private static final String CATEGORY_NOT_FOUND_CODE = "category_not_found";
	private static final String CATEGORY_SUB_DIRECTORY = "categories/";
	private static final Random RANDOM = new Random();
	private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
			"image/jpeg", "image/png", "image/gif", "image/webp");

	@Transactional(readOnly = true)
	public PageResponse<CategoryDTO> getCategories(int page, int size, String term) {

	    Page<Category> categoryPage;

	    if (!term.isBlank()) {
	    	 Pageable pageable = PaginationUtils.getPageable(page, size, "categoryId");
	        categoryPage = categoryRepository.searchByName(term, pageable);
	    } else {
	    	 Pageable pageable = PaginationUtils.getPageable(page, size, "categoryId");
	        categoryPage = categoryRepository.findAllNotDeleted(pageable);
	    }

	    List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
	            .map(this::mapCategoryToDTO)
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

		if (category.getDeletedAt() != null) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale()));
		}

		return mapCategoryToByIdDTO(category);

	}

	@Transactional(readOnly = true)
	public PageResponse<PublicCategoryDTO> getPublicCategories(int page, int size, String term) {

		Page<Category> categoryPage;

		if (!term.isBlank()) {
			Pageable pageable = PaginationUtils.getPageable(page, size, "categoryId");
			categoryPage = categoryRepository.searchByName(term, pageable);
		} else {
			Pageable pageable = PaginationUtils.getPageable(page, size, "categoryId");
			categoryPage = categoryRepository.findAllNotDeleted(pageable);
		}

		List<PublicCategoryDTO> categoryDTOs = categoryPage.getContent().stream()
				.map(this::mapCategoryToPublicDTO)
				.toList();

		return PageResponse.<PublicCategoryDTO>builder()
				.content(categoryDTOs)
				.page(categoryPage.getNumber())
				.size(categoryPage.getSize())
				.totalElements(categoryPage.getTotalElements())
				.totalPages(categoryPage.getTotalPages())
				.last(categoryPage.isLast())
				.build();
	}

	@Transactional(readOnly = true)
	public PublicCategoryByIdDTO getPublicCategoryById(Long categoryId) {

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale())));

		if (category.getDeletedAt() != null) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale()));
		}

		return mapCategoryToPublicByIdDTO(category);

	}

	private PublicCategoryDTO mapCategoryToPublicDTO(Category category) {
		return PublicCategoryDTO.builder().categoryId(category.getCategoryId()).name(category.getName())
				.products(RANDOM.nextInt(101)).imageUrl(category.getFileUrl())
				.mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(category.getFileUrl()))
				.smallThumbnailUrl(FileUtils.toSmallThumbnailKey(category.getFileUrl()))
				.build();
	}

	private PublicCategoryByIdDTO mapCategoryToPublicByIdDTO(Category category) {
		return PublicCategoryByIdDTO.builder().categoryId(category.getCategoryId()).name(category.getName())
				.products(RANDOM.nextInt(101)).imageUrl(category.getFileUrl())
				.mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(category.getFileUrl()))
				.smallThumbnailUrl(FileUtils.toSmallThumbnailKey(category.getFileUrl()))
				.build();
	}

	private CategoryDTO mapCategoryToDTO(Category category) {
		return CategoryDTO.builder().categoryId(category.getCategoryId()).name(category.getName())
				.products(RANDOM.nextInt(101)).unitsSold(RANDOM.nextInt(101))
				.revenue(new BigDecimal(RANDOM.nextInt(101))).averagePrice(new BigDecimal(RANDOM.nextInt(101)))
				.stock(RANDOM.nextInt(101)).imageUrl(category.getFileUrl())
				.mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(category.getFileUrl()))
				.smallThumbnailUrl(FileUtils.toSmallThumbnailKey(category.getFileUrl()))
				.build();
	}

	private CategoryByIdDTO mapCategoryToByIdDTO(Category category) {
		return CategoryByIdDTO.builder().categoryId(category.getCategoryId()).name(category.getName())
				.products(RANDOM.nextInt(101)).unitsSold(RANDOM.nextInt(101))
				.revenue(new BigDecimal(RANDOM.nextInt(101))).averagePrice(new BigDecimal(RANDOM.nextInt(101)))
				.stock(RANDOM.nextInt(101)).imageUrl(category.getFileUrl())
				.mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(category.getFileUrl()))
				.smallThumbnailUrl(FileUtils.toSmallThumbnailKey(category.getFileUrl()))
				.build();
	}

	@Transactional
	public void createCategoryDTO(CreateCategoryDTO createCategoryDTO) {
		String name = createCategoryDTO.getName();

		Optional<Category> categoryOpt = categoryRepository.findByName(name);

		if (categoryOpt.isPresent()) {
			Category existing = categoryOpt.get();
			if (existing.getDeletedAt() != null) {
				throw new ApiServiceException(HttpStatus.CONFLICT.value(),
						messageService.getMessage("category_was_deleted", LocaleUtils.getDefaultLocale()),
						Map.of("categoryId", existing.getCategoryId()));
			}
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("category_already_exists", LocaleUtils.getDefaultLocale()));
		}

		Category category = Category.builder().name(name).build();

		categoryRepository.save(category);

	}

	@Transactional
	public void uploadImage(MultipartFile file, Long categoryId) {
	    try {
	        String contentType = file.getContentType();
	        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
	            throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
	                    messageService.getMessage("invalid_image_type", LocaleUtils.getDefaultLocale()));
	        }

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

	        thumbnailService.generateThumbnails(key);

	        log.debug("Image uploaded for category {} with ID {}", categoryId, imageId);

	    } catch (Exception e) {
	        log.error("Error uploading image for category {}", categoryId, e);
	        throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
	                messageService.getMessage("image_upload_failed", LocaleUtils.getDefaultLocale()));
	    }
	}


	@Transactional
	public void updateCategory(Long categoryId, UpdateCategoryDTO updateCategoryDTO) {
		String name = updateCategoryDTO.getName();

		if (name == null) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("missing_name", LocaleUtils.getDefaultLocale()));
		}

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale())));

		updateName(category, name);

	}

	private void updateName(Category category, String name) {
		Optional<Category> existingCategoryOpt = categoryRepository.findByName(name);
		
		boolean categoryExistsWithSameName = existingCategoryOpt.isPresent()
				&& !existingCategoryOpt.get().equals(category);
		
		if (categoryExistsWithSameName) {
			if (existingCategoryOpt.get().getDeletedAt() != null) {
				throw new ApiServiceException(HttpStatus.CONFLICT.value(),
						messageService.getMessage("category_was_deleted", LocaleUtils.getDefaultLocale()),
						Map.of("categoryId", existingCategoryOpt.get().getCategoryId()));
			}
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("name_already_taken", LocaleUtils.getDefaultLocale()));
		}
		category.setName(name);
		categoryRepository.save(category);
	}

	@Transactional
	public void restoreCategory(Long categoryId) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale())));

		if (category.getDeletedAt() == null) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("category_not_deleted", LocaleUtils.getDefaultLocale()));
		}

		category.setDeletedAt(null);
		categoryRepository.save(category);
	}

	@Transactional
	public void deleteCategory(Long categoryId) {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(CATEGORY_NOT_FOUND_CODE, LocaleUtils.getDefaultLocale())));

		category.setDeletedAt(LocalDateTime.now());

		categoryRepository.save(category);

	}

}
