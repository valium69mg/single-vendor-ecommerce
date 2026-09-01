package com.croman.singlevendorecommerce.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.auth.LoginAttempt;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.repository.auth.LoginAttemptRepository;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;

/**
 * End-to-end coverage of {@code POST /api/v1/auth/login} through the real Spring
 * Security filter chain and a real PostgreSQL Testcontainer.
 *
 * <p>Class-level {@link Transactional}: each {@code @Test} runs in a transaction
 * rolled back at method end, so the {@code @BeforeEach} user fixture and any
 * {@code login_attempts} rows never leak into sibling integration tests sharing
 * the singleton container.
 */
@Transactional
class AuthLoginIntegrationTest extends AbstractIntegrationTest {

	private static final String LOGIN_URL = "/api/v1/auth/login";
	private static final String EMAIL = "login-it@test.com";
	private static final String PASSWORD = "correct-horse-battery";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private LoginAttemptRepository loginAttemptRepository;

	@Autowired
	private MessageService messageService;

	@BeforeEach
	void seedUser() {
		AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL, PASSWORD, RoleType.USER);
	}

	private String invalidCredentialsMessage() {
		return messageService.getMessage("invalid_credentials", Locale.of("es"));
	}

	private static String body(String email, String password) {
		return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
	}

	@Test
	void validCredentialsReturn200WithLoginResponse() throws Exception {
		mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body(EMAIL, PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").isNotEmpty())
				.andExpect(jsonPath("$.email").value(EMAIL))
				// LoginResponseDTO.name currently returns the email (latent out-of-scope bug, ticket T1).
				.andExpect(jsonPath("$.name").value(EMAIL))
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void wrongPasswordReturns401() throws Exception {
		mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body(EMAIL, "wrong-password")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value(invalidCredentialsMessage()));
	}

	@Test
	void unknownUserReturns401() throws Exception {
		mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
				.content(body("nobody-it@test.com", "any-password")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value(invalidCredentialsMessage()));
	}

	@Test
	void absentBodyReturns400() throws Exception {
		mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void malformedJsonBodyReturns400() throws Exception {
		mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("{\"email\":"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void blankEmailFailsBeanValidationWith400() throws Exception {
		mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body("", PASSWORD)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void nonEmailEmailFailsBeanValidationWith400() throws Exception {
		mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body("not-an-email", PASSWORD)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void blankPasswordFailsBeanValidationWith400() throws Exception {
		mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body(EMAIL, "")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void fiveFailedAttemptsWithinOneHourLockTheAccountWith423() throws Exception {
		// Seed 5 failed attempts and re-login in ONE method / one transaction: Hibernate
		// autoflushes before the lockout count query, so the rows are visible without a commit.
		// Do not split this into @BeforeEach + @Test — the rollback would not see the inserts.
		User user = userRepository.findByEmail(EMAIL).orElseThrow();
		for (int i = 0; i < 5; i++) {
			loginAttemptRepository.save(LoginAttempt.builder()
					.user(user)
					.email(EMAIL)
					.ipAddress("127.0.0.1")
					.successful(false)
					.attemptedAt(LocalDateTime.now().minusMinutes(i + 1))
					.build());
		}

		mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body(EMAIL, PASSWORD)))
				.andExpect(status().isLocked())
				.andExpect(jsonPath("$.status").value(423));
	}
}
