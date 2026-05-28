package com.croman.singlevendorecommerce.service.products;

import com.croman.singlevendorecommerce.dto.products.CategoryByIdDTO;
import com.croman.singlevendorecommerce.dto.products.CategoryDTO;
import com.croman.singlevendorecommerce.dto.products.CreateCategoryDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateCategoryDTO;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.storage.StorageService;
import com.croman.singlevendorecommerce.service.thumbnail.ThumbnailJobPublisher;
import com.croman.singlevendorecommerce.utils.FileUtils;
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
	private static final String NAME = "Electrónica";

	private Category category;

	@BeforeEach
	void setUp() {
		category = Category.builder().categoryId(CATEGORY_ID).name(NAME).build();
	}

	// ─── getCategories ───────────────────────────────────────────────────────

	@Test
	void testGetCategoriesReturnsNames() {
	    Page<Category> page = new PageImpl<>(List.of(category));
	    when(categoryRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(page);

	    PageResponse<CategoryDTO> response =
	            categoryService.getCategories(0, 10, "");

	    List<CategoryDTO> result = response.getContent();

	    assertThat(result).hasSize(1);
	    assertThat(result.get(0).getCategoryId()).isEqualTo(CATEGORY_ID);
	    assertThat(result.get(0).getName()).isEqualTo(NAME);
	}

	@Test
	void testGetCategoriesReturnsEmptyListWhenNoCategoriesExist() {
	    when(categoryRepository.findAllNotDeleted(any(Pageable.class)))
	            .thenReturn(Page.empty());

	    PageResponse<CategoryDTO> response =
	            categoryService.getCategories(0, 10, "");

	    assertThat(response.getContent()).isEmpty();
	}

	@Test
	void testGetCategoriesWithSearchTermUsesSearchQuery() {
	    Page<Category> page = new PageImpl<>(List.of(category));

	    when(categoryRepository.searchByName(eq("anillo"), any(Pageable.class)))
	            .thenReturn(page);

	    PageResponse<CategoryDTO> response =
	            categoryService.getCategories(0, 10, "anillo");

	    List<CategoryDTO> result = response.getContent();

	    assertThat(result).hasSize(1);
	    assertThat(result.get(0).getName()).isEqualTo(NAME);

	    verify(categoryRepository)
	            .searchByName(eq("anillo"), any(Pageable.class));
	}


	// ─── getCategoryById ─────────────────────────────────────────────────────

	@Test
	void testGetCategoryByIdReturnsName() {
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

		CategoryByIdDTO result = categoryService.getCategoryById(CATEGORY_ID);

		assertThat(result.getCategoryId()).isEqualTo(CATEGORY_ID);
		assertThat(result.getName()).isEqualTo(NAME);
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
	void testCreateCategoryDTOSaves() {
		CreateCategoryDTO dto = CreateCategoryDTO.builder().name(NAME).build();

		when(categoryRepository.findByName(NAME)).thenReturn(Optional.empty());

		categoryService.createCategoryDTO(dto);

		verify(categoryRepository).save(argThat(c -> NAME.equals(c.getName())));
	}

	@Test
	void testCreateCategoryDTOThrowsWhenCategoryAlreadyExists() {
		CreateCategoryDTO dto = CreateCategoryDTO.builder().name(NAME).build();

		when(categoryRepository.findByName(NAME)).thenReturn(Optional.of(category));
		when(messageService.getMessage(eq("category_already_exists"), any(Locale.class)))
				.thenReturn("Category already exists");

		assertThatThrownBy(() -> categoryService.createCategoryDTO(dto)).isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Category already exists");

		verify(categoryRepository, never()).save(any());
	}

	@Test
	void testCreateCategoryDTOThrowsConflictWhenNameBelongsToSoftDeletedCategory() {
		CreateCategoryDTO dto = CreateCategoryDTO.builder().name(NAME).build();

		category.setDeletedAt(LocalDateTime.now());
		when(categoryRepository.findByName(NAME)).thenReturn(Optional.of(category));
		when(messageService.getMessage(eq("category_was_deleted"), any(Locale.class)))
				.thenReturn("A category with this name was previously deleted");

		assertThatThrownBy(() -> categoryService.createCategoryDTO(dto))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("A category with this name was previously deleted")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getMetadata())
						.containsEntry("categoryId", CATEGORY_ID));

		verify(categoryRepository, never()).save(any());
	}

	// ─── updateCategory ──────────────────────────────────────────────────────

	@Test
	void testUpdateCategoryUpdatesName() {
		UpdateCategoryDTO dto = UpdateCategoryDTO.builder().name("Nueva Electrónica").build();

		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
		when(categoryRepository.findByName("Nueva Electrónica")).thenReturn(Optional.empty());

		categoryService.updateCategory(CATEGORY_ID, dto);

		verify(categoryRepository).save(argThat(c -> "Nueva Electrónica".equals(c.getName())));
	}

	@Test
	void testUpdateCategoryThrowsWhenNameAlreadyTaken() {
		UpdateCategoryDTO dto = UpdateCategoryDTO.builder().name("Collares").build();

		Category other = Category.builder().categoryId(2L).name("Collares").build();
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
		when(categoryRepository.findByName("Collares")).thenReturn(Optional.of(other));
		when(messageService.getMessage(eq("name_already_taken"), any(Locale.class)))
				.thenReturn("Name already taken");

		assertThatThrownBy(() -> categoryService.updateCategory(CATEGORY_ID, dto))
				.isInstanceOf(ApiServiceException.class).hasMessageContaining("Name already taken");

		verify(categoryRepository, never()).save(any());
	}

	@Test
	void testUpdateCategoryThrowsWhenNameIsNull() {
		UpdateCategoryDTO dto = UpdateCategoryDTO.builder().build();
		when(messageService.getMessage(eq("missing_name"), any(Locale.class)))
				.thenReturn("A name must be provided");

		assertThatThrownBy(() -> categoryService.updateCategory(CATEGORY_ID, dto))
				.isInstanceOf(ApiServiceException.class).hasMessageContaining("A name must be provided");

		verifyNoInteractions(categoryRepository);
	}

	@Test
	void testUpdateCategoryThrowsWhenCategoryNotFound() {
		UpdateCategoryDTO dto = UpdateCategoryDTO.builder().name(NAME).build();

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
