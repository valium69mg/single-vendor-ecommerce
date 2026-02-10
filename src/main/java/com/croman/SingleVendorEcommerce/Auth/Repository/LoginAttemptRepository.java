package com.croman.SingleVendorEcommerce.Auth.Repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Auth.Entity.LoginAttempt;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

	long countByEmailAndSuccessfulIsFalseAndAttemptedAtAfter(String email, LocalDateTime after);

}
