package com.croman.singlevendorecommerce.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.products.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

}
