package com.croman.SingleVendorEcommerce.Auth.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Auth.Entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{

}
