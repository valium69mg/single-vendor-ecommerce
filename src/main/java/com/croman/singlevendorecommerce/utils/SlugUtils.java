package com.croman.singlevendorecommerce.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Pure, zero-dependency helper that derives URL slugs from entity names.
 *
 * <p>Algorithm: Unicode NFD normalization -&gt; strip combining marks
 * ({@code \p{M}}) -&gt; lowercase -&gt; replace every run of non-{@code [a-z0-9]}
 * with a single {@code -} -&gt; trim leading/trailing {@code -}.
 */
public final class SlugUtils {

	private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
	private static final Pattern NON_ALNUM_RUN = Pattern.compile("[^a-z0-9]+");
	private static final Pattern EDGE_SEPARATORS = Pattern.compile("^-+|-+$");

	private SlugUtils() {
	}

	/**
	 * Derives a slug from {@code name}. Returns {@code ""} when {@code name} is
	 * {@code null}, blank, or contains no alphanumeric characters after
	 * normalization.
	 */
	public static String slugify(String name) {
		if (name == null) {
			return "";
		}
		String decomposed = Normalizer.normalize(name, Normalizer.Form.NFD);
		String withoutMarks = COMBINING_MARKS.matcher(decomposed).replaceAll("");
		String lowercased = withoutMarks.toLowerCase();
		String hyphenated = NON_ALNUM_RUN.matcher(lowercased).replaceAll("-");
		return EDGE_SEPARATORS.matcher(hyphenated).replaceAll("");
	}

	/**
	 * Appends a numeric disambiguation suffix, e.g. {@code withCounter("rings", 2)}
	 * -&gt; {@code "rings-2"}.
	 */
	public static String withCounter(String base, int counter) {
		return base + "-" + counter;
	}

	/**
	 * Builds a deterministic fallback slug for entities whose name does not yield a
	 * usable slug, e.g. {@code fallback("category", 7)} -&gt; {@code "category-7"}.
	 */
	public static String fallback(String prefix, Object id) {
		return prefix + "-" + id;
	}
}
