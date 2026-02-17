package com.croman.SingleVendorEcommerce.Products.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.SingleVendorEcommerce.Products.Entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
	Optional<Category> findByName(@Param("name") String englishName);
	
	Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
