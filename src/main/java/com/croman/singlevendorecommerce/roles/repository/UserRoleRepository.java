package com.croman.singlevendorecommerce.roles.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.roles.UserRole;
import com.croman.singlevendorecommerce.roles.dto.RoleType;

public interface UserRoleRepository extends JpaRepository<UserRole, Long>{

	Optional<UserRole> findByRoleType(RoleType roleType);

}
