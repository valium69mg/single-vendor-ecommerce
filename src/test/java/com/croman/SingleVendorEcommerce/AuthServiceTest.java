package com.croman.SingleVendorEcommerce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.croman.SingleVendorEcommerce.Auth.AuthService;
import com.croman.SingleVendorEcommerce.Auth.DTO.LoginContextDTO;
import com.croman.SingleVendorEcommerce.Auth.DTO.LoginDTO;
import com.croman.SingleVendorEcommerce.Auth.DTO.LoginResponseDTO;
import com.croman.SingleVendorEcommerce.Auth.Entity.LoginAttempt;
import com.croman.SingleVendorEcommerce.Auth.Repository.LoginAttemptRepository;
import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.Jwt.JwtUtil;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Users.UserService;
import com.croman.SingleVendorEcommerce.Users.DTO.UserDTO;
import com.croman.SingleVendorEcommerce.Users.Entity.User;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private MessageService messageService;
    @Mock private UserService userService;
    @Mock private LoginAttemptRepository loginAttemptRepository;

    @InjectMocks private AuthService authService;

    private LoginContextDTO buildContext(String email, String password, String ip) {
        LoginDTO loginDTO = new LoginDTO(email, password);
        return new LoginContextDTO(loginDTO, ip);
    }

    @Test
    void login_success() {
        LoginContextDTO ctx = buildContext("user@example.com", "secret", "127.0.0.1");

        when(userService.existsByEmail("user@example.com")).thenReturn(true);
        when(loginAttemptRepository.countByEmailAndSuccessfulIsFalseAndAttemptedAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(userService.passwordCorrect("user@example.com", "secret")).thenReturn(true);
        when(jwtUtil.generateToken("user@example.com")).thenReturn("jwt-token");
        when(userService.getUserDTOByEmail("user@example.com"))
                .thenReturn(UserDTO.builder().userId("123").build());
        when(userService.getUserByEmail("user@example.com")).thenReturn(new User());

        LoginResponseDTO response = authService.login(ctx);

        assertEquals("user@example.com", response.getEmail());
        assertEquals("jwt-token", response.getToken());
        verify(loginAttemptRepository).save(any(LoginAttempt.class));
    }

    @Test
    void login_emailNotExists_throwsException() {
        LoginContextDTO ctx = buildContext("missing@example.com", "secret", "127.0.0.1");

        when(userService.existsByEmail("missing@example.com")).thenReturn(false);
        when(messageService.getMessage(eq("invalid_credentials"), any())).thenReturn("Invalid credentials");

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> authService.login(ctx));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void login_tooManyFailedAttempts_throwsLocked() {
        LoginContextDTO ctx = buildContext("user@example.com", "secret", "127.0.0.1");

        when(userService.existsByEmail("user@example.com")).thenReturn(true);
        when(loginAttemptRepository.countByEmailAndSuccessfulIsFalseAndAttemptedAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(5L);
        when(messageService.getMessage(eq("account_locked"), any())).thenReturn("Account locked");

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> authService.login(ctx));
        assertEquals("Account locked", ex.getMessage());
    }

    @Test
    void login_wrongPassword_throwsExceptionAndRegistersAttempt() {
        LoginContextDTO ctx = buildContext("user@example.com", "wrong", "127.0.0.1");

        when(userService.existsByEmail("user@example.com")).thenReturn(true);
        when(loginAttemptRepository.countByEmailAndSuccessfulIsFalseAndAttemptedAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(userService.passwordCorrect("user@example.com", "wrong")).thenReturn(false);
        when(messageService.getMessage(eq("invalid_credentials"), any())).thenReturn("Invalid credentials");
        when(userService.getUserByEmail("user@example.com")).thenReturn(new User());

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> authService.login(ctx));
        assertEquals("Invalid credentials", ex.getMessage());
        verify(loginAttemptRepository).save(any(LoginAttempt.class));
    }
}
