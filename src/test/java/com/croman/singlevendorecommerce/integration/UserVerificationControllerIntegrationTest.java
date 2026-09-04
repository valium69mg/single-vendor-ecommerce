package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.roles.UserRole;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.entity.users.VerificationCode;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.repository.users.VerificationCodeRepository;
import com.croman.singlevendorecommerce.utils.PasswordUtils;

import jakarta.persistence.EntityManager;

/**
 * End-to-end coverage of {@code POST /api/v1/users/verify} and
 * {@code POST /api/v1/users/verify/resend} through the real Spring Security
 * filter chain (whitelist reachability, pre-auth) and a real PostgreSQL
 * Testcontainer, plus the register -&gt; generateAndSend wiring point in
 * {@code UserController.createUser}.
 *
 * <p>Class-level {@link Transactional}: each test runs in a transaction rolled
 * back at method end so seeded users/codes never leak into sibling
 * integration tests sharing the singleton container.
 */
@Transactional
class UserVerificationControllerIntegrationTest extends AbstractIntegrationTest {

	private static final String VERIFY_URL = "/api/v1/users/verify";
	private static final String RESEND_URL = "/api/v1/users/verify/resend";
	private static final String REGISTER_URL = "/api/v1/users/register";
	private static final String EMAIL = "verify-it@test.com";
	private static final String PASSWORD = "correct-horse-battery";
	private static final String RAW_CODE = "123456";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private VerificationCodeRepository verificationCodeRepository;

	@Autowired
	private EntityManager entityManager;

	private User user;

	@BeforeEach
	void seedUnverifiedUser() {
		UserRole role = userRoleRepository.findByRoleType(RoleType.USER)
				.orElseThrow(() -> new IllegalStateException("Flyway V6 role seed missing role: USER"));
		user = userRepository.save(User.builder().email(EMAIL).username(EMAIL)
				.password(PasswordUtils.hashPassword(PASSWORD)).isActive(true).isValidated(false).userRole(role)
				.build());
	}

	private VerificationCode code(String rawCode, int attempts, LocalDateTime expiresAt) {
		return verificationCodeRepository.save(VerificationCode.builder().user(user)
				.codeHash(PasswordUtils.hashPassword(rawCode)).createdAt(LocalDateTime.now()).expiresAt(expiresAt)
				.attempts(attempts).build());
	}

	private static String verifyBody(String email, String code) {
		return "{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}";
	}

	private static String resendBody(String email) {
		return "{\"email\":\"" + email + "\"}";
	}

	@Test
	void verify_correctUnexpiredCode_returns200AndMarksAccountValidated() throws Exception {
		VerificationCode issued = code(RAW_CODE, 0, LocalDateTime.now().plusMinutes(15));

		mockMvc.perform(post(VERIFY_URL).contentType(MediaType.APPLICATION_JSON).content(verifyBody(EMAIL, RAW_CODE)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200));

		// updateIsValidated is a @Modifying bulk query, and the code-consumption save()
		// is entity-based dirty checking: flush the pending changes to the DB, then clear
		// the first-level cache so both re-reads reflect the committed column values
		// (see UserRepositoryIntegrationTest for the bulk-query half of this pattern).
		entityManager.flush();
		entityManager.clear();
		User reloaded = userRepository.findByEmail(EMAIL).orElseThrow();
		assertThat(reloaded.isValidated()).isTrue();
		VerificationCode reloadedCode = verificationCodeRepository.findById(issued.getVerificationCodeId())
				.orElseThrow();
		assertThat(reloadedCode.getConsumedAt()).isNotNull();
	}

	@Test
	void verify_wrongCode_returns400WithInvalidErrorCode() throws Exception {
		code(RAW_CODE, 0, LocalDateTime.now().plusMinutes(15));

		mockMvc.perform(post(VERIFY_URL).contentType(MediaType.APPLICATION_JSON).content(verifyBody(EMAIL, "000000")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("verification_code_invalid"));
	}

	@Test
	void verify_expiredCode_returns410WithExpiredErrorCode() throws Exception {
		code(RAW_CODE, 0, LocalDateTime.now().minusMinutes(1));

		mockMvc.perform(post(VERIFY_URL).contentType(MediaType.APPLICATION_JSON).content(verifyBody(EMAIL, RAW_CODE)))
				.andExpect(status().isGone())
				.andExpect(jsonPath("$.errorCode").value("verification_code_expired"));
	}

	@Test
	void verify_burnedCode_returns400WithAttemptsExceededErrorCode() throws Exception {
		code(RAW_CODE, 5, LocalDateTime.now().plusMinutes(15));

		mockMvc.perform(post(VERIFY_URL).contentType(MediaType.APPLICATION_JSON).content(verifyBody(EMAIL, RAW_CODE)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("verification_code_attempts_exceeded"));
	}

	@Test
	void resend_overHourlyCap_returns429WithRateLimitedErrorCode() throws Exception {
		for (int i = 0; i < 5; i++) {
			code(RAW_CODE, 0, LocalDateTime.now().plusMinutes(15));
		}

		mockMvc.perform(post(RESEND_URL).contentType(MediaType.APPLICATION_JSON).content(resendBody(EMAIL)))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.errorCode").value("verification_code_rate_limited"));
	}

	@Test
	void resend_knownUnverifiedUnderCap_returns200() throws Exception {
		mockMvc.perform(post(RESEND_URL).contentType(MediaType.APPLICATION_JSON).content(resendBody(EMAIL)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200));
	}

	@Test
	void bothVerifyAndResendAreReachablePreAuth_noJwtRequired() throws Exception {
		// No Authorization header on either call — Security whitelist must permit both.
		mockMvc.perform(post(VERIFY_URL).contentType(MediaType.APPLICATION_JSON).content(verifyBody(EMAIL, "000000")))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(RESEND_URL).contentType(MediaType.APPLICATION_JSON).content(resendBody(EMAIL)))
				.andExpect(status().isOk());
	}

	@Test
	void register_issuesOneUnconsumedVerificationCodeForTheNewAccount() throws Exception {
		String newEmail = "register-issues-code-it@test.com";
		String body = "{\"email\":\"" + newEmail + "\",\"password\":\"Str0ng!Passw0rd\"}";

		mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());

		User created = userRepository.findByEmail(newEmail).orElseThrow();
		assertThat(created.isValidated()).isFalse();
		assertThat(verificationCodeRepository
				.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(created.getUserId())).isPresent();
	}

}
