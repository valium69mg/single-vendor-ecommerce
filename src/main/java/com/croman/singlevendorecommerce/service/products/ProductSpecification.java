package com.croman.singlevendorecommerce.service.products;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.croman.singlevendorecommerce.dto.products.ProductStatus;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.entity.products.ProductMaterial;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> build(
            String term, Long categoryId, Long brandId, Long materialId,
            boolean featured, ProductStatus status,
            LocalDateTime createdAtStart, LocalDateTime createdAtEnd) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (term != null && !term.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + term.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("brandId"), brandId));
            }
            if (featured) {
                predicates.add(cb.isTrue(root.get("featured")));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (createdAtStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtStart));
            }
            if (createdAtEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdAtEnd));
            }
            if (materialId != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                Root<ProductMaterial> pmRoot = sub.from(ProductMaterial.class);
                sub.select(cb.literal(1))
                        .where(
                                cb.equal(pmRoot.get("product").get("productId"), root.get("productId")),
                                cb.equal(pmRoot.get("material").get("materialId"), materialId));
                predicates.add(cb.exists(sub));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
