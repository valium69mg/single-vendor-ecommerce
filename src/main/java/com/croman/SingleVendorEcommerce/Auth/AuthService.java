package com.croman.SingleVendorEcommerce.Auth;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.SingleVendorEcommerce.Auth.DTO.LoginDTO;
import com.croman.SingleVendorEcommerce.Auth.DTO.LoginResponseDTO;
import com.croman.SingleVendorEcommerce.Auth.Entity.LoginAttempt;
import com.croman.SingleVendorEcommerce.Auth.Repository.LoginAttemptRepository;
import com.croman.SingleVendorEcommerce.DTO.LoginContextDTO;
import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Jwt.JwtUtil;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Users.UserService;
import com.croman.SingleVendorEcommerce.Users.DTO.UserDTO;
import com.croman.SingleVendorEcommerce.Users.Entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtUtil jwtUtil;
	private final MessageService messageService;
	private final UserService userService;
	private final LoginAttemptRepository loginAttemptRepository;
	
	public LoginResponseDTO login(LoginContextDTO loginContextDTO) {
		try {

			LoginDTO loginDTO = loginContextDTO.getLoginDTO();
			
			String email = loginDTO.getEmail();

			boolean existsByEmail = userService.existsByEmail(email);

			if (!existsByEmail) {
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("invalid_credentials", LocaleUtils.getDefaultLocale()));
			}

			String rawPassword = loginDTO.getPassword();

			boolean passwordMatches = userService.passwordCorrect(email, rawPassword);

			if (!passwordMatches) {
				registerLoginAttempt(loginContextDTO, false);
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("invalid_credentials", LocaleUtils.getDefaultLocale()));
			}
			
			userService.updateLastLogin(email);

			String token = jwtUtil.generateToken(email);

			UserDTO userDTO = userService.getUserDTOByEmail(email);
			
			registerLoginAttempt(loginContextDTO, true);
			
			return LoginResponseDTO.builder().email(email).userId(userDTO.getUserId()).token(token).build();

		} catch (ApiServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}
	}
	
	@Transactional
	private void registerLoginAttempt(LoginContextDTO loginContextDTO, boolean successfull) {
		String email = loginContextDTO.getLoginDTO().getEmail();
		String ipAddress = loginContextDTO.getIp();
		User user = userService.getUserByEmail(email);
		LoginAttempt loginAttempt = LoginAttempt.builder().user(user).email(email).ipAddress(ipAddress)
				.successful(successfull).attemptedAt(LocalDateTime.now()).build();
		loginAttemptRepository.save(loginAttempt);
	}
	
	
}
