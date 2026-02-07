package com.croman.SingleVendorEcommerce.Auth.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.croman.SingleVendorEcommerce.Users.Entity.User;

@Entity
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "successful", nullable = false)
    private Boolean successful;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt = LocalDateTime.now();

}
