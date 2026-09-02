package com.croman.singlevendorecommerce.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugUtilsTest {

	// ─── slugify ─────────────────────────────────────────────────────────────

	@Test
	void testSlugifyLowercasesPlainAscii() {
		assertThat(SlugUtils.slugify("Gold Rings")).isEqualTo("gold-rings");
	}

	@Test
	void testSlugifyStripsSpanishAccentsAndTilde() {
		assertThat(SlugUtils.slugify("Anillos de Diseño Óñe")).isEqualTo("anillos-de-diseno-one");
	}

	@Test
	void testSlugifyCollapsesWhitespaceAndPunctuationRuns() {
		assertThat(SlugUtils.slugify("  Gold   RINGS!! ")).isEqualTo("gold-rings");
	}

	@Test
	void testSlugifyTrimsLeadingAndTrailingSeparators() {
		assertThat(SlugUtils.slugify("--Gold-Rings--")).isEqualTo("gold-rings");
		assertThat(SlugUtils.slugify("!!!Rings???")).isEqualTo("rings");
	}

	@Test
	void testSlugifyReturnsEmptyStringForEmptyInput() {
		assertThat(SlugUtils.slugify("")).isEqualTo("");
	}

	@Test
	void testSlugifyReturnsEmptyStringForNullInput() {
		assertThat(SlugUtils.slugify(null)).isEqualTo("");
	}

	@Test
	void testSlugifyReturnsEmptyStringWhenNoAlphanumericSurvives() {
		assertThat(SlugUtils.slugify("!!! ??? ---")).isEqualTo("");
	}

	// ─── withCounter ─────────────────────────────────────────────────────────

	@Test
	void testWithCounterAppendsSuffix() {
		assertThat(SlugUtils.withCounter("rings", 2)).isEqualTo("rings-2");
		assertThat(SlugUtils.withCounter("rings", 3)).isEqualTo("rings-3");
	}

	// ─── fallback ────────────────────────────────────────────────────────────

	@Test
	void testFallbackBuildsPrefixedIdentifier() {
		assertThat(SlugUtils.fallback("category", 7L)).isEqualTo("category-7");
		assertThat(SlugUtils.fallback("brand", 3L)).isEqualTo("brand-3");
	}
}
