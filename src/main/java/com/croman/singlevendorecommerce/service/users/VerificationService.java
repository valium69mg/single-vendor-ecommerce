package com.croman.singlevendorecommerce.service.users;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.entity.users.VerificationCode;
import com.croman.singlevendorecommerce.repository.users.VerificationCodeRepository;
import com.croman.singlevendorecommerce.service.email.EmailSender;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.PasswordUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

/**
 * Owns the email-verification-code lifecycle: generation (rate-limited),
 * delivery via {@link EmailSender}, verification (expiry / attempts /
 * hash match), and resend (anti-enumeration). Mirrors the
 * {@code AuthService} login-attempt rate-limit shape.
 */
@Service
@RequiredArgsConstructor
public class VerificationService {

	private static final int MAX_CODES_PER_HOUR = 5;
	private static final int RATE_WINDOW_HOURS = 1;
	private static final int CODE_TTL_MINUTES = 15;
	private static final int MAX_VERIFY_ATTEMPTS = 5;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final VerificationCodeRepository verificationCodeRepository;
	private final UserService userService;
	private final EmailSender emailSender;
	private final MessageService messageService;

	@Transactional
	public void generateAndSend(String email) {
		User user = userService.getUserByEmail(email);
		issueForUnverifiedUser(user);
	}

	@Transactional
	public void resend(String email) {
		if (!userService.existsByEmail(email)) {
			// Anti-enumeration: unknown email is a silent no-op success.
			return;
		}
		User user = userService.getUserByEmail(email);
		issueForUnverifiedUser(user);
	}

	@Transactional
	public void verify(String email, String code) {
		if (!userService.existsByEmail(email)) {
			throw invalidCodeException();
		}

		User user = userService.getUserByEmail(email);

		if (user.isValidated()) {
			// Already-verified accounts are idempotent: success, no code needed.
			return;
		}

		VerificationCode verificationCode = verificationCodeRepository
				.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId())
				.orElseThrow(this::invalidCodeException);

		if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new ApiServiceException(HttpStatus.GONE.value(),
					messageService.getMessage("verification_code_expired", LocaleUtils.getDefaultLocale()),
					Map.of("errorCode", "verification_code_expired"));
		}

		if (verificationCode.getAttempts() >= MAX_VERIFY_ATTEMPTS) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("verification_code_attempts_exceeded", LocaleUtils.getDefaultLocale()),
					Map.of("errorCode", "verification_code_attempts_exceeded"));
		}

		if (!PasswordUtils.matches(code, verificationCode.getCodeHash())) {
			verificationCode.setAttempts(verificationCode.getAttempts() + 1);
			verificationCodeRepository.save(verificationCode);
			throw invalidCodeException();
		}

		verificationCode.setConsumedAt(LocalDateTime.now());
		verificationCodeRepository.save(verificationCode);
		userService.markEmailVerified(email);
	}

	private void issueForUnverifiedUser(User user) {
		if (user.isValidated()) {
			throw new ApiServiceException(HttpStatus.CONFLICT.value(),
					messageService.getMessage("account_already_verified", LocaleUtils.getDefaultLocale()),
					Map.of("errorCode", "account_already_verified"));
		}
		issueCode(user);
	}

	private void issueCode(User user) {
		LocalDateTime cutoff = LocalDateTime.now().minusHours(RATE_WINDOW_HOURS);
		long recentCount = verificationCodeRepository.countByUser_UserIdAndCreatedAtAfter(user.getUserId(), cutoff);
		if (recentCount >= MAX_CODES_PER_HOUR) {
			throw new ApiServiceException(HttpStatus.TOO_MANY_REQUESTS.value(),
					messageService.getMessage("verification_code_rate_limited", LocaleUtils.getDefaultLocale()),
					Map.of("errorCode", "verification_code_rate_limited"));
		}

		LocalDateTime now = LocalDateTime.now();
		String rawCode = generateCode();

		VerificationCode verificationCode = VerificationCode.builder().user(user)
				.codeHash(PasswordUtils.hashPassword(rawCode)).createdAt(now).expiresAt(now.plusMinutes(CODE_TTL_MINUTES))
				.attempts(0).build();

		verificationCodeRepository.save(verificationCode);
		emailSender.sendVerificationCode(user.getEmail(), rawCode);
	}

	private String generateCode() {
		return String.format("%06d", RANDOM.nextInt(1_000_000));
	}

	private ApiServiceException invalidCodeException() {
		return new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
				messageService.getMessage("verification_code_invalid", LocaleUtils.getDefaultLocale()),
				Map.of("errorCode", "verification_code_invalid"));
	}

}
