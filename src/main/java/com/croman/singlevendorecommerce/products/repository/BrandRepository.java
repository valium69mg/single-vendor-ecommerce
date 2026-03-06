package com.croman.singlevendorecommerce.products.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.singlevendorecommerce.products.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
	
	@Query("SELECT c FROM Brand c WHERE LOWER(c.name) = LOWER(:name)")
	Optional<Brand> findByName(@Param("name") String name);
	
}
