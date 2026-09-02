package com.croman.singlevendorecommerce.service.products;

import com.croman.singlevendorecommerce.dto.products.BrandByIdDTO;
import com.croman.singlevendorecommerce.dto.products.BrandDTO;
import com.croman.singlevendorecommerce.dto.products.CreateBrandDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateBrandDTO;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.entity.products.Brand;
import com.croman.singlevendorecommerce.entity.products.BrandSlugHistory;
import com.croman.singlevendorecommerce.repository.products.BrandRepository;
import com.croman.singlevendorecommerce.repository.products.BrandSlugHistoryRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.utils.exceptions.MovedPermanentlyException;

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
class BrandsServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private MessageService messageService;

    @Mock
    private BrandSlugHistoryRepository brandSlugHistoryRepository;

    @InjectMocks
    private BrandsService brandsService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private static final Long   BRAND_ID   = 1L;
    private static final String BRAND_NAME = "Nike";
    private static final String SEARCH_TERM = "Ni";
    private static final Long BRAND_ID_2 = 2L;
    private static final String BRAND_NAME_2 = "Adidas";
    private static final String BRAND_EXISTS_MESSAGE = "Brand already exists";
    private static final String BRAND_EXISTS_MESSAGE_KEY = "brand_exists";
    private static final String BRAND_NOT_EXISTS_MESSAGE_KEY = "brand_does_not_exists";
    private static final String BRAND_NOT_EXISTS_MESSAGE = "Brand does not exist";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private Brand brand;
    private Brand brand2;

    @BeforeEach
    void setUp() {
        brand = Brand.builder()
                .brandId(BRAND_ID)
                .name(BRAND_NAME)
                .build();
        brand2 = Brand.builder()
                .brandId(BRAND_ID_2)
                .name(BRAND_NAME_2)
                .build();
    }

    // ─── getBrands ───────────────────────────────────────────────────────────

    @Test
    void testGetBrandsReturnsMappedDTOList() {
        Page<Brand> page = new PageImpl<>(List.of(brand));
        when(brandRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(page);

        PageResponse<BrandDTO> result = brandsService.getBrands(0, 10, "");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBrandId()).isEqualTo(BRAND_ID);
        assertThat(result.getContent().get(0).getName()).isEqualTo(BRAND_NAME);
        verify(brandRepository, never()).searchByName(any(), any());
    }

    @Test
    void testGetBrandsReturnsEmptyListWhenNoBrandsExist() {
        when(brandRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(Page.empty());

        PageResponse<BrandDTO> result = brandsService.getBrands(0, 10, "");

        assertThat(result.getContent()).isEmpty();
        verify(brandRepository, never()).searchByName(any(), any());
    }

    @Test
    void testGetBrandsBySearchTerm() {
        Page<Brand> page = new PageImpl<>(List.of(brand));
        when(brandRepository.searchByName(eq(SEARCH_TERM), any(Pageable.class))).thenReturn(page);

        PageResponse<BrandDTO> result = brandsService.getBrands(0, 10, SEARCH_TERM);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBrandId()).isEqualTo(BRAND_ID);
        assertThat(result.getContent().get(0).getName()).isEqualTo(BRAND_NAME);
        verify(brandRepository, never()).findAll();
    }

    // ─── getBrandById ─────────────────────────────────────────────────────────

    @Test
    void testGetBrandByIdReturnsBrandDTO() {
        brand.setSlug("nike");
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));

        BrandByIdDTO result = brandsService.getBrandById(BRAND_ID);

        assertThat(result.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(result.getName()).isEqualTo(BRAND_NAME);
        assertThat(result.getSlug()).isEqualTo("nike");
    }

    @Test
    void testGetBrandByIdThrowsWhenBrandNotFound() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq(BRAND_NOT_EXISTS_MESSAGE_KEY), any(Locale.class)))
                .thenReturn(BRAND_NOT_EXISTS_MESSAGE);

        assertThatThrownBy(() -> brandsService.getBrandById(BRAND_ID))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining(BRAND_NOT_EXISTS_MESSAGE);
    }

    @Test
    void testGetBrandByIdThrowsWhenSoftDeleted() {
        Brand deleted = Brand.builder().brandId(BRAND_ID).name(BRAND_NAME).deletedAt(NOW).build();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(deleted));
        when(messageService.getMessage(eq(BRAND_NOT_EXISTS_MESSAGE_KEY), any(Locale.class)))
                .thenReturn(BRAND_NOT_EXISTS_MESSAGE);

        assertThatThrownBy(() -> brandsService.getBrandById(BRAND_ID))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining(BRAND_NOT_EXISTS_MESSAGE);
    }

    // ─── resolveBrandBySlug ──────────────────────────────────────────────────

    @Test
    void testResolveBrandBySlugReturnsDtoForActiveMatch() {
        brand.setSlug("nike");
        when(brandRepository.findBySlug("nike")).thenReturn(Optional.of(brand));

        BrandByIdDTO result = brandsService.resolveBrandBySlug("nike");

        assertThat(result.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(result.getSlug()).isEqualTo("nike");
    }

    @Test
    void testResolveBrandBySlugThrowsMovedPermanentlyWhenSlugIsHistorical() {
        Brand current = Brand.builder().brandId(BRAND_ID_2).name("Nike Air").slug("nike-air").build();
        when(brandRepository.findBySlug("nike")).thenReturn(Optional.empty());
        when(brandSlugHistoryRepository.findBySlug("nike"))
                .thenReturn(Optional.of(new BrandSlugHistory("nike", current, LocalDateTime.now())));

        assertThatThrownBy(() -> brandsService.resolveBrandBySlug("nike"))
                .isInstanceOf(MovedPermanentlyException.class)
                .satisfies(ex -> {
                    MovedPermanentlyException moved = (MovedPermanentlyException) ex;
                    assertThat(moved.getCanonicalSlug()).isEqualTo("nike-air");
                    assertThat(moved.getLocation()).isEqualTo("/api/v1/products/brands/by-slug/nike-air");
                });
    }

    @Test
    void testResolveBrandBySlugThrows404WhenSlugUnknown() {
        when(brandRepository.findBySlug("missing")).thenReturn(Optional.empty());
        when(brandSlugHistoryRepository.findBySlug("missing")).thenReturn(Optional.empty());
        when(messageService.getMessage(eq(BRAND_NOT_EXISTS_MESSAGE_KEY), any(Locale.class)))
                .thenReturn(BRAND_NOT_EXISTS_MESSAGE);

        assertThatThrownBy(() -> brandsService.resolveBrandBySlug("missing"))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining(BRAND_NOT_EXISTS_MESSAGE);
    }

    @Test
    void testResolveBrandBySlugThrows404WhenActiveMatchIsSoftDeletedAndNotHistorical() {
        Brand deleted = Brand.builder().brandId(BRAND_ID).name(BRAND_NAME).slug("nike").deletedAt(NOW).build();
        when(brandRepository.findBySlug("nike")).thenReturn(Optional.of(deleted));
        when(brandSlugHistoryRepository.findBySlug("nike")).thenReturn(Optional.empty());
        when(messageService.getMessage(eq(BRAND_NOT_EXISTS_MESSAGE_KEY), any(Locale.class)))
                .thenReturn(BRAND_NOT_EXISTS_MESSAGE);

        assertThatThrownBy(() -> brandsService.resolveBrandBySlug("nike"))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining(BRAND_NOT_EXISTS_MESSAGE);
    }

    // ─── createBrand ─────────────────────────────────────────────────────────

    @Test
    void testCreateBrandSavesBrandSuccessfully() {
        CreateBrandDTO dto = CreateBrandDTO.builder().name(BRAND_NAME).build();

        when(brandRepository.findByName(BRAND_NAME)).thenReturn(Optional.empty());

        brandsService.createBrand(dto);

        verify(brandRepository).save(argThat(b -> BRAND_NAME.equals(b.getName())));
    }

    @Test
    void testCreateBrandDerivesSlugFromName() {
        CreateBrandDTO dto = CreateBrandDTO.builder().name("Gold Rings").build();
        when(brandRepository.findByName("Gold Rings")).thenReturn(Optional.empty());

        brandsService.createBrand(dto);

        verify(brandRepository).save(argThat(b -> "gold-rings".equals(b.getSlug())));
    }

    @Test
    void testCreateBrandAppendsCounterWhenSlugCollidesWithActiveColumn() {
        CreateBrandDTO dto = CreateBrandDTO.builder().name("Gold Rings").build();
        when(brandRepository.findByName("Gold Rings")).thenReturn(Optional.empty());
        when(brandRepository.existsBySlug("gold-rings")).thenReturn(true);

        brandsService.createBrand(dto);

        verify(brandRepository).save(argThat(b -> "gold-rings-2".equals(b.getSlug())));
    }

    @Test
    void testCreateBrandNeverReusesASlugThatOnlyExistsInHistory() {
        CreateBrandDTO dto = CreateBrandDTO.builder().name("Gold Rings").build();
        when(brandRepository.findByName("Gold Rings")).thenReturn(Optional.empty());
        when(brandSlugHistoryRepository.existsBySlug("gold-rings")).thenReturn(true);

        brandsService.createBrand(dto);

        verify(brandRepository).save(argThat(b -> "gold-rings-2".equals(b.getSlug())));
    }

    @Test
    void testCreateBrandFallsBackToBrandIdSlugWhenNameYieldsNoSlug() {
        CreateBrandDTO dto = CreateBrandDTO.builder().name(null).build();
        java.util.List<String> slugsAtSave = new java.util.ArrayList<>();
        when(brandRepository.findByName(null)).thenReturn(Optional.empty());
        when(brandRepository.save(any())).thenAnswer(invocation -> {
            Brand saved = invocation.getArgument(0);
            slugsAtSave.add(saved.getSlug());
            if (saved.getBrandId() == null) {
                saved.setBrandId(3L);
            }
            return saved;
        });

        brandsService.createBrand(dto);

        verify(brandRepository, times(2)).save(any());
        assertThat(slugsAtSave).hasSize(2);
        assertThat(slugsAtSave.get(0)).isNotBlank();
        assertThat(slugsAtSave.get(1)).isEqualTo("brand-3");
    }

    @Test
    void testCreateBrandThrowsWhenBrandAlreadyExists() {
        CreateBrandDTO dto = CreateBrandDTO.builder().name(BRAND_NAME).build();

        when(brandRepository.findByName(BRAND_NAME)).thenReturn(Optional.of(brand));
        when(messageService.getMessage(eq(BRAND_EXISTS_MESSAGE_KEY), any(Locale.class)))
                .thenReturn(BRAND_EXISTS_MESSAGE);

        assertThatThrownBy(() -> brandsService.createBrand(dto))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining(BRAND_EXISTS_MESSAGE);

        verify(brandRepository, never()).save(any());
    }

    @Test
    void testCreateBrandThrowsConflictWhenDeletedBrandExists() {
        CreateBrandDTO dto = CreateBrandDTO.builder().name(BRAND_NAME).build();

        Brand deleted = Brand.builder().brandId(BRAND_ID).name(BRAND_NAME).deletedAt(NOW).build();
        when(brandRepository.findByName(BRAND_NAME)).thenReturn(Optional.of(deleted));
        when(messageService.getMessage(eq("brand_was_deleted"), any(Locale.class)))
                .thenReturn("Brand was deleted");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> brandsService.createBrand(dto));

        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode());
        assertEquals(BRAND_ID, ex.getMetadata().get("brandId"));
        verify(brandRepository, never()).save(any());
    }

    // ─── updateBrand ─────────────────────────────────────────────────────────

    @Test
    void testUpdateBrandBrandExists() {
        UpdateBrandDTO updateBrandDTO = UpdateBrandDTO.builder().name(BRAND_NAME_2).build();

        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(brandRepository.findByName(BRAND_NAME_2)).thenReturn(Optional.of(brand2));
        when(messageService.getMessage(eq(BRAND_EXISTS_MESSAGE_KEY), any(Locale.class)))
                .thenReturn(BRAND_EXISTS_MESSAGE);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> brandsService.updateBrand(BRAND_ID, updateBrandDTO));

        assertEquals(BRAND_EXISTS_MESSAGE, ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
    }

    @Test
    void testUpdateBrandThrowsConflictWhenNameBelongsToDeletedBrand() {
        UpdateBrandDTO updateBrandDTO = UpdateBrandDTO.builder().name(BRAND_NAME_2).build();

        Brand deleted = Brand.builder().brandId(BRAND_ID_2).name(BRAND_NAME_2).deletedAt(NOW).build();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(brandRepository.findByName(BRAND_NAME_2)).thenReturn(Optional.of(deleted));
        when(messageService.getMessage(eq("brand_was_deleted"), any(Locale.class)))
                .thenReturn("Brand was deleted");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> brandsService.updateBrand(BRAND_ID, updateBrandDTO));

        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode());
        assertEquals(BRAND_ID_2, ex.getMetadata().get("brandId"));
    }

    @Test
    void testUpdateBrandSuccessfully() {
        UpdateBrandDTO updateBrandDTO = UpdateBrandDTO.builder().name(BRAND_NAME_2).build();

        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(brandRepository.findByName(BRAND_NAME_2)).thenReturn(Optional.empty());

        brandsService.updateBrand(BRAND_ID, updateBrandDTO);

        verify(brandRepository, times(1)).save(any());
    }

    // ─── deleteBrand ─────────────────────────────────────────────────────────

    @Test
    void testDeleteBrandBrandNotFound() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq(BRAND_NOT_EXISTS_MESSAGE_KEY), any(Locale.class)))
                .thenReturn(BRAND_NOT_EXISTS_MESSAGE);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> brandsService.deleteBrand(BRAND_ID));

        verify(brandRepository, never()).save(any());
        assertEquals(BRAND_NOT_EXISTS_MESSAGE, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
    }

    @Test
    void testDeleteBrandSuccessfully() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));

        brandsService.deleteBrand(BRAND_ID);

        verify(brandRepository).save(argThat(b -> b.getDeletedAt() != null));
    }

    // ─── restoreBrand ─────────────────────────────────────────────────────────

    @Test
    void testRestoreBrandSuccessfully() {
        Brand deleted = Brand.builder().brandId(BRAND_ID).name(BRAND_NAME).deletedAt(NOW).build();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(deleted));

        brandsService.restoreBrand(BRAND_ID);

        verify(brandRepository).save(argThat(b -> b.getDeletedAt() == null));
    }

    @Test
    void testRestoreBrandThrowsWhenNotDeleted() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(messageService.getMessage(eq("brand_not_deleted"), any(Locale.class)))
                .thenReturn("Brand is not deleted");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> brandsService.restoreBrand(BRAND_ID));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(brandRepository, never()).save(any());
    }

    @Test
    void testRestoreBrandThrowsWhenNotFound() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq(BRAND_NOT_EXISTS_MESSAGE_KEY), any(Locale.class)))
                .thenReturn(BRAND_NOT_EXISTS_MESSAGE);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> brandsService.restoreBrand(BRAND_ID));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(brandRepository, never()).save(any());
    }

}
