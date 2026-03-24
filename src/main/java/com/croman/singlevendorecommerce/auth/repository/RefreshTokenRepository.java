package com.croman.singlevendorecommerce.auth.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{

}
