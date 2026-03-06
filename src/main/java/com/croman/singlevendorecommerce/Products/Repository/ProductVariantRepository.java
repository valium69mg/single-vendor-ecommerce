package com.croman.singlevendorecommerce.Products.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Products.Entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

}
