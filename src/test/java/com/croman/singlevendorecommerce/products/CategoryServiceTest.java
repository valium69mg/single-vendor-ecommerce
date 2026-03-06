package com.croman.singlevendorecommerce.products;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.products.dto.CategoryByIdDTO;
import com.croman.singlevendorecommerce.products.dto.CategoryDTO;
import com.croman.singlevendorecommerce.products.dto.CreateCategoryDTO;
import com.croman.singlevendorecommerce.products.dto.UpdateCategoryDTO;
import com.croman.singlevendorecommerce.products.entity.Category;
import com.croman.singlevendorecommerce.products.repository.CategoryRepository;
import com.croman.singlevendorecommerce.translations.TranslationService;
import com.croman.singlevendorecommerce.translations.dto.TranslatorPropertyType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
		when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

		List<CategoryDTO> result = categoryService.getCategories(DEFAULT_LANG, 0, 10);

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

		when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);
		when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.CATEGORY), anyList()))
				.thenReturn(translations);

		List<CategoryDTO> result = categoryService.getCategories(SPANISH_LANG, 0, 10);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo(SPANISH_NAME);
	}

	@Test
	void testGetCategoriesReturnsEmptyListWhenNoCategoriesExist() {
		when(categoryRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

		List<CategoryDTO> result = categoryService.getCategories(DEFAULT_LANG, 0, 10);

		assertThat(result).isEmpty();
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
		verify(translationService).createTranslation(eq(CATEGORY_ID.intValue()), eq(SPANISH_LANG),
				eq(TranslatorPropertyType.CATEGORY), eq(SPANISH_NAME));
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
		verify(translationService).updateTranslation(eq(CATEGORY_ID.intValue()), eq(SPANISH_LANG),
				eq(TranslatorPropertyType.CATEGORY), eq("Nueva Electrónica"));
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
	void testDeleteCategoryDeletesTranslationAndCategory() {
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

		categoryService.deleteCategory(CATEGORY_ID);

		verify(translationService).deleteTranslation(eq(CATEGORY_ID.intValue()), eq(SPANISH_LANG),
				eq(TranslatorPropertyType.CATEGORY));
		verify(categoryRepository).delete(category);
	}

	@Test
	void testDeleteCategoryThrowsWhenCategoryNotFound() {
		when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
		when(messageService.getMessage(eq("category_not_found"), any(Locale.class))).thenReturn("Category not found");

		assertThatThrownBy(() -> categoryService.deleteCategory(CATEGORY_ID)).isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Category not found");

		verify(categoryRepository, never()).delete(any());
		verifyNoInteractions(translationService);
	}
}