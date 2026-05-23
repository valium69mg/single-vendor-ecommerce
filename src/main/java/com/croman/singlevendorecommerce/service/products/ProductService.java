package com.croman.singlevendorecommerce.service.products;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.products.CreateProductDTO;
import com.croman.singlevendorecommerce.dto.products.CreateProductVariantDTO;
import com.croman.singlevendorecommerce.entity.products.AttributeValue;
import com.croman.singlevendorecommerce.entity.products.Brand;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.entity.products.Material;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.entity.products.ProductMaterial;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.entity.products.ProductVariantAttribute;
import com.croman.singlevendorecommerce.repository.products.AttributeValueRepository;
import com.croman.singlevendorecommerce.repository.products.BrandRepository;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.MaterialRepository;
import com.croman.singlevendorecommerce.repository.products.ProductMaterialRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantAttributeRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMaterialRepository productMaterialRepository;
    private final ProductVariantAttributeRepository productVariantAttributeRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final MessageService messageService;

    @Transactional
    public void createProduct(CreateProductDTO dto) {
        Category category = resolveCategory(dto.getCategoryId());
        Brand brand = resolveBrand(dto.getBrandId());
        List<Material> materials = resolveMaterials(dto.getMaterialIds());
        Map<Long, AttributeValue> attributeValueMap = resolveAttributeValues(dto.getVariants());

        validateSkus(dto.getVariants());

        Product savedProduct = productRepository.save(Product.builder()
                .name(dto.getName())
                .shortDescription(dto.getShortDescription())
                .longDescription(dto.getLongDescription())
                .status(dto.getStatus())
                .featured(dto.isFeatured())
                .category(category)
                .brand(brand)
                .build());

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
    }

}
