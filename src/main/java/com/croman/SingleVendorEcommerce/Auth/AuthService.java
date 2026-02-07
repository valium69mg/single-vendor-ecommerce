package com.croman.SingleVendorEcommerce.Auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.Auth.DTO.LoginDTO;
import com.croman.SingleVendorEcommerce.Auth.DTO.LoginResponseDTO;
import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Jwt.JwtUtil;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Users.UserService;
import com.croman.SingleVendorEcommerce.Users.DTO.UserDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtUtil jwtUtil;
	private final MessageService messageService;
	private final UserService userService;
	
	public LoginResponseDTO login(LoginDTO loginDTO) {
		try {

			String email = loginDTO.getEmail();

			boolean existsByEmail = userService.existsByEmail(email);

			if (!existsByEmail) {
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("invalid_credentials", LocaleUtils.getDefaultLocale()));
			}

			String rawPassword = loginDTO.getPassword();

			boolean passwordMatches = userService.passwordCorrect(email, rawPassword);

			if (!passwordMatches) {
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("invalid_credentials", LocaleUtils.getDefaultLocale()));
			}
			
			userService.updateLastLogin(email);

			String token = jwtUtil.generateToken(email);

			UserDTO userDTO = userService.getUserDTOByEmail(email);
			
			return LoginResponseDTO.builder().email(email).userId(userDTO.getUserId()).token(token).build();

		} catch (ApiServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}
	}
	
}
