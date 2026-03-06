package com.croman.singlevendorecommerce.Products.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Products.Entity.ProductVariantAttribute;

public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute, Long>{

}
