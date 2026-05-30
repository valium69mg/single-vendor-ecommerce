package com.croman.singlevendorecommerce.repository.products;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.singlevendorecommerce.entity.products.Material;

public interface MaterialRepository extends JpaRepository<Material, Long>{

	@Query("SELECT m FROM Material m WHERE LOWER(m.name) = LOWER(:name)")
	Optional<Material> findByName(@Param("name") String name);

	@Query("SELECT m FROM Material m WHERE m.deletedAt IS NULL")
	Page<Material> findAllNotDeleted(Pageable pageable);

	@Query("""
			SELECT m FROM Material m
			WHERE m.deletedAt IS NULL
			AND LOWER(m.name) LIKE LOWER(CONCAT('%', :term, '%'))""")
	Page<Material> searchByName(@Param("term") String term, Pageable pageable);

}
