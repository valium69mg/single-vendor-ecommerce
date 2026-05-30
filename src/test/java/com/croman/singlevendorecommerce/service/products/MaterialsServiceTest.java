package com.croman.singlevendorecommerce.service.products;

import com.croman.singlevendorecommerce.dto.products.CreateMaterialDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialByIdDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateMaterialDTO;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.entity.products.Material;
import com.croman.singlevendorecommerce.repository.products.MaterialRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialsServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MaterialsService materialsService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private static final Long   MATERIAL_ID    = 1L;
    private static final String NAME           = "Algodón";
    private static final String MISSING_NAME   = "missing_name";
    private static final String MATERIAL_NOT_FOUND = "material_not_found";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private Material material;

    @BeforeEach
    void setUp() {
        material = Material.builder()
                .materialId(MATERIAL_ID)
                .name(NAME)
                .build();
    }

    // ─── getMaterials ─────────────────────────────────────────────────────────

    @Test
    void testGetMaterialsReturnsNames() {
        Page<Material> page = new PageImpl<>(List.of(material));
        when(materialRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(page);

        PageResponse<MaterialDTO> result =
                materialsService.getMaterials(0, 10, "");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMaterialId()).isEqualTo(MATERIAL_ID);
        assertThat(result.getContent().get(0).getName()).isEqualTo(NAME);

        verify(materialRepository).findAllNotDeleted(any(Pageable.class));
    }

    @Test
    void testGetMaterialsReturnsEmptyListWhenNoMaterialsExist() {
        when(materialRepository.findAllNotDeleted(any(Pageable.class)))
                .thenReturn(Page.empty());

        PageResponse<MaterialDTO> result =
                materialsService.getMaterials(0, 10, "");

        assertThat(result.getContent()).isEmpty();

        verify(materialRepository).findAllNotDeleted(any(Pageable.class));
    }

    @Test
    void testGetMaterialsWithSearchTermUsesSearchRepositoryMethod() {
        String term = "alg";

        Page<Material> page = new PageImpl<>(List.of(material));
        when(materialRepository.searchByName(eq(term), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<MaterialDTO> result =
                materialsService.getMaterials(0, 10, term);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(NAME);

        verify(materialRepository).searchByName(eq(term), any(Pageable.class));
    }

    @Test
    void testGetMaterialsWithSearchTermReturnsEmptyWhenNoMatches() {
        String term = "notfound";

        when(materialRepository.searchByName(eq(term), any(Pageable.class)))
                .thenReturn(Page.empty());

        PageResponse<MaterialDTO> result =
                materialsService.getMaterials(0, 10, term);

        assertThat(result.getContent()).isEmpty();

        verify(materialRepository).searchByName(eq(term), any(Pageable.class));
    }

    // ─── getMaterialById ──────────────────────────────────────────────────────

    @Test
    void testGetMaterialByIdReturnsName() {
        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.of(material));

        MaterialByIdDTO result = materialsService.getMaterialById(MATERIAL_ID);

        assertThat(result.getMaterialId()).isEqualTo(MATERIAL_ID);
        assertThat(result.getName()).isEqualTo(NAME);
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
    void testCreateMaterialSaves() {
        CreateMaterialDTO dto = CreateMaterialDTO.builder()
                .name(NAME)
                .build();

        when(materialRepository.findByName(NAME)).thenReturn(Optional.empty());

        materialsService.createMaterial(dto);

        verify(materialRepository).save(argThat(m -> NAME.equals(m.getName())));
    }

    @Test
    void testCreateMaterialThrowsWhenMaterialAlreadyExists() {
        CreateMaterialDTO dto = CreateMaterialDTO.builder()
                .name(NAME)
                .build();

        when(materialRepository.findByName(NAME)).thenReturn(Optional.of(material));
        when(messageService.getMessage(eq("material_already_exists"), any(Locale.class)))
                .thenReturn("Material already exists");

        assertThatThrownBy(() -> materialsService.createMaterial(dto))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Material already exists");

        verify(materialRepository, never()).save(any());
    }

    @Test
    void testCreateMaterialThrowsConflictWhenDeletedMaterialExists() {
        CreateMaterialDTO dto = CreateMaterialDTO.builder().name(NAME).build();

        Material deleted = Material.builder().materialId(MATERIAL_ID).name(NAME).deletedAt(NOW).build();
        when(materialRepository.findByName(NAME)).thenReturn(Optional.of(deleted));
        when(messageService.getMessage(eq("material_was_deleted"), any(Locale.class)))
                .thenReturn("Material was deleted");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> materialsService.createMaterial(dto));

        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode());
        assertEquals(MATERIAL_ID, ex.getMetadata().get("materialId"));
        verify(materialRepository, never()).save(any());
    }

    // ─── updateMaterial ─────────────────────────────────────────────────────

    @Test
    void testUpdateMaterialSuccessfully() {
        Long materialId = 1L;
        UpdateMaterialDTO updateMaterialDTO = UpdateMaterialDTO.builder().name(NAME).build();

        String oldName = "Lana";
        Material materialEntity = new Material(materialId, oldName, NOW, NOW, null);
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(materialEntity));
        when(materialRepository.findByName(NAME)).thenReturn(Optional.empty());

        materialsService.updateMaterial(materialId, updateMaterialDTO);

        verify(materialRepository, times(1)).save(materialEntity);
        assertThat(materialEntity.getName()).isEqualTo(NAME);
    }

    @Test
    void testUpdateMaterialThrowsWhenNameAlreadyTaken() {
        Long materialId = 1L;
        UpdateMaterialDTO updateMaterialDTO = UpdateMaterialDTO.builder().name(NAME).build();

        Material materialEntity = new Material(materialId, "Lana", NOW, NOW, null);
        Material other = new Material(2L, NAME, NOW, NOW, null);
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(materialEntity));
        when(materialRepository.findByName(NAME)).thenReturn(Optional.of(other));
        when(messageService.getMessage(eq("name_already_taken"), any(Locale.class)))
                .thenReturn("Name already taken");

        assertThatThrownBy(() -> materialsService.updateMaterial(materialId, updateMaterialDTO))
                .isInstanceOf(ApiServiceException.class).hasMessageContaining("Name already taken");

        verify(materialRepository, never()).save(any());
    }

    @Test
    void testUpdateMaterialThrowsConflictWhenNameBelongsToDeletedMaterial() {
        Long materialId = 1L;
        UpdateMaterialDTO updateMaterialDTO = UpdateMaterialDTO.builder().name(NAME).build();

        Material materialEntity = new Material(materialId, "Lana", NOW, NOW, null);
        Material deleted = new Material(2L, NAME, NOW, NOW, NOW);
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(materialEntity));
        when(materialRepository.findByName(NAME)).thenReturn(Optional.of(deleted));
        when(messageService.getMessage(eq("material_was_deleted"), any(Locale.class)))
                .thenReturn("Material was deleted");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> materialsService.updateMaterial(materialId, updateMaterialDTO));

        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode());
        assertEquals(2L, ex.getMetadata().get("materialId"));
        verify(materialRepository, never()).save(any());
    }

    @Test
    void testUpdateMaterialNameMissing() {
        Long materialId = 1L;
        UpdateMaterialDTO updateMaterialDTO = UpdateMaterialDTO.builder().build();

        when(messageService.getMessage(MISSING_NAME, LocaleUtils.getDefaultLocale()))
                .thenReturn(MISSING_NAME);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> materialsService.updateMaterial(materialId, updateMaterialDTO));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        assertEquals(MISSING_NAME, ex.getMessage());
    }

    @Test
    void testUpdateMaterialMaterialNotFound() {
        Long materialId = 1L;
        UpdateMaterialDTO updateMaterialDTO = UpdateMaterialDTO.builder().name(NAME).build();

        when(materialRepository.findById(materialId)).thenReturn(Optional.empty());
        when(messageService.getMessage(MATERIAL_NOT_FOUND, LocaleUtils.getDefaultLocale()))
                .thenReturn(MATERIAL_NOT_FOUND);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> materialsService.updateMaterial(materialId, updateMaterialDTO));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        assertEquals(MATERIAL_NOT_FOUND, ex.getMessage());
    }

    // ─── deleteMaterial ─────────────────────────────────────────────────────

    @Test
    void testShouldDeleteMaterialSuccessfully() {
        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.of(material));

        materialsService.deleteMaterial(MATERIAL_ID);

        verify(materialRepository).save(argThat(m -> m.getDeletedAt() != null));
    }

    @Test
    void testShouldThrowNotFoundOnDeleteMaterial() {
        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(MATERIAL_NOT_FOUND, LocaleUtils.getDefaultLocale()))
                .thenReturn(MATERIAL_NOT_FOUND);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> materialsService.deleteMaterial(MATERIAL_ID));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        assertEquals(MATERIAL_NOT_FOUND, ex.getMessage());
        verify(materialRepository, never()).save(any());
    }

    // ─── restoreMaterial ─────────────────────────────────────────────────────

    @Test
    void testRestoreMaterialSuccessfully() {
        Material deleted = Material.builder().materialId(MATERIAL_ID).name(NAME).deletedAt(NOW).build();
        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.of(deleted));

        materialsService.restoreMaterial(MATERIAL_ID);

        verify(materialRepository).save(argThat(m -> m.getDeletedAt() == null));
    }

    @Test
    void testRestoreMaterialThrowsWhenNotDeleted() {
        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.of(material));
        when(messageService.getMessage(eq("material_not_deleted"), any(Locale.class)))
                .thenReturn("Material is not deleted");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> materialsService.restoreMaterial(MATERIAL_ID));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void testRestoreMaterialThrowsWhenNotFound() {
        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq(MATERIAL_NOT_FOUND), any(Locale.class)))
                .thenReturn("Material not found");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> materialsService.restoreMaterial(MATERIAL_ID));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(materialRepository, never()).save(any());
    }

}
