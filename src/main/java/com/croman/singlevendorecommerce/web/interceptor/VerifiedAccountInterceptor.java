package com.croman.singlevendorecommerce.web.interceptor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.users.UserService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Gates opted-in routes (registered by {@code InterceptorConfig}) behind
 * {@code users.is_validated}. An unauthenticated caller is let through — Spring
 * Security's filter chain already rejects unauthenticated requests to any
 * non-whitelisted route before {@link #preHandle} ever runs on those paths;
 * this leg only guards against a future opted-in route that is reachable
 * pre-auth. An authenticated caller is resolved against the authoritative DB
 * row (not the JWT claims) so a mid-session verification takes effect
 * immediately without forcing re-login.
 */
@Component
@RequiredArgsConstructor
public class VerifiedAccountInterceptor implements HandlerInterceptor {

	private final UserService userService;
	private final MessageService messageService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return true;
		}

		User user = userService.getUserByEmail(authentication.getName());

		if (!user.isValidated()) {
			throw new ApiServiceException(HttpStatus.FORBIDDEN.value(),
					messageService.getMessage("account_not_verified", LocaleUtils.getDefaultLocale()),
					Map.of("errorCode", "account_not_verified"));
		}

		return true;
	}
}
