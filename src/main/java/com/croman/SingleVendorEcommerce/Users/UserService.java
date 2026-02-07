package com.croman.SingleVendorEcommerce.Users;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.EnvironmentUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Users.DTO.CreateUserDTO;
import com.croman.SingleVendorEcommerce.Users.Entity.User;
import com.croman.SingleVendorEcommerce.Users.Repository.UserRepository;
import com.croman.SingleVendorEcommerce.Users.Utils.PasswordUtils;
import com.croman.SingleVendorEcommerce.Users.Utils.LocaleUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
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
			
			User newUser = create(dto);
			
			userRepository.save(newUser);

		} catch (ApiServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					e.getMessage());
		}
	}

	private User create(CreateUserDTO dto) {
		return User.builder().email(dto.getEmail()).password(PasswordUtils.hashPassword(dto.getPassword()))
				.username(dto.getEmail()).updatedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).isActive(true)
				.isValidated(true).build();
	}

	private boolean existsByEmail(String email) {
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
}
