package com.croman.singlevendorecommerce.integration.support;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.roles.UserRole;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.utils.PasswordUtils;

/**
 * Stateless construction helper for integration-test user fixtures.
 *
 * <p>Each integration test class still declares which users it depends on by
 * calling {@link #seedUser} from its own {@code @BeforeEach}; this helper only
 * removes the copy-pasted {@code User.builder()} boilerplate and the length-50
 * {@code username} trap.
 *
 * <p>Passwords are hashed through {@link PasswordUtils#hashPassword} — the exact
 * production hash path that {@code UserService.passwordCorrect} verifies against
 * — so no test-only {@code PasswordEncoder} bean is introduced.
 */
public final class AuthFixtures {

	private AuthFixtures() {
	}

	/**
	 * Persist an active, validated user with the given role.
	 *
	 * @param userRepo    the real {@link UserRepository}
	 * @param roleRepo    the real {@link UserRoleRepository} (roles are Flyway-seeded by V6)
	 * @param email       the login email; also used as {@code username}, so it MUST be &lt;= 50 chars
	 * @param rawPassword the plaintext password, hashed via {@link PasswordUtils#hashPassword}
	 * @param roleType    {@link RoleType#ADMIN} or {@link RoleType#USER}
	 * @return the persisted {@link User}
	 */
	public static User seedUser(UserRepository userRepo, UserRoleRepository roleRepo, String email, String rawPassword,
			RoleType roleType) {
		if (email == null || email.length() > 50) {
			throw new IllegalArgumentException("Fixture email must be <= 50 chars (username column length): " + email);
		}
		UserRole role = roleRepo.findByRoleType(roleType)
				.orElseThrow(() -> new IllegalStateException("Flyway V6 role seed missing role: " + roleType));
		User user = User.builder()
				.email(email)
				.username(email)
				.password(PasswordUtils.hashPassword(rawPassword))
				.isActive(true)
				.isValidated(true)
				.userRole(role)
				.build();
		return userRepo.save(user);
	}
}
