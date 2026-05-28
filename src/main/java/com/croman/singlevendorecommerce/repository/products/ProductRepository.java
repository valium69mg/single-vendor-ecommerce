package com.croman.singlevendorecommerce.repository.products;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.products.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

}