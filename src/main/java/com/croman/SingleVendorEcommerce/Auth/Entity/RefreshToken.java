package com.croman.SingleVendorEcommerce.Auth.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.croman.SingleVendorEcommerce.Users.Entity.User;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "refresh_token_id")
	private Long refreshTokenId;
	
	@Column(name = "token", nullable = false, unique = true, length = 255)
	private String token;
	
	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;
	
	@Column(name = "revoked", nullable = false)
	private Boolean revoked = false;
	
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@Column(name = "last_used_at")
	private LocalDateTime lastUsedAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
