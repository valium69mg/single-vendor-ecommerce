package com.croman.SingleVendorEcommerce.Users.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import com.croman.SingleVendorEcommerce.Roles.UserRole;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_validated", nullable = false)
    private boolean isValidated;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
   
    @ManyToOne
    @JoinColumn(name = "user_role_id")
    private UserRole userRole;

    
}
