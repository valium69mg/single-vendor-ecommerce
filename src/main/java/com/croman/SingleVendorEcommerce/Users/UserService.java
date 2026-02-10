package com.croman.SingleVendorEcommerce.Users;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.DateTimeUtils;
import com.croman.SingleVendorEcommerce.General.EnvironmentUtils;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Roles.RoleType;
import com.croman.SingleVendorEcommerce.Roles.RolesService;
import com.croman.SingleVendorEcommerce.Users.DTO.CreateUserDTO;
import com.croman.SingleVendorEcommerce.Users.DTO.UserDTO;
import com.croman.SingleVendorEcommerce.Users.Entity.User;
import com.croman.SingleVendorEcommerce.Users.Repository.UserRepository;
import com.croman.SingleVendorEcommerce.Users.Utils.PasswordUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final RolesService rolesService;
	private final MessageService messageService;
	private final EnvironmentUtils environmentUtils;

	@Transactional
	public void register(CreateUserDTO dto) {
		try {

			boolean emailExists = existsByEmail(dto.getEmail());
			
			if (emailExists) {
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("email_exists", LocaleUtils.getDefaultLocale()));
			}
			
			User newUser = create(dto, RoleType.USER);
			
			userRepository.save(newUser);

		} catch (ApiServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					e.getMessage());
		}
	}

	private User create(CreateUserDTO dto, RoleType roleType) {
		roleType = roleType != null ? roleType : RoleType.USER;
		return User.builder().email(dto.getEmail()).password(PasswordUtils.hashPassword(dto.getPassword()))
				.username(dto.getEmail()).updatedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).isActive(true)
				.isValidated(true).userRole(rolesService.getUserRoleByRoleType(roleType)).build();
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}
	
	@Transactional
	public void deleteUserByEmail(String email) {
		try {
			
			boolean isDev = environmentUtils.isDev();
		
			if (!isDev) {
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("not_in_dev_env", LocaleUtils.getDefaultLocale()));
			}
		
			boolean emailExists = existsByEmail(email);

			if (!emailExists) {
				throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage("email_does_not_exists", LocaleUtils.getDefaultLocale()));
			}
			
			userRepository.deleteByEmail(email);
			
		} catch (ApiServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					e.getMessage());
		}
	}
	
	public UserDTO getUserDTOByEmail(String email) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("invalid_credentials", LocaleUtils.getDefaultLocale()));
		}
		return mapUserToUserDTO(userOpt.get());
	}
	
	private UserDTO mapUserToUserDTO(User user) {
		return UserDTO.builder().userId(user.getUserId().toString()).username(user.getUsername()).lastLogin(
				user.getLastLogin() != null ? user.getLastLogin().format(DateTimeUtils.getDateTimeFormatter()) : null)
				.createdAt(user.getCreatedAt().format(DateTimeUtils.getDateTimeFormatter()))
				.updatedAt(user.getUpdatedAt().format(DateTimeUtils.getDateTimeFormatter()))
				.isValidated(user.isValidated()).isActive(user.isActive()).build();
	}
	
	public boolean passwordCorrect(String email, String rawPassword) {
		String hashedPassword = getHashedPasswordByEmail(email);
		return PasswordUtils.matches(rawPassword, hashedPassword);
	}
	
	private String getHashedPasswordByEmail(String email) {
		Optional<String> hashedPasswordOpt = userRepository.getHashedPasswordByEmail(email);
		if (hashedPasswordOpt.isPresent()) {
			return hashedPasswordOpt.get();
		}
		throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
				messageService.getMessage("invalid_credentials", LocaleUtils.getDefaultLocale()));
	}
	
	@Transactional
	public void updateLastLogin(String email) {
		LocalDateTime now = LocalDateTime.now();
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("invalid_credentials", LocaleUtils.getDefaultLocale()));
		}
		userRepository.updateLastLogin(email, now);
	}

	@Transactional
	public void createSiteAdmin(CreateUserDTO dto) {
		try {
			
			if (adminPresent()) {
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("admin_exists", LocaleUtils.getDefaultLocale()));
			}
			
			boolean emailExists = existsByEmail(dto.getEmail());

			if (emailExists) {
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("email_exists", LocaleUtils.getDefaultLocale()));
			}

			User newUser = create(dto, RoleType.ADMIN);
			
			userRepository.save(newUser);

		} catch (ApiServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

	}
	
	private boolean adminPresent() {
		return !userRepository.findAllByUserRole_RoleType(RoleType.ADMIN).isEmpty();
	}
	
	public User getUserByEmail(String email) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isPresent()) {
			return userOpt.get();
		}
		throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
				messageService.getMessage("email_does_not_exists", LocaleUtils.getDefaultLocale()));
	}
	
}
