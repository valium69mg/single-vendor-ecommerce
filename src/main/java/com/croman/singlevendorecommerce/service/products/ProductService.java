package com.croman.singlevendorecommerce.service.products;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

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
import com.croman.singlevendorecommerce.utils.FileUtils;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.entity.products.AttributeValue;
import com.croman.singlevendorecommerce.entity.products.Brand;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.entity.products.Material;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.entity.products.ProductMaterial;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.entity.products.ProductVariantAttribute;
import com.croman.singlevendorecommerce.utils.PaginationUtils;
import com.croman.singlevendorecommerce.utils.SlugUtils;
import com.croman.singlevendorecommerce.repository.products.AttributeValueRepository;
import com.croman.singlevendorecommerce.repository.products.BrandRepository;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.MaterialRepository;
import com.croman.singlevendorecommerce.repository.products.ProductMaterialRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductSlugHistoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantAttributeRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String PRODUCT_SUB_DIRECTORY = "products/";
    private static final String PRODUCT_SLUG_PREFIX = "product";
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMaterialRepository productMaterialRepository;
    private final ProductVariantAttributeRepository productVariantAttributeRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final MessageService messageService;
    private final StorageService storageService;
    private final ThumbnailService thumbnailService;
    private final ProductSlugHistoryRepository productSlugHistoryRepository;

    @Transactional
    public void createProduct(CreateProductDTO dto) {
        validateProductName(dto.getName());
        Category category = resolveCategory(dto.getCategoryId());
        Brand brand = resolveBrand(dto.getBrandId());
        List<Material> materials = resolveMaterials(dto.getMaterialIds());
        Map<Long, AttributeValue> attributeValueMap = resolveAttributeValues(dto.getVariants());

        validateSkus(dto.getVariants());

        String slugBase = SlugUtils.slugify(dto.getName());
        String initialSlug = slugBase.isEmpty()
                ? PRODUCT_SLUG_PREFIX + "-" + UUID.randomUUID()
                : generateUniqueSlug(dto.getName(), null);

        Product savedProduct = productRepository.save(Product.builder()
                .name(dto.getName())
                .slug(initialSlug)
                .shortDescription(dto.getShortDescription())
                .longDescription(dto.getLongDescription())
                .status(dto.getStatus())
                .featured(dto.isFeatured())
                .category(category)
                .unitsSold(0)
                .brand(brand)
                .build());

        if (slugBase.isEmpty()) {
            // The name produced no usable slug: now that the id exists, replace the
            // placeholder with the deterministic "product-{id}" fallback.
            savedProduct.setSlug(generateUniqueSlug(null, savedProduct.getProductId()));
            productRepository.save(savedProduct);
        }

        List<ProductVariant> variantEntities = dto.getVariants().stream()
                .map(v -> {
                    ProductVariant pv = new ProductVariant();
                    pv.setProduct(savedProduct);
                    pv.setSku(v.getSku());
                    pv.setPrice(v.getPrice());
                    pv.setDiscountPrice(v.getDiscountPrice());
                    pv.setStock(v.getStock());
                    pv.setWeightGrams(v.getWeightGrams());
                    return pv;
                })
                .toList();

        List<ProductVariant> savedVariants = productVariantRepository.saveAll(variantEntities);

        List<ProductVariantAttribute> pvas = new ArrayList<>();
        for (int i = 0; i < dto.getVariants().size(); i++) {
            List<Long> avIds = dto.getVariants().get(i).getAttributeValueIds();
            if (avIds == null || avIds.isEmpty()) continue;
            ProductVariant savedVariant = savedVariants.get(i);
            for (Long avId : avIds) {
                ProductVariantAttribute pva = new ProductVariantAttribute();
                pva.setVariant(savedVariant);
                pva.setAttributeValue(attributeValueMap.get(avId));
                pvas.add(pva);
            }
        }
        productVariantAttributeRepository.saveAll(pvas);

        List<ProductMaterial> pms = materials.stream()
                .map(m -> {
                    ProductMaterial pm = new ProductMaterial();
                    pm.setProduct(savedProduct);
                    pm.setMaterial(m);
                    return pm;
                })
                .toList();
        productMaterialRepository.saveAll(pms);
    }
    
	@Transactional
	public void updateProduct(UUID productId, ProductBasicInfoDTO dto) {
		Category category = resolveCategory(dto.getCategoryId());
		Brand brand = resolveBrand(dto.getBrandId());
		Product product = resolveProduct(productId);
		product.setName(dto.getName());
		product.setShortDescription(dto.getShortDescription());
		product.setLongDescription(dto.getLongDescription());
		product.setStatus(dto.getStatus());
		product.setFeatured(dto.isFeatured());
		product.setBrand(brand);
		product.setCategory(category);
		productRepository.save(product);
	}
	
	@Transactional
	public void updateMaterials(UUID productId, List<Long> materialIds) {
		Product product = resolveProduct(productId);
		List<Material> incomingMaterials = resolveMaterials(materialIds);
		List<ProductMaterial> productMaterials = productMaterialRepository.findByProductProductId(productId);

		List<Material> materialsToAdd = resolveMaterialsToAdd(incomingMaterials, productMaterials);
		List<ProductMaterial> productMaterialsToAdd = materialsToAdd.stream()
                .map(m -> {
                    ProductMaterial pm = new ProductMaterial();
                    pm.setProduct(product);
                    pm.setMaterial(m);
                    return pm;
                })
                .toList();
		if (!productMaterialsToAdd.isEmpty()) {
			productMaterialRepository.saveAll(productMaterialsToAdd);
		}
		
		List<Material> materialsToDelete = resolveMaterialsToDelete(incomingMaterials, productMaterials);
		if (!materialsToDelete.isEmpty()) {
			productMaterialRepository.deleteByProductAndMaterialIn(product, materialsToDelete);
		}
		
	}
	
	private List<Material> resolveMaterialsToDelete(List<Material> incomingMaterials, List<ProductMaterial> productMaterials) {
		List<Material> materials = productMaterials.stream().map(ProductMaterial::getMaterial).distinct().toList();
		Map<Long, Material> incomingMaterialsById = incomingMaterials.stream().collect(Collectors.toMap(
				Material::getMaterialId,
				Function.identity()
			));
		List<Material> removedMaterials = new ArrayList<>();
		for (Material existingMaterial : materials) {
			boolean materialErased = !incomingMaterialsById.containsKey(existingMaterial.getMaterialId());
			if (materialErased) {
				removedMaterials.add(existingMaterial);
			}
		}
		return removedMaterials;
	}
	
	private List<Material> resolveMaterialsToAdd(List<Material> incomingMaterials, List<ProductMaterial> productMaterials) {
		List<Material> materials = productMaterials.stream().map(ProductMaterial::getMaterial).distinct().toList();
		Map<Long, Material> existingMaterialsById = materials.stream().collect(Collectors.toMap(
				Material::getMaterialId,
				Function.identity()
			));
		List<Material> addedMaterials = new ArrayList<>();
		for (Material incomingMaterial : incomingMaterials) {
			boolean materialAdded = !existingMaterialsById.containsKey(incomingMaterial.getMaterialId());
			if (materialAdded) {
				addedMaterials.add(incomingMaterial);
			}
		}
		return addedMaterials;
	}

    @Transactional(readOnly = true)
    public PageResponse<PublicProductDTO> getPublicProducts(
            int page, int size, String sortBy, String term,
            Long categoryId, Long brandId, Long materialId,
            boolean featured, LocalDateTime createdAtStart, LocalDateTime createdAtEnd) {

        Specification<Product> spec = ProductSpecification.build(
                term, categoryId, brandId, materialId, featured,
                ProductStatus.ACTIVE, createdAtStart, createdAtEnd);

        return fetchPage(spec, page, size, sortBy, this::mapToPublicProductDTO);
    }

    @Transactional(readOnly = true)
    public PublicProductByIdDTO getPublicProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                        messageService.getMessage("product_not_found", LocaleUtils.getDefaultLocale())));

        if (product.getDeletedAt() != null) {
            throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                    messageService.getMessage("product_not_found", LocaleUtils.getDefaultLocale()));
        }

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                    messageService.getMessage("product_not_found", LocaleUtils.getDefaultLocale()));
        }

        return mapToPublicProductByIdDTO(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminProductDTO> getAdminProducts(
            int page, int size, String sortBy, String term,
            Long categoryId, Long brandId, Long materialId,
            boolean featured, ProductStatus status,
            LocalDateTime createdAtStart, LocalDateTime createdAtEnd) {

        Specification<Product> spec = ProductSpecification.build(
                term, categoryId, brandId, materialId, featured,
                status, createdAtStart, createdAtEnd);

        return fetchPage(spec, page, size, sortBy, this::mapToAdminProductDTO);
    }

    @Transactional(readOnly = true)
    public AdminProductByIdDTO getAdminProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                        messageService.getMessage("product_not_found", LocaleUtils.getDefaultLocale())));

        if (product.getDeletedAt() != null) {
            throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                    messageService.getMessage("product_not_found", LocaleUtils.getDefaultLocale()));
        }

        return mapToAdminProductByIdDTO(product);
    }

    @Transactional
    public void uploadProductImage(MultipartFile file, UUID productId) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
                        messageService.getMessage("invalid_image_type", LocaleUtils.getDefaultLocale()));
            }

            Product product = resolveProduct(productId);

            storageService.delete(product.getFileUrl());
            storageService.delete(FileUtils.toMediumThumbnailKey(product.getFileUrl()));
            storageService.delete(FileUtils.toSmallThumbnailKey(product.getFileUrl()));

            String imageId = UUID.randomUUID().toString();
            String extension = FileUtils.getFileExtension(file.getOriginalFilename());
            String key = PRODUCT_SUB_DIRECTORY + imageId + extension;

            product.setFileUrl(key);
            productRepository.save(product);

            storageService.upload(key, file.getInputStream(), file.getSize(), file.getContentType());
            thumbnailService.generateThumbnails(key);

        } catch (ApiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
                    messageService.getMessage("image_upload_failed", LocaleUtils.getDefaultLocale()));
        }
    }

    private <T> PageResponse<T> fetchPage(
            Specification<Product> spec, int page, int size, String sortBy,
            java.util.function.BiFunction<Product, Map<UUID, List<ProductVariant>>, T> mapper) {

        if ("priceAsc".equals(sortBy) || "priceDesc".equals(sortBy)) {
            List<Product> all = new ArrayList<>(productRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt")));
            List<UUID> allIds = all.stream().map(Product::getProductId).toList();
            Map<UUID, List<ProductVariant>> variantsByProduct = groupVariantsByProduct(allIds);

            Comparator<Product> comparator = Comparator.comparing(
                    p -> minPrice(variantsByProduct.getOrDefault(p.getProductId(), List.of())));
            if ("priceDesc".equals(sortBy)) comparator = comparator.reversed();
            all.sort(comparator);

            int start = page * size;
            List<Product> slice = start >= all.size() ? List.of()
                    : all.subList(start, Math.min(start + size, all.size()));
            List<T> dtos = slice.stream().map(p -> mapper.apply(p, variantsByProduct)).toList();

            return PageResponse.<T>builder()
                    .content(dtos).page(page).size(size)
                    .totalElements(all.size())
                    .totalPages((int) Math.ceil((double) all.size() / size))
                    .last(start + size >= all.size())
                    .build();
        }

        Sort sort = "mostSold".equals(sortBy)
                ? Sort.by(Sort.Direction.DESC, "unitsSold")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Page<Product> productPage = productRepository.findAll(spec, PageRequest.of(page, size, sort));
        List<UUID> ids = productPage.getContent().stream().map(Product::getProductId).toList();
        Map<UUID, List<ProductVariant>> variantsByProduct = groupVariantsByProduct(ids);

        List<T> dtos = productPage.getContent().stream()
                .map(p -> mapper.apply(p, variantsByProduct)).toList();

        return PageResponse.<T>builder()
                .content(dtos).page(productPage.getNumber()).size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    private Map<UUID, List<ProductVariant>> groupVariantsByProduct(List<UUID> productIds) {
        if (productIds.isEmpty()) return Map.of();
        return productVariantRepository.findByProductIds(productIds).stream()
                .collect(Collectors.groupingBy(v -> v.getProduct().getProductId()));
    }

    private BigDecimal minPrice(List<ProductVariant> variants) {
        return variants.stream().map(ProductVariant::getPrice)
                .min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
    }

    private BigDecimal maxPrice(List<ProductVariant> variants) {
        return variants.stream().map(ProductVariant::getPrice)
                .max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
    }

    private BigDecimal minDiscountPrice(List<ProductVariant> variants) {
        return variants.stream().map(ProductVariant::getDiscountPrice)
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);
    }

    private BigDecimal maxDiscountPrice(List<ProductVariant> variants) {
        return variants.stream().map(ProductVariant::getDiscountPrice)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    private BigDecimal avgPrice(List<ProductVariant> variants) {
        if (variants.isEmpty()) return BigDecimal.ZERO;
        return variants.stream().map(ProductVariant::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(variants.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal avgDiscountPrice(List<ProductVariant> variants) {
        List<BigDecimal> prices = variants.stream().map(ProductVariant::getDiscountPrice)
                .filter(java.util.Objects::nonNull).toList();
        if (prices.isEmpty()) return null;
        return prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    private PublicProductDTO mapToPublicProductDTO(Product p, Map<UUID, List<ProductVariant>> variantsByProduct) {
        List<ProductVariant> variants = variantsByProduct.getOrDefault(p.getProductId(), List.of());
        return PublicProductDTO.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .shortDescription(p.getShortDescription())
                .featured(Boolean.TRUE.equals(p.getFeatured()))
                .category(p.getCategory() == null ? null : PublicProductDTO.CategoryRef.builder()
                        .categoryId(p.getCategory().getCategoryId())
                        .name(p.getCategory().getName()).build())
                .brand(p.getBrand() == null ? null : PublicProductDTO.BrandRef.builder()
                        .brandId(p.getBrand().getBrandId())
                        .name(p.getBrand().getName()).build())
                .imageUrl(p.getFileUrl())
                .mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(p.getFileUrl()))
                .smallThumbnailUrl(FileUtils.toSmallThumbnailKey(p.getFileUrl()))
                .minPrice(minPrice(variants))
                .minDiscountPrice(minDiscountPrice(variants))
                .createdAt(p.getCreatedAt())
                .build();
    }

    private AdminProductDTO mapToAdminProductDTO(Product p, Map<UUID, List<ProductVariant>> variantsByProduct) {
        List<ProductVariant> variants = variantsByProduct.getOrDefault(p.getProductId(), List.of());
        return AdminProductDTO.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .shortDescription(p.getShortDescription())
                .featured(Boolean.TRUE.equals(p.getFeatured()))
                .status(p.getStatus())
                .category(p.getCategory() == null ? null : AdminProductDTO.CategoryRef.builder()
                        .categoryId(p.getCategory().getCategoryId())
                        .name(p.getCategory().getName()).build())
                .brand(p.getBrand() == null ? null : AdminProductDTO.BrandRef.builder()
                        .brandId(p.getBrand().getBrandId())
                        .name(p.getBrand().getName()).build())
                .imageUrl(p.getFileUrl())
                .mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(p.getFileUrl()))
                .smallThumbnailUrl(FileUtils.toSmallThumbnailKey(p.getFileUrl()))
                .avgPrice(avgPrice(variants))
                .avgDiscountPrice(avgDiscountPrice(variants))
                .totalStock(variants.stream().mapToInt(ProductVariant::getStock).sum())
                .variantCount(variants.size())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PublicProductByIdDTO mapToPublicProductByIdDTO(Product p) {
        List<ProductVariant> variants = productVariantRepository.findByProductProductId(p.getProductId());
        List<ProductVariantAttribute> pvas = productVariantAttributeRepository.findByVariantIn(variants);
        Map<Long, List<ProductVariantAttribute>> pvasByVariant = pvas.stream()
                .collect(Collectors.groupingBy(pva -> pva.getVariant().getProductVariantId()));

        BigDecimal minPrice = variants.stream().map(ProductVariant::getPrice)
                .min(Comparator.naturalOrder()).orElse(null);
        BigDecimal minDiscountPrice = variants.stream()
                .map(ProductVariant::getDiscountPrice).filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);

        List<PublicProductByIdDTO.VariantDTO> variantDTOs = variants.stream()
                .map(v -> PublicProductByIdDTO.VariantDTO.builder()
                        .productVariantId(v.getProductVariantId())
                        .price(v.getPrice())
                        .discountPrice(v.getDiscountPrice())
                        .stock(v.getStock())
                        .attributeValues(pvasByVariant.getOrDefault(v.getProductVariantId(), List.of())
                                .stream().map(pva -> PublicProductByIdDTO.AttributeValueDTO.builder()
                                        .attributeValueId(pva.getAttributeValue().getAttributeValueId())
                                        .value(pva.getAttributeValue().getValue()).build())
                                .toList())
                        .build())
                .toList();

        List<ProductMaterial> productMaterials =
                productMaterialRepository.findByProductProductId(p.getProductId());
        List<PublicProductByIdDTO.MaterialRef> materialRefs = productMaterials.stream()
                .map(pm -> PublicProductByIdDTO.MaterialRef.builder()
                        .materialId(pm.getMaterial().getMaterialId())
                        .name(pm.getMaterial().getName()).build())
                .toList();

        return PublicProductByIdDTO.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .shortDescription(p.getShortDescription())
                .longDescription(p.getLongDescription())
                .featured(Boolean.TRUE.equals(p.getFeatured()))
                .category(p.getCategory() == null ? null : PublicProductByIdDTO.CategoryRef.builder()
                        .categoryId(p.getCategory().getCategoryId())
                        .name(p.getCategory().getName()).build())
                .brand(p.getBrand() == null ? null : PublicProductByIdDTO.BrandRef.builder()
                        .brandId(p.getBrand().getBrandId())
                        .name(p.getBrand().getName()).build())
                .imageUrl(p.getFileUrl())
                .mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(p.getFileUrl()))
                .smallThumbnailUrl(FileUtils.toSmallThumbnailKey(p.getFileUrl()))
                .minPrice(minPrice)
                .minDiscountPrice(minDiscountPrice)
                .createdAt(p.getCreatedAt())
                .materials(materialRefs)
                .variants(variantDTOs)
                .build();
    }

    private AdminProductByIdDTO mapToAdminProductByIdDTO(Product p) {
        List<ProductVariant> variants = productVariantRepository.findByProductProductId(p.getProductId());
        List<ProductVariantAttribute> pvas = productVariantAttributeRepository.findByVariantIn(variants);
        Map<Long, List<ProductVariantAttribute>> pvasByVariant = pvas.stream()
                .collect(Collectors.groupingBy(pva -> pva.getVariant().getProductVariantId()));

        BigDecimal minPrice = variants.stream().map(ProductVariant::getPrice)
                .min(Comparator.naturalOrder()).orElse(null);
        BigDecimal minDiscountPrice = variants.stream()
                .map(ProductVariant::getDiscountPrice).filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);

        List<AdminProductByIdDTO.VariantDTO> variantDTOs = variants.stream()
                .map(v -> AdminProductByIdDTO.VariantDTO.builder()
                        .productVariantId(v.getProductVariantId())
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .discountPrice(v.getDiscountPrice())
                        .stock(v.getStock())
                        .weightGrams(v.getWeightGrams())
                        .attributeValues(pvasByVariant.getOrDefault(v.getProductVariantId(), List.of())
                                .stream().map(pva -> AdminProductByIdDTO.AttributeValueDTO.builder()
                                        .attributeValueId(pva.getAttributeValue().getAttributeValueId())
                                        .value(pva.getAttributeValue().getValue()).build())
                                .toList())
                        .build())
                .toList();

        List<ProductMaterial> productMaterials =
                productMaterialRepository.findByProductProductId(p.getProductId());
        List<AdminProductByIdDTO.MaterialRef> materialRefs = productMaterials.stream()
                .map(pm -> AdminProductByIdDTO.MaterialRef.builder()
                        .materialId(pm.getMaterial().getMaterialId())
                        .name(pm.getMaterial().getName()).build())
                .toList();

        return AdminProductByIdDTO.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .shortDescription(p.getShortDescription())
                .longDescription(p.getLongDescription())
                .featured(Boolean.TRUE.equals(p.getFeatured()))
                .status(p.getStatus())
                .category(p.getCategory() == null ? null : AdminProductByIdDTO.CategoryRef.builder()
                        .categoryId(p.getCategory().getCategoryId())
                        .name(p.getCategory().getName()).build())
                .brand(p.getBrand() == null ? null : AdminProductByIdDTO.BrandRef.builder()
                        .brandId(p.getBrand().getBrandId())
                        .name(p.getBrand().getName()).build())
                .imageUrl(p.getFileUrl())
                .mediumThumbnailUrl(FileUtils.toMediumThumbnailKey(p.getFileUrl()))
                .smallThumbnailUrl(FileUtils.toSmallThumbnailKey(p.getFileUrl()))
                .minPrice(minPrice)
                .minDiscountPrice(minDiscountPrice)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .materials(materialRefs)
                .variants(variantDTOs)
                .build();
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                        messageService.getMessage("category_not_found", LocaleUtils.getDefaultLocale())));
    }

    private Brand resolveBrand(Long brandId) {
        if (brandId == null) return null;
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                        messageService.getMessage("brand_does_not_exists", LocaleUtils.getDefaultLocale())));
    }
    
    private Product resolveProduct(UUID productId) {
        if (productId == null) return null;
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                        messageService.getMessage("product_not_found", LocaleUtils.getDefaultLocale())));
        if (product.getDeletedAt() != null) {
            throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                    messageService.getMessage("product_not_found", LocaleUtils.getDefaultLocale()));
        }
        return product;
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                        messageService.getMessage("product_not_found", LocaleUtils.getDefaultLocale())));
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Transactional
    public void restoreProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                        messageService.getMessage("product_not_found", LocaleUtils.getDefaultLocale())));
        if (product.getDeletedAt() == null) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
                    messageService.getMessage("product_not_deleted", LocaleUtils.getDefaultLocale()));
        }
        product.setDeletedAt(null);
        productRepository.save(product);
    }

    /**
     * Derives a slug from {@code name} (or the {@code product-{id}} fallback when
     * the name yields nothing) and appends {@code -2}, {@code -3}, ... until the
     * candidate collides with neither an active {@code slug} column nor a
     * {@code product_slug_history} row.
     */
    private String generateUniqueSlug(String name, Object idOrNull) {
        String base = SlugUtils.slugify(name);
        if (base.isEmpty()) {
            base = SlugUtils.fallback(PRODUCT_SLUG_PREFIX, idOrNull);
        }
        String candidate = base;
        int counter = 2;
        while (productRepository.existsBySlug(candidate) || productSlugHistoryRepository.existsBySlug(candidate)) {
            candidate = SlugUtils.withCounter(base, counter++);
        }
        return candidate;
    }

    private void validateProductName(String name) {
        productRepository.findByName(name).ifPresent(existing -> {
            if (existing.getDeletedAt() != null) {
                throw new ApiServiceException(HttpStatus.CONFLICT.value(),
                        messageService.getMessage("product_was_deleted", LocaleUtils.getDefaultLocale()),
                        Map.of("productId", existing.getProductId()));
            }
            throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
                    messageService.getMessage("product_already_exists", LocaleUtils.getDefaultLocale()));
        });
    }

    private List<Material> resolveMaterials(List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) return List.of();
        List<Material> found = materialRepository.findAllById(materialIds);
        if (found.size() != materialIds.size()) {
            throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                    messageService.getMessage("material_not_found", LocaleUtils.getDefaultLocale()));
        }
        return found;
    }

    private Map<Long, AttributeValue> resolveAttributeValues(List<CreateProductVariantDTO> variants) {
        List<Long> allIds = variants.stream()
                .filter(v -> v.getAttributeValueIds() != null)
                .flatMap(v -> v.getAttributeValueIds().stream())
                .distinct()
                .toList();
        if (allIds.isEmpty()) return Map.of();
        List<AttributeValue> found = attributeValueRepository.findAllById(allIds);
        if (found.size() != allIds.size()) {
            throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
                    messageService.getMessage("attribute_value_not_found", LocaleUtils.getDefaultLocale()));
        }
        return found.stream().collect(Collectors.toMap(AttributeValue::getAttributeValueId, av -> av));
    }

    private void validateSkus(List<CreateProductVariantDTO> variants) {
        List<String> skus = variants.stream().map(CreateProductVariantDTO::getSku).toList();

        Set<String> unique = new HashSet<>(skus);
        if (unique.size() != skus.size()) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
                    messageService.getMessage("sku_already_exists", LocaleUtils.getDefaultLocale()));
        }

        List<String> existing = productVariantRepository.findExistingSkus(skus);
        if (!existing.isEmpty()) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
                    messageService.getMessage("sku_already_exists", LocaleUtils.getDefaultLocale()));
        }

        List<ProductVariant> deletedVariants = productVariantRepository.findVariantsFromDeletedProducts(skus);
        if (!deletedVariants.isEmpty()) {
            UUID deletedProductId = deletedVariants.get(0).getProduct().getProductId();
            throw new ApiServiceException(HttpStatus.CONFLICT.value(),
                    messageService.getMessage("product_was_deleted", LocaleUtils.getDefaultLocale()),
                    Map.of("productId", deletedProductId));
        }
    }

}
