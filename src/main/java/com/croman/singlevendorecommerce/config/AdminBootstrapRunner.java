package com.croman.singlevendorecommerce.config;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.croman.singlevendorecommerce.dto.users.CreateUserDTO;
import com.croman.singlevendorecommerce.service.users.UserService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the single site ADMIN account at startup when none exists yet, using
 * {@code APP_ADMIN_EMAIL} / {@code APP_ADMIN_PASSWORD} from the environment.
 *
 * <p>Replaces the former public {@code POST /api/v1/users/register/admin}
 * endpoint: bootstrapping the superuser is a deploy-time concern, not an
 * unauthenticated HTTP route. Idempotent — once an ADMIN row exists this is a
 * no-op on every subsequent boot.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

	private final UserService userService;
	private final Validator validator;

	@Value("${app.admin.email:}")
	private String adminEmail;

	@Value("${app.admin.password:}")
	private String adminPassword;

	@Override
	public void run(ApplicationArguments args) {
		if (userService.adminPresent()) {
			log.info("Admin bootstrap: an ADMIN account already exists, skipping seed");
			return;
		}

		if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
			log.warn("Admin bootstrap: no ADMIN account found and APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD "
					+ "are not both set — no admin was created");
			return;
		}

		CreateUserDTO dto = new CreateUserDTO(adminEmail.trim(), adminPassword);

		Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
		if (!violations.isEmpty()) {
			String messages = violations.stream()
					.map(v -> v.getPropertyPath() + " " + v.getMessage())
					.collect(Collectors.joining("; "));
			throw new IllegalStateException(
					"Admin bootstrap: APP_ADMIN_* credentials do not meet the account policy: " + messages);
		}

		userService.createSiteAdmin(dto);
		log.info("Admin bootstrap: seeded ADMIN account for {}", dto.getEmail());
	}
}
