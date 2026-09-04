package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.roles.UserRole;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.utils.PasswordUtils;

import jakarta.persistence.EntityManager;

/**
 * Covers the {@code @Modifying} {@link UserRepository#updateIsValidated} query
 * against a real PostgreSQL Testcontainer. The persistence context is cleared
 * after the bulk update so the re-read reflects the committed column value
 * rather than the stale first-level cache entry.
 */
@Transactional
class UserRepositoryIntegrationTest extends AbstractIntegrationTest {

	private static final String EMAIL = "user-repo-it@test.com";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private EntityManager entityManager;

	@BeforeEach
	void seedUnverifiedUser() {
		UserRole role = userRoleRepository.findByRoleType(RoleType.USER).orElseThrow();
		userRepository.save(User.builder()
				.email(EMAIL)
				.username(EMAIL)
				.password(PasswordUtils.hashPassword("correct-horse-battery"))
				.isActive(true)
				.isValidated(false)
				.userRole(role)
				.build());
	}

	@Test
	void updateIsValidated_flipsTheColumnForTheMatchingEmail() {
		int rows = userRepository.updateIsValidated(EMAIL, true);
		entityManager.clear();

		assertThat(rows).isEqualTo(1);
		assertThat(userRepository.findByEmail(EMAIL).orElseThrow().isValidated()).isTrue();
	}

	@Test
	void updateIsValidated_unknownEmailUpdatesNoRows() {
		int rows = userRepository.updateIsValidated("nobody-repo-it@test.com", true);
		entityManager.clear();

		assertThat(rows).isZero();
		assertThat(userRepository.findByEmail(EMAIL).orElseThrow().isValidated()).isFalse();
	}
}
