package com.croman.SingleVendorEcommerce.Users.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.croman.SingleVendorEcommerce.Users.Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	
	boolean existsByEmail(String email);

	void deleteByEmail(String email);
	
}
