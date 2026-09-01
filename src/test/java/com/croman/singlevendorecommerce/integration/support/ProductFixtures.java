package com.croman.singlevendorecommerce.integration.support;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.croman.singlevendorecommerce.dto.products.ProductStatus;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;

/**
 * Stateless construction helper for public-catalog integration-test product fixtures.
 *
 * <p>Each catalog integration test class calls {@link #seedCatalog} from its own
 * {@code @BeforeEach}; this helper only removes the copy-pasted {@code Product}/
 * {@code ProductVariant} boilerplate and its NOT-NULL traps, mirroring {@link AuthFixtures}.
 *
 * <h2>Traps encoded here</h2>
 * <ul>
 *   <li>Every {@code Product.builder()} call MUST set {@code .featured(false)} AND
 *       {@code .unitsSold(n)} explicitly — both columns are {@code NOT NULL}, and Lombok's
 *       {@code @Builder} drops field initialisers (the entity's {@code unitsSold = 0} default
 *       is invisible to the builder).</li>
 *   <li>{@link ProductVariant} has no {@code @Builder} — it is constructed via the no-args
 *       constructor and setters. {@code sku} must be unique and &lt;= 100 chars; {@code price}
 *       and {@code stock} are {@code NOT NULL}.</li>
 *   <li>Categories are looked up via {@link CategoryRepository#findByName} against the
 *       Flyway-seeded catalog. V12 seeds English names, but V21 (spanish-only) overwrites
 *       them with the Spanish translation and drops the translations table, so the runtime
 *       names are {@code "Anillos"} (Rings) and {@code "Collares"} (Necklaces) — they are
 *       never created or mutated here.</li>
 *   <li>The {@code products} table has no Flyway seed; every product row used by catalog
 *       ITs is created by this fixture.</li>
 * </ul>
 */
public final class ProductFixtures {

    // Flyway V12 seeds English category names; V21 ("spanish only") overwrites
    // `categories.name` with the Spanish translation and drops the translations
    // table, so the names visible at runtime are Spanish, not the V12 originals.
    private static final String RINGS_CATEGORY = "Anillos";
    private static final String NECKLACES_CATEGORY = "Collares";

    private ProductFixtures() {
    }

    /**
     * Seeds a deterministic catalog: 3 ACTIVE Rings + 2 ACTIVE Necklaces (distinct variant
     * prices 100/200/300/400/500 and distinct {@code unitsSold} 1..5), 1 INACTIVE Rings
     * product, and 1 soft-deleted Rings product ({@code status = ACTIVE}, {@code deletedAt}
     * set — the regression case for the public get-by-id 404 guard).
     *
     * @param productRepository        the real {@link ProductRepository}
     * @param categoryRepository       the real {@link CategoryRepository} (categories are
     *                                  Flyway-seeded by V12)
     * @param productVariantRepository the real {@link ProductVariantRepository}
     * @return the ids each catalog IT asserts against
     */
    public static SeededCatalog seedCatalog(ProductRepository productRepository,
            CategoryRepository categoryRepository, ProductVariantRepository productVariantRepository) {

        Category rings = categoryRepository.findByName(RINGS_CATEGORY)
                .orElseThrow(() -> new IllegalStateException("Flyway V12 category seed missing: " + RINGS_CATEGORY));
        Category necklaces = categoryRepository.findByName(NECKLACES_CATEGORY)
                .orElseThrow(
                        () -> new IllegalStateException("Flyway V12 category seed missing: " + NECKLACES_CATEGORY));

        UUID cheapestId = saveActiveProduct(productRepository, productVariantRepository, rings,
                "Catalog IT Ring Cheap", "CATALOG-IT-SKU-1", BigDecimal.valueOf(100), 3);
        UUID ringMidLowId = saveActiveProduct(productRepository, productVariantRepository, rings,
                "Catalog IT Ring Mid Low", "CATALOG-IT-SKU-2", BigDecimal.valueOf(200), 1);
        UUID mostSoldId = saveActiveProduct(productRepository, productVariantRepository, rings,
                "Catalog IT Ring Mid High", "CATALOG-IT-SKU-3", BigDecimal.valueOf(300), 5);

        UUID necklaceLowId = saveActiveProduct(productRepository, productVariantRepository, necklaces,
                "Catalog IT Necklace Low", "CATALOG-IT-SKU-4", BigDecimal.valueOf(400), 2);
        UUID dearestId = saveActiveProduct(productRepository, productVariantRepository, necklaces,
                "Catalog IT Necklace Dear", "CATALOG-IT-SKU-5", BigDecimal.valueOf(500), 4);

        Product inactive = productRepository.save(Product.builder()
                .name("Catalog IT Inactive Ring")
                .status(ProductStatus.INACTIVE)
                .featured(false)
                .unitsSold(0)
                .category(rings)
                .build());
        saveVariant(productVariantRepository, inactive, "CATALOG-IT-SKU-INACTIVE", BigDecimal.valueOf(150), 2);

        Product softDeleted = productRepository.save(Product.builder()
                .name("Catalog IT Soft Deleted Ring")
                .status(ProductStatus.ACTIVE)
                .featured(false)
                .unitsSold(0)
                .deletedAt(LocalDateTime.now())
                .category(rings)
                .build());
        saveVariant(productVariantRepository, softDeleted, "CATALOG-IT-SKU-DELETED", BigDecimal.valueOf(150), 2);

        return new SeededCatalog(
                rings.getCategoryId(),
                necklaces.getCategoryId(),
                List.of(cheapestId, ringMidLowId, mostSoldId),
                List.of(necklaceLowId, dearestId),
                inactive.getProductId(),
                softDeleted.getProductId(),
                cheapestId,
                dearestId,
                mostSoldId);
    }

    private static UUID saveActiveProduct(ProductRepository productRepository,
            ProductVariantRepository productVariantRepository, Category category, String name, String sku,
            BigDecimal price, int unitsSold) {
        Product product = productRepository.save(Product.builder()
                .name(name)
                .status(ProductStatus.ACTIVE)
                .featured(false)
                .unitsSold(unitsSold)
                .category(category)
                .build());
        saveVariant(productVariantRepository, product, sku, price, 10);
        return product.getProductId();
    }

    private static void saveVariant(ProductVariantRepository productVariantRepository, Product product, String sku,
            BigDecimal price, int stock) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(sku);
        variant.setPrice(price);
        variant.setStock(stock);
        productVariantRepository.save(variant);
    }

    /**
     * Ids each catalog IT asserts against.
     *
     * @param ringsCategoryId     the Flyway-seeded {@code "Rings"} category id
     * @param necklacesCategoryId the Flyway-seeded {@code "Necklaces"} category id
     * @param ringsActiveIds      the 3 ACTIVE Rings product ids (cheapest, mid-low, most-sold)
     * @param necklacesActiveIds  the 2 ACTIVE Necklaces product ids (low, dearest)
     * @param inactiveId          the INACTIVE Rings product id
     * @param softDeletedId       the soft-deleted ACTIVE Rings product id ({@code deletedAt} set)
     * @param cheapestId          the ACTIVE product id with the lowest variant price (100)
     * @param dearestId           the ACTIVE product id with the highest variant price (500)
     * @param mostSoldId          the ACTIVE product id with the highest {@code unitsSold}
     */
    public record SeededCatalog(
            Long ringsCategoryId,
            Long necklacesCategoryId,
            List<UUID> ringsActiveIds,
            List<UUID> necklacesActiveIds,
            UUID inactiveId,
            UUID softDeletedId,
            UUID cheapestId,
            UUID dearestId,
            UUID mostSoldId) {
    }
}
