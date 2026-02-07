package com.croman.SingleVendorEcommerce.Roles;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Roles.Repository.UserRoleRepository;

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
