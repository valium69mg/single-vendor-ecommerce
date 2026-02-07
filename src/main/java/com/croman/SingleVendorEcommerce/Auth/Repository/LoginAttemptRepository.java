package com.croman.SingleVendorEcommerce.Auth.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Auth.Entity.LoginAttempt;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

}
