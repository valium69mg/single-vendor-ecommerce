package com.croman.singlevendorecommerce.repository.products;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.products.ProductVariantAttribute;

public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute, Long>{

}
