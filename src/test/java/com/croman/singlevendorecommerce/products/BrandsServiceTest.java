package com.croman.singlevendorecommerce.products;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.products.dto.BrandByIdDTO;
import com.croman.singlevendorecommerce.products.dto.BrandDTO;
import com.croman.singlevendorecommerce.products.dto.CreateBrandDTO;
import com.croman.singlevendorecommerce.products.entity.Brand;
import com.croman.singlevendorecommerce.products.repository.BrandRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandsServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private BrandsService brandsService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private static final Long   BRAND_ID   = 1L;
    private static final String BRAND_NAME = "Nike";

    private Brand brand;

    @BeforeEach
    void setUp() {
        brand = Brand.builder()
                .brandId(BRAND_ID)
                .name(BRAND_NAME)
                .build();
    }

    // ─── getBrands ───────────────────────────────────────────────────────────

    @Test
    void testGetBrandsReturnsMappedDTOList() {
        Page<Brand> page = new PageImpl<>(List.of(brand));
        when(brandRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<BrandDTO> result = brandsService.getBrands(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrandId()).isEqualTo(BRAND_ID);
        assertThat(result.get(0).getName()).isEqualTo(BRAND_NAME);
    }

    @Test
    void testGetBrandsReturnsEmptyListWhenNoBrandsExist() {
        when(brandRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        List<BrandDTO> result = brandsService.getBrands(0, 10);

        assertThat(result).isEmpty();
    }

    // ─── getBrandById ─────────────────────────────────────────────────────────

    @Test
    void testGetBrandByIdReturnsBrandDTO() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));

        BrandByIdDTO result = brandsService.getBrandById(BRAND_ID);

        assertThat(result.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(result.getName()).isEqualTo(BRAND_NAME);
    }

    @Test
    void testGetBrandByIdThrowsWhenBrandNotFound() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("brand_does_not_exists"), any(Locale.class)))
                .thenReturn("Brand does not exist");

        assertThatThrownBy(() -> brandsService.getBrandById(BRAND_ID))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Brand does not exist");
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
    void testCreateBrandThrowsWhenBrandAlreadyExists() {
        CreateBrandDTO dto = CreateBrandDTO.builder().name(BRAND_NAME).build();

        when(brandRepository.findByName(BRAND_NAME)).thenReturn(Optional.of(brand));
        when(messageService.getMessage(eq("brand_exists"), any(Locale.class)))
                .thenReturn("Brand already exists");

        assertThatThrownBy(() -> brandsService.createBrand(dto))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Brand already exists");

        verify(brandRepository, never()).save(any());
    }
}