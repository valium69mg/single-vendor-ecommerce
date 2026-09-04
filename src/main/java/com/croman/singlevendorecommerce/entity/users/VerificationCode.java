package com.croman.singlevendorecommerce.entity.users;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single issued email-verification code. Mirrors {@code login_attempts}
 * (append-only, one row per issuance): the raw 6-digit code is never stored,
 * only its BCrypt hash. Rate limiting counts rows in a rolling 1-hour window;
 * a resend simply inserts a new row.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "verification_codes")
public class VerificationCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "verification_code_id")
	private Long verificationCodeId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "code_hash", nullable = false, length = 255)
	private String codeHash;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "consumed_at")
	private LocalDateTime consumedAt;

	@Column(name = "attempts", nullable = false)
	private int attempts;

}
