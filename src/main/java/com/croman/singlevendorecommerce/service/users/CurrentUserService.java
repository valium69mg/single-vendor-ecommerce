package com.croman.singlevendorecommerce.service.users;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

	private final UserRepository userRepository;
	private final MessageService messageService;

	@Transactional(readOnly = true)
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			throw new ApiServiceException(HttpStatus.UNAUTHORIZED.value(),
					messageService.getMessage("auth_required", LocaleUtils.getDefaultLocale()));
		}

		return userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new ApiServiceException(HttpStatus.UNAUTHORIZED.value(),
						messageService.getMessage("cart_user_not_found", LocaleUtils.getDefaultLocale())));
	}
}
