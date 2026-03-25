package com.croman.singlevendorecommerce.repository.products;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.products.ProductMaterial;

public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, Long> {

}
