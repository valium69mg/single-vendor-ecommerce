package com.croman.SingleVendorEcommerce.Users.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.croman.SingleVendorEcommerce.Users.Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByEmail(String email);

	void deleteByEmail(String email);

	@Query("SELECT u.password FROM User u WHERE u.email = :email")
	Optional<String> getHashedPasswordByEmail(@Param("email") String email);

	Optional<User> findByEmail(String email);

	@Modifying
	@Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.email = :email")
	int updateLastLogin(@Param("email") String email, @Param("lastLogin") LocalDateTime lastLogin);
}
