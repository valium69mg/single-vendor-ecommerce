package com.croman.singlevendorecommerce.service.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Default {@link EmailSender} adapter: logs the code instead of sending a
 * real email. Active whenever {@code email.provider} is unset or set to
 * {@code logging} — a real transactional-email provider is out of scope.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "email.provider", havingValue = "logging", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

	@Override
	public void sendVerificationCode(String email, String code) {
		log.info("[EMAIL:verification] to={} code={} (logging provider - not sent)", email, code);
	}

}
