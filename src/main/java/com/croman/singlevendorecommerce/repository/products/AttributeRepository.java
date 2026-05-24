package com.croman.singlevendorecommerce.repository.products;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.products.Attribute;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {

	Optional<Attribute> findByAttributeType(String attributeType);
}
