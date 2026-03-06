package com.croman.singlevendorecommerce.products.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.croman.singlevendorecommerce.products.entity.Material;

public interface MaterialRepository extends JpaRepository<Material, Long>{

	@Query("SELECT m FROM Material m WHERE LOWER(m.name) = LOWER(:name)")
	Optional<Material> findByName(@Param("name") String englishName);

}
