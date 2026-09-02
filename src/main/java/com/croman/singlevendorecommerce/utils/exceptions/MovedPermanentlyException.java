package com.croman.singlevendorecommerce.utils.exceptions;

/**
 * Raised when a public by-slug lookup matches a superseded (historical) slug. The
 * {@link com.croman.singlevendorecommerce.utils.exceptions.GlobalExceptionHandler}
 * turns it into an HTTP 301 whose {@code Location} header points at the current
 * canonical by-slug path and whose body carries the canonical slug for clients
 * that read the body instead of following the redirect.
 *
 * <p>{@code location} is always a server-built, relative, same-origin API path
 * derived from the entity's current DB slug — never from caller input.
 */
public class MovedPermanentlyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String canonicalSlug;
	private final String location;

	public MovedPermanentlyException(String canonicalSlug, String location) {
		super("Slug moved permanently to " + canonicalSlug);
		this.canonicalSlug = canonicalSlug;
		this.location = location;
	}

	public String getCanonicalSlug() {
		return canonicalSlug;
	}

	public String getLocation() {
		return location;
	}
}
