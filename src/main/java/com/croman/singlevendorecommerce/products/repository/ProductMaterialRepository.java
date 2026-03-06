package com.croman.singlevendorecommerce.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.products.entity.ProductMaterial;

public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, Long> {

}
