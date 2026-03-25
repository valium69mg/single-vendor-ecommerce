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
	
	@Query(value = """
			SELECT b.* FROM brands b
			WHERE b.name ILIKE CONCAT('%', :term, '%')
			OR EXISTS (
			SELECT 1 FROM translations t
			WHERE t.register_id = b.brand_id
			AND t.translation ILIKE CONCAT('%', :term, '%')
			)""",
		    countQuery = """
			SELECT COUNT(*) FROM brands b
			WHERE b.name ILIKE CONCAT('%', :term, '%')
			OR EXISTS (
			SELECT 1 FROM translations t
			WHERE t.register_id = b.brand_id
			AND t.translation ILIKE CONCAT('%', :term, '%'))""",
		    nativeQuery = true)
		Page<Brand> searchByNameOrTranslation(@Param("term") String term, Pageable pageable);
	
}
