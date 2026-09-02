package com.croman.singlevendorecommerce.repository.products;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.products.CategorySlugHistory;

public interface CategorySlugHistoryRepository extends JpaRepository<CategorySlugHistory, Long> {

	Optional<CategorySlugHistory> findBySlug(String slug);

	boolean existsBySlug(String slug);

}
