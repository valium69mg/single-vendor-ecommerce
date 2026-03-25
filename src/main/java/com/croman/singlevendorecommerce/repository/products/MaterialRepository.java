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
	Optional<Material> findByName(@Param("name") String englishName);
	
	@Query(value = """
			SELECT m.* FROM materials m
			WHERE m.name ILIKE CONCAT('%', :term, '%')
			OR EXISTS (
			SELECT 1 FROM translations t
			WHERE t.register_id = m.material_id
			AND t.translation ILIKE CONCAT('%', :term, '%')
			)""",
		    countQuery = """
			SELECT COUNT(*) FROM materials m
			WHERE m.name ILIKE CONCAT('%', :term, '%')
			OR EXISTS (
			SELECT 1 FROM translations t
			WHERE t.register_id = m.material_id
			AND t.translation ILIKE CONCAT('%', :term, '%'))""",
		    nativeQuery = true)
		Page<Material> searchByNameOrTranslation(@Param("term") String term, Pageable pageable);

}
