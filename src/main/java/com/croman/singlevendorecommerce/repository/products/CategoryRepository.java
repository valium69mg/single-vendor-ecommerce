package com.croman.singlevendorecommerce.repository.products;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.singlevendorecommerce.entity.products.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
	Optional<Category> findByName(@Param("name") String name);

	@Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
	Page<Category> findAllNotDeleted(Pageable pageable);

	@Query("""
			SELECT c FROM Category c
			WHERE c.deletedAt IS NULL
			AND LOWER(c.name) LIKE LOWER(CONCAT('%', :term, '%'))""")
	Page<Category> searchByName(@Param("term") String term, Pageable pageable);

}
