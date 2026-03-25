package com.croman.singlevendorecommerce.repository.auth;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.auth.LoginAttempt;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

	long countByEmailAndSuccessfulIsFalseAndAttemptedAtAfter(String email, LocalDateTime after);

}
