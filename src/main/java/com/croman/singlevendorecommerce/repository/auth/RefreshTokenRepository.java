package com.croman.singlevendorecommerce.repository.auth;


import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.auth.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{

}
