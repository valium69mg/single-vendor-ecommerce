package com.croman.singlevendorecommerce.repository.products;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.singlevendorecommerce.entity.products.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
	
	@Query("SELECT b FROM Brand b WHERE LOWER(b.name) = LOWER(:name)")
	Optional<Brand> findByName(@Param("name") String name);

	Optional<Brand> findBySlug(String slug);

	boolean existsBySlug(String slug);

	@Query("SELECT b FROM Brand b WHERE b.deletedAt IS NULL")
	Page<Brand> findAllNotDeleted(Pageable pageable);

	@Query("""
			SELECT b FROM Brand b
			WHERE b.deletedAt IS NULL
			AND LOWER(b.name) LIKE LOWER(CONCAT('%', :term, '%'))""")
	Page<Brand> searchByName(@Param("term") String term, Pageable pageable);
	
}
