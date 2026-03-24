package com.croman.singlevendorecommerce.exceptions;

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
	
	private static final String STATUS_FIELD = "status";

	@ExceptionHandler(ApiServiceException.class)
	public ResponseEntity<Map<String, Object>> handleApiServiceException(ApiServiceException ex) {
		return ResponseEntity.status(ex.getStatusCode())
				.body(Map.of(STATUS_FIELD, ex.getStatusCode(), "error", ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		return ResponseEntity.badRequest().body(Map.of(STATUS_FIELD, HttpStatus.BAD_REQUEST.value(), "errors", errors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getConstraintViolations()
				.forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
		return ResponseEntity.badRequest().body(Map.of(STATUS_FIELD, HttpStatus.BAD_REQUEST.value(), "errors", errors));
	}
}
