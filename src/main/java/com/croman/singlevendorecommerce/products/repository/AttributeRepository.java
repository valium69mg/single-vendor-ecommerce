package com.croman.singlevendorecommerce.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.products.entity.Attribute;

public interface AttributeRepository extends JpaRepository<Attribute, Long>{

}
