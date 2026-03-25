package com.croman.singlevendorecommerce.service.roles;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.roles.UserRole;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

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
