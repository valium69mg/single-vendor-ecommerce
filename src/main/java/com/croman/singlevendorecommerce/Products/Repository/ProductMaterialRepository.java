package com.croman.singlevendorecommerce.Products.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Products.Entity.ProductMaterial;

public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, Long> {

}
