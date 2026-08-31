package com.croman.singlevendorecommerce.service.users;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private MessageService messageService;

	@InjectMocks
	private CurrentUserService currentUserService;

	// ─── Fixtures ────────────────────────────────────────────────────────────

	private static final String EMAIL = "shopper@example.com";
	private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private User user;

	@BeforeEach
	void setUp() {
		user = User.builder().userId(USER_ID).email(EMAIL).username("shopper").build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private void authenticateAs(String principal) {
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	// ─── getCurrentUser ──────────────────────────────────────────────────────

	@Test
	void testGetCurrentUserReturnsUserForAuthenticatedEmail() {
		authenticateAs(EMAIL);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

		User result = currentUserService.getCurrentUser();

		assertThat(result.getUserId()).isEqualTo(USER_ID);
		assertThat(result.getEmail()).isEqualTo(EMAIL);
	}

	@Test
	void testGetCurrentUserThrowsWhenNoAuthentication() {
		SecurityContextHolder.clearContext();
		lenient().when(messageService.getMessage(eq("auth_required"), any(Locale.class)))
				.thenReturn("Authentication required");

		assertThatThrownBy(() -> currentUserService.getCurrentUser())
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Authentication required")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(401));
	}

	@Test
	void testGetCurrentUserThrowsWhenAuthenticationIsAnonymous() {
		AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
				List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
		SecurityContextHolder.getContext().setAuthentication(anonymous);
		lenient().when(messageService.getMessage(eq("auth_required"), any(Locale.class)))
				.thenReturn("Authentication required");

		assertThatThrownBy(() -> currentUserService.getCurrentUser())
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Authentication required");
	}

	@Test
	void testGetCurrentUserThrowsWhenEmailNotFound() {
		authenticateAs(EMAIL);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(messageService.getMessage(eq("cart_user_not_found"), any(Locale.class)))
				.thenReturn("Authenticated user not found");

		assertThatThrownBy(() -> currentUserService.getCurrentUser())
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Authenticated user not found")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(401));
	}
}
