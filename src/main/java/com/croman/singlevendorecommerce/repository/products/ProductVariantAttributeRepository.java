package com.croman.singlevendorecommerce.repository.products;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.entity.products.ProductVariantAttribute;

public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute, Long> {

    @Query("SELECT pva FROM ProductVariantAttribute pva WHERE pva.variant IN :variants")
    List<ProductVariantAttribute> findByVariantIn(@Param("variants") List<ProductVariant> variants);

}
