package com.croman.SingleVendorEcommerce.Products.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Products.Entity.ProductVariantAttribute;

public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute, Long>{

}
