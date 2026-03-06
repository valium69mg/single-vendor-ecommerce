package com.croman.singlevendorecommerce.auth.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.auth.entity.LoginAttempt;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

	long countByEmailAndSuccessfulIsFalseAndAttemptedAtAfter(String email, LocalDateTime after);

}
