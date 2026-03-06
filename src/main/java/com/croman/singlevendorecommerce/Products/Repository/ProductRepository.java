package com.croman.singlevendorecommerce.Products.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Products.Entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}