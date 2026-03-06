package com.croman.singlevendorecommerce.products;

import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.products.dto.AttributeType;
import com.croman.singlevendorecommerce.products.dto.AttributesDTO;
import com.croman.singlevendorecommerce.products.entity.Attribute;
import com.croman.singlevendorecommerce.products.entity.AttributeValue;
import com.croman.singlevendorecommerce.products.repository.AttributeRepository;
import com.croman.singlevendorecommerce.products.repository.AttributeValueRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttributesServiceTest {

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private AttributeValueRepository attributeValueRepository;

    @Mock
    private TranslationService translationService;

    @InjectMocks
    private AttributesService attributesService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private static final Long   COLOR_ATTRIBUTE_ID       = 1L;
    private static final Long   SIZE_ATTRIBUTE_ID        = 2L;
    private static final Long   COLOR_VALUE_ID           = 10L;
    private static final Long   SIZE_VALUE_ID            = 20L;
    private static final String DEFAULT_LANG             = LocaleUtils.DATABASE_DEFAULT_LANG;
    private static final String SPANISH_LANG             = LocaleUtils.ES;
    private static final String COLOR_TRANSLATED         = "Color";
    private static final String SIZE_TRANSLATED          = "Talla";
    private static final String COLOR_VALUE_TRANSLATED   = "Rojo";

    private Attribute colorAttribute;
    private Attribute sizeAttribute;
    private AttributeValue colorValue;
    private AttributeValue sizeValue;

    @BeforeEach
    void setUp() {
        colorAttribute = new Attribute(COLOR_ATTRIBUTE_ID, AttributeType.COLOR);
        sizeAttribute  = new Attribute(SIZE_ATTRIBUTE_ID,  AttributeType.SIZE);
        colorValue     = new AttributeValue(COLOR_VALUE_ID, colorAttribute, "Red");
        sizeValue      = new AttributeValue(SIZE_VALUE_ID,  sizeAttribute,  "M");
    }

    // ─── getAttributes – default language ────────────────────────────────────

    @Test
    void testGetAttributesWithDefaultLanguageReturnsAttributeTypeAsName() {
        Page<Attribute> page = new PageImpl<>(List.of(colorAttribute));
        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(colorValue));

        List<AttributesDTO> result = attributesService.getAttributes(DEFAULT_LANG, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAttributeId()).isEqualTo(COLOR_ATTRIBUTE_ID);
        assertThat(result.get(0).getName()).isEqualTo(AttributeType.COLOR.toString());
        assertThat(result.get(0).getAttributeValues()).hasSize(1);
        assertThat(result.get(0).getAttributeValues().get(0).getValue()).isEqualTo("Red");
        verifyNoInteractions(translationService);
    }

    @Test
    void testGetAttributesWithDefaultLanguageReturnsMultipleAttributesWithTheirValues() {
        Page<Attribute> page = new PageImpl<>(List.of(colorAttribute, sizeAttribute));
        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(colorValue, sizeValue));

        List<AttributesDTO> result = attributesService.getAttributes(DEFAULT_LANG, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AttributesDTO::getAttributeId)
                .containsExactly(COLOR_ATTRIBUTE_ID, SIZE_ATTRIBUTE_ID);
    }

    @Test
    void testGetAttributesReturnsEmptyListWhenNoAttributesExist() {
        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of());

        List<AttributesDTO> result = attributesService.getAttributes(DEFAULT_LANG, 0, 10);

        assertThat(result).isEmpty();
        verifyNoInteractions(translationService);
    }

    // ─── getAttributes – Spanish, COLOR type ─────────────────────────────────

    @Test
    void testGetAttributesWithSpanishLanguageTranslatesAttributeAndColorValue() {
        Page<Attribute> page = new PageImpl<>(List.of(colorAttribute));
        HashMap<Integer, String> attributeTranslations = new HashMap<>();
        attributeTranslations.put(COLOR_ATTRIBUTE_ID.intValue(), COLOR_TRANSLATED);

        HashMap<Integer, String> colorValueTranslations = new HashMap<>();
        colorValueTranslations.put(COLOR_VALUE_ID.intValue(), COLOR_VALUE_TRANSLATED);

        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(colorValue));
        when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.ATTRIBUTE), anyList()))
                .thenReturn(attributeTranslations);
        when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.COLOR), anyList()))
                .thenReturn(colorValueTranslations);

        List<AttributesDTO> result = attributesService.getAttributes(SPANISH_LANG, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(COLOR_TRANSLATED);
        assertThat(result.get(0).getAttributeValues().get(0).getValue()).isEqualTo(COLOR_VALUE_TRANSLATED);
    }

    @Test
    void testGetAttributesWithSpanishLanguageTranslatesAttributeButNotNonColorValues() {
        Page<Attribute> page = new PageImpl<>(List.of(sizeAttribute));
        HashMap<Integer, String> attributeTranslations = new HashMap<>();
        attributeTranslations.put(SIZE_ATTRIBUTE_ID.intValue(), SIZE_TRANSLATED);

        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(sizeValue));
        when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.ATTRIBUTE), anyList()))
                .thenReturn(attributeTranslations);

        List<AttributesDTO> result = attributesService.getAttributes(SPANISH_LANG, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(SIZE_TRANSLATED);
        // SIZE values are not translated — original value is kept
        assertThat(result.get(0).getAttributeValues().get(0).getValue()).isEqualTo("M");
        verify(translationService, never()).batchTranslate(anyString(), eq(TranslatorPropertyType.COLOR), anyList());
    }

    @Test
    void testGetAttributesWithSpanishLanguageFallsBackToOriginalValueWhenColorTranslationMissing() {
        Page<Attribute> page = new PageImpl<>(List.of(colorAttribute));
        HashMap<Integer, String> attributeTranslations = new HashMap<>();
        attributeTranslations.put(COLOR_ATTRIBUTE_ID.intValue(), COLOR_TRANSLATED);

        // color value translation map does NOT contain COLOR_VALUE_ID → getOrDefault falls back
        HashMap<Integer, String> colorValueTranslations = new HashMap<>();

        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(colorValue));
        when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.ATTRIBUTE), anyList()))
                .thenReturn(attributeTranslations);
        when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.COLOR), anyList()))
                .thenReturn(colorValueTranslations);

        List<AttributesDTO> result = attributesService.getAttributes(SPANISH_LANG, 0, 10);

        assertThat(result.get(0).getAttributeValues().get(0).getValue()).isEqualTo("Red");
    }

    @Test
    void testGetAttributesWithSpanishLanguageAndNoColorValuesSkipsColorTranslation() {
        Page<Attribute> page = new PageImpl<>(List.of(sizeAttribute));
        HashMap<Integer, String> attributeTranslations = new HashMap<>();
        attributeTranslations.put(SIZE_ATTRIBUTE_ID.intValue(), SIZE_TRANSLATED);

        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(sizeValue));
        when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.ATTRIBUTE), anyList()))
                .thenReturn(attributeTranslations);

        List<AttributesDTO> result = attributesService.getAttributes(SPANISH_LANG, 0, 10);

        assertThat(result).hasSize(1);
        verify(translationService, never()).batchTranslate(anyString(), eq(TranslatorPropertyType.COLOR), anyList());
    }
}