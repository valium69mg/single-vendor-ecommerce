package com.croman.SingleVendorEcommerce.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiServiceException.class)
	public ResponseEntity<Map<String, Object>> handleApiServiceException(ApiServiceException ex) {
		return ResponseEntity.status(ex.getStatusCode())
				.body(Map.of("status", ex.getStatusCode(), "error", ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		return ResponseEntity.badRequest().body(Map.of("status", HttpStatus.BAD_REQUEST.value(), "errors", errors));
	}

	public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getConstraintViolations()
				.forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
		return ResponseEntity.badRequest().body(Map.of("status", HttpStatus.BAD_REQUEST.value(), "errors", errors));
	}
}
