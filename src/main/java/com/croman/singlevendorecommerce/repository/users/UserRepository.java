package com.croman.singlevendorecommerce.repository.users;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.users.User;

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

	@Modifying
	@Query("UPDATE User u SET u.isValidated = :validated WHERE u.email = :email")
	int updateIsValidated(@Param("email") String email, @Param("validated") boolean validated);
	
	
	List<User> findAllByUserRole_RoleType(RoleType roleType);

}
