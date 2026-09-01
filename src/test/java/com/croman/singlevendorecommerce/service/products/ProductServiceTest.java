package com.croman.singlevendorecommerce.service.products;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.croman.singlevendorecommerce.dto.products.AdminProductByIdDTO;
import com.croman.singlevendorecommerce.dto.products.AdminProductDTO;
import com.croman.singlevendorecommerce.dto.products.CreateProductDTO;
import com.croman.singlevendorecommerce.dto.products.CreateProductVariantDTO;
import com.croman.singlevendorecommerce.dto.products.ProductBasicInfoDTO;
import com.croman.singlevendorecommerce.dto.products.ProductStatus;
import com.croman.singlevendorecommerce.dto.products.PublicProductByIdDTO;
import com.croman.singlevendorecommerce.dto.products.PublicProductDTO;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.service.storage.StorageService;
import com.croman.singlevendorecommerce.service.thumbnail.ThumbnailService;
import com.croman.singlevendorecommerce.entity.products.AttributeValue;
import com.croman.singlevendorecommerce.entity.products.Brand;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.entity.products.Material;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.entity.products.ProductMaterial;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.repository.products.AttributeValueRepository;
import com.croman.singlevendorecommerce.repository.products.BrandRepository;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.MaterialRepository;
import com.croman.singlevendorecommerce.repository.products.ProductMaterialRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantAttributeRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private ProductMaterialRepository productMaterialRepository;
    @Mock private ProductVariantAttributeRepository productVariantAttributeRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private AttributeValueRepository attributeValueRepository;
    @Mock private MessageService messageService;
    @Mock private StorageService storageService;
    @Mock private ThumbnailService thumbnailService;
    @Mock private MultipartFile multipartFile;

    @InjectMocks
    private ProductService productService;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private static final Long   CATEGORY_ID         = 1L;
    private static final Long   BRAND_ID             = 2L;
    private static final Long   MATERIAL_ID          = 3L;
    private static final Long   ATTRIBUTE_VALUE_ID   = 4L;
    private static final String SKU                  = "RING-GOLD-001";
    private static final String SKU_2                = "RING-SILVER-001";
    private static final String PRODUCT_NAME         = "Gold Ring";

    private static final String CATEGORY_NOT_FOUND_MSG       = "Category not found";
    private static final String BRAND_NOT_FOUND_MSG           = "Brand not found";
    private static final String MATERIAL_NOT_FOUND_MSG        = "Material not found";
    private static final String ATTRIBUTE_VALUE_NOT_FOUND_MSG = "Attribute value not found";
    private static final String SKU_EXISTS_MSG                = "SKU already exists";
    private static final String PRODUCT_NOT_FOUND_MSG         = "Product not found";

    private Category       category;
    private Brand          brand;
    private Material       material;
    private AttributeValue attributeValue;
    private Product        savedProduct;
    private ProductVariant savedVariant;

    @BeforeEach
    void setUp() {
        category = Category.builder().categoryId(CATEGORY_ID).name("Rings").build();
        brand    = Brand.builder().brandId(BRAND_ID).name("Tiffany").build();
        material = Material.builder().materialId(MATERIAL_ID).name("Gold").build();

        attributeValue = new AttributeValue();
        attributeValue.setAttributeValueId(ATTRIBUTE_VALUE_ID);

        savedProduct = Product.builder()
                .productId(UUID.randomUUID())
                .name(PRODUCT_NAME)
                .status(ProductStatus.ACTIVE)
                .featured(false)
                .build();

        savedVariant = new ProductVariant();
        savedVariant.setProductVariantId(10L);
        savedVariant.setSku(SKU);
        savedVariant.setProduct(savedProduct);
        savedVariant.setPrice(BigDecimal.valueOf(100));
        savedVariant.setStock(5);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private CreateProductVariantDTO variantDTO(String sku, List<Long> avIds) {
        return CreateProductVariantDTO.builder()
                .sku(sku)
                .price(BigDecimal.valueOf(100))
                .stock(5)
                .attributeValueIds(avIds)
                .build();
    }

    private CreateProductDTO minimalProductDTO(List<CreateProductVariantDTO> variants) {
        return CreateProductDTO.builder()
                .name(PRODUCT_NAME)
                .status(ProductStatus.ACTIVE)
                .featured(false)
                .variants(variants)
                .build();
    }

    /**
     * Stubs the five repository calls every successful product creation goes through
     * when there are no external relationships (no category, brand, material, or attribute values).
     * Only call this in tests where the full service flow completes successfully.
     */
    private void stubMinimalHappyPath() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant));
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());
    }

    // ─── createProduct – happy paths ──────────────────────────────────────────

    @Test
    void testCreateProductSavesProductSuccessfullyWithAllRelationships() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(materialRepository.findAllById(anyList())).thenReturn(List.of(material));
        when(attributeValueRepository.findAllById(anyList())).thenReturn(List.of(attributeValue));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant));
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        CreateProductDTO dto = CreateProductDTO.builder()
                .name(PRODUCT_NAME)
                .status(ProductStatus.ACTIVE)
                .categoryId(CATEGORY_ID)
                .brandId(BRAND_ID)
                .materialIds(List.of(MATERIAL_ID))
                .variants(List.of(variantDTO(SKU, List.of(ATTRIBUTE_VALUE_ID))))
                .build();

        assertDoesNotThrow(() -> productService.createProduct(dto));

        verify(productRepository).save(any(Product.class));
        verify(productVariantRepository).saveAll(anyList());
        verify(productVariantAttributeRepository).saveAll(anyList());
        verify(productMaterialRepository).saveAll(anyList());
    }

    @Test
    void testCreateProductSavesSuccessfullyWithNullCategoryAndBrand() {
        stubMinimalHappyPath();

        assertDoesNotThrow(() -> productService.createProduct(
                minimalProductDTO(List.of(variantDTO(SKU, null)))));

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(brandRepository);
    }

    @Test
    void testCreateProductSavesSuccessfullyWithNullMaterialsAndAttributeValues() {
        stubMinimalHappyPath();

        assertDoesNotThrow(() -> productService.createProduct(
                minimalProductDTO(List.of(variantDTO(SKU, null)))));

        verifyNoInteractions(materialRepository);
        verifyNoInteractions(attributeValueRepository);
    }

    @Test
    void testCreateProductMapsProductFieldsCorrectlyOntoSavedEntity() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant));
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        CreateProductDTO dto = CreateProductDTO.builder()
                .name(PRODUCT_NAME)
                .shortDescription("Short desc")
                .longDescription("Long desc")
                .status(ProductStatus.INACTIVE)
                .featured(true)
                .categoryId(CATEGORY_ID)
                .variants(List.of(variantDTO(SKU, null)))
                .build();

        productService.createProduct(dto);

        verify(productRepository).save(argThat(p ->
                PRODUCT_NAME.equals(p.getName())
                && "Short desc".equals(p.getShortDescription())
                && "Long desc".equals(p.getLongDescription())
                && ProductStatus.INACTIVE == p.getStatus()
                && Boolean.TRUE.equals(p.getFeatured())
                && p.getCategory() == category
                && p.getBrand() == null
        ));
    }

    @Test
    void testCreateProductMapsVariantFieldsCorrectlyOntoSavedEntity() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant));
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        CreateProductVariantDTO variantDTO = CreateProductVariantDTO.builder()
                .sku(SKU)
                .price(BigDecimal.valueOf(250.00))
                .discountPrice(BigDecimal.valueOf(200.00))
                .stock(10)
                .weightGrams(5)
                .build();

        productService.createProduct(minimalProductDTO(List.of(variantDTO)));

        verify(productVariantRepository).saveAll(argThat(variants -> {
            ProductVariant v = ((List<ProductVariant>) variants).get(0);
            return SKU.equals(v.getSku())
                    && BigDecimal.valueOf(250.00).compareTo(v.getPrice()) == 0
                    && BigDecimal.valueOf(200.00).compareTo(v.getDiscountPrice()) == 0
                    && v.getStock() == 10
                    && v.getWeightGrams() == 5
                    && v.getProduct() == savedProduct;
        }));
    }

    // ─── createProduct – batch saves ──────────────────────────────────────────

    @Test
    void testCreateProductCallsSaveAllOnceForVariantsRegardlessOfVariantCount() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        ProductVariant savedVariant2 = new ProductVariant();
        savedVariant2.setProductVariantId(11L);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant, savedVariant2));

        productService.createProduct(minimalProductDTO(
                List.of(variantDTO(SKU, null), variantDTO(SKU_2, null))));

        verify(productVariantRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateProductCallsSaveAllOnceForProductMaterialsRegardlessOfMaterialCount() {
        Material material2 = Material.builder().materialId(5L).name("Silver").build();
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(materialRepository.findAllById(anyList())).thenReturn(List.of(material, material2));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant));
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        CreateProductDTO dto = CreateProductDTO.builder()
                .name(PRODUCT_NAME)
                .status(ProductStatus.ACTIVE)
                .materialIds(List.of(MATERIAL_ID, 5L))
                .variants(List.of(variantDTO(SKU, null)))
                .build();

        productService.createProduct(dto);

        verify(productMaterialRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateProductCallsSaveAllOnceForProductVariantAttributes() {
        AttributeValue attributeValue2 = new AttributeValue();
        attributeValue2.setAttributeValueId(5L);
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(attributeValueRepository.findAllById(anyList())).thenReturn(List.of(attributeValue, attributeValue2));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant));
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        productService.createProduct(minimalProductDTO(
                List.of(variantDTO(SKU, List.of(ATTRIBUTE_VALUE_ID, 5L)))));

        verify(productVariantAttributeRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateProductDeduplicatesAttributeValueIdsAcrossAllVariantsBeforeQuerying() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(attributeValueRepository.findAllById(anyList())).thenReturn(List.of(attributeValue));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        ProductVariant savedVariant2 = new ProductVariant();
        savedVariant2.setProductVariantId(11L);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant, savedVariant2));

        // Same avId used by two different variants — must only reach DB once with 1 distinct ID
        productService.createProduct(minimalProductDTO(List.of(
                variantDTO(SKU,   List.of(ATTRIBUTE_VALUE_ID)),
                variantDTO(SKU_2, List.of(ATTRIBUTE_VALUE_ID))
        )));

        verify(attributeValueRepository, times(1)).findAllById(anyList());
    }

    // ─── createProduct – category validation ─────────────────────────────────

    @Test
    void testCreateProductThrowsNotFoundWhenCategoryIdProvidedButNotFound() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("category_not_found"), any(Locale.class)))
                .thenReturn(CATEGORY_NOT_FOUND_MSG);

        CreateProductDTO dto = CreateProductDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE)
                .categoryId(CATEGORY_ID)
                .variants(List.of(variantDTO(SKU, null)))
                .build();

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.createProduct(dto));

        assertEquals(CATEGORY_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateProductDoesNotQueryCategoryWhenCategoryIdIsNull() {
        stubMinimalHappyPath();

        productService.createProduct(minimalProductDTO(List.of(variantDTO(SKU, null))));

        verifyNoInteractions(categoryRepository);
    }

    // ─── createProduct – brand validation ────────────────────────────────────

    @Test
    void testCreateProductThrowsNotFoundWhenBrandIdProvidedButNotFound() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("brand_does_not_exists"), any(Locale.class)))
                .thenReturn(BRAND_NOT_FOUND_MSG);

        CreateProductDTO dto = CreateProductDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE)
                .brandId(BRAND_ID)
                .variants(List.of(variantDTO(SKU, null)))
                .build();

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.createProduct(dto));

        assertEquals(BRAND_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateProductDoesNotQueryBrandWhenBrandIdIsNull() {
        stubMinimalHappyPath();

        productService.createProduct(minimalProductDTO(List.of(variantDTO(SKU, null))));

        verifyNoInteractions(brandRepository);
    }

    // ─── createProduct – material validation ─────────────────────────────────

    @Test
    void testCreateProductThrowsNotFoundWhenAllMaterialsNotFound() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(materialRepository.findAllById(anyList())).thenReturn(List.of());
        when(messageService.getMessage(eq("material_not_found"), any(Locale.class)))
                .thenReturn(MATERIAL_NOT_FOUND_MSG);

        CreateProductDTO dto = CreateProductDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE)
                .materialIds(List.of(MATERIAL_ID))
                .variants(List.of(variantDTO(SKU, null)))
                .build();

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.createProduct(dto));

        assertEquals(MATERIAL_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateProductThrowsNotFoundWhenSomeMaterialsNotFound() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(materialRepository.findAllById(anyList())).thenReturn(List.of(material)); // 1 of 2 found
        when(messageService.getMessage(eq("material_not_found"), any(Locale.class)))
                .thenReturn(MATERIAL_NOT_FOUND_MSG);

        CreateProductDTO dto = CreateProductDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE)
                .materialIds(List.of(MATERIAL_ID, 99L))
                .variants(List.of(variantDTO(SKU, null)))
                .build();

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining(MATERIAL_NOT_FOUND_MSG);
    }

    @Test
    void testCreateProductDoesNotQueryMaterialsWhenMaterialIdsIsNull() {
        stubMinimalHappyPath();

        productService.createProduct(minimalProductDTO(List.of(variantDTO(SKU, null))));

        verifyNoInteractions(materialRepository);
    }

    @Test
    void testCreateProductDoesNotQueryMaterialsWhenMaterialIdsIsEmpty() {
        stubMinimalHappyPath();

        CreateProductDTO dto = CreateProductDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE)
                .materialIds(List.of())
                .variants(List.of(variantDTO(SKU, null)))
                .build();

        productService.createProduct(dto);

        verifyNoInteractions(materialRepository);
    }

    // ─── createProduct – attribute value validation ───────────────────────────

    @Test
    void testCreateProductThrowsNotFoundWhenAllAttributeValuesNotFound() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(attributeValueRepository.findAllById(anyList())).thenReturn(List.of());
        when(messageService.getMessage(eq("attribute_value_not_found"), any(Locale.class)))
                .thenReturn(ATTRIBUTE_VALUE_NOT_FOUND_MSG);

        CreateProductDTO dto = minimalProductDTO(List.of(variantDTO(SKU, List.of(ATTRIBUTE_VALUE_ID))));

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.createProduct(dto));

        assertEquals(ATTRIBUTE_VALUE_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateProductThrowsNotFoundWhenSomeAttributeValuesNotFound() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(attributeValueRepository.findAllById(anyList())).thenReturn(List.of(attributeValue)); // 1 of 2 found
        when(messageService.getMessage(eq("attribute_value_not_found"), any(Locale.class)))
                .thenReturn(ATTRIBUTE_VALUE_NOT_FOUND_MSG);

        CreateProductDTO dto = minimalProductDTO(
                List.of(variantDTO(SKU, List.of(ATTRIBUTE_VALUE_ID, 99L))));

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining(ATTRIBUTE_VALUE_NOT_FOUND_MSG);
    }

    @Test
    void testCreateProductDoesNotQueryAttributeValuesWhenAllVariantsHaveNullAvIds() {
        stubMinimalHappyPath();

        productService.createProduct(minimalProductDTO(List.of(variantDTO(SKU, null))));

        verifyNoInteractions(attributeValueRepository);
    }

    @Test
    void testCreateProductDoesNotQueryAttributeValuesWhenAllVariantsHaveEmptyAvIds() {
        stubMinimalHappyPath();

        CreateProductDTO dto = CreateProductDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE)
                .variants(List.of(variantDTO(SKU, List.of())))
                .build();

        productService.createProduct(dto);

        verifyNoInteractions(attributeValueRepository);
    }

    // ─── createProduct – SKU validation ──────────────────────────────────────

    @Test
    void testCreateProductThrowsBadRequestWhenDuplicateSkuWithinRequest() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("sku_already_exists"), any(Locale.class)))
                .thenReturn(SKU_EXISTS_MSG);

        CreateProductDTO dto = minimalProductDTO(
                List.of(variantDTO(SKU, null), variantDTO(SKU, null)));

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.createProduct(dto));

        assertEquals(SKU_EXISTS_MSG, ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(productVariantRepository, never()).findExistingSkus(anyCollection());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateProductThrowsBadRequestWhenSkuAlreadyExistsInDb() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of(SKU));
        when(messageService.getMessage(eq("sku_already_exists"), any(Locale.class)))
                .thenReturn(SKU_EXISTS_MSG);

        CreateProductDTO dto = minimalProductDTO(List.of(variantDTO(SKU, null)));

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.createProduct(dto));

        assertEquals(SKU_EXISTS_MSG, ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateProductUsesSingleDbQueryToValidateAllSkusRegardlessOfVariantCount() {
        when(productRepository.findByName(any())).thenReturn(Optional.empty());
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        ProductVariant savedVariant2 = new ProductVariant();
        savedVariant2.setProductVariantId(11L);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant, savedVariant2));

        productService.createProduct(minimalProductDTO(
                List.of(variantDTO(SKU, null), variantDTO(SKU_2, null))));

        verify(productVariantRepository, times(1)).findExistingSkus(anyCollection());
    }

    // ─── updateProduct – happy paths ──────────────────────────────────────────

    @Test
    void testUpdateProductSavesAllFieldsOntoExistingEntity() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));

        ProductBasicInfoDTO dto = ProductBasicInfoDTO.builder()
                .name("Updated Name")
                .shortDescription("Short")
                .longDescription("Long")
                .status(ProductStatus.INACTIVE)
                .featured(true)
                .categoryId(CATEGORY_ID)
                .brandId(BRAND_ID)
                .build();

        assertDoesNotThrow(() -> productService.updateProduct(savedProduct.getProductId(), dto));

        verify(productRepository).save(argThat(p ->
                "Updated Name".equals(p.getName())
                && "Short".equals(p.getShortDescription())
                && "Long".equals(p.getLongDescription())
                && ProductStatus.INACTIVE == p.getStatus()
                && Boolean.TRUE.equals(p.getFeatured())
                && p.getCategory() == category
                && p.getBrand() == brand
        ));
    }

    @Test
    void testUpdateProductDoesNotQueryCategoryWhenCategoryIdIsNull() {
        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));

        ProductBasicInfoDTO dto = ProductBasicInfoDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE).build();

        productService.updateProduct(savedProduct.getProductId(), dto);

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void testUpdateProductDoesNotQueryBrandWhenBrandIdIsNull() {
        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));

        ProductBasicInfoDTO dto = ProductBasicInfoDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE).build();

        productService.updateProduct(savedProduct.getProductId(), dto);

        verifyNoInteractions(brandRepository);
    }

    // ─── updateProduct – error paths ──────────────────────────────────────────

    @Test
    void testUpdateProductThrowsNotFoundWhenProductDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(productRepository.findById(unknownId)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ProductBasicInfoDTO dto = ProductBasicInfoDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE).build();

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.updateProduct(unknownId, dto));

        assertEquals(PRODUCT_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testUpdateProductThrowsNotFoundWhenCategoryIdProvidedButNotFound() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("category_not_found"), any(Locale.class)))
                .thenReturn(CATEGORY_NOT_FOUND_MSG);

        ProductBasicInfoDTO dto = ProductBasicInfoDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE).categoryId(CATEGORY_ID).build();

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.updateProduct(savedProduct.getProductId(), dto));

        assertEquals(CATEGORY_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testUpdateProductThrowsNotFoundWhenBrandIdProvidedButNotFound() {
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("brand_does_not_exists"), any(Locale.class)))
                .thenReturn(BRAND_NOT_FOUND_MSG);

        ProductBasicInfoDTO dto = ProductBasicInfoDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE).brandId(BRAND_ID).build();

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.updateProduct(savedProduct.getProductId(), dto));

        assertEquals(BRAND_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    // ─── updateMaterials – error paths ────────────────────────────────────────

    @Test
    void testUpdateMaterialsThrowsNotFoundWhenProductDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(productRepository.findById(unknownId)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.updateMaterials(unknownId, List.of(MATERIAL_ID)));

        assertEquals(PRODUCT_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productMaterialRepository, never()).saveAll(anyList());
        verify(productMaterialRepository, never()).deleteByProductAndMaterialIn(any(), anyList());
    }

    @Test
    void testUpdateMaterialsThrowsNotFoundWhenMaterialDoesNotExist() {
        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));
        when(materialRepository.findAllById(anyList())).thenReturn(List.of());
        when(messageService.getMessage(eq("material_not_found"), any(Locale.class)))
                .thenReturn(MATERIAL_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.updateMaterials(savedProduct.getProductId(), List.of(MATERIAL_ID)));

        assertEquals(MATERIAL_NOT_FOUND_MSG, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productMaterialRepository, never()).saveAll(anyList());
        verify(productMaterialRepository, never()).deleteByProductAndMaterialIn(any(), anyList());
    }

    // ─── updateMaterials – add-only ───────────────────────────────────────────

    @Test
    void testUpdateMaterialsAddsNewMaterialsWhenProductHasNone() {
        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));
        when(materialRepository.findAllById(anyList())).thenReturn(List.of(material));
        when(productMaterialRepository.findByProductProductId(savedProduct.getProductId())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        productService.updateMaterials(savedProduct.getProductId(), List.of(MATERIAL_ID));

        verify(productMaterialRepository).saveAll(argThat(pms -> {
            List<ProductMaterial> list = (List<ProductMaterial>) pms;
            return list.size() == 1
                    && list.get(0).getMaterial() == material
                    && list.get(0).getProduct() == savedProduct;
        }));
        verify(productMaterialRepository, never()).deleteByProductAndMaterialIn(any(), anyList());
    }

    // ─── updateMaterials – delete-only ────────────────────────────────────────

    @Test
    void testUpdateMaterialsDeletesAllExistingMaterialsWhenIncomingListIsEmpty() {
        ProductMaterial existing = new ProductMaterial();
        existing.setProduct(savedProduct);
        existing.setMaterial(material);

        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));
        when(productMaterialRepository.findByProductProductId(savedProduct.getProductId()))
                .thenReturn(List.of(existing));

        productService.updateMaterials(savedProduct.getProductId(), List.of());

        verify(productMaterialRepository, never()).saveAll(anyList());
        verify(productMaterialRepository).deleteByProductAndMaterialIn(eq(savedProduct), argThat(mats -> {
            List<Material> list = (List<Material>) mats;
            return list.size() == 1 && list.get(0) == material;
        }));
    }

    // ─── updateMaterials – add and delete ─────────────────────────────────────

    @Test
    void testUpdateMaterialsAddsNewAndDeletesRemovedMaterialsWhenIncomingDiffersFromExisting() {
        Material material2 = Material.builder().materialId(5L).name("Silver").build();
        ProductMaterial existing = new ProductMaterial();
        existing.setProduct(savedProduct);
        existing.setMaterial(material); // materialId=3 will be removed

        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));
        when(materialRepository.findAllById(anyList())).thenReturn(List.of(material2));
        when(productMaterialRepository.findByProductProductId(savedProduct.getProductId()))
                .thenReturn(List.of(existing));
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());

        productService.updateMaterials(savedProduct.getProductId(), List.of(5L));

        verify(productMaterialRepository).saveAll(argThat(pms -> {
            List<ProductMaterial> list = (List<ProductMaterial>) pms;
            return list.size() == 1 && list.get(0).getMaterial() == material2;
        }));
        verify(productMaterialRepository).deleteByProductAndMaterialIn(eq(savedProduct), argThat(mats -> {
            List<Material> list = (List<Material>) mats;
            return list.size() == 1 && list.get(0) == material;
        }));
    }

    // ─── updateMaterials – no changes ─────────────────────────────────────────

    @Test
    void testUpdateMaterialsDoesNothingWhenIncomingListMatchesExistingMaterials() {
        ProductMaterial existing = new ProductMaterial();
        existing.setProduct(savedProduct);
        existing.setMaterial(material);

        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));
        when(materialRepository.findAllById(anyList())).thenReturn(List.of(material));
        when(productMaterialRepository.findByProductProductId(savedProduct.getProductId()))
                .thenReturn(List.of(existing));

        productService.updateMaterials(savedProduct.getProductId(), List.of(MATERIAL_ID));

        verify(productMaterialRepository, never()).saveAll(anyList());
        verify(productMaterialRepository, never()).deleteByProductAndMaterialIn(any(), anyList());
    }

    // ─── createProduct – name conflict ────────────────────────────────────────

    @Test
    void testCreateProductThrowsBadRequestWhenNameAlreadyExistsInActiveProduct() {
        Product existing = Product.builder()
                .productId(UUID.randomUUID())
                .name(PRODUCT_NAME)
                .status(ProductStatus.ACTIVE)
                .featured(false)
                .build();

        when(productRepository.findByName(PRODUCT_NAME)).thenReturn(Optional.of(existing));
        when(messageService.getMessage(eq("product_already_exists"), any(Locale.class)))
                .thenReturn("Product already exists");

        CreateProductDTO dto = minimalProductDTO(List.of(variantDTO(SKU, null)));

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.createProduct(dto));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateProductThrowsConflictWhenNameMatchesDeletedProduct() {
        UUID deletedId = UUID.randomUUID();
        Product deleted = Product.builder()
                .productId(deletedId)
                .name(PRODUCT_NAME)
                .status(ProductStatus.INACTIVE)
                .featured(false)
                .deletedAt(LocalDateTime.now())
                .build();

        when(productRepository.findByName(PRODUCT_NAME)).thenReturn(Optional.of(deleted));
        when(messageService.getMessage(eq("product_was_deleted"), any(Locale.class)))
                .thenReturn("Product was deleted");

        CreateProductDTO dto = minimalProductDTO(List.of(variantDTO(SKU, null)));

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.createProduct(dto));

        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode());
        assertEquals(deletedId, ex.getMetadata().get("productId"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateProductThrowsConflictWhenSkuBelongsToDeletedProduct() {
        UUID deletedProductId = UUID.randomUUID();
        Product deletedProduct = Product.builder()
                .productId(deletedProductId)
                .name("Deleted Product")
                .status(ProductStatus.INACTIVE)
                .featured(false)
                .deletedAt(LocalDateTime.now())
                .build();

        ProductVariant deletedVariant = new ProductVariant();
        deletedVariant.setProductVariantId(99L);
        deletedVariant.setSku(SKU);
        deletedVariant.setProduct(deletedProduct);

        when(productRepository.findByName(PRODUCT_NAME)).thenReturn(Optional.empty());
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productVariantRepository.findVariantsFromDeletedProducts(anyCollection()))
                .thenReturn(List.of(deletedVariant));
        when(messageService.getMessage(eq("product_was_deleted"), any(Locale.class)))
                .thenReturn("Product was deleted");

        CreateProductDTO dto = minimalProductDTO(List.of(variantDTO(SKU, null)));

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.createProduct(dto));

        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode());
        assertEquals(deletedProductId, ex.getMetadata().get("productId"));
        verify(productRepository, never()).save(any());
    }

    // ─── deleteProduct ────────────────────────────────────────────────────────

    @Test
    void testDeleteProductSetsDeletedAt() {
        when(productRepository.findById(savedProduct.getProductId()))
                .thenReturn(Optional.of(savedProduct));

        productService.deleteProduct(savedProduct.getProductId());

        verify(productRepository).save(argThat(p -> p.getDeletedAt() != null));
    }

    @Test
    void testDeleteProductThrowsNotFoundWhenProductDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(productRepository.findById(unknownId)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.deleteProduct(unknownId));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    // ─── restoreProduct ───────────────────────────────────────────────────────

    @Test
    void testRestoreProductClearsDeletedAt() {
        savedProduct.setDeletedAt(LocalDateTime.now());
        when(productRepository.findById(savedProduct.getProductId()))
                .thenReturn(Optional.of(savedProduct));

        productService.restoreProduct(savedProduct.getProductId());

        verify(productRepository).save(argThat(p -> p.getDeletedAt() == null));
    }

    @Test
    void testRestoreProductThrowsNotFoundWhenProductDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(productRepository.findById(unknownId)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.restoreProduct(unknownId));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void testRestoreProductThrowsBadRequestWhenProductIsNotDeleted() {
        when(productRepository.findById(savedProduct.getProductId()))
                .thenReturn(Optional.of(savedProduct));
        when(messageService.getMessage(eq("product_not_deleted"), any(Locale.class)))
                .thenReturn("Product is not deleted");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.restoreProduct(savedProduct.getProductId()));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    // ─── resolveProduct (used by updateProduct / uploadImage) – deleted guard ──

    @Test
    void testUpdateProductThrowsNotFoundWhenProductIsDeleted() {
        savedProduct.setDeletedAt(LocalDateTime.now());
        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ProductBasicInfoDTO dto = ProductBasicInfoDTO.builder()
                .name(PRODUCT_NAME).status(ProductStatus.ACTIVE).build();

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.updateProduct(savedProduct.getProductId(), dto));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
        verify(productRepository, never()).save(any());
    }

    // ─── getAdminProductById – deleted guard ──────────────────────────────────

    @Test
    void testGetAdminProductByIdThrowsWhenProductIsDeleted() {
        savedProduct.setDeletedAt(LocalDateTime.now());
        when(productRepository.findById(savedProduct.getProductId()))
                .thenReturn(Optional.of(savedProduct));
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.getAdminProductById(savedProduct.getProductId()));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
    }

    // ─── getPublicProducts ────────────────────────────────────────────────────

    @Test
    void testGetPublicProductsReturnsPageOfActiveProducts() {
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(savedProduct)));
        when(productVariantRepository.findByProductIds(anyList())).thenReturn(List.of(savedVariant));

        PageResponse<PublicProductDTO> result = productService.getPublicProducts(
                0, 10, "newest", "", null, null, null, false, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductId()).isEqualTo(savedProduct.getProductId());
        assertThat(result.getContent().get(0).getName()).isEqualTo(PRODUCT_NAME);
    }

    @Test
    void testGetPublicProductsReturnsEmptyPageWhenNoProducts() {
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        PageResponse<PublicProductDTO> result = productService.getPublicProducts(
                0, 10, "newest", "", null, null, null, false, null, null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetPublicProductsSortsByPriceAscUsingInMemorySort() {
        Product cheapProduct = Product.builder().productId(UUID.randomUUID()).name("Cheap")
                .status(ProductStatus.ACTIVE).featured(false).build();
        Product expensiveProduct = Product.builder().productId(UUID.randomUUID()).name("Expensive")
                .status(ProductStatus.ACTIVE).featured(false).build();

        ProductVariant cheapVariant = new ProductVariant();
        cheapVariant.setProductVariantId(1L);
        cheapVariant.setProduct(cheapProduct);
        cheapVariant.setPrice(BigDecimal.valueOf(100));
        cheapVariant.setStock(5);

        ProductVariant expensiveVariant = new ProductVariant();
        expensiveVariant.setProductVariantId(2L);
        expensiveVariant.setProduct(expensiveProduct);
        expensiveVariant.setPrice(BigDecimal.valueOf(500));
        expensiveVariant.setStock(3);

        when(productRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(expensiveProduct, cheapProduct));
        when(productVariantRepository.findByProductIds(anyList()))
                .thenReturn(List.of(expensiveVariant, cheapVariant));

        PageResponse<PublicProductDTO> result = productService.getPublicProducts(
                0, 10, "priceAsc", "", null, null, null, false, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Cheap");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Expensive");
    }

    // ─── getPublicProductById ─────────────────────────────────────────────────

    @Test
    void testGetPublicProductByIdReturnsActiveProduct() {
        when(productRepository.findById(savedProduct.getProductId()))
                .thenReturn(Optional.of(savedProduct));
        when(productVariantRepository.findByProductProductId(savedProduct.getProductId()))
                .thenReturn(List.of(savedVariant));
        when(productVariantAttributeRepository.findByVariantIn(anyList())).thenReturn(List.of());
        when(productMaterialRepository.findByProductProductId(savedProduct.getProductId()))
                .thenReturn(List.of());

        PublicProductByIdDTO result = productService.getPublicProductById(savedProduct.getProductId());

        assertThat(result.getProductId()).isEqualTo(savedProduct.getProductId());
        assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
        assertNotNull(result.getVariants());
    }

    @Test
    void testGetPublicProductByIdThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.getPublicProductById(id));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
    }

    @Test
    void testGetPublicProductByIdThrowsWhenProductIsInactive() {
        Product inactive = Product.builder().productId(UUID.randomUUID()).name("Inactive")
                .status(ProductStatus.INACTIVE).featured(false).build();
        when(productRepository.findById(inactive.getProductId())).thenReturn(Optional.of(inactive));
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.getPublicProductById(inactive.getProductId()));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
    }

    @Test
    void testGetPublicProductByIdThrowsWhenProductIsSoftDeleted() {
        savedProduct.setDeletedAt(LocalDateTime.now());
        when(productRepository.findById(savedProduct.getProductId()))
                .thenReturn(Optional.of(savedProduct));
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.getPublicProductById(savedProduct.getProductId()));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
    }

    // ─── getAdminProducts ─────────────────────────────────────────────────────

    @Test
    void testGetAdminProductsReturnsAllStatuses() {
        Product inactive = Product.builder().productId(UUID.randomUUID()).name("Inactive Ring")
                .status(ProductStatus.INACTIVE).featured(false).build();

        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(savedProduct, inactive)));
        when(productVariantRepository.findByProductIds(anyList())).thenReturn(List.of(savedVariant));

        PageResponse<AdminProductDTO> result = productService.getAdminProducts(
                0, 10, "newest", "", null, null, null, false, null, null, null);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void testGetAdminProductsIncludesVariantStats() {
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(savedProduct)));
        when(productVariantRepository.findByProductIds(anyList())).thenReturn(List.of(savedVariant));

        PageResponse<AdminProductDTO> result = productService.getAdminProducts(
                0, 10, "newest", "", null, null, null, false, null, null, null);

        AdminProductDTO dto = result.getContent().get(0);
        assertThat(dto.getTotalStock()).isEqualTo(savedVariant.getStock());
        assertThat(dto.getVariantCount()).isEqualTo(1);
    }

    @Test
    void testGetAdminProductsIncludesAvgPriceAndAvgDiscountPriceWhenVariantsHaveDiscountPrices() {
        ProductVariant cheap = new ProductVariant();
        cheap.setProductVariantId(1L);
        cheap.setProduct(savedProduct);
        cheap.setPrice(BigDecimal.valueOf(200));
        cheap.setDiscountPrice(BigDecimal.valueOf(150));
        cheap.setStock(3);

        ProductVariant expensive = new ProductVariant();
        expensive.setProductVariantId(2L);
        expensive.setProduct(savedProduct);
        expensive.setPrice(BigDecimal.valueOf(500));
        expensive.setDiscountPrice(BigDecimal.valueOf(400));
        expensive.setStock(2);

        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(savedProduct)));
        when(productVariantRepository.findByProductIds(anyList())).thenReturn(List.of(cheap, expensive));

        PageResponse<AdminProductDTO> result = productService.getAdminProducts(
                0, 10, "newest", "", null, null, null, false, null, null, null);

        AdminProductDTO dto = result.getContent().get(0);
        // avg price: (200 + 500) / 2 = 350.00
        assertThat(dto.getAvgPrice()).isEqualByComparingTo(BigDecimal.valueOf(350));
        // avg discount price: (150 + 400) / 2 = 275.00
        assertThat(dto.getAvgDiscountPrice()).isEqualByComparingTo(BigDecimal.valueOf(275));
    }

    @Test
    void testGetAdminProductsAvgDiscountPriceIsNullWhenNoVariantsHaveDiscountPrice() {
        ProductVariant variant = new ProductVariant();
        variant.setProductVariantId(1L);
        variant.setProduct(savedProduct);
        variant.setPrice(BigDecimal.valueOf(300));
        variant.setDiscountPrice(null);
        variant.setStock(5);

        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(savedProduct)));
        when(productVariantRepository.findByProductIds(anyList())).thenReturn(List.of(variant));

        PageResponse<AdminProductDTO> result = productService.getAdminProducts(
                0, 10, "newest", "", null, null, null, false, null, null, null);

        AdminProductDTO dto = result.getContent().get(0);
        // avg price: 300 / 1 = 300.00
        assertThat(dto.getAvgPrice()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(dto.getAvgDiscountPrice()).isNull();
    }

    // ─── getAdminProductById ──────────────────────────────────────────────────

    @Test
    void testGetAdminProductByIdReturnsProductRegardlessOfStatus() {
        Product inactive = Product.builder().productId(UUID.randomUUID()).name("Inactive Ring")
                .status(ProductStatus.INACTIVE).featured(false).build();
        when(productRepository.findById(inactive.getProductId())).thenReturn(Optional.of(inactive));
        when(productVariantRepository.findByProductProductId(inactive.getProductId()))
                .thenReturn(List.of());
        when(productVariantAttributeRepository.findByVariantIn(anyList())).thenReturn(List.of());
        when(productMaterialRepository.findByProductProductId(inactive.getProductId()))
                .thenReturn(List.of());

        AdminProductByIdDTO result = productService.getAdminProductById(inactive.getProductId());

        assertThat(result.getProductId()).isEqualTo(inactive.getProductId());
        assertThat(result.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void testGetAdminProductByIdThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.getAdminProductById(id));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
    }

    @Test
    void testGetAdminProductByIdIncludesSkuAndWeightInVariants() {
        savedVariant.setSku(SKU);
        savedVariant.setWeightGrams(10);
        savedVariant.setProduct(savedProduct);

        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));
        when(productVariantRepository.findByProductProductId(savedProduct.getProductId()))
                .thenReturn(List.of(savedVariant));
        when(productVariantAttributeRepository.findByVariantIn(anyList())).thenReturn(List.of());
        when(productMaterialRepository.findByProductProductId(savedProduct.getProductId()))
                .thenReturn(List.of());

        AdminProductByIdDTO result = productService.getAdminProductById(savedProduct.getProductId());

        assertThat(result.getVariants()).hasSize(1);
        assertThat(result.getVariants().get(0).getSku()).isEqualTo(SKU);
        assertThat(result.getVariants().get(0).getWeightGrams()).isEqualTo(10);
    }

    // ─── uploadProductImage ───────────────────────────────────────────────────

    @Test
    void testUploadProductImageSuccessfully() throws Exception {
        savedProduct.setFileUrl("products/old-image.jpg");
        when(productRepository.findById(savedProduct.getProductId())).thenReturn(Optional.of(savedProduct));
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(multipartFile.getSize()).thenReturn(10L);
        when(multipartFile.getInputStream())
                .thenReturn(new java.io.ByteArrayInputStream("img".getBytes()));

        assertDoesNotThrow(() -> productService.uploadProductImage(multipartFile, savedProduct.getProductId()));

        verify(storageService).delete("products/old-image.jpg");
        verify(productRepository).save(any(Product.class));
        verify(thumbnailService).generateThumbnails(any(String.class));
    }

    @Test
    void testUploadProductImageThrowsWhenContentTypeInvalid() {
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(messageService.getMessage(eq("invalid_image_type"), any(Locale.class)))
                .thenReturn("Invalid image type");

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.uploadProductImage(multipartFile, savedProduct.getProductId()));

        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode());
        verify(storageService, never()).upload(any(), any(), anyLong(), any());
    }

    @Test
    void testUploadProductImageThrowsWhenProductNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(messageService.getMessage(eq("product_not_found"), any(Locale.class)))
                .thenReturn(PRODUCT_NOT_FOUND_MSG);

        ApiServiceException ex = assertThrows(ApiServiceException.class,
                () -> productService.uploadProductImage(multipartFile, id));

        assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode());
    }

}
