package com.croman.singlevendorecommerce.roles;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.roles.dto.RoleType;
import com.croman.singlevendorecommerce.roles.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolesService {

	private final UserRoleRepository userRoleRepository;
	private final MessageService messageService;
	
	public UserRole getUserRoleByRoleType(RoleType roleType) {
		Optional<UserRole> userRoleOpt = userRoleRepository.findByRoleType(roleType);
		if (userRoleOpt.isPresent()) {
			return userRoleOpt.get();
		}
		throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
				messageService.getMessage("user_role_not_found", LocaleUtils.getDefaultLocale()));
	}
	
}
