package com.croman.singlevendorecommerce.repository.products;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.singlevendorecommerce.entity.products.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Query("SELECT v.sku FROM ProductVariant v WHERE v.sku IN :skus AND v.product.deletedAt IS NULL")
    List<String> findExistingSkus(@Param("skus") Collection<String> skus);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product p WHERE v.sku IN :skus AND p.deletedAt IS NOT NULL")
    List<ProductVariant> findVariantsFromDeletedProducts(@Param("skus") Collection<String> skus);

    List<ProductVariant> findByProductProductId(UUID productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.productId IN :productIds")
    List<ProductVariant> findByProductIds(@Param("productIds") List<UUID> productIds);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.productVariantId = :id")
    Optional<ProductVariant> findByIdWithProduct(@Param("id") Long id);

}
