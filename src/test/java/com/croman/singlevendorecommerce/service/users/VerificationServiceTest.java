package com.croman.singlevendorecommerce.service.users;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
 * Covers {@code generateAndSend} and {@code resend} only. Verification
 * ({@code verify}) branch coverage is added in a later slice on top of
 * this class.
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
