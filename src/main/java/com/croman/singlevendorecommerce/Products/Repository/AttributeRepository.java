package com.croman.singlevendorecommerce.Products.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Products.Entity.Attribute;

public interface AttributeRepository extends JpaRepository<Attribute, Long>{

}
