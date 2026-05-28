package com.croman.singlevendorecommerce.service.products;

import com.croman.singlevendorecommerce.dto.products.BrandByIdDTO;
import com.croman.singlevendorecommerce.dto.products.BrandDTO;
import com.croman.singlevendorecommerce.dto.products.CreateBrandDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateBrandDTO;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.entity.products.Brand;
import com.croman.singlevendorecommerce.repository.products.BrandRepository;
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
        when(brandRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<BrandDTO> result = brandsService.getBrands(0, 10, "");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBrandId()).isEqualTo(BRAND_ID);
        assertThat(result.getContent().get(0).getName()).isEqualTo(BRAND_NAME);
        verify(brandRepository, never()).searchByName(any(), any());
    }

    @Test
    void testGetBrandsReturnsEmptyListWhenNoBrandsExist() {
        when(brandRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

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
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));

        BrandByIdDTO result = brandsService.getBrandById(BRAND_ID);

        assertThat(result.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(result.getName()).isEqualTo(BRAND_NAME);
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
        when(messageService.getMessage(eq(BRAND_EXISTS_MESSAGE_KEY), any(Locale.class)))
                .thenReturn(BRAND_EXISTS_MESSAGE);

        assertThatThrownBy(() -> brandsService.createBrand(dto))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining(BRAND_EXISTS_MESSAGE);

        verify(brandRepository, never()).save(any());
    }
    
	@Test
	void testUpdateBrandBrandExists() {
		// Arrange
		UpdateBrandDTO updateBrandDTO = UpdateBrandDTO.builder().brandId(BRAND_ID).name(BRAND_NAME_2).build();
		
		when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
		when(brandRepository.findByName(BRAND_NAME_2)).thenReturn(Optional.of(brand2));
		 when(messageService.getMessage(eq(BRAND_EXISTS_MESSAGE_KEY), any(Locale.class)))
         .thenReturn(BRAND_EXISTS_MESSAGE);
		// Act
		ApiServiceException ex = assertThrows(ApiServiceException.class,
				() -> brandsService.updateBrand(updateBrandDTO));
		// Assert
		assertEquals(BRAND_EXISTS_MESSAGE, ex.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
	}
	
	@Test
	void testUpdateBrandSuccessfully() {
		// Arrange
		UpdateBrandDTO updateBrandDTO = UpdateBrandDTO.builder().brandId(BRAND_ID).name(BRAND_NAME_2).build();
		
		when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
		when(brandRepository.findByName(BRAND_NAME_2)).thenReturn(Optional.empty());
		
		// Act
		brandsService.updateBrand(updateBrandDTO);
		// Assert
		verify(brandRepository, times(1)).save(any());
	}
	
	@Test
	void testDeleteBrandBrandNotFound() {
		// Arrange
		when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
		when(messageService.getMessage(eq(BRAND_NOT_EXISTS_MESSAGE_KEY), any(Locale.class)))
        .thenReturn(BRAND_NOT_EXISTS_MESSAGE);
		// Act
		ApiServiceException ex = assertThrows(ApiServiceException.class,
				() ->brandsService.deleteBrand(BRAND_ID));
		// Assert
		verify(brandRepository, never()).delete(any());
		assertEquals(BRAND_NOT_EXISTS_MESSAGE, ex.getMessage());
		assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
	}
	
	@Test
	void testDeleteBrandSuccessfully() {
		// Arrange
		when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
		// Act
		brandsService.deleteBrand(BRAND_ID);
		// Assert
		verify(brandRepository, times(1)).delete(any());
	}
}