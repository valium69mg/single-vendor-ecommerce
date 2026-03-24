package com.croman.singlevendorecommerce.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    // ─── handleApiServiceException ────────────────────────────────────────────

    @Test
    void testHandleApiServiceExceptionReturnsCorrectStatusAndBody() {
        ApiServiceException ex = new ApiServiceException(HttpStatus.NOT_FOUND.value(), "Resource not found");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleApiServiceException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody()).containsEntry("status", HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody()).containsEntry("error", "Resource not found");
    }

    @Test
    void testHandleApiServiceExceptionReturnsBadRequestWhenStatusIs400() {
        ApiServiceException ex = new ApiServiceException(HttpStatus.BAD_REQUEST.value(), "Invalid input");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleApiServiceException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody()).containsEntry("error", "Invalid input");
    }

    // ─── handleValidationExceptions ──────────────────────────────────────────

    @Test
    void testHandleValidationExceptionsReturnsBadRequestWithFieldErrors() {
        FieldError fieldError = new FieldError("object", "name", "must not be blank");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("errors");

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).containsEntry("name", "must not be blank");
    }

    @Test
    void testHandleValidationExceptionsReturnsAllFieldErrors() {
        FieldError nameError  = new FieldError("object", "name",  "must not be blank");
        FieldError emailError = new FieldError("object", "email", "must be a valid email");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(nameError, emailError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationExceptions(ex);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).hasSize(2)
                .containsEntry("name",  "must not be blank")
                .containsEntry("email", "must be a valid email");
    }

    // ─── handleConstraintViolationException ──────────────────────────────────

    @Test
    void testHandleConstraintViolationExceptionReturnsBadRequestWithViolations() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.handleConstraintViolationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).containsEntry("name", "must not be blank");
    }
}