package com.croman.singlevendorecommerce.repository.users;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.croman.singlevendorecommerce.entity.users.VerificationCode;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

	/** Number of codes issued for an account after {@code after} — the rolling generation cap. */
	long countByUser_UserIdAndCreatedAtAfter(UUID userId, LocalDateTime after);

	/** Newest still-unconsumed code for an account, if any (expiry is enforced by the service). */
	Optional<VerificationCode> findFirstByUser_UserIdAndConsumedAtIsNullOrderByCreatedAtDesc(UUID userId);

}
