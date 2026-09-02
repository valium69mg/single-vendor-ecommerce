package com.croman.singlevendorecommerce.repository.products;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.products.BrandSlugHistory;

public interface BrandSlugHistoryRepository extends JpaRepository<BrandSlugHistory, Long> {

	Optional<BrandSlugHistory> findBySlug(String slug);

	boolean existsBySlug(String slug);

}
