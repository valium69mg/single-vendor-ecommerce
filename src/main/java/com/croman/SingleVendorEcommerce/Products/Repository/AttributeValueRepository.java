package com.croman.SingleVendorEcommerce.Products.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.croman.SingleVendorEcommerce.Products.Entity.AttributeValue;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {

	@Query("""
	        SELECT av
	        FROM AttributeValue av
	        JOIN FETCH av.attribute a
	        WHERE a.attributeId IN :attributeIds
	        """)
	List<AttributeValue> findByAttributeIdIn(List<Long> attributeIds);
}

