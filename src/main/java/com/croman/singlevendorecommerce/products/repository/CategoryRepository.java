package com.croman.singlevendorecommerce.products.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.singlevendorecommerce.products.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
	Optional<Category> findByName(@Param("name") String englishName);
	
	Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
	
	@Query(value = """
		    SELECT c.* FROM categories c
		    WHERE c.name ILIKE CONCAT('%', :term, '%')
		       OR EXISTS (
		           SELECT 1 FROM translations t
		           WHERE t.register_id = c.category_id
		             AND t.translation ILIKE CONCAT('%', :term, '%')
		       )
		    """,
		    countQuery = """
		    SELECT COUNT(*) FROM categories c
		    WHERE c.name ILIKE CONCAT('%', :term, '%')
		       OR EXISTS (
		           SELECT 1 FROM translations t
		           WHERE t.register_id = c.category_id
		             AND t.translation ILIKE CONCAT('%', :term, '%')
		       )
		    """,
		    nativeQuery = true)
		Page<Category> searchByNameOrTranslation(@Param("term") String term, Pageable pageable);




}
