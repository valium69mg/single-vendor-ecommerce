package com.croman.singlevendorecommerce.service.products;

import com.croman.singlevendorecommerce.dto.products.AttributeByIdDTO;
import com.croman.singlevendorecommerce.dto.products.AttributeType;
import com.croman.singlevendorecommerce.dto.products.AttributesDTO;
import com.croman.singlevendorecommerce.dto.products.CreateAttributeDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateAttributeDTO;
import com.croman.singlevendorecommerce.dto.translations.TranslatorPropertyType;
import com.croman.singlevendorecommerce.entity.products.Attribute;
import com.croman.singlevendorecommerce.entity.products.AttributeValue;
import com.croman.singlevendorecommerce.repository.products.AttributeRepository;
import com.croman.singlevendorecommerce.repository.products.AttributeValueRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.translations.TranslationService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private MessageService messageService;

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
    private static final String ATTRIBUTE_NOT_FOUND_MSG  = "Attribute not found";
    private static final String ATTRIBUTE_NOT_FOUND_KEY  = "attribute_not_found";
    private static final String ATTRIBUTE_EXISTS_MSG     = "Attribute already exists";
    private static final String ATTRIBUTE_EXISTS_KEY     = "attribute_already_exists";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private Attribute colorAttribute;
    private Attribute sizeAttribute;
    private AttributeValue colorValue;
    private AttributeValue sizeValue;

    @BeforeEach
    void setUp() {
        colorAttribute = new Attribute(COLOR_ATTRIBUTE_ID, "COLOR", NOW, NOW);
        sizeAttribute  = new Attribute(SIZE_ATTRIBUTE_ID,  "SIZE",  NOW, NOW);
        colorValue     = new AttributeValue(COLOR_VALUE_ID, colorAttribute, "Red", NOW, NOW);
        sizeValue      = new AttributeValue(SIZE_VALUE_ID,  sizeAttribute,  "M",   NOW, NOW);
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
        assertThat(result.get(0).getAttributeValues().get(0).getValue()).isEqualTo("M");
        verify(translationService, never()).batchTranslate(anyString(), eq(TranslatorPropertyType.COLOR), anyList());
    }

    @Test
    void testGetAttributesWithSpanishLanguageFallsBackToOriginalValueWhenColorTranslationMissing() {
        Page<Attribute> page = new PageImpl<>(List.of(colorAttribute));
        HashMap<Integer, String> attributeTranslations = new HashMap<>();
        attributeTranslations.put(COLOR_ATTRIBUTE_ID.intValue(), COLOR_TRANSLATED);

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

    // ─── getAttributeById ─────────────────────────────────────────────────────

    @Test
    void testGetAttributeByIdReturnsDTO() {
        HashMap<Integer, String> translation = new HashMap<>();
        translation.put(COLOR_ATTRIBUTE_ID.intValue(), COLOR_TRANSLATED);

        when(attributeRepository.findById(COLOR_ATTRIBUTE_ID)).thenReturn(Optional.of(colorAttribute));
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(colorValue));
        when(translationService.batchTranslate(eq(SPANISH_LANG), eq(TranslatorPropertyType.ATTRIBUTE), anyList()))
                .thenReturn(translation);

        AttributeByIdDTO result = attributesService.getAttributeById(COLOR_ATTRIBUTE_ID);

        assertThat(result.getAttributeId()).isEqualTo(COLOR_ATTRIBUTE_ID);
        assertThat(result.getAttributeType()).isEqualTo("COLOR");
        assertThat(result.getSpanishName()).isEqualTo(COLOR_TRANSLATED);
        assertThat(result.getAttributeValues()).hasSize(1);
        assertThat(result.getAttributeValues().get(0).getValue()).isEqualTo("Red");
    }

    @Test
    void testGetAttributeByIdThrowsWhenNotFound() {
        when(attributeRepository.findById(COLOR_ATTRIBUTE_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq(ATTRIBUTE_NOT_FOUND_KEY), any(Locale.class)))
                .thenReturn(ATTRIBUTE_NOT_FOUND_MSG);

        assertThatThrownBy(() -> attributesService.getAttributeById(COLOR_ATTRIBUTE_ID))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining(ATTRIBUTE_NOT_FOUND_MSG);
    }

    // ─── createAttribute ──────────────────────────────────────────────────────

    @Test
    void testCreateAttributeSavesAndCreatesTranslation() {
        CreateAttributeDTO dto = CreateAttributeDTO.builder()
                .attributeType("TEXTURE")
                .spanishName("Textura")
                .build();

        when(attributeRepository.findByAttributeType("TEXTURE")).thenReturn(Optional.empty());
        when(attributeRepository.save(any())).thenReturn(
                Attribute.builder().attributeId(5L).attributeType("TEXTURE").build());

        attributesService.createAttribute(dto);

        verify(attributeRepository).save(argThat(a -> "TEXTURE".equals(a.getAttributeType())));
        verify(translationService).createTranslation(eq(5), eq(SPANISH_LANG),
                eq(TranslatorPropertyType.ATTRIBUTE), eq("Textura"));
    }

    @Test
    void testCreateAttributeNormalizesTypeToUpperCase() {
        CreateAttributeDTO dto = CreateAttributeDTO.builder()
                .attributeType("texture")
                .spanishName("Textura")
                .build();

        when(attributeRepository.findByAttributeType("TEXTURE")).thenReturn(Optional.empty());
        when(attributeRepository.save(any())).thenReturn(
                Attribute.builder().attributeId(5L).attributeType("TEXTURE").build());

        attributesService.createAttribute(dto);

        verify(attributeRepository).save(argThat(a -> "TEXTURE".equals(a.getAttributeType())));
    }

    @Test
    void testCreateAttributeThrowsWhenAlreadyExists() {
        CreateAttributeDTO dto = CreateAttributeDTO.builder()
                .attributeType("COLOR")
                .spanishName("Color")
                .build();

        when(attributeRepository.findByAttributeType("COLOR")).thenReturn(Optional.of(colorAttribute));
        when(messageService.getMessage(eq(ATTRIBUTE_EXISTS_KEY), any(Locale.class)))
                .thenReturn(ATTRIBUTE_EXISTS_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> attributesService.createAttribute(dto));

        assertEquals(ATTRIBUTE_EXISTS_MSG, ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(attributeRepository, never()).save(any());
        verifyNoInteractions(translationService);
    }

    // ─── updateAttribute ──────────────────────────────────────────────────────

    @Test
    void testUpdateAttributeUpdatesTranslationSuccessfully() {
        UpdateAttributeDTO dto = UpdateAttributeDTO.builder().spanishName("Color Actualizado").build();

        when(attributeRepository.existsById(COLOR_ATTRIBUTE_ID)).thenReturn(true);

        attributesService.updateAttribute(COLOR_ATTRIBUTE_ID, dto);

        verify(translationService).updateTranslation(eq(COLOR_ATTRIBUTE_ID.intValue()), eq(SPANISH_LANG),
                eq(TranslatorPropertyType.ATTRIBUTE), eq("Color Actualizado"));
    }

    @Test
    void testUpdateAttributeThrowsWhenNotFound() {
        UpdateAttributeDTO dto = UpdateAttributeDTO.builder().spanishName("X").build();

        when(attributeRepository.existsById(COLOR_ATTRIBUTE_ID)).thenReturn(false);
        when(messageService.getMessage(eq(ATTRIBUTE_NOT_FOUND_KEY), any(Locale.class)))
                .thenReturn(ATTRIBUTE_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> attributesService.updateAttribute(COLOR_ATTRIBUTE_ID, dto));

        assertEquals(ATTRIBUTE_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verifyNoInteractions(translationService);
    }

    @Test
    void testUpdateAttributeThrowsWhenSpanishNameIsNull() {
        UpdateAttributeDTO dto = UpdateAttributeDTO.builder().spanishName(null).build();

        when(messageService.getMessage(eq("missing_language_names"), any(Locale.class)))
                .thenReturn("At least one name must be provided");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> attributesService.updateAttribute(COLOR_ATTRIBUTE_ID, dto));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(attributeRepository, never()).existsById(anyLong());
    }

    // ─── deleteAttribute ──────────────────────────────────────────────────────

    @Test
    void testDeleteAttributeDeletesTranslationAndAttribute() {
        when(attributeRepository.existsById(COLOR_ATTRIBUTE_ID)).thenReturn(true);

        attributesService.deleteAttribute(COLOR_ATTRIBUTE_ID);

        verify(translationService).deleteTranslation(eq(COLOR_ATTRIBUTE_ID.intValue()), eq(SPANISH_LANG),
                eq(TranslatorPropertyType.ATTRIBUTE));
        verify(attributeRepository).deleteById(COLOR_ATTRIBUTE_ID);
    }

    @Test
    void testDeleteAttributeThrowsWhenNotFound() {
        when(attributeRepository.existsById(COLOR_ATTRIBUTE_ID)).thenReturn(false);
        when(messageService.getMessage(eq(ATTRIBUTE_NOT_FOUND_KEY), any(Locale.class)))
                .thenReturn(ATTRIBUTE_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> attributesService.deleteAttribute(COLOR_ATTRIBUTE_ID));

        assertEquals(ATTRIBUTE_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(translationService, never()).deleteTranslation(anyInt(), anyString(), any());
        verify(attributeRepository, never()).deleteById(any());
    }
}
