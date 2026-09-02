package com.croman.singlevendorecommerce.repository.products;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.products.ProductSlugHistory;

public interface ProductSlugHistoryRepository extends JpaRepository<ProductSlugHistory, Long> {

	Optional<ProductSlugHistory> findBySlug(String slug);

	boolean existsBySlug(String slug);

}
