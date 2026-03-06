package com.croman.singlevendorecommerce.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.products.entity.ProductVariantAttribute;

public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute, Long>{

}
