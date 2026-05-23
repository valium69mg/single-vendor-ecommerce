package com.croman.singlevendorecommerce.repository.products;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.singlevendorecommerce.entity.products.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Query("SELECT v.sku FROM ProductVariant v WHERE v.sku IN :skus")
    List<String> findExistingSkus(@Param("skus") Collection<String> skus);

}
