package com.croman.singlevendorecommerce.utils.exceptions;

import java.util.Map;

public class ApiServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final int statusCode;
	private final Map<String, Object> metadata;

	public ApiServiceException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
		this.metadata = Map.of();
	}

	public ApiServiceException(int statusCode, String message, Map<String, Object> metadata) {
		super(message);
		this.statusCode = statusCode;
		this.metadata = metadata;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}
}
