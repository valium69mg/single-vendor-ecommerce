package com.croman.singlevendorecommerce.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.products.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}