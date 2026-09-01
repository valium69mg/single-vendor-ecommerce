package com.croman.singlevendorecommerce.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import com.croman.singlevendorecommerce.dto.users.CreateUserDTO;
import com.croman.singlevendorecommerce.service.users.UserService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

	@Mock private UserService userService;
	@Mock private Validator validator;
	@Mock private ApplicationArguments args;

	private AdminBootstrapRunner runner;

	@BeforeEach
	void setUp() {
		runner = new AdminBootstrapRunner(userService, validator);
	}

	private void setCredentials(String email, String password) {
		ReflectionTestUtils.setField(runner, "adminEmail", email);
		ReflectionTestUtils.setField(runner, "adminPassword", password);
	}

	@Test
	void skipsWhenAdminAlreadyExists() {
		when(userService.adminPresent()).thenReturn(true);
		setCredentials("admin@example.com", "Carlos123$");

		runner.run(args);

		verify(userService, never()).createSiteAdmin(any());
		verifyNoInteractions(validator);
	}

	@Test
	void skipsWhenCredentialsMissing() {
		when(userService.adminPresent()).thenReturn(false);
		setCredentials("", "");

		runner.run(args);

		verify(userService, never()).createSiteAdmin(any());
		verifyNoInteractions(validator);
	}

	@Test
	void seedsAdminWhenAbsentAndCredentialsValid() {
		when(userService.adminPresent()).thenReturn(false);
		when(validator.validate(any(CreateUserDTO.class))).thenReturn(Set.of());
		setCredentials("  admin@example.com  ", "Carlos123$");

		runner.run(args);

		ArgumentCaptor<CreateUserDTO> captor = ArgumentCaptor.forClass(CreateUserDTO.class);
		verify(userService).createSiteAdmin(captor.capture());
		assertEquals("admin@example.com", captor.getValue().getEmail());
		assertEquals("Carlos123$", captor.getValue().getPassword());
	}

	@Test
	void throwsWhenCredentialsInvalid() {
		@SuppressWarnings("unchecked")
		ConstraintViolation<CreateUserDTO> violation = mock(ConstraintViolation.class);
		when(userService.adminPresent()).thenReturn(false);
		when(validator.validate(any(CreateUserDTO.class))).thenReturn(Set.of(violation));
		setCredentials("not-an-email", "weak");

		assertThrows(IllegalStateException.class, () -> runner.run(args));

		verify(userService, never()).createSiteAdmin(any());
	}
}
