package com.croman.SingleVendorEcommerce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Roles.RolesService;
import com.croman.SingleVendorEcommerce.Roles.UserRole;
import com.croman.SingleVendorEcommerce.Roles.DTO.RoleType;
import com.croman.SingleVendorEcommerce.Users.UserService;
import com.croman.SingleVendorEcommerce.Users.DTO.CreateUserDTO;
import com.croman.SingleVendorEcommerce.Users.DTO.UserDTO;
import com.croman.SingleVendorEcommerce.Users.Entity.User;
import com.croman.SingleVendorEcommerce.Users.Repository.UserRepository;
import com.croman.SingleVendorEcommerce.Users.Utils.PasswordUtils;
import com.croman.SingleVendorEcommerce.General.EnvironmentUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RolesService rolesService;
    @Mock private MessageService messageService;
    @Mock private EnvironmentUtils environmentUtils;

    @InjectMocks private UserService userService;

    @Test
    void register_success() {
        CreateUserDTO dto = new CreateUserDTO("test@example.com", "password");
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(rolesService.getUserRoleByRoleType(RoleType.USER)).thenReturn(new UserRole());

        userService.register(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailExists_throwsException() {
        CreateUserDTO dto = new CreateUserDTO("test@example.com", "password");
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        when(messageService.getMessage(eq("email_exists"), any())).thenReturn("Email exists");

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.register(dto));
        assertEquals("Email exists", ex.getMessage());
    }

    @Test
    void deleteUserByEmail_success_inDev() {
        when(environmentUtils.isDev()).thenReturn(true);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        userService.deleteUserByEmail("test@example.com");

        verify(userRepository).deleteByEmail("test@example.com");
    }

    @Test
    void deleteUserByEmail_notDev_throwsException() {
        when(environmentUtils.isDev()).thenReturn(false);
        when(messageService.getMessage(eq("not_in_dev_env"), any())).thenReturn("Not in dev");

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.deleteUserByEmail("test@example.com"));
        assertEquals("Not in dev", ex.getMessage());
    }

    @Test
    void getUserDTOByEmail_success() {
        User user = User.builder()
                .userId(java.util.UUID.randomUUID())
                .username("test")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .isValidated(true)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDTO dto = userService.getUserDTOByEmail("test@example.com");
        assertEquals("test", dto.getUsername());
    }

    @Test
    void getUserDTOByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("invalid_credentials"), any())).thenReturn("Invalid credentials");

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.getUserDTOByEmail("missing@example.com"));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void passwordCorrect_success() {
        when(userRepository.getHashedPasswordByEmail("test@example.com")).thenReturn(Optional.of(PasswordUtils.hashPassword("secret")));
        assertTrue(userService.passwordCorrect("test@example.com", "secret"));
    }

    @Test
    void passwordCorrect_invalid_throwsException() {
        when(userRepository.getHashedPasswordByEmail("test@example.com")).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("invalid_credentials"), any())).thenReturn("Invalid credentials");

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.passwordCorrect("test@example.com", "wrong"));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    void updateLastLogin_success() {
        User user = User.builder().email("test@example.com").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        userService.updateLastLogin("test@example.com");

        verify(userRepository).updateLastLogin(eq("test@example.com"), any(LocalDateTime.class));
    }

    @Test
    void createSiteAdmin_success() {
        CreateUserDTO dto = new CreateUserDTO("admin@example.com", "password");
        when(userRepository.findAllByUserRole_RoleType(RoleType.ADMIN)).thenReturn(java.util.Collections.emptyList());
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(rolesService.getUserRoleByRoleType(RoleType.ADMIN)).thenReturn(new UserRole());

        userService.createSiteAdmin(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createSiteAdmin_adminAlreadyExists_throwsException() {
        CreateUserDTO dto = new CreateUserDTO("admin@example.com", "password");
        when(userRepository.findAllByUserRole_RoleType(RoleType.ADMIN)).thenReturn(java.util.List.of(new User()));
        when(messageService.getMessage(eq("admin_exists"), any())).thenReturn("Admin exists");

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.createSiteAdmin(dto));
        assertEquals("Admin exists", ex.getMessage());
    }

    @Test
    void getUserByEmail_success() {
        User user = User.builder().email("test@example.com").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("test@example.com");
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("email_does_not_exists"), any())).thenReturn("Email does not exist");

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> userService.getUserByEmail("missing@example.com"));
        assertEquals("Email does not exist", ex.getMessage());
    }
}
