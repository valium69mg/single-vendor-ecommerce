package com.croman.singlevendorecommerce.service.products;

import com.croman.singlevendorecommerce.dto.products.AttributeByIdDTO;
import com.croman.singlevendorecommerce.dto.products.AttributesDTO;
import com.croman.singlevendorecommerce.dto.products.CreateAttributeDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateAttributeDTO;
import com.croman.singlevendorecommerce.entity.products.Attribute;
import com.croman.singlevendorecommerce.entity.products.AttributeValue;
import com.croman.singlevendorecommerce.repository.products.AttributeRepository;
import com.croman.singlevendorecommerce.repository.products.AttributeValueRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttributesServiceTest {

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private AttributeValueRepository attributeValueRepository;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private AttributesService attributesService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private static final Long   COLOR_ATTRIBUTE_ID       = 1L;
    private static final Long   SIZE_ATTRIBUTE_ID        = 2L;
    private static final Long   COLOR_VALUE_ID           = 10L;
    private static final Long   SIZE_VALUE_ID            = 20L;
    private static final String COLOR_NAME               = "Color";
    private static final String SIZE_NAME                = "Talla";
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
        colorAttribute = new Attribute(COLOR_ATTRIBUTE_ID, "COLOR", COLOR_NAME, NOW, NOW);
        sizeAttribute  = new Attribute(SIZE_ATTRIBUTE_ID,  "SIZE",  SIZE_NAME,  NOW, NOW);
        colorValue     = new AttributeValue(COLOR_VALUE_ID, colorAttribute, "Rojo", NOW, NOW);
        sizeValue      = new AttributeValue(SIZE_VALUE_ID,  sizeAttribute,  "M",    NOW, NOW);
    }

    // ─── getAttributes ────────────────────────────────────────────────────────

    @Test
    void testGetAttributesReturnsNameAndValues() {
        Page<Attribute> page = new PageImpl<>(List.of(colorAttribute));
        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(colorValue));

        List<AttributesDTO> result = attributesService.getAttributes(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAttributeId()).isEqualTo(COLOR_ATTRIBUTE_ID);
        assertThat(result.get(0).getName()).isEqualTo(COLOR_NAME);
        assertThat(result.get(0).getAttributeValues()).hasSize(1);
        assertThat(result.get(0).getAttributeValues().get(0).getValue()).isEqualTo("Rojo");
    }

    @Test
    void testGetAttributesReturnsMultipleAttributesWithTheirValues() {
        Page<Attribute> page = new PageImpl<>(List.of(colorAttribute, sizeAttribute));
        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(colorValue, sizeValue));

        List<AttributesDTO> result = attributesService.getAttributes(0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AttributesDTO::getAttributeId)
                .containsExactly(COLOR_ATTRIBUTE_ID, SIZE_ATTRIBUTE_ID);
        assertThat(result).extracting(AttributesDTO::getName)
                .containsExactly(COLOR_NAME, SIZE_NAME);
    }

    @Test
    void testGetAttributesReturnsEmptyListWhenNoAttributesExist() {
        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of());

        List<AttributesDTO> result = attributesService.getAttributes(0, 10);

        assertThat(result).isEmpty();
    }

    // ─── getAttributeById ─────────────────────────────────────────────────────

    @Test
    void testGetAttributeByIdReturnsDTO() {
        when(attributeRepository.findById(COLOR_ATTRIBUTE_ID)).thenReturn(Optional.of(colorAttribute));
        when(attributeValueRepository.findByAttributeIdIn(anyList())).thenReturn(List.of(colorValue));

        AttributeByIdDTO result = attributesService.getAttributeById(COLOR_ATTRIBUTE_ID);

        assertThat(result.getAttributeId()).isEqualTo(COLOR_ATTRIBUTE_ID);
        assertThat(result.getAttributeType()).isEqualTo("COLOR");
        assertThat(result.getName()).isEqualTo(COLOR_NAME);
        assertThat(result.getAttributeValues()).hasSize(1);
        assertThat(result.getAttributeValues().get(0).getValue()).isEqualTo("Rojo");
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
    void testCreateAttributeSaves() {
        CreateAttributeDTO dto = CreateAttributeDTO.builder()
                .attributeType("TEXTURE")
                .name("Textura")
                .build();

        when(attributeRepository.findByAttributeType("TEXTURE")).thenReturn(Optional.empty());

        attributesService.createAttribute(dto);

        verify(attributeRepository).save(argThat(a ->
                "TEXTURE".equals(a.getAttributeType()) && "Textura".equals(a.getName())));
    }

    @Test
    void testCreateAttributeNormalizesTypeToUpperCase() {
        CreateAttributeDTO dto = CreateAttributeDTO.builder()
                .attributeType("texture")
                .name("Textura")
                .build();

        when(attributeRepository.findByAttributeType("TEXTURE")).thenReturn(Optional.empty());

        attributesService.createAttribute(dto);

        verify(attributeRepository).save(argThat(a -> "TEXTURE".equals(a.getAttributeType())));
    }

    @Test
    void testCreateAttributeThrowsWhenAlreadyExists() {
        CreateAttributeDTO dto = CreateAttributeDTO.builder()
                .attributeType("COLOR")
                .name("Color")
                .build();

        when(attributeRepository.findByAttributeType("COLOR")).thenReturn(Optional.of(colorAttribute));
        when(messageService.getMessage(eq(ATTRIBUTE_EXISTS_KEY), any(Locale.class)))
                .thenReturn(ATTRIBUTE_EXISTS_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> attributesService.createAttribute(dto));

        assertEquals(ATTRIBUTE_EXISTS_MSG, ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(attributeRepository, never()).save(any());
    }

    // ─── updateAttribute ──────────────────────────────────────────────────────

    @Test
    void testUpdateAttributeUpdatesNameSuccessfully() {
        UpdateAttributeDTO dto = UpdateAttributeDTO.builder().name("Color Actualizado").build();

        when(attributeRepository.findById(COLOR_ATTRIBUTE_ID)).thenReturn(Optional.of(colorAttribute));

        attributesService.updateAttribute(COLOR_ATTRIBUTE_ID, dto);

        verify(attributeRepository).save(argThat(a -> "Color Actualizado".equals(a.getName())));
        assertThat(colorAttribute.getName()).isEqualTo("Color Actualizado");
    }

    @Test
    void testUpdateAttributeThrowsWhenNotFound() {
        UpdateAttributeDTO dto = UpdateAttributeDTO.builder().name("X").build();

        when(attributeRepository.findById(COLOR_ATTRIBUTE_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq(ATTRIBUTE_NOT_FOUND_KEY), any(Locale.class)))
                .thenReturn(ATTRIBUTE_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> attributesService.updateAttribute(COLOR_ATTRIBUTE_ID, dto));

        assertEquals(ATTRIBUTE_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(attributeRepository, never()).save(any());
    }

    @Test
    void testUpdateAttributeThrowsWhenNameIsNull() {
        UpdateAttributeDTO dto = UpdateAttributeDTO.builder().name(null).build();

        when(messageService.getMessage(eq("missing_name"), any(Locale.class)))
                .thenReturn("A name must be provided");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> attributesService.updateAttribute(COLOR_ATTRIBUTE_ID, dto));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(attributeRepository, never()).findById(anyLong());
    }

    // ─── deleteAttribute ──────────────────────────────────────────────────────

    @Test
    void testDeleteAttributeDeletesAttribute() {
        when(attributeRepository.existsById(COLOR_ATTRIBUTE_ID)).thenReturn(true);

        attributesService.deleteAttribute(COLOR_ATTRIBUTE_ID);

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
        verify(attributeRepository, never()).deleteById(any());
    }
}
