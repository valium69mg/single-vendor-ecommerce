package com.croman.singlevendorecommerce.Auth.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Auth.Entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{

}
