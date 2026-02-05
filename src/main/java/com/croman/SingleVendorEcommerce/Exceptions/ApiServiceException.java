package com.croman.SingleVendorEcommerce.Exceptions;


public class ApiServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final int statusCode;

	public ApiServiceException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
