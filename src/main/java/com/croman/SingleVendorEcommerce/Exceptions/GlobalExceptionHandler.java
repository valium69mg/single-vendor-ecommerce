package com.croman.SingleVendorEcommerce.Exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiServiceException.class)
	public ResponseEntity<Map<String, Object>> handleApiServiceException(ApiServiceException ex) {
		return ResponseEntity.status(ex.getStatusCode())
				.body(Map.of("status", ex.getStatusCode(), "error", ex.getMessage()));
	}
}
