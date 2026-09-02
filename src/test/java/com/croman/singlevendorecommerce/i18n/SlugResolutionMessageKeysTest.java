package com.croman.singlevendorecommerce.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Guard test for spec entity-slugs Requirement 13 (i18n Keys).
 *
 * <p>Slug-resolution 404 responses reuse existing not-found message keys instead of introducing new,
 * untranslated ones. This test locks that contract: each reused key MUST exist and be non-blank in
 * BOTH {@code messages.properties} and {@code messages_es.properties}, and the two files MUST stay in
 * sync (every key present in one is present in the other).
 */
class SlugResolutionMessageKeysTest {

    private static final String BASE_BUNDLE = "messages.properties";
    private static final String SPANISH_BUNDLE = "messages_es.properties";

    /**
     * The existing not-found keys reused by slug-resolution 404 responses for Product, Category and
     * Brand. The 301 (superseded-slug) redirect deliberately resolves no key.
     */
    private static final List<String> SLUG_RESOLUTION_NOT_FOUND_KEYS =
        List.of("product_not_found", "category_not_found", "brand_does_not_exists");

    private static Properties baseMessages;
    private static Properties spanishMessages;

    @BeforeAll
    static void loadBundles() throws IOException {
        baseMessages = load(BASE_BUNDLE);
        spanishMessages = load(SPANISH_BUNDLE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"product_not_found", "category_not_found", "brand_does_not_exists"})
    @DisplayName("slug-resolution 404 key exists and is non-blank in the base bundle")
    void slugResolutionKeyPresentInBaseBundle(String key) {
        assertThat(baseMessages.getProperty(key))
            .as("key '%s' must exist in %s", key, BASE_BUNDLE)
            .isNotNull()
            .isNotBlank();
    }

    @ParameterizedTest
    @ValueSource(strings = {"product_not_found", "category_not_found", "brand_does_not_exists"})
    @DisplayName("slug-resolution 404 key exists and is non-blank in the Spanish bundle")
    void slugResolutionKeyPresentInSpanishBundle(String key) {
        assertThat(spanishMessages.getProperty(key))
            .as("key '%s' must exist in %s", key, SPANISH_BUNDLE)
            .isNotNull()
            .isNotBlank();
    }

    @org.junit.jupiter.api.Test
    @DisplayName("base and Spanish message bundles expose the exact same set of keys")
    void bundlesStayInSync() {
        assertThat(spanishMessages.stringPropertyNames())
            .as("%s and %s must contain the same keys", BASE_BUNDLE, SPANISH_BUNDLE)
            .containsExactlyInAnyOrderElementsOf(baseMessages.stringPropertyNames());
    }

    private static Properties load(String resourceName) throws IOException {
        Properties properties = new Properties();
        try (InputStream stream =
                SlugResolutionMessageKeysTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            assertThat(stream).as("classpath resource '%s' must be present", resourceName).isNotNull();
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        }
        return properties;
    }
}
