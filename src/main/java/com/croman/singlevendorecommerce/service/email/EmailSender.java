package com.croman.singlevendorecommerce.service.email;

/**
 * Port for sending a 6-digit email-verification code to an account. Mirrors
 * {@code StorageService}: the active adapter is selected by configuration
 * ({@code email.provider}); a real transactional-email provider is out of
 * scope for this change, only the seam and a logging fallback exist.
 */
public interface EmailSender {

	void sendVerificationCode(String email, String code);

}
