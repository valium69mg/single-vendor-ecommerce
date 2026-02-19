package com.croman.SingleVendorEcommerce.Products.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Products.Entity.AttributeValue;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long>{

}
