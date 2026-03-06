package com.croman.singlevendorecommerce.products;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.products.dto.CreateMaterialDTO;
import com.croman.singlevendorecommerce.products.dto.MaterialByIdDTO;
import com.croman.singlevendorecommerce.products.dto.MaterialDTO;
import com.croman.singlevendorecommerce.products.entity.Material;
import com.croman.singlevendorecommerce.products.repository.MaterialRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialsServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private TranslationService translationService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MaterialsService materialsService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private static final Long   MATERIAL_ID    = 1L;
    private static final String ENGLISH_NAME   = "Cotton";
    private static final String SPANISH_NAME   = "Algodón";
    private static final String DEFAULT_LANG   = LocaleUtils.DATABASE_DEFAULT_LANG;
    private static final String SPANISH_LANG   = LocaleUtils.ES;

    private Material material;

    @BeforeEach
    void setUp() {
        material = Material.builder()
                .materialId(MATERIAL_ID)
                .name(ENGLISH_NAME)
                .build();
    }

    // ─── getMaterials ─────────────────────────────────────────────────────────

    @Test
    void testGetMaterialsWithDefaultLanguageReturnsOriginalNames() {
        Page<Material> page = new PageImpl<>(List.of(material));
        when(materialRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<MaterialDTO> result = materialsService.getMaterials(DEFAULT_LANG, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMaterialId()).isEqualTo(MATERIAL_ID);
        assertThat(result.get(0).getName()).isEqualTo(ENGLISH_NAME);
        verifyNoInteractions(translationService);
    }

    @Test
    void testGetMaterialsWithSpanishLanguageReturnsTranslatedNames() {
        Page<Material> page = new PageImpl<>(List.of(material));
        HashMap<Integer, String> translations = new HashMap<>();
        translations.put(MATERIAL_ID.intValue(), SPANISH_NAME);

        when(materialRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.MATERIAL), anyList()))
                .thenReturn(translations);

        List<MaterialDTO> result = materialsService.getMaterials(SPANISH_LANG, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(SPANISH_NAME);
    }

    @Test
    void testGetMaterialsReturnsEmptyListWhenNoMaterialsExist() {
        when(materialRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        List<MaterialDTO> result = materialsService.getMaterials(DEFAULT_LANG, 0, 10);

        assertThat(result).isEmpty();
    }

    // ─── getMaterialById ──────────────────────────────────────────────────────

    @Test
    void testGetMaterialByIdReturnsBothEnglishAndSpanishNames() {
        HashMap<Integer, String> translations = new HashMap<>();
        translations.put(MATERIAL_ID.intValue(), SPANISH_NAME);

        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.of(material));
        when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.MATERIAL), anyList()))
                .thenReturn(translations);

        MaterialByIdDTO result = materialsService.getMaterialById(MATERIAL_ID);

        assertThat(result.getMaterialId()).isEqualTo(MATERIAL_ID);
        assertThat(result.getEnglishName()).isEqualTo(ENGLISH_NAME);
        assertThat(result.getSpanishName()).isEqualTo(SPANISH_NAME);
    }

    @Test
    void testGetMaterialByIdThrowsWhenMaterialNotFound() {
        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("material_not_found"), any(Locale.class)))
                .thenReturn("Material not found");

        assertThatThrownBy(() -> materialsService.getMaterialById(MATERIAL_ID))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Material not found");
    }

    // ─── createMaterial ───────────────────────────────────────────────────────

    @Test
    void testCreateMaterialSavesAndCreatesTranslation() {
        CreateMaterialDTO dto = CreateMaterialDTO.builder()
                .englishName(ENGLISH_NAME)
                .spanishName(SPANISH_NAME)
                .build();

        when(materialRepository.findByName(ENGLISH_NAME)).thenReturn(Optional.empty());
        when(materialRepository.save(any(Material.class))).thenReturn(material);

        materialsService.createMaterial(dto);

        verify(materialRepository).save(any(Material.class));
        verify(translationService).createTranslation(
                eq(MATERIAL_ID.intValue()), eq(SPANISH_LANG),
                eq(TranslatorPropertyType.MATERIAL), eq(SPANISH_NAME));
    }

    @Test
    void testCreateMaterialThrowsWhenMaterialAlreadyExists() {
        CreateMaterialDTO dto = CreateMaterialDTO.builder()
                .englishName(ENGLISH_NAME)
                .spanishName(SPANISH_NAME)
                .build();

        when(materialRepository.findByName(ENGLISH_NAME)).thenReturn(Optional.of(material));
        when(messageService.getMessage(eq("material_already_exists"), any(Locale.class)))
                .thenReturn("Material already exists");

        assertThatThrownBy(() -> materialsService.createMaterial(dto))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Material already exists");

        verify(materialRepository, never()).save(any());
        verifyNoInteractions(translationService);
    }
}