package com.croman.singlevendorecommerce.web.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.users.UserService;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

/**
 * Unit coverage of {@link VerifiedAccountInterceptor#preHandle}: every branch
 * of the passthrough guard (no context, unauthenticated token, anonymous
 * token) plus the verified/unverified legs of the authoritative DB check,
 * independent of any HTTP route wiring.
 */
@ExtendWith(MockitoExtension.class)
class VerifiedAccountInterceptorTest {

	private static final String EMAIL = "shopper@test.com";

	@Mock
	private UserService userService;

	@Mock
	private MessageService messageService;

	@InjectMocks
	private VerifiedAccountInterceptor interceptor;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void preHandle_noSecurityContext_returnsTrueWithoutTouchingUserService() throws Exception {
		boolean result = interceptor.preHandle(null, null, null);

		assertThat(result).isTrue();
		verifyNoInteractions(userService);
	}

	@Test
	void preHandle_notAuthenticatedToken_returnsTrueWithoutTouchingUserService() throws Exception {
		Authentication authentication = mock(Authentication.class);
		when(authentication.isAuthenticated()).thenReturn(false);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		boolean result = interceptor.preHandle(null, null, null);

		assertThat(result).isTrue();
		verifyNoInteractions(userService);
	}

	@Test
	void preHandle_unauthenticatedPrincipal_returnsTrueWithoutTouchingUserService() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key", "anonymousUser",
				List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

		boolean result = interceptor.preHandle(null, null, null);

		assertThat(result).isTrue();
		verifyNoInteractions(userService);
	}

	@Test
	void preHandle_verifiedUser_returnsTrue() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null,
				List.of(new SimpleGrantedAuthority("ROLE_USER"))));
		User user = User.builder().email(EMAIL).isValidated(true).build();
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);

		boolean result = interceptor.preHandle(null, null, null);

		assertThat(result).isTrue();
	}

	@Test
	void preHandle_unverifiedUser_throws403WithAccountNotVerifiedErrorCode() {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null,
				List.of(new SimpleGrantedAuthority("ROLE_USER"))));
		User user = User.builder().email(EMAIL).isValidated(false).build();
		when(userService.getUserByEmail(EMAIL)).thenReturn(user);
		when(messageService.getMessage(eq("account_not_verified"), any())).thenReturn("Account is not verified yet");

		assertThatThrownBy(() -> interceptor.preHandle(null, null, null)).isInstanceOf(ApiServiceException.class)
				.satisfies(ex -> {
					ApiServiceException apiEx = (ApiServiceException) ex;
					assertThat(apiEx.getStatusCode()).isEqualTo(403);
					assertThat(apiEx.getMessage()).isEqualTo("Account is not verified yet");
					assertThat(apiEx.getMetadata()).containsEntry("errorCode", "account_not_verified");
				});
	}
}
