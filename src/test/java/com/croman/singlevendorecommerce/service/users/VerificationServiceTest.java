package com.croman.singlevendorecommerce.service.users;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.entity.users.VerificationCode;
import com.croman.singlevendorecommerce.repository.users.VerificationCodeRepository;
import com.croman.singlevendorecommerce.service.email.EmailSender;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.PasswordUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

/**
 * Covers {@code generateAndSend}, {@code resend}, and {@code verify}
 * (expiry / attempts / hash match / idempotent already-verified).
 */
@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

	private static final String EMAIL = "test@example.com";

	@Mock private VerificationCodeRepository verificationCodeRepository;
	@Mock private UserService userService;
	@Mock private EmailSender emailSender;
	@Mock private MessageService messageService;

	@InjectMocks private VerificationService verificationService;

	private User user(boolean validated) {
		return User.builder().userId(UUID.randomUUID()).email(EMAIL).isValidated(validated).build();
	}

	private VerificationCode codeFor(User user, String rawCode, int attempts, LocalDateTime expiresAt) {
		return VerificationCode.builder().user(user).codeHash(PasswordUtils.hashPassword(rawCode))
				.createdAt(LocalDateTime.now().minusMinutes(1)).expiresAt(expiresAt).attempts(attempts).build();
	}

	// --- generateAndSend: happy path (task 2.6) ---

	@Test
	void generateAndSend_success_issuesHashedCodeAndSendsEmail() {
		User user = user(false);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.countByUser_UserIdAndCreatedAtAfter(eq(user.getUserId()), any(LocalDateTime.class)))
				.thenReturn(0L);

		verificationService.generateAndSend(EMAIL);

		ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
		verify(verificationCodeRepository).save(captor.capture());
		VerificationCode saved = captor.getValue();
		assertEquals(0, saved.getAttempts());
		assertNotNull(saved.getCodeHash());
		assertTrue(saved.getExpiresAt().isAfter(saved.getCreatedAt().plusMinutes(14)));
		assertTrue(saved.getExpiresAt().isBefore(saved.getCreatedAt().plusMinutes(16)));

		ArgumentCaptor<String> rawCodeCaptor = ArgumentCaptor.forClass(String.class);
		verify(emailSender).sendVerificationCode(eq(EMAIL), rawCodeCaptor.capture());
		String rawCode = rawCodeCaptor.getValue();
		assertTrue(rawCode.matches("\\d{6}"), "code must be exactly 6 digits");
		assertTrue(PasswordUtils.matches(rawCode, saved.getCodeHash()), "stored hash must match the raw code sent");
	}

	// --- generateAndSend: hourly cap branch pair (task 2.7) ---

	@Test
	void generateAndSend_underHourlyCap_succeeds() {
		User user = user(false);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.countByUser_UserIdAndCreatedAtAfter(eq(user.getUserId()), any(LocalDateTime.class)))
				.thenReturn(4L);

		verificationService.generateAndSend(EMAIL);

		verify(verificationCodeRepository).save(any(VerificationCode.class));
		verify(emailSender).sendVerificationCode(eq(EMAIL), anyString());
	}

	@Test
	void generateAndSend_atHourlyCap_throwsRateLimited() {
		User user = user(false);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.countByUser_UserIdAndCreatedAtAfter(eq(user.getUserId()), any(LocalDateTime.class)))
				.thenReturn(5L);
		when(messageService.getMessage(eq("verification_code_rate_limited"), any())).thenReturn("Too many codes");

		ApiServiceException ex = assertThrows(ApiServiceException.class, () -> verificationService.generateAndSend(EMAIL));

		assertEquals(429, ex.getStatusCode());
		assertEquals("verification_code_rate_limited", ex.getMetadata().get("errorCode"));
		verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
		verify(emailSender, never()).sendVerificationCode(anyString(), anyString());
	}

	// --- generateAndSend: already-verified branch pair (task 2.8) ---

	@Test
	void generateAndSend_alreadyVerified_throwsConflict() {
		User user = user(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(messageService.getMessage(eq("account_already_verified"), any())).thenReturn("Already verified");

		ApiServiceException ex = assertThrows(ApiServiceException.class, () -> verificationService.generateAndSend(EMAIL));

		assertEquals(409, ex.getStatusCode());
		assertEquals("account_already_verified", ex.getMetadata().get("errorCode"));
		verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
	}

	@Test
	void generateAndSend_unverified_proceedsToIssueCode() {
		User user = user(false);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.countByUser_UserIdAndCreatedAtAfter(eq(user.getUserId()), any(LocalDateTime.class)))
				.thenReturn(0L);

		verificationService.generateAndSend(EMAIL);

		verify(verificationCodeRepository).save(any(VerificationCode.class));
	}

	// --- verify: TTL/expiry branch pair (task 2.9) ---

	@Test
	void verify_expiredCode_throwsExpired() {
		User user = user(false);
		VerificationCode expired = codeFor(user, "123456", 0, LocalDateTime.now().minusMinutes(1));
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId()))
				.thenReturn(Optional.of(expired));
		when(messageService.getMessage(eq("verification_code_expired"), any())).thenReturn("Expired");

		ApiServiceException ex = assertThrows(ApiServiceException.class, () -> verificationService.verify(EMAIL, "123456"));

		assertEquals(410, ex.getStatusCode());
		assertEquals("verification_code_expired", ex.getMetadata().get("errorCode"));
		verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
	}

	@Test
	void verify_notExpired_proceedsPastExpiryCheck() {
		User user = user(false);
		VerificationCode notExpired = codeFor(user, "123456", 0, LocalDateTime.now().plusMinutes(10));
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId()))
				.thenReturn(Optional.of(notExpired));

		verificationService.verify(EMAIL, "123456");

		verify(userService).markEmailVerified(EMAIL);
	}

	// --- verify: attempts >= 5 branch pair (task 2.10) ---

	@Test
	void verify_wrongCode_attemptsBelowMax_incrementsAndThrowsInvalid() {
		User user = user(false);
		VerificationCode code = codeFor(user, "111111", 4, LocalDateTime.now().plusMinutes(10));
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId()))
				.thenReturn(Optional.of(code));
		when(messageService.getMessage(eq("verification_code_invalid"), any())).thenReturn("Invalid");

		ApiServiceException ex = assertThrows(ApiServiceException.class, () -> verificationService.verify(EMAIL, "000000"));

		assertEquals(400, ex.getStatusCode());
		assertEquals("verification_code_invalid", ex.getMetadata().get("errorCode"));
		ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
		verify(verificationCodeRepository).save(captor.capture());
		assertEquals(5, captor.getValue().getAttempts(), "wrong attempt must increment the counter");
		verify(userService, never()).markEmailVerified(anyString());
	}

	@Test
	void verify_attemptsAlreadyExhausted_throwsAttemptsExceeded_evenWithCorrectCode() {
		User user = user(false);
		VerificationCode burned = codeFor(user, "654321", 5, LocalDateTime.now().plusMinutes(10));
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId()))
				.thenReturn(Optional.of(burned));
		when(messageService.getMessage(eq("verification_code_attempts_exceeded"), any())).thenReturn("Burned");

		ApiServiceException ex = assertThrows(ApiServiceException.class, () -> verificationService.verify(EMAIL, "654321"));

		assertEquals(400, ex.getStatusCode());
		assertEquals("verification_code_attempts_exceeded", ex.getMetadata().get("errorCode"));
		verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
		verify(userService, never()).markEmailVerified(anyString());
	}

	// --- verify: match / mismatch branch pair (task 2.11) ---

	@Test
	void verify_correctCode_consumesCodeAndMarksVerified() {
		User user = user(false);
		VerificationCode code = codeFor(user, "246810", 0, LocalDateTime.now().plusMinutes(10));
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId()))
				.thenReturn(Optional.of(code));

		verificationService.verify(EMAIL, "246810");

		ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
		verify(verificationCodeRepository).save(captor.capture());
		assertNotNull(captor.getValue().getConsumedAt(), "matching code must be marked consumed");
		verify(userService).markEmailVerified(EMAIL);
	}

	@Test
	void verify_wrongCode_doesNotConsumeOrMarkVerified() {
		User user = user(false);
		VerificationCode code = codeFor(user, "999999", 0, LocalDateTime.now().plusMinutes(10));
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId()))
				.thenReturn(Optional.of(code));
		when(messageService.getMessage(eq("verification_code_invalid"), any())).thenReturn("Invalid");

		assertThrows(ApiServiceException.class, () -> verificationService.verify(EMAIL, "000000"));

		ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
		verify(verificationCodeRepository).save(captor.capture());
		assertNull(captor.getValue().getConsumedAt(), "mismatched code must stay unconsumed");
		verify(userService, never()).markEmailVerified(anyString());
	}

	// --- verify: unknown email / no unconsumed row (task 2.12) ---

	@Test
	void verify_unknownEmail_throwsInvalid() {
		when(userService.existsByEmail(EMAIL)).thenReturn(false);
		when(messageService.getMessage(eq("verification_code_invalid"), any())).thenReturn("Invalid");

		ApiServiceException ex = assertThrows(ApiServiceException.class, () -> verificationService.verify(EMAIL, "123456"));

		assertEquals(400, ex.getStatusCode());
		assertEquals("verification_code_invalid", ex.getMetadata().get("errorCode"));
	}

	@Test
	void verify_noUnconsumedCode_throwsInvalid() {
		User user = user(false);
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId()))
				.thenReturn(Optional.empty());
		when(messageService.getMessage(eq("verification_code_invalid"), any())).thenReturn("Invalid");

		ApiServiceException ex = assertThrows(ApiServiceException.class, () -> verificationService.verify(EMAIL, "123456"));

		assertEquals(400, ex.getStatusCode());
	}

	// --- verify: already-verified idempotent success (task 2.8b) ---

	@Test
	void verify_alreadyVerifiedAccount_isIdempotentSuccess() {
		User user = user(true);
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);

		assertDoesNotThrow(() -> verificationService.verify(EMAIL, "anything"));

		verify(verificationCodeRepository, never())
				.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(any(UUID.class));
		verify(userService, never()).markEmailVerified(anyString());
	}

	// --- resend (task 2.13) ---

	@Test
	void resend_knownUnverifiedUnderCap_issuesNewCode() {
		User user = user(false);
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.countByUser_UserIdAndCreatedAtAfter(eq(user.getUserId()), any(LocalDateTime.class)))
				.thenReturn(0L);

		verificationService.resend(EMAIL);

		verify(verificationCodeRepository).save(any(VerificationCode.class));
		verify(emailSender).sendVerificationCode(eq(EMAIL), anyString());
	}

	@Test
	void resend_unknownEmail_isNoOpSuccess() {
		when(userService.existsByEmail(EMAIL)).thenReturn(false);

		assertDoesNotThrow(() -> verificationService.resend(EMAIL));

		verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
		verify(emailSender, never()).sendVerificationCode(anyString(), anyString());
		verify(userService, never()).getUserByEmail(anyString());
	}

	@Test
	void resend_overHourlyCap_throwsRateLimited() {
		User user = user(false);
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(verificationCodeRepository.countByUser_UserIdAndCreatedAtAfter(eq(user.getUserId()), any(LocalDateTime.class)))
				.thenReturn(5L);
		when(messageService.getMessage(eq("verification_code_rate_limited"), any())).thenReturn("Too many codes");

		ApiServiceException ex = assertThrows(ApiServiceException.class, () -> verificationService.resend(EMAIL));

		assertEquals(429, ex.getStatusCode());
		verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
	}

	@Test
	void resend_alreadyVerifiedAccount_throwsConflict() {
		User user = user(true);
		when(userService.existsByEmail(EMAIL)).thenReturn(true);
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(messageService.getMessage(eq("account_already_verified"), any())).thenReturn("Already verified");

		ApiServiceException ex = assertThrows(ApiServiceException.class, () -> verificationService.resend(EMAIL));

		assertEquals(409, ex.getStatusCode());
		verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
	}
}
