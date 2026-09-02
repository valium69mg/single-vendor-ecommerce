package com.croman.singlevendorecommerce.entity.products;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Historical (superseded) slug for a {@link Brand}. A row is written only when an
 * admin rename changes the derived slug; the old slug is retained here so that
 * stale links can be resolved with an HTTP 301 to the current canonical slug.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "brand_slug_history")
public class BrandSlugHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "slug", length = 255, nullable = false, unique = true)
	private String slug;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "brand_id", nullable = false)
	private Brand brand;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public BrandSlugHistory(String slug, Brand brand, LocalDateTime createdAt) {
		this.slug = slug;
		this.brand = brand;
		this.createdAt = createdAt;
	}
}
