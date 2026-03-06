package com.croman.singlevendorecommerce.Roles;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.singlevendorecommerce.Exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.General.LocaleUtils;
import com.croman.singlevendorecommerce.Message.MessageService;
import com.croman.singlevendorecommerce.Roles.DTO.RoleType;
import com.croman.singlevendorecommerce.Roles.Repository.UserRoleRepository;

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
