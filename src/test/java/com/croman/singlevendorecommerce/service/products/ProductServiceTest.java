package com.croman.singlevendorecommerce.service.products;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.springframework.http.HttpStatus;

import com.croman.singlevendorecommerce.dto.products.CreateProductDTO;
import com.croman.singlevendorecommerce.dto.products.CreateProductVariantDTO;
import com.croman.singlevendorecommerce.dto.products.ProductStatus;
import com.croman.singlevendorecommerce.entity.products.AttributeValue;
import com.croman.singlevendorecommerce.entity.products.Brand;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.entity.products.Material;
import com.croman.singlevendorecommerce.entity.products.Product;
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
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(savedProduct);
        when(productVariantRepository.saveAll(anyList())).thenReturn(List.of(savedVariant));
        when(productVariantAttributeRepository.saveAll(anyList())).thenReturn(List.of());
        when(productMaterialRepository.saveAll(anyList())).thenReturn(List.of());
    }

    // ─── createProduct – happy paths ──────────────────────────────────────────

    @Test
    void testCreateProductSavesProductSuccessfullyWithAllRelationships() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand));
        when(materialRepository.findAllById(anyList())).thenReturn(List.of(material));
        when(attributeValueRepository.findAllById(anyList())).thenReturn(List.of(attributeValue));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
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
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
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
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
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
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
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
        when(materialRepository.findAllById(anyList())).thenReturn(List.of(material, material2));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
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
        when(attributeValueRepository.findAllById(anyList())).thenReturn(List.of(attributeValue, attributeValue2));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
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
        when(attributeValueRepository.findAllById(anyList())).thenReturn(List.of(attributeValue));
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
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
        when(productVariantRepository.findExistingSkus(anyCollection())).thenReturn(List.of());
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

}
