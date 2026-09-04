package com.croman.singlevendorecommerce.service.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class LoggingEmailSenderTest {

	private final LoggingEmailSender sender = new LoggingEmailSender();
	private Logger logger;
	private ListAppender<ILoggingEvent> listAppender;

	@BeforeEach
	void setUp() {
		logger = (Logger) LoggerFactory.getLogger(LoggingEmailSender.class);
		listAppender = new ListAppender<>();
		listAppender.start();
		logger.addAppender(listAppender);
	}

	@AfterEach
	void tearDown() {
		logger.detachAppender(listAppender);
	}

	@Test
	void sendVerificationCode_emitsExactlyOneLogLine_andDoesNotThrow() {
		assertDoesNotThrow(() -> sender.sendVerificationCode("test@example.com", "123456"));

		List<ILoggingEvent> events = listAppender.list;
		assertEquals(1, events.size(), "logging adapter must emit exactly one log line per code issued");

		String message = events.get(0).getFormattedMessage();
		assertTrue(message.contains("test@example.com"), "log line must contain the recipient email");
		assertTrue(message.contains("123456"), "log line must contain the 6-digit code");
	}
}
