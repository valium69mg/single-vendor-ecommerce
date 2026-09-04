package com.croman.singlevendorecommerce.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.entity.users.VerificationCode;
import com.croman.singlevendorecommerce.integration.support.AuthFixtures;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.repository.users.VerificationCodeRepository;

/**
 * Proves the V29 {@code verification_codes} migration applies on a real
 * PostgreSQL Testcontainer and that {@link VerificationCodeRepository}'s two
 * derived queries behave: the rolling generation {@code COUNT} window and the
 * "latest unconsumed code for an account" lookup.
 *
 * <p>Class-level {@link Transactional}: each test runs in a rolled-back
 * transaction so seeded rows never leak into sibling integration tests sharing
 * the singleton container.
 */
@Transactional
class VerificationCodeRepositoryIntegrationTest extends AbstractIntegrationTest {

	private static final String EMAIL = "verif-repo-it@test.com";
	private static final String OTHER_EMAIL = "verif-repo-other@test.com";
	private static final String PASSWORD = "correct-horse-battery";

	@Autowired
	private VerificationCodeRepository verificationCodeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	private User user;

	@BeforeEach
	void seedUser() {
		user = AuthFixtures.seedUser(userRepository, userRoleRepository, EMAIL, PASSWORD, RoleType.USER);
	}

	private VerificationCode code(User owner, LocalDateTime createdAt, LocalDateTime consumedAt) {
		return VerificationCode.builder()
				.user(owner)
				.codeHash("$2a$10$hash")
				.createdAt(createdAt)
				.expiresAt(createdAt.plusMinutes(15))
				.consumedAt(consumedAt)
				.attempts(0)
				.build();
	}

	@Test
	void countByUser_UserIdAndCreatedAtAfter_countsOnlyRowsInsideTheWindow() {
		LocalDateTime now = LocalDateTime.now();
		verificationCodeRepository.save(code(user, now.minusMinutes(30), null));
		verificationCodeRepository.save(code(user, now.minusMinutes(45), null));
		verificationCodeRepository.save(code(user, now.minusMinutes(90), null));

		long within = verificationCodeRepository
				.countByUser_UserIdAndCreatedAtAfter(user.getUserId(), now.minusHours(1));

		assertThat(within).isEqualTo(2);
	}

	@Test
	void countByUser_UserIdAndCreatedAtAfter_isScopedToTheAccount() {
		User other = AuthFixtures.seedUser(userRepository, userRoleRepository, OTHER_EMAIL, PASSWORD, RoleType.USER);
		LocalDateTime now = LocalDateTime.now();
		verificationCodeRepository.save(code(user, now.minusMinutes(10), null));
		verificationCodeRepository.save(code(other, now.minusMinutes(10), null));
		verificationCodeRepository.save(code(other, now.minusMinutes(20), null));

		long forUser = verificationCodeRepository
				.countByUser_UserIdAndCreatedAtAfter(user.getUserId(), now.minusHours(1));
		long forOther = verificationCodeRepository
				.countByUser_UserIdAndCreatedAtAfter(other.getUserId(), now.minusHours(1));

		assertThat(forUser).isEqualTo(1);
		assertThat(forOther).isEqualTo(2);
	}

	@Test
	void findFirstUnconsumed_returnsNewestUnconsumedRow() {
		LocalDateTime now = LocalDateTime.now();
		verificationCodeRepository.save(code(user, now.minusMinutes(20), null));
		VerificationCode newest = verificationCodeRepository.save(code(user, now.minusMinutes(2), null));

		Optional<VerificationCode> found = verificationCodeRepository
				.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId());

		assertThat(found).isPresent();
		assertThat(found.get().getVerificationCodeId()).isEqualTo(newest.getVerificationCodeId());
	}

	@Test
	void findFirstUnconsumed_skipsConsumedRowsAndReturnsEmptyWhenAllConsumed() {
		LocalDateTime now = LocalDateTime.now();
		verificationCodeRepository.save(code(user, now.minusMinutes(5), now.minusMinutes(1)));
		verificationCodeRepository.save(code(user, now.minusMinutes(10), now.minusMinutes(9)));

		Optional<VerificationCode> found = verificationCodeRepository
				.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getUserId());

		assertThat(found).isEmpty();
	}

	@Test
	void findFirstUnconsumed_returnsEmptyForAccountWithNoCodes() {
		Optional<VerificationCode> found = verificationCodeRepository
				.findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(UUID.randomUUID());

		assertThat(found).isEmpty();
	}
}
