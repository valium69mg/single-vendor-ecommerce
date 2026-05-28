package com.croman.singlevendorecommerce.service.products;

import com.croman.singlevendorecommerce.dto.products.CategoryByIdDTO;
import com.croman.singlevendorecommerce.dto.products.CategoryDTO;
import com.croman.singlevendorecommerce.dto.products.CreateCategoryDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateCategoryDTO;
import com.croman.singlevendorecommerce.dto.translations.TranslatorPropertyType;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.storage.StorageService;
import com.croman.singlevendorecommerce.service.thumbnail.ThumbnailJobPublisher;
import com.croman.singlevendorecommerce.service.translations.TranslationService;
import com.croman.singlevendorecommerce.utils.FileUtils;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private TranslationService translationService;

	@Mock
	private MessageService messageService;
	
	@Mock
	private StorageService storageService;

	@Mock
	private ThumbnailJobPublisher thumbnailJobPublisher;

	@Mock
	private MultipartFile multipartFile;

	@InjectMocks
	private CategoryService categoryService;

	// ─── Fixtures ────────────────────────────────────────────────────────────

	private static final Long CATEGORY_ID = 1L;
	private static final String ENGLISH_NAME = "Electronics";
	private static final String SPANISH_NAME = "Electrónica";
	private static final String DEFAULT_LANG = LocaleUtils.DATABASE_DEFAULT_LANG;
	private static final String SPANISH_LANG = LocaleUtils.ES;

	private Category category;

	@BeforeEach
	void setUp() {
		category = Category.builder().categoryId(CATEGORY_ID).name(ENGLISH_NAME).build();
	}

	// ─── getCategories ───────────────────────────────────────────────────────

	@Test
	void testGetCategoriesWithDefaultLanguageReturnsOriginalNames() {
	    Page<Category> page = new PageImpl<>(List.of(category));
	    when(categoryRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(page);

	    PageResponse<CategoryDTO> response =
	            categoryService.getCategories(DEFAULT_LANG, 0, 10, "");

	    List<CategoryDTO> result = response.getContent();

	    assertThat(result).hasSize(1);
	    assertThat(result.get(0).getCategoryId()).isEqualTo(CATEGORY_ID);
	    assertThat(result.get(0).getName()).isEqualTo(ENGLISH_NAME);

	    verifyNoInteractions(translationService);
	}
	
	@Test
	void testGetCategoriesWithSpanishLanguageReturnsTranslatedNames() {
	    Page<Category> page = new PageImpl<>(List.of(category));

	    HashMap<Integer, String> translations = new HashMap<>();
	    translations.put(CATEGORY_ID.intValue(), SPANISH_NAME);

	    when(categoryRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(page);

	    when(translationService.batchTranslate(
	            eq(SPANISH_LANG),
	            eq(TranslatorPropertyType.CATEGORY),
	            anyList()))
	            .thenReturn(translations);

	    PageResponse<CategoryDTO> response =
	            categoryService.getCategories(SPANISH_LANG, 0, 10, "");

	    List<CategoryDTO> result = response.getContent();

	    assertThat(result).hasSize(1);
	    assertThat(result.get(0).getName()).isEqualTo(SPANISH_NAME);
	}
	
	@Test
	void testGetCategoriesReturnsEmptyListWhenNoCategoriesExist() {
	    when(categoryRepository.findAllNotDeleted(any(Pageable.class)))
	            .thenReturn(Page.empty());

	    PageResponse<CategoryDTO> response =
	            categoryService.getCategories(DEFAULT_LANG, 0, 10, "");

	    assertThat(response.getContent()).isEmpty();
	}
	
	@Test
	void testGetCategoriesWithSearchTermUsesSearchQuery() {
	    Page<Category> page = new PageImpl<>(List.of(category));

	    when(categoryRepository.searchByNameOrTranslation(eq("shoe"), any(Pageable.class)))
	            .thenReturn(page);

	    PageResponse<CategoryDTO> response =
	            categoryService.getCategories(DEFAULT_LANG, 0, 10, "shoe");

	    List<CategoryDTO> result = response.getContent();

	    assertThat(result).hasSize(1);
	    assertThat(result.get(0).getName()).isEqualTo(ENGLISH_NAME);

	    verify(categoryRepository)
	            .searchByNameOrTranslation(eq("shoe"), any(Pageable.class));
	}


	// ─── getCategoryById ─────────────────────────────────────────────────────

	@Test
	void testGetCategoryByIdReturnsBothEnglishAndSpanishNames() {
		HashMap<Integer, String> translations = new HashMap<>();
		translations.put(CATEGORY_ID.intValue(), SPANISH_NAME);

		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
		when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.CATEGORY), anyList()))
				.thenReturn(translations);

		CategoryByIdDTO result = categoryService.getCategoryById(CATEGORY_ID);

		assertThat(result.getCategoryId()).isEqualTo(CATEGORY_ID);
		assertThat(result.getEnglishName()).isEqualTo(ENGLISH_NAME);
		assertThat(result.getSpanishName()).isEqualTo(SPANISH_NAME);
	}

	@Test
	void testGetCategoryByIdThrowsWhenCategoryNotFound() {
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
		when(messageService.getMessage(eq("category_not_found"), any(Locale.class))).thenReturn("Category not found");

		assertThatThrownBy(() -> categoryService.getCategoryById(CATEGORY_ID)).isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Category not found");
	}

	// ─── createCategoryDTO ───────────────────────────────────────────────────

	@Test
	void testCreateCategoryDTOSavesAndCreatesTranslation() {
		CreateCategoryDTO dto = CreateCategoryDTO.builder().englishName(ENGLISH_NAME).spanishName(SPANISH_NAME).build();

		when(categoryRepository.findByName(ENGLISH_NAME)).thenReturn(Optional.empty());
		when(categoryRepository.save(any(Category.class))).thenReturn(category);

		categoryService.createCategoryDTO(dto);

		verify(categoryRepository).save(any(Category.class));
		verify(translationService).createTranslation(CATEGORY_ID.intValue(), SPANISH_LANG,
				TranslatorPropertyType.CATEGORY, SPANISH_NAME);
	}

	@Test
	void testCreateCategoryDTOThrowsWhenCategoryAlreadyExists() {
		CreateCategoryDTO dto = CreateCategoryDTO.builder().englishName(ENGLISH_NAME).spanishName(SPANISH_NAME).build();

		when(categoryRepository.findByName(ENGLISH_NAME)).thenReturn(Optional.of(category));
		when(messageService.getMessage(eq("category_already_exists"), any(Locale.class)))
				.thenReturn("Category already exists");

		assertThatThrownBy(() -> categoryService.createCategoryDTO(dto)).isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Category already exists");

		verify(categoryRepository, never()).save(any());
		verifyNoInteractions(translationService);
	}

	@Test
	void testCreateCategoryDTOThrowsConflictWhenNameBelongsToSoftDeletedCategory() {
		CreateCategoryDTO dto = CreateCategoryDTO.builder().englishName(ENGLISH_NAME).spanishName(SPANISH_NAME).build();

		category.setDeletedAt(LocalDateTime.now());
		when(categoryRepository.findByName(ENGLISH_NAME)).thenReturn(Optional.of(category));
		when(messageService.getMessage(eq("category_was_deleted"), any(Locale.class)))
				.thenReturn("A category with this name was previously deleted");

		assertThatThrownBy(() -> categoryService.createCategoryDTO(dto))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("A category with this name was previously deleted")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getMetadata())
						.containsEntry("categoryId", CATEGORY_ID));

		verify(categoryRepository, never()).save(any());
		verifyNoInteractions(translationService);
	}

	// ─── updateCategory ──────────────────────────────────────────────────────

	@Test
	void testUpdateCategoryUpdatesEnglishNameOnly() {
		UpdateCategoryDTO dto = UpdateCategoryDTO.builder().englishName("New Electronics").build();

		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

		categoryService.updateCategory(CATEGORY_ID, dto);

		verify(categoryRepository).save(argThat(c -> "New Electronics".equals(c.getName())));
		verify(translationService, never()).updateTranslation(anyInt(), anyString(), any(), anyString());
	}

	@Test
	void testUpdateCategoryUpdatesSpanishNameOnly() {
		UpdateCategoryDTO dto = UpdateCategoryDTO.builder().spanishName("Nueva Electrónica").build();

		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

		categoryService.updateCategory(CATEGORY_ID, dto);

		verify(categoryRepository, never()).save(any());
		verify(translationService).updateTranslation(CATEGORY_ID.intValue(), SPANISH_LANG,
				TranslatorPropertyType.CATEGORY, "Nueva Electrónica");
	}

	@Test
	void testUpdateCategoryUpdatesBothNames() {
		UpdateCategoryDTO dto = UpdateCategoryDTO.builder().englishName("New Electronics")
				.spanishName("Nueva Electrónica").build();

		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

		categoryService.updateCategory(CATEGORY_ID, dto);

		verify(categoryRepository).save(any(Category.class));
		verify(translationService).updateTranslation(anyInt(), anyString(), any(), anyString());
	}

	@Test
	void testUpdateCategoryThrowsWhenBothNamesAreNull() {
		UpdateCategoryDTO dto = UpdateCategoryDTO.builder().build();
		when(messageService.getMessage(eq("missing_language_names"), any(Locale.class)))
				.thenReturn("Missing language names");

		assertThatThrownBy(() -> categoryService.updateCategory(CATEGORY_ID, dto))
				.isInstanceOf(ApiServiceException.class).hasMessageContaining("Missing language names");

		verifyNoInteractions(categoryRepository);
	}

	@Test
	void testUpdateCategoryThrowsWhenCategoryNotFound() {
		UpdateCategoryDTO dto = UpdateCategoryDTO.builder().englishName(ENGLISH_NAME).build();

		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
		when(messageService.getMessage(eq("category_not_found"), any(Locale.class))).thenReturn("Category not found");

		assertThatThrownBy(() -> categoryService.updateCategory(CATEGORY_ID, dto))
				.isInstanceOf(ApiServiceException.class).hasMessageContaining("Category not found");
	}

	// ─── deleteCategory ──────────────────────────────────────────────────────

	@Test
	void testDeleteCategorySetsSoftDeleteTimestamp() {
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

		categoryService.deleteCategory(CATEGORY_ID);

		assertThat(category.getDeletedAt()).isNotNull();
		verify(categoryRepository).save(category);
	}

	@Test
	void testDeleteCategoryThrowsWhenCategoryNotFound() {
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
		when(messageService.getMessage(eq("category_not_found"), any(Locale.class))).thenReturn("Category not found");

		assertThatThrownBy(() -> categoryService.deleteCategory(CATEGORY_ID)).isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Category not found");

		verify(categoryRepository, never()).save(any());
	}
	
	// ─── restoreCategory ─────────────────────────────────────────────────────

	@Test
	void testRestoreCategoryClearsDeletedAt() {
		category.setDeletedAt(LocalDateTime.now());
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

		categoryService.restoreCategory(CATEGORY_ID);

		assertThat(category.getDeletedAt()).isNull();
		verify(categoryRepository).save(category);
	}

	@Test
	void testRestoreCategoryThrowsWhenCategoryNotDeleted() {
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
		when(messageService.getMessage(eq("category_not_deleted"), any(Locale.class)))
				.thenReturn("Category is not deleted");

		assertThatThrownBy(() -> categoryService.restoreCategory(CATEGORY_ID))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Category is not deleted");

		verify(categoryRepository, never()).save(any());
	}

	@Test
	void testRestoreCategoryThrowsWhenCategoryNotFound() {
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
		when(messageService.getMessage(eq("category_not_found"), any(Locale.class))).thenReturn("Category not found");

		assertThatThrownBy(() -> categoryService.restoreCategory(CATEGORY_ID))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Category not found");

		verify(categoryRepository, never()).save(any());
	}

	@Test
	void testUploadImageUploadsFileAndPublishesThumbnailJob() throws Exception {
	    // Arrange
	    String existingFileUrl = "categories/old-image.jpg";
	    category.setFileUrl(existingFileUrl);

	    when(categoryRepository.findById(CATEGORY_ID))
	            .thenReturn(Optional.of(category));

	    when(multipartFile.getOriginalFilename()).thenReturn("image.png");
	    when(multipartFile.getContentType()).thenReturn("image/png");
	    when(multipartFile.getSize()).thenReturn(10L);

	    InputStream inputStream = new ByteArrayInputStream("image".getBytes());
	    when(multipartFile.getInputStream()).thenReturn(inputStream);

	    // Act
	    assertDoesNotThrow(() ->
	            categoryService.uploadImage(multipartFile, CATEGORY_ID)
	    );

	    // Assert - deletions of old files
	    verify(storageService).delete(existingFileUrl);
	    verify(storageService).delete(FileUtils.toMediumThumbnailKey(existingFileUrl));
	    verify(storageService).delete(FileUtils.toSmallThumbnailKey(existingFileUrl));

	    // Capture the actual key used for upload
	    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
	    verify(storageService).upload(
	            keyCaptor.capture(),
	            any(InputStream.class),
	            eq(10L),
	            eq("image/png")
	    );

	    String actualKey = keyCaptor.getValue();

	    // Verify key structure without caring about the specific UUID value
	    assertThat(actualKey)
	            .startsWith("categories/")
	            .endsWith(".png")
	            .matches("categories/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.png");

	    // Verify thumbnail job published with same key
	    verify(thumbnailJobPublisher).publishJob(actualKey);

	    // Verify category was updated with the new key
	    assertThat(category.getFileUrl())
	            .isEqualTo(actualKey)
	            .startsWith("categories/")
	            .endsWith(".png");
	}

}