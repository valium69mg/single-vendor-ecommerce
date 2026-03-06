package com.croman.singlevendorecommerce.Auth.Repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Auth.Entity.LoginAttempt;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

	long countByEmailAndSuccessfulIsFalseAndAttemptedAtAfter(String email, LocalDateTime after);

}
