package com.croman.SingleVendorEcommerce.Products.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Products.Entity.Material;

public interface MaterialRepository extends JpaRepository<Material, Long>{

}
