package com.croman.singlevendorecommerce.integration.support;

import java.time.LocalDateTime;
import java.util.UUID;

import com.croman.singlevendorecommerce.dto.products.ProductStatus;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.utils.SlugUtils;

/**
 * Stateless construction helper for admin-category integration-test fixtures.
 *
 * <p>Each category integration test class calls these helpers from its own
 * {@code @BeforeEach}; this helper only removes the copy-pasted
 * {@code Category.builder()} / {@code Product.builder()} boilerplate and its
 * NOT-NULL traps, mirroring {@link AuthFixtures} and {@link ProductFixtures}.
 *
 * <h2>Traps encoded here</h2>
 * <ul>
 *   <li>The {@code categories} table is <em>never empty</em>: Flyway seeds 9 rows
 *       (Spanish names, after V21 drops the translations table). Every list/count
 *       assertion in a category IT MUST therefore be relative to a pre-seed count
 *       captured in {@code @BeforeEach}, never an absolute number, and never an
 *       "empty table" assumption. Fixtures here only ever <em>add</em> disposable
 *       rows with distinctive {@code "IT "}-prefixed names.</li>
 *   <li>{@code Category.name} is {@code unique} (column length 255) — callers must
 *       pass names that cannot collide with the 9 seeded Spanish rows or with each
 *       other within one test.</li>
 *   <li>Every {@code Product.builder()} call MUST set {@code .status(...)},
 *       {@code .featured(false)} AND {@code .unitsSold(0)} explicitly — all three
 *       columns are {@code NOT NULL} and Lombok's {@code @Builder} drops the
 *       entity's field initialisers.</li>
 *   <li>Class-level {@code @Transactional} on the IT rolls every row created here
 *       back at method end, so nothing leaks into sibling ITs sharing the
 *       singleton Testcontainers Postgres.</li>
 * </ul>
 */
public final class CategoryFixtures {

	private CategoryFixtures() {
	}

	/**
	 * Persist an active (non-deleted) disposable category.
	 *
	 * @param categoryRepository the real {@link CategoryRepository}
	 * @param name               a distinctive name that does not collide with the 9
	 *                           Flyway-seeded rows
	 * @return the persisted {@link Category} (id, {@code createdAt}, {@code updatedAt} populated)
	 */
	public static Category seedCategory(CategoryRepository categoryRepository, String name) {
		return categoryRepository.save(Category.builder().name(name).slug(SlugUtils.slugify(name)).build());
	}

	/**
	 * Persist a soft-deleted disposable category ({@code deletedAt} set to now).
	 *
	 * @param categoryRepository the real {@link CategoryRepository}
	 * @param name               a distinctive name that does not collide with the 9
	 *                           Flyway-seeded rows
	 * @return the persisted soft-deleted {@link Category}
	 */
	public static Category seedSoftDeletedCategory(CategoryRepository categoryRepository, String name) {
		return categoryRepository.save(
				Category.builder().name(name).slug(SlugUtils.slugify(name)).deletedAt(LocalDateTime.now()).build());
	}

	/**
	 * Persist an active category plus one ACTIVE {@link Product} that references it,
	 * for the "delete a category that still has products" characterization: after
	 * {@code DELETE /categories/{id}} the category is soft-deleted while the product
	 * row stays readable and keeps its {@code category_id} (no block, no cascade).
	 *
	 * @param categoryRepository the real {@link CategoryRepository}
	 * @param productRepository  the real {@link ProductRepository}
	 * @param name               a distinctive category name
	 * @return the seeded category and the referencing product's id
	 */
	public static SeededCategoryWithProduct seedCategoryWithProduct(CategoryRepository categoryRepository,
			ProductRepository productRepository, String name) {
		Category category = categoryRepository.save(Category.builder().name(name).slug(SlugUtils.slugify(name)).build());
		Product product = productRepository.save(Product.builder()
				.name(name + " Product")
				.slug(SlugUtils.slugify(name + " Product"))
				.status(ProductStatus.ACTIVE)
				.featured(false)
				.unitsSold(0)
				.category(category)
				.build());
		return new SeededCategoryWithProduct(category, product.getProductId());
	}

	/**
	 * @param category  the seeded active category
	 * @param productId the id of the ACTIVE product referencing {@code category}
	 */
	public record SeededCategoryWithProduct(Category category, UUID productId) {
	}
}
