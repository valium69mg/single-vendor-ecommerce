package com.croman.SingleVendorEcommerce.Users;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Users.DTO.CreateUserDTO;
import com.croman.SingleVendorEcommerce.Users.Entity.User;
import com.croman.SingleVendorEcommerce.Users.Repository.UserRepository;
import com.croman.SingleVendorEcommerce.Users.Utils.PasswordUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final MessageService messageService;

	@Transactional
	public void register(CreateUserDTO dto) {
		try {

			if (existsByEmail(dto.getEmail())) {
				throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage("email_exists", Locale.of("es")));
			}
			
			User newUser = create(dto);
			
			userRepository.save(newUser);

		} catch (ApiServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					messageService.getMessage("email_exists", Locale.of("es")));
		}
	}

	private User create(CreateUserDTO dto) {
		return User.builder().email(dto.getEmail()).password(PasswordUtils.hashPassword(dto.getPassword()))
				.updatedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).isActive(true).isValidated(true).build();
	}

	private boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}
}
