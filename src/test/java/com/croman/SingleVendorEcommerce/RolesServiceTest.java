package com.croman.SingleVendorEcommerce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Roles.RoleType;
import com.croman.SingleVendorEcommerce.Roles.RolesService;
import com.croman.SingleVendorEcommerce.Roles.UserRole;
import com.croman.SingleVendorEcommerce.Roles.Repository.UserRoleRepository;

@ExtendWith(MockitoExtension.class)
class RolesServiceTest {

    @Mock private UserRoleRepository userRoleRepository;
    @Mock private MessageService messageService;

    @InjectMocks private RolesService rolesService;

    @Test
    void getUserRoleByRoleType_roleExists_returnsRole() {
        UserRole role = new UserRole();
        role.setRoleType(RoleType.ADMIN);

        when(userRoleRepository.findByRoleType(RoleType.ADMIN)).thenReturn(Optional.of(role));

        UserRole result = rolesService.getUserRoleByRoleType(RoleType.ADMIN);

        assertNotNull(result);
        assertEquals(RoleType.ADMIN, result.getRoleType());
    }

    @Test
    void getUserRoleByRoleType_roleNotFound_throwsException() {
        when(userRoleRepository.findByRoleType(RoleType.USER)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("user_role_not_found"), any())).thenReturn("Role not found");

        ApiServiceException ex = assertThrows(ApiServiceException.class, () -> rolesService.getUserRoleByRoleType(RoleType.USER));
        assertEquals("Role not found", ex.getMessage());
        assertEquals(404, ex.getStatusCode());
    }
}

