package com.croman.singlevendorecommerce.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.integration.support.ProductFixtures;
import com.croman.singlevendorecommerce.integration.support.ProductFixtures.SeededCatalog;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;

/**
 * End-to-end coverage of {@code GET /api/v1/products/{productId}} through the real Spring
 * Security filter chain (public/no-auth) and a real PostgreSQL Testcontainer.
 *
 * <p>Class-level {@link Transactional}: each {@code @Test} runs in a transaction rolled back
 * at method end, so the {@code @BeforeEach} catalog fixture never leaks into sibling
 * integration tests sharing the singleton container.
 */
@Transactional
class ProductByIdIntegrationTest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1/products/";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private SeededCatalog catalog;

    @BeforeEach
    void seedCatalog() {
        catalog = ProductFixtures.seedCatalog(productRepository, categoryRepository, productVariantRepository);
    }

    @Test
    void activeProductReturns200WithDetailBody() throws Exception {
        UUID activeId = catalog.cheapestId();

        mockMvc.perform(get(BASE_URL + activeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(activeId.toString()))
                .andExpect(jsonPath("$.variants").isArray())
                .andExpect(jsonPath("$.category.name").value("Anillos"));
    }

    @Test
    void unknownIdReturns404() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get(BASE_URL + unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void inactiveProductReturns404() throws Exception {
        mockMvc.perform(get(BASE_URL + catalog.inactiveId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void softDeletedActiveProductReturns404() throws Exception {
        mockMvc.perform(get(BASE_URL + catalog.softDeletedId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void malformedUuidReturns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
