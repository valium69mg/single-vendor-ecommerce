package com.croman.SingleVendorEcommerce.Products.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Products.Entity.Attribute;

public interface AttributeRepository extends JpaRepository<Attribute, Long>{

}
