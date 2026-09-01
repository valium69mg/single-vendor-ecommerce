package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.integration.support.ProductFixtures;
import com.croman.singlevendorecommerce.integration.support.ProductFixtures.SeededCatalog;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end coverage of {@code GET /api/v1/products} (visibility, pagination, sorting,
 * category filter) through the real Spring Security filter chain (public/no-auth) and a
 * real PostgreSQL Testcontainer.
 *
 * <p>Class-level {@link Transactional}: each {@code @Test} runs in a transaction rolled back
 * at method end, so the {@code @BeforeEach} catalog fixture never leaks into sibling
 * integration tests sharing the singleton container.
 */
@Transactional
class ProductListIntegrationTest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1/products";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SeededCatalog catalog;

    @BeforeEach
    void seedCatalog() {
        catalog = ProductFixtures.seedCatalog(productRepository, categoryRepository, productVariantRepository);
    }

    private JsonNode bodyOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static List<String> fieldNames(JsonNode node) {
        Iterator<String> it = node.fieldNames();
        return StreamSupport
                .stream(((Iterable<String>) () -> it).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Test
    void listReturnsOnlyActiveNonDeletedProducts() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL).param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andReturn();

        JsonNode body = bodyOf(result);
        assertThat(fieldNames(body))
                .containsExactlyInAnyOrder("content", "page", "size", "totalElements", "totalPages", "last");

        List<String> ids = new java.util.ArrayList<>();
        body.get("content").forEach(node -> ids.add(node.get("productId").asText()));

        assertThat(ids).doesNotContain(catalog.inactiveId().toString(), catalog.softDeletedId().toString());
    }

    @Test
    void paginationFirstPageSizeTwo() throws Exception {
        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void paginationLastPageHasRemainderAndLastTrue() throws Exception {
        mockMvc.perform(get(BASE_URL).param("page", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void outOfRangeHighPageReturnsEmptyContentAnd200() throws Exception {
        mockMvc.perform(get(BASE_URL).param("page", "9").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void sortByPriceAscOrdersByMinVariantPrice() throws Exception {
        mockMvc.perform(get(BASE_URL).param("sortBy", "priceAsc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].minPrice").value(100))
                .andExpect(jsonPath("$.content[4].minPrice").value(500));
    }

    @Test
    void sortByPriceDescOrdersByMinVariantPrice() throws Exception {
        mockMvc.perform(get(BASE_URL).param("sortBy", "priceDesc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].minPrice").value(500));
    }

    @Test
    void sortByMostSoldOrdersByUnitsSoldDescending() throws Exception {
        mockMvc.perform(get(BASE_URL).param("sortBy", "mostSold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productId").value(catalog.mostSoldId().toString()));
    }

    @Test
    void categoryIdFilterReturnsOnlyRings() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL).param("categoryId", catalog.ringsCategoryId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andReturn();

        JsonNode body = bodyOf(result);
        body.get("content").forEach(node -> assertThat(node.get("category").get("categoryId").asLong())
                .isEqualTo(catalog.ringsCategoryId()));
    }

    @Test
    void unknownCategoryIdReturnsEmptyPageAnd200() throws Exception {
        mockMvc.perform(get(BASE_URL).param("categoryId", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
