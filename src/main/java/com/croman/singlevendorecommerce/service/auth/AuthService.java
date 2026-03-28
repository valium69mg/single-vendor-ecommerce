package com.croman.singlevendorecommerce.service.auth;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.singlevendorecommerce.dto.auth.LoginContextDTO;
import com.croman.singlevendorecommerce.dto.auth.LoginDTO;
import com.croman.singlevendorecommerce.dto.auth.LoginResponseDTO;
import com.croman.singlevendorecommerce.dto.users.UserDTO;
import com.croman.singlevendorecommerce.entity.auth.LoginAttempt;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.repository.auth.LoginAttemptRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.users.UserService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.utils.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtUtil jwtUtil;
	private final MessageService messageService;
	private final UserService userService;
	private final LoginAttemptRepository loginAttemptRepository;
	private static final Integer MAX_LOGGIN_ATTEMPTS = 5;
	private static final Integer LOCKOUT_WINDOW_HOURS = 1;
	
	public LoginResponseDTO login(LoginContextDTO loginContextDTO) {
		try {

			LoginDTO loginDTO = loginContextDTO.getLoginDTO();
			
			String email = loginDTO.getEmail();

			boolean existsByEmail = userService.existsByEmail(email);

			if (!existsByEmail) {
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("invalid_credentials", LocaleUtils.getDefaultLocale()));
			}
					
			validateTooManyFailedAttempts(email);

			String rawPassword = loginDTO.getPassword();

			boolean passwordMatches = userService.passwordCorrect(email, rawPassword);

			if (!passwordMatches) {
				registerLoginAttempt(loginContextDTO, false);
				throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
						messageService.getMessage("invalid_credentials", LocaleUtils.getDefaultLocale()));
			}
			
			userService.updateLastLogin(email);

			String role = userService.getUserRoleNameByEmail(email);
			
			String token = jwtUtil.generateToken(email, role);

			UserDTO userDTO = userService.getUserDTOByEmail(email);
			
			registerLoginAttempt(loginContextDTO, true);
			
			return LoginResponseDTO.builder().email(email).userId(userDTO.getUserId()).token(token)
					.name(email).role(role).build();

		} catch (ApiServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}
	}
	
	private void registerLoginAttempt(LoginContextDTO loginContextDTO, boolean successfull) {
		String email = loginContextDTO.getLoginDTO().getEmail();
		String ipAddress = loginContextDTO.getIp();
		User user = userService.getUserByEmail(email);
		LoginAttempt loginAttempt = LoginAttempt.builder().user(user).email(email).ipAddress(ipAddress)
				.successful(successfull).attemptedAt(LocalDateTime.now()).build();
		loginAttemptRepository.save(loginAttempt);
	}
	
	private void validateTooManyFailedAttempts(String email) {
		LocalDateTime cutoff = LocalDateTime.now().minusHours(LOCKOUT_WINDOW_HOURS);
		long failedAttempts = loginAttemptRepository.countByEmailAndSuccessfulIsFalseAndAttemptedAtAfter(email, cutoff);
		if (failedAttempts >= MAX_LOGGIN_ATTEMPTS) {
			throw new ApiServiceException(HttpStatus.LOCKED.value(),
					messageService.getMessage("account_locked", LocaleUtils.getDefaultLocale()));
		}
	}
	
}
